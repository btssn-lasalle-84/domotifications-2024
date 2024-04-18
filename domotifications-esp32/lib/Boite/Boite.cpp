#include "Boite.h"
#include "BandeauLeds.h"

Boite::Boite(int id, int numeroLed, uint32_t couleurLed, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleurLed(couleurLed), activation(false), notification(false),
    leds(leds)
{
}

int Boite::getId() const
{
    return id;
}

String Boite::getCouleur() const
{
    return String(couleurLed);
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
    // @todo Seulement si le module est activé
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_BOITE + numeroLed,
                           couleurLed); // Appliquer la couleur correspondante
        leds.show();
    }
}

void Boite::eteindreNotification()
{
    // @todo Seulement si le module est activé
    if(activation)
    {
        leds.setPixelColor(INDEX_LEDS_NOTIFICATION_BOITE + numeroLed, leds.Color(0, 0, 0));
        leds.show();
    }
}