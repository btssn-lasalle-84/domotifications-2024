#include "Machine.h"
#include "BandeauLeds.h"

Machine::Machine(int id, int numeroLed, uint32_t couleurLed, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleurLed(couleurLed), activation(false), notification(false),
    leds(leds)
{
}

int Machine::getId() const
{
    return id;
}

String Machine::getCouleur() const
{
    return String(couleurLed);
}

bool Machine::getActivation() const
{
    return activation;
}#include <sstream>
{
    if(etat != activation)
    {
        activation = etat;
    }
}

bool Machine::getEtatNotification() const
{
    return notification;
}

void Machine::setEtatNotification(bool etat)
{
    if(etat != notification)
    {
        notification = etat;
        if(notification)
        {
            allumerNotification();
        }
        else
        {
            eteindreNotification();
        }
    }
}

void Machine::resetEtatNotification()
{
    setEtatNotification(false);
}

void Machine::allumerNotification()
{
    // @todo Seulement si le module est activé
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_MACHINES + numeroLed,
                           couleurLed); // Appliquer la couleur correspondante
        leds.show();
    }
}

void Machine::eteindreNotification()
{
    // @todo Seulement si le module est activé
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_MACHINES + numeroLed, leds.Color(0, 0, 0));
        leds.show();
    }
}

String Boite::getCouleur() const
{
    return StationLumineuse::getCouleurToString(couleurLed);
}

void Boite::setCouleurLed(String couleur)
{
    uint32_t couleurLed = StationLumineuse::getCouleurToRGB(couleur);
    if(couleurLed != this->couleurLed && couleurLed != 0)
    {
        this->couleurLed = couleurLed;
    }
}