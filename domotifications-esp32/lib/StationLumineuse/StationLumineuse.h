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

#define DEBUG_STATION_LUMINEUSE

#define TEST_TEMPORISATION 2000 // en ms

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
    ServeurWeb*            serveurWeb;  //!< le serveur web (HTTP API REST)
    Adafruit_NeoPixel      leds;        //!< le bandeau à leds multi-couleurs
    Preferences            preferences; //!< pour le stockage interne
    std::vector<Boite*>    boites;      //!< les boites de la station lumineuse
    std::vector<Machine*>  machines;    //!< les machines de la station lumineuse
    std::vector<Poubelle*> poubelles;   //!< les poubelles de la station lumineuse
    static uint32_t
      couleursPoubelles[NB_LEDS_NOTIFICATION_POUBELLES]; //!< les couleurs par défaut des poubelles

    void restaurerEtats();
    void sauvegarderCouleurPoubelle(int id);
    void restaurerCouleurPoubelle(int id);
    void sauvegarderCouleurBoite(int id);
    void restaurerCouleurBoite(int id);
    void sauvegarderCouleurMachine(int id);
    void restaurerCouleurMachine(int id);

  public:
    StationLumineuse();

    void demarrer();
    void traiterRequetes();
    void testerBandeau();

    // Méthodes statiques
    static uint32_t convertirCouleurRGB(uint8_t r, uint8_t g, uint8_t b);
    static String   getCouleurToString(uint32_t couleur);
    static uint32_t getCouleurToRGB(String couleur);

    // pour les modules Poubelle
    std::size_t getNbPoubelles() const;
    Poubelle*   getPoubelle(int id);
    void        sauvegarderEtatsPoubelle(int id);

    // pour les modules Boite
    std::size_t getNbBoites() const;
    Boite*      getBoite(int id);
    void        sauvegarderEtatsBoite(int id);

    // pour les modules Machine
    std::size_t getNbMachines() const;
    Machine*    getMachine(int id);
    void        sauvegarderEtatsMachine(int id);
};

#endif // STATIONLUMINEUSE_H
