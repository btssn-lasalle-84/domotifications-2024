/**
 *
 * @file src/main.cpp
 * @brief Simulateur pour Station de notifications lumineuses (2024)
 * @author Thierry Vaira
 * @version 0.1
 */

#include <Arduino.h>
#include <Preferences.h>
#include <WiFi.h>
#include <WiFiManager.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <afficheur.h>

// Brochages
#define GPIO_LED_ROUGE   5    //!<
#define GPIO_LED_ORANGE  17   //!< Trame OK
#define GPIO_LED_VERTE   16   //!< Trame START
#define GPIO_SW1         12   //!< Pour simuler la boîte aux lettres
#define GPIO_SW2         14   //!< Pour simuler les machines et poubelles
#define ADRESSE_I2C_OLED 0x3c //!< Adresse I2C de l'OLED
#define BROCHE_I2C_SDA   21   //!< Broche SDA
#define BROCHE_I2C_SCL   22   //!< Broche SCL

/**
 * @def NOM_SERVEUR_WEB
 * @brief Le nom de la station lumineuse (cela donnera http://station-lumineuse.local/)
 */
#define NOM_SERVEUR_WEB  "station-lumineuse"
#define PORT_SERVEUR_WEB 80

#define REPONSE_OK          200
#define REPONSE_INVALIDE    400
#define REPONSE_NON_TROUVEE 404
#define REPONSE_ERREUR      -1

#define NB_NOTIFICATION_MACHINES  6
#define NB_NOTIFICATION_POUBELLES 5
#define NB_NOTIFICATION_BOITE     1

WiFiManager             wm;
Preferences             preferences;
StaticJsonDocument<256> documentJSON;
char                    buffer[256];

bool etatMachines[NB_NOTIFICATION_MACHINES]   = { false, false, false, false, false, false };
bool etatPoubelles[NB_NOTIFICATION_POUBELLES] = { false, false, false, false, false };
bool etatBoiteAuxLettres                      = false;
// Ticker timersSoftware[NB_ENREGISTREMENTS_MAX]; //!< 4 timers logiciels
// Ticker timerScrutation;                        //!< la tâche pour la scrutation

WiFiClient client;
HTTPClient httpClient;
String     url = "http://" + String(NOM_SERVEUR_WEB) + ".local";

bool      refresh         = false; //!< demande rafraichissement de l'écran OLED
bool      antiRebond      = false; //!< anti-rebond
bool      simulationBoite = false; //!<
bool      simulation      = false; //!<
Afficheur afficheur(ADRESSE_I2C_OLED, BROCHE_I2C_SDA,
                    BROCHE_I2C_SCL); //!< afficheur OLED SSD1306

/**
 * @brief Déclenchée par interruption sur le bouton SW1
 */
void IRAM_ATTR simulerBoite()
{
    if(antiRebond || simulationBoite)
        return;

    simulationBoite = true;
    antiRebond      = true;
}

/**
 * @brief Déclenchée par interruption sur le bouton SW2
 */
void IRAM_ATTR simuler()
{
    if(antiRebond || simulation)
        return;

    simulation = true;
    antiRebond = true;
}

void reinitialiserAffichage()
{
    afficheur.setMessageLigne(Afficheur::Ligne1, "");
    afficheur.setMessageLigne(Afficheur::Ligne2, "");
    afficheur.setMessageLigne(Afficheur::Ligne3, "");
    refresh = true;
}

void initialiser();
int  envoyerRequetePOSTBoite(bool etat);
int  envoyerRequetePOSTPoubelle(int id, bool etat);
int  envoyerRequetePOSTMachine(int id, bool etat);
int  envoyerRequeteGETBoite(bool etat);
int  envoyerRequeteGETPoubelle(int id, bool etat);
int  envoyerRequeteGETMachine(int id, bool etat);

bool estIdValide(int id, String type);
bool getEtatMachine(int numeroMachine);
void setEtatMachine(int numeroMachine, bool etat);
void setEtatMachines();
void resetEtatMachines();
int  allumerNotificationMachine(int numeroMachine);
int  eteindreNotificationMachine(int numeroMachine);
void allumerNotificationMachines();
void eteindreNotificationMachines();

