/**
 *
 * @file src/main.cpp
 * @brief Simulateur de modules pour Station de notifications lumineuses (2024)
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
#define GPIO_LED_ROUGE   5    //!< une notification boite
#define GPIO_LED_ORANGE  17   //!< une notification machine
#define GPIO_LED_VERTE   16   //!< une notification poubelle
#define GPIO_SW1         12   //!< pour signaler une notification
#define GPIO_SW2         14   //!< pour acquitter une notification
#define GPIO_ENCODEUR_A  2    //!< pour sélectionner un module boite ou poubelle ou machine
#define GPIO_ENCODEUR_B  4    //!< pour sélectionner un module boite ou poubelle ou machine
#define GPIO_ENCODEUR_E  13   //!< non utilisé
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

#define NB_NOMS_MODULES           3
#define NB_NOTIFICATION_MACHINES  6
#define NB_NOTIFICATION_POUBELLES 5
#define NB_NOTIFICATION_BOITES    1
#define NB_NOTIFICATION_MODULES                                                                    \
    (NB_NOTIFICATION_MACHINES + NB_NOTIFICATION_POUBELLES + NB_NOTIFICATION_BOITES)
#define NOTIFICATION_BOITE    GPIO_LED_ROUGE
#define NOTIFICATION_MACHINE  GPIO_LED_ORANGE
#define NOTIFICATION_POUBELLE GPIO_LED_VERTE

#define INDEX_NOTIFICATION_MACHINES  0                                                          // 0
#define INDEX_NOTIFICATION_POUBELLES (INDEX_NOTIFICATION_MACHINES + NB_NOTIFICATION_MACHINES)   // 6
#define INDEX_NOTIFICATION_BOITE     (INDEX_NOTIFICATION_POUBELLES + NB_NOTIFICATION_POUBELLES) // 11

WiFiManager             wm;
Preferences             preferences;
StaticJsonDocument<256> documentJSON;
char                    buffer[256];

bool etatMachines[NB_NOTIFICATION_MACHINES]       = { false, false, false, false, false, false };
bool etatPoubelles[NB_NOTIFICATION_POUBELLES]     = { false, false, false, false, false };
bool etatBoitesAuxLettres[NB_NOTIFICATION_BOITES] = { false };

// Ticker timersSoftware[NB_ENREGISTREMENTS_MAX]; //!< 4 timers logiciels
// Ticker timerScrutation;                        //!< la tâche pour la scrutation

WiFiClient client;
HTTPClient httpClient;
String     url = "http://" + String(NOM_SERVEUR_WEB) + ".local";

bool         refresh             = false; //!< demande rafraichissement de l'écran OLED
bool         antiRebond          = false; //!< gestion de l'anti-rebond
bool         demandeNotification = false; //!< une demande de notification SW1
bool         demandeAcquittement = false; //!< une demande d'acquittement SW2
bool         encodeurA           = false;
bool         encodeurB           = false;
int          choixNomModule      = 0;
int          choixIdModule       = 1;
String       nomModule;
int          idModule;
bool         etatModule;
const String nomsModules[NB_NOMS_MODULES] = { "Machine", "Poubelle", "Boite" }; //!< nom des modules
Afficheur    afficheur(ADRESSE_I2C_OLED, BROCHE_I2C_SDA,
                    BROCHE_I2C_SCL); //!< afficheur OLED SSD1306

/**
 * @brief Déclenchée par interruption sur le bouton SW1
 */
void IRAM_ATTR signalerNotification()
{
    if(antiRebond || demandeNotification)
        return;

    demandeNotification = true;
    antiRebond          = true;
}

/**
 * @brief Déclenchée par interruption sur le bouton SW2
 */
void IRAM_ATTR signalerAcquittement()
{
    if(antiRebond || demandeAcquittement)
        return;

    demandeAcquittement = true;
    antiRebond          = true;
}

void IRAM_ATTR encoderA()
{
    if(antiRebond || encodeurA)
        return;

    encodeurA  = true;
    antiRebond = true;
}

