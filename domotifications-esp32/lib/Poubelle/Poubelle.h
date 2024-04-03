#ifndef POUBELLE_H
#define POUBELLE_H

#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

class Poubelle
{
  private:
    int                id;
    int                numeroLed;
    uint32_t           couleur;
    bool               activation;
    bool               notification;
    Adafruit_NeoPixel& leds; //!< le bandeau à leds multi-couleurs

  public:
    Poubelle(int id, int numeroLed, uint32_t couleur, Adafruit_NeoPixel& leds);

    bool getActivation() const;
    void setActivation(bool etat);
    bool getEtatNotification() const;
    void setEtatNotification(bool etat);
    void resetEtatNotification();
    void allumerNotification();
    void eteindreNotification();
};

#endif // POUBELLE_H