bool getEtatPoubelle(int numeroPoubelle);
void setEtatPoubelle(int numeroPoubelle, bool etat);
void resetEtatPoubelles();
int  allumerNotificationPoubelle(int numeroPoubelle);
int  eteindreNotificationPoubelle(int numeroPoubelle);
void allumerNotificationPoubelles();
void eteindreNotificationPoubelles();

bool getEtatBoiteAuxLettres();
void setEtatBoiteAuxLettres(bool etat);
int  resetEtatBoiteAuxLettres();
int  allumerNotificationBoiteAuxLettres();
int  eteindreNotificationBoiteAuxLettres();

// Fonctions utilitaires
int    compterDelimiteurs(const String& chaine, char delimiteur);
String extraireChamp(const String& donnee, char delimiteur, unsigned int numeroChamp);

void setup()
{
    Serial.begin(115200);
    while(!Serial)
        ;

    pinMode(GPIO_LED_ROUGE, OUTPUT);
    pinMode(GPIO_LED_ORANGE, OUTPUT);
    pinMode(GPIO_LED_VERTE, OUTPUT);
    pinMode(GPIO_SW1, INPUT_PULLUP);
    pinMode(GPIO_SW2, INPUT_PULLUP);

    attachInterrupt(digitalPinToInterrupt(GPIO_SW1), simulerBoite, FALLING);
    attachInterrupt(digitalPinToInterrupt(GPIO_SW2), simuler, FALLING);

    digitalWrite(GPIO_LED_ROUGE, HIGH);
    digitalWrite(GPIO_LED_ORANGE, LOW);
    digitalWrite(GPIO_LED_VERTE, LOW);

    afficheur.initialiser();

    Serial.println(F("Station de notifications lumineuses"));

    preferences.begin("eeprom", false); // false pour r/w
    etatBoiteAuxLettres = preferences.getBool("boite", false);
    char cle[64]        = "";
    for(int i = 0; i < NB_NOTIFICATION_MACHINES; ++i)
    {
        sprintf((char*)cle, "%s%d", "machine", i);
        etatMachines[i] = preferences.getBool(cle, false);
    }
    for(int i = 0; i < NB_NOTIFICATION_POUBELLES; ++i)
    {
        sprintf((char*)cle, "%s%d", "poubelle", i);
        etatPoubelles[i] = preferences.getBool(cle, false);
    }

    WiFi.mode(WIFI_STA); // explicitly set mode, esp defaults to STA+AP
    // reset settings - wipe credentials for testing
    // wm.resetSettings();
    wm.setTitle("Simulateur pour station de notifications lumineuses");
    // wm.setDarkMode(true);
    bool res;
    res = wm.autoConnect(); // auto generated AP name from chipid

    if(!res)
    {
        Serial.println("Erreur de connexion !");
        // ESP.restart();
    }

    String titre  = " Domotifications 2023";
    String stitre = "=====================";
    afficheur.setTitre(titre);
    afficheur.setSTitre(stitre);
    afficheur.afficher();

    // initialise le générateur pseudo-aléatoire
    // Serial.println(randomSeed(analogRead(34)));
    Serial.println(esp_random());

    initialiser();
}