void IRAM_ATTR encoderB()
{
    if(antiRebond || encodeurB)
        return;

    encodeurB  = true;
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
int  envoyerRequetePATCHBoite(int id, bool etat);
int  envoyerRequetePATCHPoubelle(int id, bool etat);
int  envoyerRequetePATCHMachine(int id, bool etat);
int  envoyerRequeteGETBoite(int id);
int  envoyerRequeteGETPoubelle(int id);
int  envoyerRequeteGETMachine(int id);

bool estIdValide(int id, String type);
bool getEtatMachine(int numeroMachine);
bool setEtatMachine(int numeroMachine, bool etat);
void setEtatMachines();
void resetEtatMachines();
int  allumerNotificationMachine(int numeroMachine);
int  eteindreNotificationMachine(int numeroMachine);
void allumerNotificationMachines();
void eteindreNotificationMachines();

bool getEtatPoubelle(int numeroPoubelle);
bool setEtatPoubelle(int numeroPoubelle, bool etat);
void resetEtatPoubelles();
int  allumerNotificationPoubelle(int numeroPoubelle);
int  eteindreNotificationPoubelle(int numeroPoubelle);
void allumerNotificationPoubelles();
void eteindreNotificationPoubelles();

bool getEtatBoiteAuxLettres(int numeroBoite);
bool setEtatBoiteAuxLettres(int numeroBoite, bool etat);
void resetEtatBoitesAuxLettres();
int  allumerNotificationBoiteAuxLettres(int numeroBoite);
int  eteindreNotificationBoiteAuxLettres(int numeroBoite);
void allumerNotificationBoitesAuxLettres();
void eteindreNotificationBoitesAuxLettres();

// Fonctions utilitaires
void   determinerModuleSelectionne(int choixIdModule);
bool   getEtatModuleSelectionne();
bool   setEtatModuleSelectionne(bool etat);
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
    pinMode(GPIO_ENCODEUR_A, INPUT_PULLUP);
    pinMode(GPIO_ENCODEUR_B, INPUT_PULLUP);

    attachInterrupt(digitalPinToInterrupt(GPIO_SW1), signalerNotification, FALLING);
    attachInterrupt(digitalPinToInterrupt(GPIO_SW2), signalerAcquittement, FALLING);
    attachInterrupt(digitalPinToInterrupt(GPIO_ENCODEUR_A), encoderA, FALLING);
    attachInterrupt(digitalPinToInterrupt(GPIO_ENCODEUR_B), encoderB, FALLING);

    digitalWrite(NOTIFICATION_BOITE, LOW);
    digitalWrite(NOTIFICATION_POUBELLE, LOW);
    digitalWrite(NOTIFICATION_MACHINE, LOW);

    afficheur.initialiser();

    preferences.begin("eeprom", false); // false pour r/w
    // récupère les états
    char cle[64] = "";
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
    for(int i = 0; i < NB_NOTIFICATION_BOITES; ++i)
    {
        sprintf((char*)cle, "%s%d", "boite", i);
        etatBoitesAuxLettres[i] = preferences.getBool(cle, false);
    }

    WiFi.mode(WIFI_STA); // explicitly set mode, esp defaults to STA+AP
    // reset settings - wipe credentials for testing
    // wm.resetSettings();
    wm.setTitle("Simulateur pour station de notifications lumineuses");
    // wm.setDarkMode(true);
    bool res = false;
    res      = wm.autoConnect(); // auto generated AP name from chipid

    if(!res)
    {
        Serial.println("Erreur de connexion !");
        // ESP.restart();
    }

    String titre  = "Domotifications 2024";
    String stitre = "=====================";
    afficheur.setTitre(titre);
    afficheur.setSTitre(stitre);
    afficheur.setMessageLigne(Afficheur::Ligne1, String(url));
    if(res)
        afficheur.setMessageLigne(Afficheur::Ligne4, String("WiFi ok"));
    else
        afficheur.setMessageLigne(Afficheur::Ligne4, String("Wifi erreur"));
    afficheur.afficher();

    // initialise le générateur pseudo-aléatoire
    // Serial.println(esp_random());

    Serial.println(F("Simulateur modules"));

    if(res)
    {
        // Serial.println(F("Initialisation des modules"));
        // initialiser();
    }
}

