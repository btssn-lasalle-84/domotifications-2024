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
#include <sstream>
#include <iomanip>

// les couleurs par défaut des poubelles
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
        // couleur par défaut verte
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
        // couleur par défaut rouge
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

String StationLumineuse::getCouleurToString(uint32_t couleur)
{
    std::stringstream couleurStream;

    // #RRGGBB
    couleurStream << std::setfill('0') << std::setw(2) << std::hex << ((couleur >> 16) & 0xff);
    couleurStream << std::setfill('0') << std::setw(2) << std::hex << ((couleur >> 8) & 0xff);
    couleurStream << std::setfill('0') << std::setw(2) << std::hex << (couleur & 0xff);

    return String("#") + String(couleurStream.str().c_str());
}

uint32_t StationLumineuse::getCouleurToRGB(String couleur)
{
    if(couleur[0] == '#' && couleur.length() == 7)
    {
        uint32_t c;
        couleur.remove(0, 1);
        std::stringstream(couleur.c_str()) >> std::hex >> c;
        return c;
    }
    return 0;
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
    sauvegarderCouleurPoubelle(id);
}

std::size_t StationLumineuse::getNbBoites() const
{
    return boites.size();
}

Boite* StationLumineuse::getBoite(int id)
{
    if(id < 1 || id > boites.size() || boites[id - 1] == nullptr)
    {
        return nullptr;
    }
    return boites[id - 1];
}

void StationLumineuse::sauvegarderEtatsBoite(int id)
{
    // id valide ?
    if(id < 1 || id > boites.size() || boites[id - 1] == nullptr)
    {
        return;
    }
    char cle[64] = "";

    // sauvegarde les deux états pour cet id
    sprintf((char*)cle, "%s%d", "notif_b", id);
    preferences.putBool(cle, boites[id - 1]->getEtatNotification());
    sprintf((char*)cle, "%s%d", "actif_b", id);
    preferences.putBool(cle, boites[id - 1]->getActivation());
    sauvegarderCouleurBoite(id);
}

std::size_t StationLumineuse::getNbMachines() const
{
    return machines.size();
}

Machine* StationLumineuse::getMachine(int id)
{
    if(id < 1 || id > machines.size() || machines[id - 1] == nullptr)
    {
        return nullptr;
    }
    return machines[id - 1];
}

void StationLumineuse::sauvegarderEtatsMachine(int id)
{
    // id valide ?
    if(id < 1 || id > machines.size() || machines[id - 1] == nullptr)
    {
        return;
    }
    char cle[64] = "";

    // sauvegarde les deux états pour cet id
    sprintf((char*)cle, "%s%d", "notif_m", id);
    preferences.putBool(cle, machines[id - 1]->getEtatNotification());
    sprintf((char*)cle, "%s%d", "actif_m", id);
    preferences.putBool(cle, machines[id - 1]->getActivation());
    sauvegarderCouleurMachine(id);
}

// Méthodes statiques
uint32_t StationLumineuse::convertirCouleurRGB(uint8_t r, uint8_t g, uint8_t b)
{
    return ((uint32_t)r << 16) | ((uint32_t)g << 8) | b;
}

void StationLumineuse::sauvegarderCouleurPoubelle(int id)
{
    if(id < 1 || id > poubelles.size() || poubelles[id - 1] == nullptr)
    {
        return;
    }
    char cle[64] = "";
    sprintf(cle, "couleur_p%d", id);
    preferences.putString(cle, poubelles[id - 1]->getCouleur());
}

void StationLumineuse::restaurerCouleurPoubelle(int id)
{
    if(id < 1 || id > poubelles.size() || poubelles[id - 1] == nullptr)
    {
        return;
    }

    char cle[64] = "";
    sprintf((char*)cle, "%s%d", "couleur_p", id);
    Poubelle* poubelle = getPoubelle(id);
    poubelle->setCouleurLed(
      preferences.getString(cle, StationLumineuse::getCouleurToString(couleursPoubelles[id])));
}

