#include "Poubelle.h"
#include "BandeauLeds.h"
#include "StationLumineuse.h"

Poubelle::Poubelle(int id, int numeroLed, uint32_t couleurLed, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleurLed(couleurLed), activation(false), notification(false),
    leds(leds)
{
}

int Poubelle::getId() const
{
    return id;
}

uint32_t Poubelle::getCouleurLed() const
{
    return couleurLed;
}

bool Poubelle::getActivation() const
{
    return activation;
}

void Poubelle::setActivation(bool etat)
{
    if(etat != activation)
    {
        activation = etat;
    }
}

bool Poubelle::getEtatNotification() const
{
    return notification;
}

void Poubelle::setEtatNotification(bool etat)
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

void Poubelle::resetEtatNotification()
{
    setEtatNotification(false);
}

void Poubelle::allumerNotification()
{
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_POUBELLES + numeroLed,
                           couleurLed); // Appliquer la couleur correspondante
        leds.show();
    }
}

void Poubelle::eteindreNotification()
{
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_POUBELLES + numeroLed, leds.Color(255, 0, 0));
        leds.show();
    }
}

String Poubelle::getCouleur() const
{
    return StationLumineuse::getCouleurToString(couleurLed);
}

void Poubelle::setCouleurLed(String couleur)
{
    uint32_t couleurLed = StationLumineuse::getCouleurToRGB(couleur);
    if(couleurLed != this->couleurLed && couleurLed != 0)
    {
        this->couleurLed = couleurLed;
    }
}