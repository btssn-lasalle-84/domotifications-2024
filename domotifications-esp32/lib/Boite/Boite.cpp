#include "Boite.h"
#include "BandeauLeds.h"

Boite::Boite(int id, int numeroLed, uint32_t couleur, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleur(couleur), activation(false), notification(false),
    leds(leds)
{
}
