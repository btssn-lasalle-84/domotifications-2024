/**
 * @file ServeurWeb.cpp
 * @brief Définition de la classe ServeurWeb
 * @author Corentin MOUTTE
 * @version 0.1
 */

#include "ServeurWeb.h"
#include "StationLumineuse.h"
#include "Boite.h"
#include "Machine.h"
#include "Poubelle.h"
#include <ESPmDNS.h>
#include <uri/UriRegex.h>

/**
 * @brief Constructeur de la classe ServeurWeb
 * @fn ServeurWeb::ServeurWeb
 * @param stationLumineuse Adresse vers l'objet de la classe StationLumineuse
 */
ServeurWeb::ServeurWeb(StationLumineuse* stationLumineuse) :
    WebServer(PORT_SERVEUR_WEB), stationLumineuse(stationLumineuse)
{
}

/**
 * @brief Démarre le serveur web et installe les gestionnaires de requêtes
 * @fn ServeurWeb::demarrer
 */
void ServeurWeb::demarrer()
{
    // Pour un accès : http://NOM_SERVEUR_WEB.local/
    if(!MDNS.begin(NOM_SERVEUR_WEB))
    {
#ifdef DEBUG_SERVEUR_WEB
        Serial.println(F("ServeurWeb::demarrer() : Erreur mDNS !"));
#endif
    }

#ifdef DEBUG_SERVEUR_WEB
    Serial.print(F("ServeurWeb::demarrer() : nom = "));
    Serial.println("http://" + String(NOM_SERVEUR_WEB) + ".local/");
    Serial.print(F("ServeurWeb::demarrer() : adresse IP = "));
    Serial.println(WiFi.localIP());
#endif

    installerGestionnairesRequetes();

    // Démarre le serveur
    begin();
}

/**
 * @brief traite les requêtes reçues par le serveur web
 * @fn ServeurWeb::traiterRequetes
 */
void ServeurWeb::traiterRequetes()
{
    handleClient();
}

/**
 * @brief Installe les gestionnaires de requêtes de l'API HTTP REST
 * @fn ServeurWeb::installerGestionnairesRequetes
 * @details Cette méthode installe les gestionnaires de requêtes pour les différentes routes du
 * serveur Web. Les gestionnaires sont définis à l'aide de la méthode on(). Les routes sont
 * associées aux méthodes de traitement des requêtes correspondantes.
 */
void ServeurWeb::installerGestionnairesRequetes()
{
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::installerGestionnairesRequetes()"));
#endif
    on("/", HTTP_GET, std::bind(&ServeurWeb::afficherAccueil, this));
    onNotFound(std::bind(&ServeurWeb::traiterRequeteNonTrouvee, this));
    on("/test", HTTP_GET, std::bind(&ServeurWeb::testerBandeau, this));

    // pour les modules Poubelles
    on("/poubelles", HTTP_GET, std::bind(&ServeurWeb::traiterRequeteGetPoubelles, this));
    on(UriRegex("/poubelles/([1-" + String(NB_LEDS_NOTIFICATION_POUBELLES) + "]+)$"),
       HTTP_GET,
       std::bind(&ServeurWeb::traiterRequeteGetPoubelle, this));
    on(UriRegex("/poubelles/([1-" + String(NB_LEDS_NOTIFICATION_POUBELLES) + "]+)$"),
       HTTP_PATCH,
       std::bind(&ServeurWeb::traiterRequeteUpdatePoubelle, this));

    // @todo idem pour les modules Machine
    on("/machines", HTTP_GET, std::bind(&ServeurWeb::traiterRequeteGetMachines, this));
    on(UriRegex("/machines/([1-" + String(NB_LEDS_NOTIFICATION_MACHINES) + "]+)$"),
       HTTP_GET,
       std::bind(&ServeurWeb::traiterRequeteGetMachine, this));
    on(UriRegex("/machines/([1-" + String(NB_LEDS_NOTIFICATION_MACHINES) + "]+)$"),
       HTTP_PATCH,
       std::bind(&ServeurWeb::traiterRequeteUpdateMachine, this));

    // @todo idem pour les modules Boite
    /*on("/boites", HTTP_GET, std::bind(&ServeurWeb::traiterRequeteGetBoites, this));
    on(UriRegex("/boites/([1-" + String(NB_LEDS_NOTIFICATION_BOITE) + "]+)$"),
       HTTP_GET,
       std::bind(&ServeurWeb::traiterRequeteGetBoite, this));
    on(UriRegex("/boites/([1-" + String(NB_LEDS_NOTIFICATION_BOITE) + "]+)$"),
       HTTP_PATCH,
       std::bind(&ServeurWeb::traiterRequeteUpdateBoite, this));*/
}

