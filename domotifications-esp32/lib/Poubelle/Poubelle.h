#ifndef POUBELLE_H
#define POUBELLE_H

#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

class Poubelle
{
  private:
    int                id; //!< l'identifiant du module (de 1 à NB_LEDS_NOTIFICATION_POUBELLES)
    int                numeroLed;
    uint32_t           couleurLed;
    bool               activation;
    bool               notification;
    Adafruit_NeoPixel& leds; //!< association au bandeau à leds multi-couleurs

  public:
    Poubelle(int id, int numeroLed, uint32_t couleurLed, Adafruit_NeoPixel& leds);

    int      getId() const;
    uint32_t getCouleurLed() const;
    bool     getActivation() const;
    void     setActivation(bool etat);
    bool     getEtatNotification() const;
    void     setEtatNotification(bool etat);
    void     resetEtatNotification();
    void     allumerNotification();
    void     eteindreNotification();
    String   getCouleur() const;
    void     setCouleurLed(String couleur);
};

#endif // POUBELLE_H