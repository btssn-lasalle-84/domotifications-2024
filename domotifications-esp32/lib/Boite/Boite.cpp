#include "Boite.h"
#include "BandeauLeds.h"
#include "StationLumineuse.h"

Boite::Boite(int id, int numeroLed, uint32_t couleurLed, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleurLed(couleurLed), activation(false), notification(false),
    leds(leds)
{
}

int Boite::getId() const
{
    return id;
}

uint32_t Boite::getCouleurLed() const
{
    return couleurLed;
}

bool Boite::getActivation() const
{
    return activation;
}

void Boite::setActivation(bool etat)
{
    if(etat != activation)
    {
        activation = etat;
    }
}

bool Boite::getEtatNotification() const
{
    return notification;
}

void Boite::setEtatNotification(bool etat)
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

void Boite::resetEtatNotification()
{
    setEtatNotification(false);
}

void Boite::allumerNotification()
{
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_BOITE + numeroLed,
                           couleurLed); // Appliquer la couleur correspondante
        leds.show();
    }
}

void Boite::eteindreNotification()
{
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_BOITE + numeroLed, leds.Color(0, 0, 0));
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