void loop()
{
    if(refresh)
    {
        afficheur.afficher();
        refresh = false;
    }

    if(antiRebond)
    {
        afficheur.afficher();
        delay(300);
        antiRebond = false;
    }

    if(simulationBoite)
    {
        setEtatBoiteAuxLettres(getEtatBoiteAuxLettres() ? false : true);
        char strMessageDisplay[24];
        sprintf(strMessageDisplay, "boite : %d", getEtatBoiteAuxLettres());
        afficheur.setMessageLigne(Afficheur::Ligne1, String(strMessageDisplay));
        afficheur.setMessageLigne(Afficheur::Ligne4, String("method : POST"));
        refresh         = true;
        simulationBoite = false;
    }

    if(simulation)
    {
        char strMessageDisplay[24];
        int  id  = 0;
        int  tos = random(0, 2);
        if(tos > 0)
        {
            id = random(0, NB_NOTIFICATION_MACHINES);
            setEtatMachine(id, getEtatMachine(id) ? false : true);
            sprintf(strMessageDisplay, "machine %d : %d", id, getEtatMachine(id));
            afficheur.setMessageLigne(Afficheur::Ligne2, String(strMessageDisplay));
            afficheur.setMessageLigne(Afficheur::Ligne4, String("method : GET"));
        }
        else
        {
            id = random(0, NB_NOTIFICATION_POUBELLES);
            setEtatPoubelle(id, getEtatPoubelle(id) ? false : true);
            sprintf(strMessageDisplay, "poubelle %d : %d", id, getEtatPoubelle(id));
            afficheur.setMessageLigne(Afficheur::Ligne3, String(strMessageDisplay));
            afficheur.setMessageLigne(Afficheur::Ligne4, String("method : POST"));
        }
        refresh    = true;
        simulation = false;
    }
}

void initialiser()
{
    if(etatBoiteAuxLettres)
    {
        allumerNotificationBoiteAuxLettres();
    }
    else
    {
        eteindreNotificationBoiteAuxLettres();
    }
    for(int i = 0; i < NB_NOTIFICATION_MACHINES; ++i)
    {
        if(etatMachines[i])
        {
            allumerNotificationMachine(i);
        }
        else
        {
            eteindreNotificationMachine(i);
        }
    }
    for(int i = 0; i < NB_NOTIFICATION_POUBELLES; ++i)
    {
        if(etatPoubelles[i])
        {
            allumerNotificationPoubelle(i);
        }
        else
        {
            eteindreNotificationPoubelle(i);
        }
    }
}

// POST	/boite	{"etat": true|false}
int envoyerRequetePOSTBoite(bool etat)
{
    String urlBoite = url + String("/boite");
    httpClient.begin(client, urlBoite);
    httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
    httpClient.addHeader("Content-Type", "application/json");
    String payload = "{}";
    if(etat)
    {
        payload = "{\"etat\":true}";
    }
    else
    {
        payload = "{\"etat\":false}";
    }
    Serial.println("envoyerRequetePOSTBoite()");
    Serial.print("   etat = ");
    Serial.println(etat);
    Serial.print("   url  = ");
    Serial.println(urlBoite);
    Serial.print("   json = ");
    Serial.println(payload);
    int codeReponse = httpClient.POST(payload);
    httpClient.end();
    Serial.print("   code reponse : ");
    Serial.println(codeReponse);
    return codeReponse;
}

// POST	/poubelle	{"etat": true|false, "id": 0|1|2|3|4}
int envoyerRequetePOSTPoubelle(int id, bool etat)
{
    String urlPoubelle = url + String("/poubelle");
    httpClient.begin(client, urlPoubelle);
    httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
    httpClient.addHeader("Content-Type", "application/json");
    String payload = "{}";
    if(etat)
    {
        payload = String("{\"etat\":true,") + String("\"id\":") + String(id) + String("}");
    }
    else
    {
        payload = String("{\"etat\":false,") + String("\"id\":") + String(id) + String("}");
    }
    Serial.println("envoyerRequetePOSTPoubelle()");
    Serial.print("   id   = ");
    Serial.println(id);
    Serial.print("   etat = ");
    Serial.println(etat);
    Serial.print("   url  = ");
    Serial.println(urlPoubelle);
    Serial.print("   json = ");
    Serial.println(payload);
    int codeReponse = httpClient.POST(payload);
    httpClient.end();
    Serial.print("   code reponse : ");
    Serial.println(codeReponse);
    return codeReponse;
}

