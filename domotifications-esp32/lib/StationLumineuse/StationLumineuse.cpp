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
    serveurWeb(new ServeurWeb(this)), leds(NB_LEDS, PIN_BANDEAU, NEO_GRB + NEO_KHZ800)
{
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
}