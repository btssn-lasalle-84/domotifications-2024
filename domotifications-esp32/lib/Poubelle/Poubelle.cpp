#include "Poubelle.h"
#include "BandeauLeds.h"

Poubelle::Poubelle(int                id,
                   String             couleur,
                   int                numeroLed,
                   uint32_t           couleurLed,
                   Adafruit_NeoPixel& leds) :
    id(id),
    couleur(couleur), numeroLed(numeroLed), couleurLed(couleurLed), activation(false),
    notification(false), leds(leds)
{
}

int Poubelle::getId() const
{
    return id;
}

String Poubelle::getCouleur() const
{
    return couleur;
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
    // @todo Seulement si le module est activé
    leds.setPixelColor(INDEX_LEDS_NOTIFICATION_POUBELLES + numeroLed,
                       couleurLed); // Appliquer la couleur correspondante
    leds.show();
}

void Poubelle::eteindreNotification()
{
    // @todo Seulement si le module est activé
    leds.setPixelColor(INDEX_LEDS_NOTIFICATION_POUBELLES + numeroLed, leds.Color(0, 0, 0));
    leds.show();
}