// POST	/machine	{"etat": true|false, "id": 0|1|2|3|4|5}
int envoyerRequetePOSTMachine(int id, bool etat)
{
    String urlMachine = url + String("/machine");
    httpClient.begin(client, urlMachine);
    httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
    httpClient.addHeader("Content-Type", "application/json");
    String payload = "{}";
    if(etat)
    {
        payload = String("{\"etat\":true,") + String("\"id\":") + String(id) + String("}");
    }
    else
    {
        payload = String("{\"etat\":false,") + String("\"id\":") + String(id) + String("}");
    }
    Serial.println("envoyerRequetePOSTMachine()");
    Serial.print("   id   = ");
    Serial.println(id);
    Serial.print("   etat = ");
    Serial.println(etat);
    Serial.print("   url  = ");
    Serial.println(urlMachine);
    Serial.print("   json = ");
    Serial.println(payload);
    int codeReponse = httpClient.POST(payload);
    httpClient.end();
    Serial.print("   code reponse : ");
    Serial.println(codeReponse);
    return codeReponse;
}

int envoyerRequeteGETBoite(bool etat)
{
    String urlBoite = url + String("/boite?etat=") + String(etat);
    httpClient.begin(urlBoite.c_str());

    Serial.println("envoyerRequeteGETBoite()");
    Serial.print("   etat = ");
    Serial.println(etat);
    Serial.print("   url  = ");
    Serial.println(urlBoite);
    int codeReponse = httpClient.GET();

    if(codeReponse > 0)
    {
        Serial.print("   code reponse : ");
        Serial.println(codeReponse);
        String payload = httpClient.getString();
        Serial.println(payload);
    }
    else
    {
        Serial.print("   erreur : ");
        Serial.println(codeReponse);
    }

    httpClient.end();

    return codeReponse;
}

int envoyerRequeteGETPoubelle(int id, bool etat)
{
    String urlPoubelle =
      url + String("/poubelle?id=") + String(id) + String("&etat=") + String(etat);
    httpClient.begin(urlPoubelle.c_str());

    Serial.println("envoyerRequeteGETPoubelle()");
    Serial.print("   etat = ");
    Serial.println(etat);
    Serial.print("   url  = ");
    Serial.println(urlPoubelle);
    int codeReponse = httpClient.GET();

    if(codeReponse > 0)
    {
        Serial.print("   code reponse : ");
        Serial.println(codeReponse);
        String payload = httpClient.getString();
        Serial.println(payload);
    }
    else
    {
        Serial.print("   erreur : ");
        Serial.println(codeReponse);
    }

    httpClient.end();

    return codeReponse;
}

int envoyerRequeteGETMachine(int id, bool etat)
{
    String urlMachine = url + String("/machine?id=") + String(id) + String("&etat=") + String(etat);
    httpClient.begin(urlMachine.c_str());

    Serial.println("envoyerRequeteGETMachine()");
    Serial.print("   etat = ");
    Serial.println(etat);
    Serial.print("   url  = ");
    Serial.println(urlMachine);
    int codeReponse = httpClient.GET();

    if(codeReponse > 0)
    {
        Serial.print("   code reponse : ");
        Serial.println(codeReponse);
        String payload = httpClient.getString();
        Serial.println(payload);
    }
    else
    {
        Serial.print("   erreur : ");
        Serial.println(codeReponse);
    }

    httpClient.end();

    return codeReponse;
}

bool estIdValide(int id, String type)
{
    if(type == "machine")
        return (id >= 0 && id < NB_NOTIFICATION_MACHINES);
    else if(type == "poubelle")
        return (id >= 0 && id < NB_NOTIFICATION_POUBELLES);
    else
        return false;
}

bool getEtatMachine(int numeroMachine)
{
    if(numeroMachine >= 0 && numeroMachine < NB_NOTIFICATION_MACHINES)
        return etatMachines[numeroMachine];
    return false;
}

void setEtatMachine(int numeroMachine, bool etat)
{
    if(numeroMachine >= 0 && numeroMachine < NB_NOTIFICATION_MACHINES)
    {
        char cle[64] = "";

        if(etat)
        {
            if(allumerNotificationMachine(numeroMachine) == REPONSE_OK)
            {
                etatMachines[numeroMachine] = etat;
                sprintf((char*)cle, "%s%d", "machine", numeroMachine);
                preferences.putBool(cle, etatMachines[numeroMachine]);
            }
        }
        else
        {
            if(eteindreNotificationMachine(numeroMachine) == REPONSE_OK)
            {
                etatMachines[numeroMachine] = etat;
                sprintf((char*)cle, "%s%d", "machine", numeroMachine);
                preferences.putBool(cle, etatMachines[numeroMachine]);
            }
        }
    }
}

