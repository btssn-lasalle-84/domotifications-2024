#ifndef POUBELLE_H
#define POUBELLE_H

#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

class Poubelle
{
  private:
    int                id; //!< l'identifiant du module (de 1 à NB_LEDS_NOTIFICATION_POUBELLES)
    String             couleur;
    int                numeroLed;
    uint32_t           couleurLed;
    bool               activation;
    bool               notification;
    Adafruit_NeoPixel& leds; //!< association au bandeau à leds multi-couleurs

  public:
    Poubelle(int id, String couleur, int numeroLed, uint32_t couleurLed, Adafruit_NeoPixel& leds);

    int    getId() const;
    String getCouleur() const;
    bool   getActivation() const;
    void   setActivation(bool etat);
    bool   getEtatNotification() const;
    void   setEtatNotification(bool etat);
    void   resetEtatNotification();
    void   allumerNotification();
    void   eteindreNotification();
};

#endif // POUBELLE_H