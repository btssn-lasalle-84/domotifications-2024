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
    // exemple pour 3 poubelles
    // poubelle bleue
    poubelles.push_back(new Poubelle(1, 0, leds.Color(0, 0, 255), leds));
    // poubelle verte
    poubelles.push_back(new Poubelle(2, 1, leds.Color(0, 255, 0), leds));
    // poubelle rouge
    poubelles.push_back(new Poubelle(3, 2, leds.Color(255, 255, 0), leds));
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
}