void loop()
{
    char strMessageDisplay[24];

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

    // SW1
    if(demandeNotification)
    {
        Serial.println("Notification");
        if(setEtatModuleSelectionne(true))
        {
            afficheur.setMessageLigne(Afficheur::Ligne3, String("Notification"));
        }
        else
        {
            afficheur.setMessageLigne(Afficheur::Ligne3, String("Erreur HTTP"));
        }
        etatModule = getEtatModuleSelectionne();
        if(etatModule)
            afficheur.setMessageLigne(Afficheur::Ligne2, String("true"));
        else
            afficheur.setMessageLigne(Afficheur::Ligne2, String("false"));
        refresh             = true;
        demandeNotification = false;
    }

    // SW2
    if(demandeAcquittement)
    {
        Serial.println("Acquittement");
        if(setEtatModuleSelectionne(false))
        {
            afficheur.setMessageLigne(Afficheur::Ligne3, String("Acquittement"));
        }
        else
        {
            afficheur.setMessageLigne(Afficheur::Ligne3, String("Erreur HTTP"));
        }
        etatModule = getEtatModuleSelectionne();
        if(etatModule)
            afficheur.setMessageLigne(Afficheur::Ligne2, String("true"));
        else
            afficheur.setMessageLigne(Afficheur::Ligne2, String("false"));
        refresh             = true;
        demandeAcquittement = false;
    }

    // Encodeur A
    if(encodeurA)
    {
        choixIdModule = (choixIdModule + 1) % NB_NOTIFICATION_MODULES;
        // Serial.print("Encodeur A : ");
        // Serial.println(choixIdModule);
        determinerModuleSelectionne(choixIdModule);
        sprintf(strMessageDisplay, "%s %d", nomModule.c_str(), idModule + 1);
        afficheur.setMessageLigne(Afficheur::Ligne1, String(strMessageDisplay));
        etatModule = getEtatModuleSelectionne();
        if(etatModule)
            afficheur.setMessageLigne(Afficheur::Ligne2, String("true"));
        else
            afficheur.setMessageLigne(Afficheur::Ligne2, String("false"));
        afficheur.setMessageLigne(Afficheur::Ligne3, String(""));
        refresh   = true;
        encodeurA = false;
    }

    // Encodeur B
    if(encodeurB)
    {
        choixIdModule = (choixIdModule - 1 + NB_NOTIFICATION_MODULES) % NB_NOTIFICATION_MODULES;
        if(choixIdModule == -1)
            choixIdModule = NB_NOTIFICATION_MODULES - 1;
        // Serial.print("Encodeur B : ");
        // Serial.println(choixIdModule);
        determinerModuleSelectionne(choixIdModule);
        sprintf(strMessageDisplay, "%s %d", nomModule.c_str(), idModule + 1);
        afficheur.setMessageLigne(Afficheur::Ligne1, String(strMessageDisplay));
        etatModule = getEtatModuleSelectionne();
        if(etatModule)
            afficheur.setMessageLigne(Afficheur::Ligne2, String("true"));
        else
            afficheur.setMessageLigne(Afficheur::Ligne2, String("false"));
        afficheur.setMessageLigne(Afficheur::Ligne3, String(""));
        refresh   = true;
        encodeurB = false;
    }
}

void initialiser()
{
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
    for(int i = 0; i < NB_NOTIFICATION_BOITES; ++i)
    {
        if(etatBoitesAuxLettres[i])
        {
            allumerNotificationBoiteAuxLettres(i);
        }
        else
        {
            eteindreNotificationBoiteAuxLettres(i);
        }
    }
}

