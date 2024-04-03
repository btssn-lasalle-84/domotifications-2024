/**
 * @file ServeurWeb.h
 * @brief Déclaration de la classe ServeurWeb
 * @author Corentin MOUTTE
 * @version 0.1
 */

#ifndef SERVEURWEB_H
#define SERVEURWEB_H

#include <WebServer.h>
#include <ArduinoJson.h>

#define DEBUG_SERVEUR_WEB

/**
 * @def NOM_SERVEUR_WEB
 * @brief Le nom du serveur web (http://station-lumineuse.local/)
 */
#define NOM_SERVEUR_WEB "station-lumineuse"

/**
 * @def PORT_SERVEUR_WEB
 * @brief Le numéro de port du serveur web
 */
#define PORT_SERVEUR_WEB 80

/**
 * @def TAILLE_JSON
 * @brief Définit la taille en octets max pour un document JSON
 */
#define TAILLE_JSON 256

class StationLumineuse;

/**
 * @class ServeurWeb
 * @brief Déclaration de la classe ServeurWeb
 */
class ServeurWeb : public WebServer
{
  private:
    StationLumineuse* stationLumineuse;

    void installerGestionnairesRequetes();
    void afficherAccueil();
    void traiterRequeteNonTrouvee();

  public:
    ServeurWeb(StationLumineuse* stationLumineuse);
    void demarrer();
    void traiterRequetes();
};

#endif // SERVEURWEB_H