/**
 * @brief Affiche la page d'accueil du serveur web
 * @fn ServeurWeb::afficherAccueil
 */
void ServeurWeb::afficherAccueil()
{
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::afficherAccueil()"));
    Serial.print(F("  REQUETE   : "));
    Serial.println((method() == HTTP_GET) ? "GET" : "?");
    Serial.print(F("  URI       : "));
    Serial.println(uri());
#endif
    String message = "<h1>Bienvenue sur la station de notifications lumineuses</h1>\n";
    message += "<p>LaSalle Avignon v1.0</p>\n";
    send(200, F("text/html"), message);
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("  REPONSE   : 200"));
#endif
}

/**
 * @brief Traite une requête qui n'a pas été trouvée
 * @fn ServeurWeb::traiterRequeteNonTrouvee
 */
void ServeurWeb::traiterRequeteNonTrouvee()
{
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::traiterRequeteNonTrouvee()"));
    Serial.print(F("  REQUETE   : "));
    Serial.print((method() == HTTP_GET) ? "GET" : "");
    Serial.print((method() == HTTP_POST) ? "POST" : "");
    Serial.print((method() == HTTP_PATCH) ? "PATCH" : "");
    Serial.print((method() == HTTP_PUT) ? "PUT" : "");
    Serial.print((method() == HTTP_DELETE) ? "DELETE" : "");
    Serial.println();
    Serial.print(F("  URI       : "));
    Serial.println(uri());
    Serial.print(F("  {id}      : "));
    Serial.println(uri().substring(uri().lastIndexOf('/') + 1).toInt());
    Serial.print(F("  BODY      : "));
    Serial.println(arg("plain"));
#endif

    String message = "404 Module non trouvé\r\n";
    send(404, "text/plain", message);

#ifdef DEBUG_SERVEUR_WEB
    Serial.print(F("  REPONSE   : "));
    Serial.println(message);
#endif
}

/**
 * @brief Traite La requête qui qui permet de tester les leds du bandeau
 * @fn ServeurWeb::testerBandeau
 */
void ServeurWeb::testerBandeau()
{
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::testerBandeau()"));
    Serial.print(F("  REQUETE   : "));
    Serial.println((method() == HTTP_GET) ? "GET" : "?");
    Serial.print(F("  URI       : "));
    Serial.println(uri());
#endif

    stationLumineuse->testerBandeau();
    send(200, F("text/plain"), F("Ok"));

#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("  REPONSE   : 200 Ok"));
#endif
}

/**
 * @brief Traite la requête GET pour obtenir la liste des poubelles
 * @fn ServeurWeb::traiterRequeteGetPoubelles
 */
void ServeurWeb::traiterRequeteGetPoubelles()
{
    /*  Test :
        $ curl --location http://station-lumineuse.local:80/poubelles
    */
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::traiterRequeteGetPoubelles()"));
    Serial.print(F("  REQUETE   : "));
    Serial.println((method() == HTTP_GET) ? "GET" : "?");
    Serial.print(F("  URI       : "));
    Serial.println(uri());
#endif

    /*  Exemple de réponse :
        [{"idPoubelle":1,"etat":false,"couleur":"bleu","actif":true},{"idPoubelle":2,"etat":true,"couleur":"verte","actif":true}]
    */
    documentJSON.clear();
    Poubelle* poubelle = nullptr;
    for(int i = 1; i <= stationLumineuse->getNbPoubelles(); ++i)
    {
        poubelle = stationLumineuse->getPoubelle(i);
        if(poubelle == nullptr)
        {
            continue;
        }
        JsonObject objetPoubelle    = documentJSON.createNestedObject();
        objetPoubelle["idPoubelle"] = poubelle->getId();
        objetPoubelle["couleur"]    = poubelle->getCouleur();
        objetPoubelle["etat"]       = poubelle->getEtatNotification();
        objetPoubelle["actif"]      = poubelle->getActivation();
    }

    char buffer[TAILLE_JSON];
    serializeJson(documentJSON, buffer);
    send(200, "application/json", buffer);

#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("  REPONSE   : 200"));
    serializeJson(documentJSON, Serial);
    Serial.println();
#endif
}