// PATCH /boites {"etat": true|false, "idBoite": 0}
int envoyerRequetePATCHBoite(int id, bool etat)
{
    String urlBoite = url + String("/boites/") + String(id);
    httpClient.begin(client, urlBoite);
    httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
    httpClient.addHeader("Content-Type", "application/json");
    String payload = "{}";
    if(etat)
    {
        payload = String("{\"etat\":true,") + String("\"idBoite\":") + String(id) + String("}");
    }
    else
    {
        payload = String("{\"etat\":false,") + String("\"idBoite\":") + String(id) + String("}");
    }
    Serial.println("envoyerRequetePATCHBoite()");
    Serial.print("   id   = ");
    Serial.println(id);
    Serial.print("   etat = ");
    Serial.println(etat);
    Serial.print("   url  = ");
    Serial.println(urlBoite);
    Serial.print("   json = ");
    Serial.println(payload);
    int codeReponse = httpClient.PATCH(payload);
    httpClient.end();
    Serial.print("   code reponse : ");
    Serial.println(codeReponse);
    return codeReponse;
}

// PATCH /poubelles {"etat": true|false, "idPoubelle": 0|1|2|3|4}
int envoyerRequetePATCHPoubelle(int id, bool etat)
{
    String urlPoubelle = url + String("/poubelles/") + String(id);
    httpClient.begin(client, urlPoubelle);
    httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
    httpClient.addHeader("Content-Type", "application/json");
    String payload = "{}";
    if(etat)
    {
        payload = String("{\"etat\":true,") + String("\"idPoubelle\":") + String(id) + String("}");
    }
    else
    {
        payload = String("{\"etat\":false,") + String("\"idPoubelle\":") + String(id) + String("}");
    }
    Serial.println("envoyerRequetePATCHPoubelle()");
    Serial.print("   id   = ");
    Serial.println(id);
    Serial.print("   etat = ");
    Serial.println(etat);
    Serial.print("   url  = ");
    Serial.println(urlPoubelle);
    Serial.print("   json = ");
    Serial.println(payload);
    int codeReponse = httpClient.PATCH(payload);
    httpClient.end();
    Serial.print("   code reponse : ");
    Serial.println(codeReponse);
    return codeReponse;
}

// PATCH /machines {"etat": true|false, "idMachine": 0|1|2|3|4|5}
int envoyerRequetePATCHMachine(int id, bool etat)
{
    String urlMachine = url + String("/machines/") + String(id);
    httpClient.begin(client, urlMachine);
    httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
    httpClient.addHeader("Content-Type", "application/json");
    String payload = "{}";
    if(etat)
    {
        payload = String("{\"etat\":true,") + String("\"idMachine\":") + String(id) + String("}");
    }
    else
    {
        payload = String("{\"etat\":false,") + String("\"idMachine\":") + String(id) + String("}");
    }
    Serial.println("envoyerRequetePATCHMachine()");
    Serial.print("   id   = ");
    Serial.println(id);
    Serial.print("   etat = ");
    Serial.println(etat);
    Serial.print("   url  = ");
    Serial.println(urlMachine);
    Serial.print("   json = ");
    Serial.println(payload);
    int codeReponse = httpClient.PATCH(payload);
    httpClient.end();
    Serial.print("   code reponse : ");
    Serial.println(codeReponse);
    return codeReponse;
}

