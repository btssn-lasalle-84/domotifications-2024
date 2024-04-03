/**
 * @file ServeurWeb.cpp
 * @brief Définition de la classe ServeurWeb
 * @author Corentin MOUTTE
 * @version 0.1
 */

#include "ServeurWeb.h"
#include "StationLumineuse.h"
#include <ESPmDNS.h>

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
 * @brief Installe les gestionnaires de requêtes
 * @fn ServeurWeb::installerGestionnairesRequetes
 * @details Cette méthode installe les gestionnaires de requêtes pour les différentes routes du
 * serveur Web. Les gestionnaires sont définis à l'aide de la méthode on(). Les routes sont
 * associées aux méthodes de traitement des requêtes correspondantes.
 */
void ServeurWeb::installerGestionnairesRequetes()
{
    on("/", HTTP_GET, std::bind(&ServeurWeb::afficherAccueil, this));
    onNotFound(std::bind(&ServeurWeb::traiterRequeteNonTrouvee, this));
}

/**
 * @brief Affiche la page d'accueil du serveur web
 * @fn ServeurWeb::afficherAccueil
 */
void ServeurWeb::afficherAccueil()
{
#ifdef DEBUG_SERVEUR_WEB
    Serial.print(F("ServeurWeb::afficherAccueil() : requête = "));
    Serial.println((method() == HTTP_GET) ? "GET" : "POST");
    Serial.print(F("URI : "));
    Serial.println(uri());
#endif
    String message = "<h1>Bienvenue sur la station de notifications lumineuses</h1>\n";
    message += "<p>LaSalle Avignon v1.0</p>\n";
    send(200, F("text/html"), message);
}

/**
 * @brief Traite une requête qui n'a pas été trouvée
 * @fn ServeurWeb::traiterRequeteNonTrouvee
 */
void ServeurWeb::traiterRequeteNonTrouvee()
{
#ifdef DEBUG_SERVEUR_WEB
    Serial.print(F("ServeurWeb::traiterRequeteNonTrouvee() : requête = "));
    Serial.println((method() == HTTP_GET) ? "GET" : "POST");
    Serial.print(F("URI : "));
    Serial.println(uri());
#endif

    String message = "404 File Not Found\n\n";
    message += "URI: ";
    message += uri();
    message += "\nMethod: ";
    message += (method() == HTTP_GET) ? "GET" : "POST";
    message += "\nArguments: ";
    message += args();
    message += "\n";
    for(uint8_t i = 0; i < args(); i++)
    {
        message += " " + argName(i) + ": " + arg(i) + "\n";
    }
    send(404, "text/plain", message);
}