void StationLumineuse::sauvegarderCouleurBoite(int id)
{
    if(id < 1 || id > boites.size() || boites[id - 1] == nullptr)
    {
        return;
    }
    char cle[64] = "";
    sprintf(cle, "couleur_b%d", id);
    preferences.putString(cle, boites[id - 1]->getCouleur());
}

void StationLumineuse::restaurerCouleurBoite(int id)
{
    if(id < 1 || id > boites.size() || boites[id - 1] == nullptr)
    {
        return;
    }
    char cle[64] = "";
    sprintf(cle, "couleur_b%d", id);
    Boite* boite = getBoite(id);
    boite->setCouleurLed(
      preferences.getString(cle, StationLumineuse::getCouleurToString(leds.Color(255, 0, 0))));
}

void StationLumineuse::sauvegarderCouleurMachine(int id)
{
    if(id < 1 || id > machines.size() || machines[id - 1] == nullptr)
    {
        return;
    }
    char cle[64] = "";
    sprintf(cle, "couleur_m%d", id);
    preferences.putString(cle, machines[id - 1]->getCouleur());
}

void StationLumineuse::restaurerCouleurMachine(int id)
{
    if(id < 1 || id > machines.size() || machines[id - 1] == nullptr)
    {
        return;
    }
    char cle[64] = "";
    sprintf(cle, "couleur_m%d", id);
    Machine* machine = getMachine(id);
    machine->setCouleurLed(
      preferences.getString(cle, StationLumineuse::getCouleurToString(leds.Color(0, 255, 0))));
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
        sprintf((char*)cle, "%s%d", "notif_p", i);
        poubelle->setEtatNotification(preferences.getBool(cle, false));
        sprintf((char*)cle, "%s%d", "actif_p", i);
        poubelle->setActivation(preferences.getBool(cle, false));
        restaurerCouleurPoubelle(i);
        // @todo restaurer l'état de la led :
        // si la poubelle est active
        // alors si la notification est activee
        // alors allumer la led
        // sinon eteindre la led
        if(poubelle->getActivation())
        {
            if(poubelle->getEtatNotification())
            {
                poubelle->allumerNotification();
            }
            else
            {
                poubelle->eteindreNotification();
            }
        }
    }

    // Restaurer les états des modules Boite
    Boite* boite = nullptr;
    for(int i = 1; i <= getNbBoites(); i++)
    {
        boite = getBoite(i);
        if(boite == nullptr)
        {
            continue;
        }
        sprintf((char*)cle, "%s%d", "notif_b", i);
        boite->setEtatNotification(preferences.getBool(cle, false));
        sprintf((char*)cle, "%s%d", "actif_b", i);
        boite->setActivation(preferences.getBool(cle, false));
        restaurerCouleurBoite(i);
        // @todo restaurer l'état de la led :
        // si la boite est active
        // alors si la notification est activee
        // alors allumer la led
        // sinon eteindre la led
        if(boite->getActivation())
        {
            if(boite->getEtatNotification())
            {
                boite->allumerNotification();
            }
            else
            {
                boite->eteindreNotification();
            }
        }
    }

    // Restaurer les états des modules Machine
    Machine* machine = nullptr;
    for(int i = 1; i <= getNbMachines(); i++)
    {
        machine = getMachine(i);
        if(machine == nullptr)
        {
            continue;
        }
        sprintf((char*)cle, "%s%d", "notif_m", i);
        machine->setEtatNotification(preferences.getBool(cle, false));
        sprintf((char*)cle, "%s%d", "actif_m", i);
        machine->setActivation(preferences.getBool(cle, false));
        restaurerCouleurMachine(i);
        // @todo restaurer l'état de la led :
        // si la machine est active
        // alors si la notification est activee
        // alors allumer la led
        // sinon eteindre la led
        if(machine->getActivation())
        {
            if(machine->getEtatNotification())
            {
                machine->allumerNotification();
            }
            else
            {
                machine->eteindreNotification();
            }
        }
    }
}