void ServeurWeb::traiterRequeteGetPoubelle()
{
    /*  Tests :
        $ curl --location http://station-lumineuse.local:80/poubelles/1
        $ curl --location http://station-lumineuse.local:80/poubelles/6
    */
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::traiterRequeteGetPoubelle()"));
    Serial.print(F("  REQUETE   : "));
    Serial.println((method() == HTTP_GET) ? "GET" : "?");
    Serial.print(F("  URI       : "));
    Serial.println(uri());
    Serial.print(F("  {id}      : "));
    Serial.println(uri().substring(uri().lastIndexOf('/') + 1).toInt());
#endif

    /*  Exemple de réponse :
        [{"idPoubelle":1,"etat":false,"couleur":"bleu","actif":true}]
    */

    // Récupère l'id dans l'URI
    int idPoubelle = uri().substring(uri().lastIndexOf('/') + 1).toInt();
    // Récupère l'objet Poubelle correspondant à l'id demandé
    Poubelle* poubelle = stationLumineuse->getPoubelle(idPoubelle);
    // L'objet Poubelle existe ?
    if(poubelle == nullptr)
    {
        String message = "404 Poubelle non trouvée\r\n";
        // Renvoie un message avec un code HTTP 404 (id Poubelle introuvable)
        send(404, "text/plain", message);
#ifdef DEBUG_SERVEUR_WEB
        Serial.print(F("  REPONSE   : "));
        Serial.println(message);
#endif
        return;
    }

    // Initialise les données JSON à renvoyer
    documentJSON.clear();
    // Crée un objet JSON
    JsonObject objetPoubelle = documentJSON.createNestedObject();
    // Ajoute les données de l'objet Poubelle en JSON
    objetPoubelle["idPoubelle"] = poubelle->getId();
    objetPoubelle["couleur"]    = poubelle->getCouleur();
    objetPoubelle["etat"]       = poubelle->getEtatNotification();
    objetPoubelle["actif"]      = poubelle->getActivation();

    char buffer[TAILLE_JSON];
    // Convertit les données JSON en chaîne de caractères
    serializeJson(documentJSON, buffer);
    // Renvoie les données JSON avec un code HTTP 200 (succès)
    send(200, "application/json", buffer);

#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("  REPONSE   : 200"));
    serializeJson(documentJSON, Serial);
    Serial.println();
#endif
}

void ServeurWeb::traiterRequeteUpdatePoubelle()
{
    /*  Tests :
        $ curl --location 'http://station-lumineuse.local:80/poubelles/1' --request PATCH \
        --header 'Content-Type: application/json' --data '{"idPoubelle": "1","etat": true}'
        $ curl --location 'http://station-lumineuse.local:80/poubelles/1' --request PATCH \
        --header 'Content-Type: application/json' --data '{"idPoubelle": "1","etat": false}'
    */
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::traiterRequeteUpdatePoubelle()"));
    Serial.print(F("  REQUETE   : "));
    Serial.println((method() == HTTP_PATCH) ? "PATCH" : "?");
    Serial.print(F("  URI       : "));
    Serial.println(uri());
    Serial.print(F("  {id}      : "));
    Serial.println(uri().substring(uri().lastIndexOf('/') + 1).toInt());
    Serial.print(F("  BODY      : "));
    Serial.println(arg("plain"));
#endif

    String               body   = arg("plain");
    DeserializationError erreur = deserializeJson(documentJSON, body);
    if(erreur)
    {
        String contenu = "{\"code\": 2,\"message\": \"La demande est invalide\"}";
        send(400, "application/json", contenu);
#ifdef DEBUG_SERVEUR_WEB
        Serial.print(F("  REPONSE   : 400 "));
        Serial.println(contenu);
#endif
        return;
    }
    int       idPoubelle = ID_INVALIDE;
    Poubelle* poubelle   = nullptr;
    // Récupère les données envoyées dans la requête PATCH
    JsonObject objetJSON = documentJSON.as<JsonObject>();
    if(objetJSON.containsKey("idPoubelle"))
    {
        idPoubelle = documentJSON["idPoubelle"].as<int>();
        poubelle   = stationLumineuse->getPoubelle(idPoubelle);
        if(poubelle == nullptr)
        {
            String contenu = "{\"code\": 2,\"message\": \"La demande est invalide\"}";
            send(400, "application/json", contenu);
#ifdef DEBUG_SERVEUR_WEB
            Serial.print(F("  REPONSE   : 400 "));
            Serial.println(contenu);
#endif
            return;
        }
    }
    // Mise à jour de l'activation du module ?
    if(objetJSON.containsKey("actif"))
    {
        bool activationPoubelle = documentJSON["actif"].as<bool>();
        poubelle->setActivation(activationPoubelle);
    }
    // Mise à jour de l'état de notification du module ?
    if(objetJSON.containsKey("etat"))
    {
        bool etatPoubelle = documentJSON["etat"].as<bool>();
        poubelle->setEtatNotification(etatPoubelle);
    }

    // Sauvegarde les états de ce module
    stationLumineuse->sauvegarderEtatsPoubelle(idPoubelle);

    documentJSON.clear();
    JsonObject objetPoubelle = documentJSON.createNestedObject();
    // Ajoute les données mises à jour de l'objet Poubelle en JSON
    objetPoubelle["idPoubelle"] = poubelle->getId();
    objetPoubelle["couleur"]    = poubelle->getCouleur();
    objetPoubelle["etat"]       = poubelle->getEtatNotification();
    objetPoubelle["actif"]      = poubelle->getActivation();

    char buffer[TAILLE_JSON];
    serializeJson(documentJSON, buffer);
    send(200, "application/json", buffer);

#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("  REPONSE   : 200"));
    serializeJson(documentJSON, Serial);
    Serial.println();
#endif
}

