#include "Boite.h"
#include "BandeauLeds.h"

Boite::Boite(int id, int numeroLed, uint32_t couleur, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleur(couleur), activation(false), notification(false),
    leds(leds)
{
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
        // @todo Sauvegarder le nouvel état dans Preferences
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

void Boite::resetEtatNotification()
{
    setEtatNotification(false);
}

void Boite::allumerNotification()
{
    leds.setPixelColor(INDEX_LEDS_NOTIFICATION_BOITE + numeroLed,
                       couleur); // Appliquer la couleur correspondante
    leds.show();
}

void Boite::eteindreNotification()
{
    leds.setPixelColor(INDEX_LEDS_NOTIFICATION_BOITE + numeroLed, leds.Color(0, 0, 0));
    leds.show();
}