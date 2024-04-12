/**
 * @file StationLumineuse.h
 * @brief Déclaration de la classe StationLumineuse
 * @author Corentin MOUTTE
 * @version 0.1
 */

#ifndef STATIONLUMINEUSE_H
#define STATIONLUMINEUSE_H

#include <Arduino.h>
#include <Preferences.h>
#include <Adafruit_NeoPixel.h>
#include <vector>
#include "BandeauLeds.h"

// #define DEBUG_STATION_LUMINEUSE

#define TEST_BANDEAU_LEDS
#define TEST_TEMPORISATION 5000 // en ms

class ServeurWeb;
class Boite;
class Machine;
class Poubelle;

/**
 * @class StationLumineuse
 * @brief Déclaration de la classe StationLumineuse
 */
class StationLumineuse
{
  private:
    ServeurWeb*            serveurWeb; //!< le serveur web (HTTP API REST)
    Adafruit_NeoPixel      leds;       //!< le bandeau à leds multi-couleurs
    std::vector<Boite*>    boites;     //!< les boites de la station lumineuse
    std::vector<Machine*>  machines;   //!< les machines de la station lumineuse
    std::vector<Poubelle*> poubelles;  //!< les poubelles de la station lumineuse
    uint32_t couleursPoubelles[NB_LEDS_NOTIFICATION_POUBELLES]; //!< les couleurs des poubelles

  public:
    StationLumineuse();

    void demarrer();
    void traiterRequetes();
};

#endif // STATIONLUMINEUSE_H
