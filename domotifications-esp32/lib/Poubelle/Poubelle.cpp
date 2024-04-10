#include "Poubelle.h"
#include "BandeauLeds.h"

Poubelle::Poubelle(int id, int numeroLed, uint32_t couleur, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleur(couleur), activation(false), notification(false),
    leds(leds)
{
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
        // @todo Sauvegarder le nouvel état dans Preferences
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
        // @todo Sauvegarder le nouvel état dans Preferences
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
    leds.setPixelColor(INDEX_LEDS_NOTIFICATION_POUBELLES + numeroLed,
                       couleur); // Appliquer la couleur correspondante
    leds.show();
}

void Poubelle::eteindreNotification()
{
    leds.setPixelColor(INDEX_LEDS_NOTIFICATION_POUBELLES + numeroLed, leds.Color(0, 0, 0));
    leds.show();
}