int envoyerRequeteGETBoite(int id)
{
    String urlBoite = url + String("/boites/") + String(id);
    httpClient.begin(urlBoite.c_str());

    Serial.println("envoyerRequeteGETBoite()");
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

int envoyerRequeteGETPoubelle(int id)
{
    String urlPoubelle = url + String("/poubelles/") + String(id);
    httpClient.begin(urlPoubelle.c_str());

    Serial.println("envoyerRequeteGETPoubelle()");
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

int envoyerRequeteGETMachine(int id)
{
    String urlMachine = url + String("/machines/") + String(id);
    httpClient.begin(urlMachine.c_str());

    Serial.println("envoyerRequeteGETMachine()");
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

bool setEtatMachine(int numeroMachine, bool etat)
{
    if(numeroMachine >= 0 && numeroMachine < NB_NOTIFICATION_MACHINES)
    {
        char cle[64] = "";

        if(etat)
        {
            if(allumerNotificationMachine(numeroMachine) == REPONSE_OK)
            {
                digitalWrite(NOTIFICATION_MACHINE, (etat ? HIGH : LOW));
                etatMachines[numeroMachine] = etat;
                sprintf((char*)cle, "%s%d", "machine", numeroMachine);
                preferences.putBool(cle, etatMachines[numeroMachine]);
                return true;
            }
            else
                return false;
        }
        else
        {
            if(eteindreNotificationMachine(numeroMachine) == REPONSE_OK)
            {
                digitalWrite(NOTIFICATION_MACHINE, (etat ? HIGH : LOW));
                etatMachines[numeroMachine] = etat;
                sprintf((char*)cle, "%s%d", "machine", numeroMachine);
                preferences.putBool(cle, etatMachines[numeroMachine]);
                return true;
            }
            else
                return false;
        }
    }

    return false;
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
    digitalWrite(NOTIFICATION_MACHINE, LOW);
    eteindreNotificationMachines();
}

int allumerNotificationMachine(int numeroMachine)
{
    return envoyerRequetePATCHMachine(numeroMachine + 1, true);
}

int eteindreNotificationMachine(int numeroMachine)
{
    return envoyerRequetePATCHMachine(numeroMachine + 1, false);
}

void allumerNotificationMachines()
{
    for(int i = 0; i < NB_NOTIFICATION_MACHINES; ++i)
    {
        allumerNotificationMachine(i);
    }
}

void eteindreNotificationMachines()
{
    for(int i = 0; i < NB_NOTIFICATION_MACHINES; ++i)
    {
        eteindreNotificationMachine(i);
    }
}

bool getEtatPoubelle(int numeroPoubelle)
{
    if(numeroPoubelle >= 0 && numeroPoubelle < NB_NOTIFICATION_POUBELLES)
        return etatPoubelles[numeroPoubelle];
    return false;
}

bool setEtatPoubelle(int numeroPoubelle, bool etat)
{
    if(numeroPoubelle >= 0 && numeroPoubelle < NB_NOTIFICATION_POUBELLES)
    {
        char cle[64] = "";

        if(etat)
        {
            if(allumerNotificationPoubelle(numeroPoubelle) == REPONSE_OK)
            {
                digitalWrite(NOTIFICATION_POUBELLE, (etat ? HIGH : LOW));
                etatPoubelles[numeroPoubelle] = etat;
                sprintf((char*)cle, "%s%d", "poubelle", numeroPoubelle);
                preferences.putBool(cle, etatPoubelles[numeroPoubelle]);
                return true;
            }
            else
                return false;
        }
        else
        {
            if(eteindreNotificationPoubelle(numeroPoubelle) == REPONSE_OK)
            {
                digitalWrite(NOTIFICATION_POUBELLE, (etat ? HIGH : LOW));
                etatPoubelles[numeroPoubelle] = etat;
                sprintf((char*)cle, "%s%d", "poubelle", numeroPoubelle);
                preferences.putBool(cle, etatPoubelles[numeroPoubelle]);
                return true;
            }
            else
                return false;
        }
    }

    return false;
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
    digitalWrite(NOTIFICATION_POUBELLE, LOW);
    eteindreNotificationPoubelles();
}

int allumerNotificationPoubelle(int numeroPoubelle)
{
    return envoyerRequetePATCHPoubelle(numeroPoubelle + 1, true);
}

int eteindreNotificationPoubelle(int numeroPoubelle)
{
    return envoyerRequetePATCHPoubelle(numeroPoubelle + 1, false);
}

void allumerNotificationPoubelles()
{
    for(int i = 0; i < NB_NOTIFICATION_POUBELLES; ++i)
    {
        allumerNotificationPoubelle(i);
    }
}

void eteindreNotificationPoubelles()
{
    for(int i = 0; i < NB_NOTIFICATION_POUBELLES; ++i)
    {
        eteindreNotificationPoubelle(i);
    }
}

bool getEtatBoiteAuxLettres(int numeroBoite)
{
    return etatBoitesAuxLettres[numeroBoite];
}

bool setEtatBoiteAuxLettres(int numeroBoite, bool etat)
{
    if(numeroBoite >= 0 && numeroBoite < NB_NOTIFICATION_BOITES)
    {
        char cle[64] = "";

        if(etat)
        {
            if(allumerNotificationBoiteAuxLettres(numeroBoite) == REPONSE_OK)
            {
                digitalWrite(NOTIFICATION_BOITE, (etat ? HIGH : LOW));
                etatBoitesAuxLettres[numeroBoite] = etat;
                sprintf((char*)cle, "%s%d", "boite", numeroBoite);
                preferences.putBool(cle, etatBoitesAuxLettres[numeroBoite]);
                return true;
            }
            else
                return false;
        }
        else
        {
            if(eteindreNotificationBoiteAuxLettres(numeroBoite) == REPONSE_OK)
            {
                digitalWrite(NOTIFICATION_BOITE, (etat ? HIGH : LOW));
                etatBoitesAuxLettres[numeroBoite] = etat;
                sprintf((char*)cle, "%s%d", "boite", numeroBoite);
                preferences.putBool(cle, etatBoitesAuxLettres[numeroBoite]);
                return true;
            }
            else
                return false;
        }
    }

    return false;
}

void resetEtatBoitesAuxLettres()
{
    for(int numeroBoite = 0; numeroBoite < NB_NOTIFICATION_BOITES; ++numeroBoite)
    {
        etatBoitesAuxLettres[numeroBoite] = false;
    }
    char cle[64] = "";
    for(int i = 0; i < NB_NOTIFICATION_BOITES; ++i)
    {
        sprintf((char*)cle, "%s%d", "boite", i);
        preferences.putBool(cle, etatBoitesAuxLettres[i]);
    }
    digitalWrite(NOTIFICATION_BOITE, LOW);
    eteindreNotificationBoitesAuxLettres();
}

int allumerNotificationBoiteAuxLettres(int numeroBoite)
{
    return envoyerRequetePATCHBoite(numeroBoite + 1, true);
}

int eteindreNotificationBoiteAuxLettres(int numeroBoite)
{
    return envoyerRequetePATCHBoite(numeroBoite + 1, false);
}

void allumerNotificationBoitesAuxLettres()
{
    for(int i = 0; i < NB_NOTIFICATION_BOITES; ++i)
    {
        allumerNotificationBoiteAuxLettres(i);
    }
}

void eteindreNotificationBoitesAuxLettres()
{
    for(int i = 0; i < NB_NOTIFICATION_BOITES; ++i)
    {
        eteindreNotificationBoiteAuxLettres(i);
    }
}

// Fonctions utilitaires
void determinerModuleSelectionne(int choixIdModule)
{
    // nomModule et idModule ?
    if(choixIdModule < INDEX_NOTIFICATION_POUBELLES)
    {
        nomModule = "Machine";
        idModule  = choixIdModule;
    }
    else if(choixIdModule >= INDEX_NOTIFICATION_POUBELLES &&
            choixIdModule < INDEX_NOTIFICATION_BOITE)
    {
        nomModule = "Poubelle";
        idModule  = choixIdModule - INDEX_NOTIFICATION_POUBELLES;
    }
    else
    {
        nomModule = "Boite";
        idModule  = choixIdModule - INDEX_NOTIFICATION_BOITE;
    }
}

bool getEtatModuleSelectionne()
{
    // nomModule ?
    if(choixIdModule < INDEX_NOTIFICATION_POUBELLES)
    {
        // "Machine"
        return getEtatMachine(idModule);
    }
    else if(choixIdModule >= INDEX_NOTIFICATION_POUBELLES &&
            choixIdModule < INDEX_NOTIFICATION_BOITE)
    {
        // "Poubelle"
        return getEtatPoubelle(idModule);
    }
    else
    {
        // "Boite"
        return getEtatBoiteAuxLettres(idModule);
    }
    return false;
}

bool setEtatModuleSelectionne(bool etat)
{
    // nomModule ?
    if(choixIdModule < INDEX_NOTIFICATION_POUBELLES)
    {
        // "Machine"
        return setEtatMachine(idModule, etat);
    }
    else if(choixIdModule >= INDEX_NOTIFICATION_POUBELLES &&
            choixIdModule < INDEX_NOTIFICATION_BOITE)
    {
        // "Poubelle"
        return setEtatPoubelle(idModule, etat);
    }
    else
    {
        // "Boite"
        return setEtatBoiteAuxLettres(idModule, etat);
    }
    return false;
}

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