void setEtatMachines()
{
    for(int numeroMachine = 0; numeroMachine < NB_NOTIFICATION_MACHINES; ++numeroMachine)
    {
        etatMachines[numeroMachine] = true;
    }
    char cle[64] = "";
    for(int i = 0; i < NB_NOTIFICATION_MACHINES; ++i)
    {
        sprintf((char*)cle, "%s%d", "machine", i);
        preferences.putBool(cle, etatMachines[i]);
    }
    eteindreNotificationMachines();
}

void resetEtatMachines()
{
    for(int numeroMachine = 0; numeroMachine < NB_NOTIFICATION_MACHINES; ++numeroMachine)
    {
        etatMachines[numeroMachine] = false;
    }
    char cle[64] = "";
    for(int i = 0; i < NB_NOTIFICATION_MACHINES; ++i)
    {
        sprintf((char*)cle, "%s%d", "machine", i);
        preferences.putBool(cle, etatMachines[i]);
    }
    eteindreNotificationMachines();
}

int allumerNotificationMachine(int numeroMachine)
{
    return envoyerRequeteGETMachine(numeroMachine, true);
    // return envoyerRequetePOSTMachine(numeroMachine, true);
}

int eteindreNotificationMachine(int numeroMachine)
{
    return envoyerRequeteGETMachine(numeroMachine, false);
    // return envoyerRequetePOSTMachine(numeroMachine, false);
}

void allumerNotificationMachines()
{
    for(int i = 0; i < NB_NOTIFICATION_MACHINES; ++i)
    {
        envoyerRequetePOSTMachine(i, true);
    }
}

void eteindreNotificationMachines()
{
    for(int i = 0; i < NB_NOTIFICATION_MACHINES; ++i)
    {
        envoyerRequetePOSTMachine(i, false);
    }
}

bool getEtatPoubelle(int numeroPoubelle)
{
    if(numeroPoubelle >= 0 && numeroPoubelle < NB_NOTIFICATION_POUBELLES)
        return etatPoubelles[numeroPoubelle];
    return false;
}

void setEtatPoubelle(int numeroPoubelle, bool etat)
{
    if(numeroPoubelle >= 0 && numeroPoubelle < NB_NOTIFICATION_POUBELLES)
    {
        char cle[64] = "";

        if(etat)
        {
            if(allumerNotificationPoubelle(numeroPoubelle) == REPONSE_OK)
            {
                etatPoubelles[numeroPoubelle] = etat;
                sprintf((char*)cle, "%s%d", "poubelle", numeroPoubelle);
                preferences.putBool(cle, etatPoubelles[numeroPoubelle]);
            }
        }
        else
        {
            if(eteindreNotificationPoubelle(numeroPoubelle) == REPONSE_OK)
            {
                etatPoubelles[numeroPoubelle] = etat;
                sprintf((char*)cle, "%s%d", "poubelle", numeroPoubelle);
                preferences.putBool(cle, etatPoubelles[numeroPoubelle]);
            }
        }
    }
}

void resetEtatPoubelles()
{
    for(int numeroPoubelle = 0; numeroPoubelle < NB_NOTIFICATION_POUBELLES; ++numeroPoubelle)
    {
        etatPoubelles[numeroPoubelle] = false;
    }
    char cle[64] = "";
    for(int i = 0; i < NB_NOTIFICATION_POUBELLES; ++i)
    {
        sprintf((char*)cle, "%s%d", "poubelle", i);
        preferences.putBool(cle, etatPoubelles[i]);
    }
    eteindreNotificationPoubelles();
}

int allumerNotificationPoubelle(int numeroPoubelle)
{
    return envoyerRequetePOSTPoubelle(numeroPoubelle, true);
}

