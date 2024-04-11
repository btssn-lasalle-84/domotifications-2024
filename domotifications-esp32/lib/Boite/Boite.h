#ifndef BOITE_H
#define BOITE_H

#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

class Boite
{
  private:
    int                id;
    int                numeroLed;
    uint32_t           couleur;
    bool               activation;
    bool               notification;
    Adafruit_NeoPixel& leds; //!< le bandeau à leds multi-couleurs

  public:
    Boite(int id, int numeroLed, uint32_t couleur, Adafruit_NeoPixel& leds);

    bool getActivation() const;
    void setActivation(bool etat);
    bool getEtatNotification() const;
    void setEtatNotification(bool etat);
    void resetEtatNotification();
    void allumerNotification();
    void eteindreNotification();
};

#endif // BOITE_H