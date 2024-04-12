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

#define ROUGE 255, 0, 0
#define BLEU  0, 0, 255
#define VERT  0, 255, 0
#define JAUNE 255, 255, 0
#define GRIS  105, 105, 105

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
<<<<<<< HEAD
    for(int i = 0; i < 6; ++i)
    {
        int color;

        if(i == 0)
        {
            color = leds.Color(ROUGE);
        }
        else if(i == 1)
        {
            color = leds.Color(JAUNE);
        }
        else if(i == 2)
        {
            color = leds.Color(BLEU);
        }
        else if(i == 3)
        {
            color = leds.Color(GRIS);
        }
        else if(i == 4)
        {
            color = leds.Color(VERT);
        }
        poubelles.push_back(new Poubelle(i + 1, i, color, leds));
        machines.push_back(new Machine(i + 1, i, leds.Color(VERT), leds));
        boites.push_back(new Boite(i + 1, i, leds.Color(ROUGE), leds));
    }
=======
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
>>>>>>> 0522af4c7dbb09a010d6cd192dfa34ccef4d89ce
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
<<<<<<< HEAD
    machines[0]->allumerNotification();
    // machines[1]->allumerNotification();
    machines[2]->allumerNotification();
    // machines[3]->allumerNotification();
    machines[4]->allumerNotification();
    // machines[5]->allumerNotification();
    poubelles[0]->allumerNotification();
    // poubelles[1]->allumerNotification();
    poubelles[2]->allumerNotification();
    // poubelles[3]->allumerNotification();
    poubelles[4]->allumerNotification();
    // boites[0]->allumerNotification();
    boites[1]->allumerNotification();
    // boites[2]->allumerNotification();
    boites[3]->allumerNotification();
=======
>>>>>>> 0522af4c7dbb09a010d6cd192dfa34ccef4d89ce
}