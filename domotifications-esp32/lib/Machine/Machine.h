#ifndef MACHINE_H
#define MACHINE_H

#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

class Machine
{
  private:
    int                id;
    int                numeroLed;
    uint32_t           couleur;
    bool               activation;
    bool               notification;
    Adafruit_NeoPixel& leds; //!< le bandeau à leds multi-couleurs

  public:
    Machine(int id, int numeroLed, uint32_t couleur, Adafruit_NeoPixel& leds);
};

#endif // MACHINE_H