int eteindreNotificationPoubelle(int numeroPoubelle)
{
    return envoyerRequetePOSTPoubelle(numeroPoubelle, false);
}

void allumerNotificationPoubelles()
{
    for(int i = 0; i < NB_NOTIFICATION_POUBELLES; ++i)
    {
        envoyerRequetePOSTPoubelle(i, true);
    }
}

void eteindreNotificationPoubelles()
{
    for(int i = 0; i < NB_NOTIFICATION_POUBELLES; ++i)
    {
        envoyerRequetePOSTPoubelle(i, false);
    }
}

bool getEtatBoiteAuxLettres()
{
    return etatBoiteAuxLettres;
}

void setEtatBoiteAuxLettres(bool etat)
{
    if(etat)
    {
        if(allumerNotificationBoiteAuxLettres() == REPONSE_OK)
        {
            etatBoiteAuxLettres = etat;
            preferences.putBool("boite", etatBoiteAuxLettres);
        }
    }
    else
    {
        if(eteindreNotificationBoiteAuxLettres() == REPONSE_OK)
        {
            etatBoiteAuxLettres = etat;
            preferences.putBool("boite", etatBoiteAuxLettres);
        }
    }
}

int resetEtatBoiteAuxLettres()
{
    etatBoiteAuxLettres = false;
    preferences.putBool("boite", etatBoiteAuxLettres);
    return eteindreNotificationBoiteAuxLettres();
}

int allumerNotificationBoiteAuxLettres()
{
    return envoyerRequetePOSTBoite(true);
}

int eteindreNotificationBoiteAuxLettres()
{
    return envoyerRequetePOSTBoite(false);
}

// Fonctions utilitaires
int compterDelimiteurs(const String& chaine, char delimiteur)
{
    int n = 0;
    for(int i = 0; i < chaine.length(); i++)
    {
        if(chaine[i] == delimiteur)
            ++n;
    }
    return n;
}

String extraireChamp(const String& donnee, char delimiteur, unsigned int numeroChamp)
{
    char champ[128]         = ""; // + fin de chaîne
    int  compteurCaractere  = 0;
    int  compteurDelimiteur = 0;

    for(int i = 0; i < donnee.length(); i++)
    {
        if(donnee[i] == delimiteur)
        {
            compteurDelimiteur++;
        }
        else if(compteurDelimiteur == numeroChamp)
        {
            champ[compteurCaractere] = donnee[i];
            compteurCaractere++;
        }
    }

    if(compteurCaractere > 0 && compteurCaractere <= 127)
        champ[compteurCaractere] = 0; // fin de chaîne
    else
        champ[127] = 0; // fin de chaîne

    return (String)champ;
}

/**
 * @brief Active le timer correspondant
 *
 * @fn activerTimer(int numeroTimer, uint64_t dureeProgrammation)
 * @param numeroTimer
 * @param dureeProgrammation
 */
void activerTimer(int numeroTimer, uint64_t dureeProgrammation)
{
    /*
    uint64_t dureeProgrammationSecondes = (dureeProgrammation / 1000000L);

#ifdef DEBUG_TIMER
    Serial.print("<Activation> timer ");
    Serial.print(numeroTimer);
    Serial.print(" : ");
    Serial.print(dureeProgrammationSecondes);
    Serial.println(" secondes");
#endif

    timersSoftware[numeroTimer].once(dureeProgrammationSecondes,
                                     traiterProgrammationCafe,
                                     numeroTimer);

    timerScrutation.attach(TOUTES_LES_SECONDES, traiterProgrammationCafe); // toutes les s
    */
}

/**
 * @brief Désactive le timer correspondant
 *
 * @fn desactiverTimer(int numeroTimer)
 * @param numeroTimer
 */
void desactiverTimer(int numeroTimer)
{
#ifdef DEBUG_TIMER
    Serial.print("<Désactivation> timer ");
    Serial.println(numeroTimer);
#endif

    // timersSoftware[numeroTimer].detach();
}
