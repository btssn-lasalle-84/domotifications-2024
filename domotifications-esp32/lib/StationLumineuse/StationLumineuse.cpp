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

// définition globale des couleurs des poubelles
const char* nomCouleursPoubelles[NB_LEDS_NOTIFICATION_POUBELLES] = { "rouge",
                                                                     "jaune",
                                                                     "bleue",
                                                                     "grise",
                                                                     "verte" };

uint32_t StationLumineuse::couleursPoubelles[NB_LEDS_NOTIFICATION_POUBELLES] = {
    StationLumineuse::convertirCouleurRGB(255, 0, 0),     // Couleur poubelle 0 (rouge)
    StationLumineuse::convertirCouleurRGB(255, 255, 0),   // Couleur poubelle 1 (jaune)
    StationLumineuse::convertirCouleurRGB(0, 0, 255),     // Couleur poubelle 2 (bleue)
    StationLumineuse::convertirCouleurRGB(240, 240, 242), // Couleur poubelle 3 (grise)
    StationLumineuse::convertirCouleurRGB(0, 255, 0)      // Couleur poubelle 4 (verte)
};

/**
 * @brief Constructeur de la classe StationLumineuse
 * @fn StationLumineuse::StationLumineuse
 */
StationLumineuse::StationLumineuse() :
    serveurWeb(new ServeurWeb(this)), leds(NB_LEDS, PIN_BANDEAU, NEO_GRB + NEO_KHZ800)
{
    // les machines
    for(int i = 0; i < NB_LEDS_NOTIFICATION_MACHINES; ++i)
    {
        machines.push_back(new Machine(i + 1, i, leds.Color(0, 255, 0), leds));
    }

    // les poubelles
    for(int i = 0; i < NB_LEDS_NOTIFICATION_POUBELLES; ++i)
    {
        poubelles.push_back(
          new Poubelle(i + 1, nomCouleursPoubelles[i], i, couleursPoubelles[i], leds));
    }

    // les boîtes aux lettres
    for(int i = 0; i < NB_LEDS_NOTIFICATION_BOITE; ++i)
    {
        boites.push_back(new Boite(i + 1, i, leds.Color(255, 0, 0), leds));
    }
}

/**
 * @brief Démarre la station lumineuse
 * @fn StationLumineuse::demarrer
 */
void StationLumineuse::demarrer()
{
    // pour la sauvegarde
    preferences.begin("eeprom", false); // false pour r/w

#ifdef DEBUG_STATION_LUMINEUSE
    Serial.println(F("StationLumineuse::demarrer()"));
    Serial.println(F("  Modules"));
    Serial.print(F("    Nb machines  : "));
    Serial.println(machines.size());
    Serial.print(F("    Nb poubelles : "));
    Serial.println(poubelles.size());
    Serial.print(F("    Nb boites    : "));
    Serial.println(boites.size());
#endif

    leds.begin();

#ifdef DEBUG_STATION_LUMINEUSE
    Serial.println(F("  Bandeau initialisé"));
    Serial.print(F("    Broche        : "));
    Serial.println(leds.getPin());
    Serial.print(F("    Nb leds       : "));
    Serial.println(leds.numPixels());
    Serial.print(F("    Peut afficher : "));
    Serial.println((leds.canShow() ? "oui" : "non"));
#endif

    restaurerEtats();

    serveurWeb->demarrer();
}

/**
 * @brief Demande le traitement des requêtes web au serveur
 * @fn StationLumineuse::traiterRequetes
 */
void StationLumineuse::traiterRequetes()
{
    serveurWeb->traiterRequetes();
}

/**
 * @brief Assure un test des leds du bandeau
 * @fn StationLumineuse::testerBandeau
 */
void StationLumineuse::testerBandeau()
{
    // Allume les leds de notification de tous les modules
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

    // Attente
    delay(TEST_TEMPORISATION);

    // Eteint les leds de notification de tous les modules
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
}

std::size_t StationLumineuse::getNbPoubelles() const
{
    return poubelles.size();
}

Poubelle* StationLumineuse::getPoubelle(int id)
{
    // id valide ?
    if(id < 1 || id > poubelles.size() || poubelles[id - 1] == nullptr)
    {
        return nullptr;
    }
    return poubelles[id - 1];
}

void StationLumineuse::sauvegarderEtatsPoubelle(int id)
{
    // id valide ?
    if(id < 1 || id > poubelles.size() || poubelles[id - 1] == nullptr)
    {
        return;
    }
    char cle[64] = "";

    // sauvegarde les deux états pour cet id
    sprintf((char*)cle, "%s%d", "notif_p", id);
    preferences.putBool(cle, poubelles[id - 1]->getEtatNotification());
    sprintf((char*)cle, "%s%d", "actif_p", id);
    preferences.putBool(cle, poubelles[id - 1]->getActivation());
}

// Méthodes statiques
uint32_t StationLumineuse::convertirCouleurRGB(uint8_t r, uint8_t g, uint8_t b)
{
    return ((uint32_t)r << 16) | ((uint32_t)g << 8) | b;
}

uint32_t StationLumineuse::getCouleurPoubelle(String nom)
{
    for(int i = 0; i < NB_LEDS_NOTIFICATION_POUBELLES; ++i)
    {
        if(nom == nomCouleursPoubelles[i])
        {
            return couleursPoubelles[i];
        }
    }
    return 0;
}

String StationLumineuse::getNomCouleurPoubelle(uint32_t couleur)
{
    for(int i = 0; i < NB_LEDS_NOTIFICATION_POUBELLES; ++i)
    {
        if(couleur == couleursPoubelles[i])
        {
            return nomCouleursPoubelles[i];
        }
    }
    return "";
}

// Méthodes privées
/**
 * @brief Restaure les états de la station
 * @fn StationLumineuse::restaurerEtats()
 */
void StationLumineuse::restaurerEtats()
{
    char cle[64] = "";

    // pour les modules Poubelle
    Poubelle* poubelle = nullptr;
    for(int i = 1; i <= getNbPoubelles(); ++i)
    {
        poubelle = getPoubelle(i);
        if(poubelle == nullptr)
        {
            continue;
        }
        // "p" pour poubelle
        sprintf((char*)cle, "%s%d", "notif_p", i);
        poubelle->setEtatNotification(preferences.getBool(cle, false));
        sprintf((char*)cle, "%s%d", "actif_p", i);
        poubelle->setActivation(preferences.getBool(cle, false));
    }

    // @todo idem pour les autres modules
}
