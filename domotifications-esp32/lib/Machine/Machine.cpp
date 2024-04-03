#include "Machine.h"
#include "BandeauLeds.h"

Machine::Machine(int id, int numeroLed, uint32_t couleur, Adafruit_NeoPixel& leds) :
    id(id), numeroLed(numeroLed), couleur(couleur), activation(false), notification(false),
    leds(leds)
{
}
