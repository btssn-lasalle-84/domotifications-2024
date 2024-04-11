#include "Machine.h"
#include "BandeauLeds.h"

Machine::Machine(int id, int numeroLed, uint32_t couleur, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleur(couleur), activation(false), notification(false),
    leds(leds)
{
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
        // @todo Sauvegarder le nouvel état dans Preferences
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

void Machine::resetEtatNotification()
{
    setEtatNotification(false);
}

void Machine::allumerNotification()
{
    leds.setPixelColor(INDEX_LEDS_NOTIFICATION_MACHINES + numeroLed,
                       couleur); // Appliquer la couleur correspondante
    leds.show();
}

void Machine::eteindreNotification()
{
    leds.setPixelColor(INDEX_LEDS_NOTIFICATION_MACHINES + numeroLed, leds.Color(0, 0, 0));
    leds.show();
}