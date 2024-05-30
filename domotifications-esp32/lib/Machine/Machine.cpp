#include "Machine.h"
#include "BandeauLeds.h"
#include "StationLumineuse.h"

Machine::Machine(int id, int numeroLed, uint32_t couleurLed, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleurLed(couleurLed), activation(false), notification(false),
    leds(leds)
{
}

int Machine::getId() const
{
    return id;
}

uint32_t Machine::getCouleurLed() const
{
    return couleurLed;
}

bool Machine::getActivation() const
{
    return activation;
}

void Machine::setActivation(bool etat)
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

/*void setCouleur(const String& couleur)
{
    this->couleur = couleur;
}*/

void Machine::resetEtatNotification()
{
    setEtatNotification(false);
}

void Machine::allumerNotification()
{
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_MACHINES + numeroLed,
                           couleurLed); // Appliquer la couleur correspondante
        leds.show();
    }
}

void Machine::eteindreNotification()
{
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_MACHINES + numeroLed, leds.Color(0, 0, 0));
        leds.show();
    }
}

String Machine::getCouleur() const
{
    return StationLumineuse::getCouleurToString(couleurLed);
}

void Machine::setCouleurLed(String couleur)
{
    uint32_t couleurLed = StationLumineuse::getCouleurToRGB(couleur);
    if(couleurLed != this->couleurLed && couleurLed != 0)
    {
        this->couleurLed = couleurLed;
    }
}
