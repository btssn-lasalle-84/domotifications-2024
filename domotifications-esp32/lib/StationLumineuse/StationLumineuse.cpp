/**
 * @file StationLumineuse.cpp
 * @brief Définition de la classe StationLumineuse
 * @author Corentin MOUTTE
 * @version 0.1
 */

#include "StationLumineuse.h"
#include "ServeurWeb.h"
#include "Boite.h"
#include "Machine.h"
#include "Poubelle.h"

/**
 * @brief Constructeur de la classe StationLumineuse
 * @fn StationLumineuse::StationLumineuse
 */
StationLumineuse::StationLumineuse() :
    serveurWeb(new ServeurWeb(this)),
    leds(NB_LEDS, PIN_BANDEAU, NEO_GRB + NEO_KHZ800), couleursPoubelles{
        leds.Color(255, 0, 0),     // Couleur poubelle 0 (rouge)
        leds.Color(255, 255, 0),   // Couleur poubelle 1 (jaune)
        leds.Color(0, 0, 255),     // Couleur poubelle 2 (bleue)
        leds.Color(240, 240, 242), // Couleur poubelle 3 (grise)
        leds.Color(0, 255, 0)      // Couleur poubelle 4 (verte)
    }
{
    // les machines
    for(int i = 0; i < NB_LEDS_NOTIFICATION_MACHINES; ++i)
    {
        machines.push_back(new Machine(i + 1, i, leds.Color(0, 255, 0), leds));
    }

    // les poubelles
    for(int i = 0; i < NB_LEDS_NOTIFICATION_POUBELLES; ++i)
    {
        poubelles.push_back(new Poubelle(i + 1, i, couleursPoubelles[i], leds));
    }

    // les boîtes aux lettres
    for(int i = 0; i < NB_LEDS_NOTIFICATION_BOITE; ++i)
    {
        boites.push_back(new Boite(i + 1, i, leds.Color(255, 0, 0), leds));
    }

#ifdef TEST_BANDEAU_LEDS
    // Signale les notifications sur le bandeau
    for(int i = 0; i < NB_LEDS_NOTIFICATION_MACHINES; ++i)
    {
        machines[i]->allumerNotification();
    }
    for(int i = 0; i < NB_LEDS_NOTIFICATION_POUBELLES; ++i)
    {
        poubelles[i]->allumerNotification();
    }
    for(int i = 0; i < NB_LEDS_NOTIFICATION_BOITE; ++i)
    {
        boites[i]->allumerNotification();
    }

    delay(TEST_TEMPORISATION);

    for(int i = 0; i < NB_LEDS_NOTIFICATION_MACHINES; ++i)
    {
        machines[i]->eteindreNotification();
    }
    for(int i = 0; i < NB_LEDS_NOTIFICATION_POUBELLES; ++i)
    {
        poubelles[i]->eteindreNotification();
    }
    for(int i = 0; i < NB_LEDS_NOTIFICATION_BOITE; ++i)
    {
        boites[i]->eteindreNotification();
    }
#endif
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