void ServeurWeb::traiterRequeteGetMachines()
{
    /*  Test :
        $ curl --location http://station-lumineuse.local:80/poubelles
    */
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::traiterRequeteGetPoubelles()"));
    Serial.print(F("  REQUETE   : "));
    Serial.println((method() == HTTP_GET) ? "GET" : "?");
    Serial.print(F("  URI       : "));
    Serial.println(uri());
#endif

    /*  Exemple de réponse :
        [{"idPoubelle":1,"etat":false,"couleur":"bleu","actif":true},{"idPoubelle":2,"etat":true,"couleur":"verte","actif":true}]
    */
    documentJSON.clear();
    Poubelle* poubelle = nullptr;
    for(int i = 1; i <= stationLumineuse->getNbPoubelles(); ++i)
    {
        poubelle = stationLumineuse->getPoubelle(i);
        if(poubelle == nullptr)
        {
            continue;
        }
        JsonObject objetPoubelle    = documentJSON.createNestedObject();
        objetPoubelle["idPoubelle"] = poubelle->getId();
        objetPoubelle["couleur"]    = poubelle->getCouleur();
        objetPoubelle["etat"]       = poubelle->getEtatNotification();
        objetPoubelle["actif"]      = poubelle->getActivation();
    }

    char buffer[TAILLE_JSON];
    serializeJson(documentJSON, buffer);
    send(200, "application/json", buffer);

#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("  REPONSE   : 200"));
    serializeJson(documentJSON, Serial);
    Serial.println();
#endif
}

void ServeurWeb::traiterRequeteGetMachine()
{
    /*  Tests :
        $ curl --location http://station-lumineuse.local:80/poubelles/1
        $ curl --location http://station-lumineuse.local:80/poubelles/6
    */
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::traiterRequeteGetPoubelle()"));
    Serial.print(F("  REQUETE   : "));
    Serial.println((method() == HTTP_GET) ? "GET" : "?");
    Serial.print(F("  URI       : "));
    Serial.println(uri());
    Serial.print(F("  {id}      : "));
    Serial.println(uri().substring(uri().lastIndexOf('/') + 1).toInt());
#endif

    /*  Exemple de réponse :
        [{"idPoubelle":1,"etat":false,"couleur":"bleu","actif":true}]
    */

    // Récupère l'id dans l'URI
    int idPoubelle = uri().substring(uri().lastIndexOf('/') + 1).toInt();
    // Récupère l'objet Poubelle correspondant à l'id demandé
    Poubelle* poubelle = stationLumineuse->getPoubelle(idPoubelle);
    // L'objet Poubelle existe ?
    if(poubelle == nullptr)
    {
        String message = "404 Poubelle non trouvée\r\n";
        // Renvoie un message avec un code HTTP 404 (id Poubelle introuvable)
        send(404, "text/plain", message);
#ifdef DEBUG_SERVEUR_WEB
        Serial.print(F("  REPONSE   : "));
        Serial.println(message);
#endif
        return;
    }

    // Initialise les données JSON à renvoyer
    documentJSON.clear();
    // Crée un objet JSON
    JsonObject objetPoubelle = documentJSON.createNestedObject();
    // Ajoute les données de l'objet Poubelle en JSON
    objetPoubelle["idPoubelle"] = poubelle->getId();
    objetPoubelle["couleur"]    = poubelle->getCouleur();
    objetPoubelle["etat"]       = poubelle->getEtatNotification();
    objetPoubelle["actif"]      = poubelle->getActivation();

    char buffer[TAILLE_JSON];
    // Convertit les données JSON en chaîne de caractères
    serializeJson(documentJSON, buffer);
    // Renvoie les données JSON avec un code HTTP 200 (succès)
    send(200, "application/json", buffer);

#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("  REPONSE   : 200"));
    serializeJson(documentJSON, Serial);
    Serial.println();
#endif
}

