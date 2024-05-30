#ifndef MACHINE_H
#define MACHINE_H

#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

class Machine
{
  private:
    int                id;
    int                numeroLed;
    uint32_t           couleurLed;
    bool               activation;
    bool               notification;
    Adafruit_NeoPixel& leds; //!< le bandeau à leds multi-couleurs

  public:
    Machine(int id, int numeroLed, uint32_t couleurLed, Adafruit_NeoPixel& leds);

    int      getId() const;
    uint32_t getCouleurLed() const;
    bool     getActivation() const;
    void     setCouleur();
    void     setActivation(bool etat);
    bool     getEtatNotification() const;
    void     setEtatNotification(bool etat);
    void     resetEtatNotification();
    void     allumerNotification();
    void     eteindreNotification();
    String   getCouleur() const;
    void     setCouleurLed(String couleur);
};

#endif // MACHINE_H