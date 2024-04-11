/**
 * @file StationLumineuse.cpp
 * @brief Définition de la classe StationLumineuse
 * @author Corentin MOUTTE
 * @version 0.1
 */

#include "StationLumineuse.h"
#include "ServeurWeb.h"
#include "BandeauLeds.h"
#include "Boite.h"
#include "Machine.h"
#include "Poubelle.h"

/**
 * @brief Constructeur de la classe StationLumineuse
 * @fn StationLumineuse::StationLumineuse
 */
StationLumineuse::StationLumineuse() :
    serveurWeb(new ServeurWeb(this)), leds(NB_LEDS, PIN_BANDEAU, NEO_GRB + NEO_KHZ800)
{
    // machine 1
    machines.push_back(new Machine(1, 0, leds.Color(0, 255, 0), leds));
    // machine 2
    machines.push_back(new Machine(2, 1, leds.Color(0, 255, 0), leds));
    // machine 3
    machines.push_back(new Machine(3, 2, leds.Color(0, 255, 0), leds));
    // machine 4
    machines.push_back(new Machine(4, 3, leds.Color(0, 255, 0), leds));
    // machine 5
    machines.push_back(new Machine(5, 4, leds.Color(0, 255, 0), leds));
    // machine 6
    machines.push_back(new Machine(6, 5, leds.Color(0, 255, 0), leds));

    // poubelle rouge
    poubelles.push_back(new Poubelle(1, 0, leds.Color(255, 0, 0), leds));
    // poubelle jaune
    poubelles.push_back(new Poubelle(2, 1, leds.Color(255, 255, 0), leds));
    // poubelle bleue
    poubelles.push_back(new Poubelle(3, 2, leds.Color(0, 0, 255), leds));
    // poubelle grise
    poubelles.push_back(new Poubelle(4, 3, leds.Color(240, 240, 242), leds));
    // poubelle verte
    poubelles.push_back(new Poubelle(5, 4, leds.Color(0, 255, 0), leds));

    // boite 1
    boites.push_back(new Boite(4, 3, leds.Color(255, 0, 0), leds));
    // boite 2
    boites.push_back(new Boite(3, 2, leds.Color(255, 0, 0), leds));
    // boite 3
    boites.push_back(new Boite(2, 1, leds.Color(255, 0, 0), leds));
    // boite 4
    boites.push_back(new Boite(1, 0, leds.Color(255, 0, 0), leds));
}

/**
 * @brief Démarre la station lumineuse
 * @fn StationLumineuse::demarrer
 */
void StationLumineuse::demarrer()
{
    serveurWeb->demarrer();
}

/**
 * @brief Démarre la station lumineuse
 * @fn StationLumineuse::demarrer
 */
void StationLumineuse::traiterRequetes()
{
    serveurWeb->traiterRequetes();
    poubelles[0]->allumerNotification();
    poubelles[1]->allumerNotification();
    poubelles[2]->allumerNotification();
    poubelles[3]->allumerNotification();
    poubelles[4]->allumerNotification();
    machines[0]->allumerNotification();
    machines[1]->allumerNotification();
    machines[2]->allumerNotification();
    machines[3]->allumerNotification();
    machines[4]->allumerNotification();
    machines[5]->allumerNotification();
    boites[0]->allumerNotification();
    boites[1]->allumerNotification();
    boites[2]->allumerNotification();
    boites[3]->allumerNotification();
}