void ServeurWeb::traiterRequeteUpdateMachine()
{
    /*  Tests :
        $ curl --location 'http://station-lumineuse.local:80/machines/1' --request PATCH \
        --header 'Content-Type: application/json' --data '{"idMachine": "1","etat": true}'
        $ curl --location 'http://station-lumineuse.local:80/machines/1' --request PATCH \
        --header 'Content-Type: application/json' --data '{"idMachine": "1","etat": false}'
    */
#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("ServeurWeb::traiterRequeteUpdateMachine()"));
    Serial.print(F("  REQUETE   : "));
    Serial.println((method() == HTTP_PATCH) ? "PATCH" : "?");
    Serial.print(F("  URI       : "));
    Serial.println(uri());
    Serial.print(F("  {id}      : "));
    Serial.println(uri().substring(uri().lastIndexOf('/') + 1).toInt());
    Serial.print(F("  BODY      : "));
    Serial.println(arg("plain"));
#endif

    String               body   = arg("plain");
    DeserializationError erreur = deserializeJson(documentJSON, body);
    if(erreur)
    {
        String contenu = "{\"code\": 2,\"message\": \"La demande est invalide\"}";
        send(400, "application/json", contenu);
#ifdef DEBUG_SERVEUR_WEB
        Serial.print(F("  REPONSE   : 400 "));
        Serial.println(contenu);
#endif
        return;
    }
    int      idMachine = ID_INVALIDE;
    Machine* machine   = nullptr;
    // Récupère les données envoyées dans la requête PATCH
    JsonObject objetJSON = documentJSON.as<JsonObject>();
    if(objetJSON.containsKey("idMachine"))
    {
        idMachine = documentJSON["idMachine"].as<int>();
        machine   = stationLumineuse->getMachine(idMachine);
        if(machine == nullptr)
        {
            String contenu = "{\"code\": 2,\"message\": \"La demande est invalide\"}";
            send(400, "application/json", contenu);
#ifdef DEBUG_SERVEUR_WEB
            Serial.print(F("  REPONSE   : 400 "));
            Serial.println(contenu);
#endif
            return;
        }
    }
    // Mise à jour de l'activation du module ?
    if(objetJSON.containsKey("actif"))
    {
        bool activationMachine = documentJSON["actif"].as<bool>();
        machine->setActivation(activationMachine);
    }
    // Mise à jour de l'état de notification du module ?
    if(objetJSON.containsKey("etat"))
    {
        bool etatMachine = documentJSON["etat"].as<bool>();
        machine->setEtatNotification(etatMachine);
    }

    // Sauvegarde les états de ce module
    stationLumineuse->sauvegarderEtatsPoubelle(idMachine);

    documentJSON.clear();
    JsonObject objetMachine = documentJSON.createNestedObject();
    // Ajoute les données mises à jour de l'objet Poubelle en JSON
    objetMachine["idPoubelle"] = machine->getId();
    objetMachine["couleur"]    = machine->getCouleur();
    objetMachine["etat"]       = machine->getEtatNotification();
    objetMachine["actif"]      = machine->getActivation();

    char buffer[TAILLE_JSON];
    serializeJson(documentJSON, buffer);
    send(200, "application/json", buffer);

#ifdef DEBUG_SERVEUR_WEB
    Serial.println(F("  REPONSE   : 200"));
    serializeJson(documentJSON, Serial);
    Serial.println();
#endif
}