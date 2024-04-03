#ifndef BANDEAULEDS_H
#define BANDEAULEDS_H

/**
 * @def PIN_BANDEAU
 * @brief Le numéro de broche du GPIO
 */
#define PIN_BANDEAU 16

/**
 * @def NB_LEDS
 * @brief Le nombre de leds sur le bandeau
 */
#define NB_LEDS 15

/**
 * @def NB_LEDS_NOTIFICATION_MACHINES
 * @brief Le nombre de leds pour les notifications machine
 */
#define NB_LEDS_NOTIFICATION_MACHINES 6
/**
 * @def NB_LEDS_NOTIFICATION_POUBELLES
 * @brief Le nombre de leds pour les notifications poubelle
 */
#define NB_LEDS_NOTIFICATION_POUBELLES 5
/**
 * @def NB_LEDS_NOTIFICATION_BOITE
 * @brief Le nombre de leds pour les notifications boite
 */
#define NB_LEDS_NOTIFICATION_BOITE                                                                 \
    (NB_LEDS - NB_LEDS_NOTIFICATION_MACHINES - NB_LEDS_NOTIFICATION_POUBELLES)
/**
 * @def INDEX_LEDS_NOTIFICATION_MACHINES
 * @brief L'index de la première led pour les notifications machine
 */
#define INDEX_LEDS_NOTIFICATION_MACHINES 0
/**
 * @def INDEX_LEDS_NOTIFICATION_POUBELLES
 * @brief L'index de la première led pour les notifications poubelle
 */
#define INDEX_LEDS_NOTIFICATION_POUBELLES                                                          \
    (INDEX_LEDS_NOTIFICATION_MACHINES + NB_LEDS_NOTIFICATION_MACHINES)
/**
 * @def INDEX_LEDS_NOTIFICATION_BOITE
 * @brief L'index de la première led pour les notifications boite
 */
#define INDEX_LEDS_NOTIFICATION_BOITE                                                              \
    (INDEX_LEDS_NOTIFICATION_POUBELLES + NB_LEDS_NOTIFICATION_POUBELLES)

#endif // BANDEAULEDS_H