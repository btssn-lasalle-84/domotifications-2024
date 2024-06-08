![C++ Badge](https://img.shields.io/badge/C%2B%2B-00599C?logo=cplusplus&logoColor=fff&style=plastic) ![Espressif Badge](https://img.shields.io/badge/Espressif-E7352C?logo=espressif&logoColor=fff&style=plastic) ![Android Badge](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=fff&style=plastic)

[![android-build](https://github.com/btssn-lasalle-84/domotifications-2024/actions/workflows/android-build.yml/badge.svg)](https://github.com/btssn-lasalle-84/domotifications-2024/actions/workflows/android-build.yml) [![platformio-build](https://github.com/btssn-lasalle-84/domotifications-2024/actions/workflows/build-platformio.yml/badge.svg)](https://github.com/btssn-lasalle-84/domotifications-2024/actions/workflows/build-platformio.yml) [![pages-build-deployment](https://github.com/btssn-lasalle-84/domotifications-2024/actions/workflows/pages/pages-build-deployment/badge.svg?branch=develop)](https://github.com/btssn-lasalle-84/domotifications-2024/actions/workflows/pages/pages-build-deployment)

# Le projet domotifications 2024

- [Le projet domotifications 2024](#le-projet-domotifications-2024)
  - [Présentation](#présentation)
  - [Fonctionnalités](#fonctionnalités)
  - [Screenshots](#screenshots)
  - [Diagramme de classes](#diagramme-de-classes)
  - [Protocole](#protocole)
  - [Historique des versions](#historique-des-versions)
  - [Documentation du code](#documentation-du-code)
  - [Auteurs](#auteurs)

---

## Présentation

Le système **domotifications** doit permettre de notifier visuellement des évènements domotiques (quand et quelle poubelle sortir, quand est-ce qu’un colis a été livré dans la boîte aux lettres et quand est-ce qu’une des machines à laver ou sécher le linge a terminé, ...).

La station de notifications lumineuses est composée d’un bandeau circulaire à leds piloté par un ESP32. Celui-ci est découpé en trois groupes distincts pour les notifications :

- en vert : la notification d’une machine terminée (le système peut gérer jusqu’à _n_ machines individuellement),
- en rouge : la présence d’un colis dans la boîte aux lettres,
- en rouge/bleu/vert/gris/jaune : la notification d'une poubelle de tri à sortir.

![alt text](images/modules.png)

## Fonctionnalités

- Application Android

![diagramme-cas-utilisation](images/diagramme-cas-utilisation-android.png)

| Fonctionnalité                      | OUI | NON |
| ----------------------------------- | :-: | :-: |
| Visualiser une notification         |  X  |     |
| Acquitter une notification          |  X  |     |
| Activer/désactiver un module        |  X  |     |
| Dialoguer avec la station lumineuse |  X  |     |
| Configurer le système               |  X  |     |

- Station lumineuse

![diagramme-cas-utilisation](images/diagramme-cas-utilisation-esp32.png)

| Fonctionnalité               | OUI | NON |
| ---------------------------- | :-: | :-: |
| Visualiser une notification  |  X  |     |
| Acquitter une notification   |  X  |     |
| Activer/désactiver un module |  X  |     |
| Traiter les requêtes HTTP    |  X  |     |
| Configurer la station        |     |  X  |

## Screenshots

![](images/domotifications-android-v1.0.gif)

## Diagramme de classes

- Application Android

![diagramme-classes-android-ihm](images/domotifications-android-classes-ihm-v1.0.png)

![diagramme-classes-android-machine](images/domotifications-android-classes-machine-v1.0.png)

- Station lumineuse (ESP32)

![diagramme-classes-esp32](images/domotifications-esp32-classes-1.0.png)

## Protocole

cf. [Spécification API HTTP REST](./specifications-openapi/README.md)

![](images/api-machines.png)
![](images/api-poubelles.png)
![](images/api-boites.png)

## Historique des versions

- 1.0

![](images/jira_v1.0.png)

- 0.2

![](images/jira_v0.2.png)

- 0.1

![](images/jira_v0.1.png)

## Documentation du code

https://btssn-lasalle-84.github.io/domotifications-2024/

## Auteurs

- Étudiant IR (Android) : LATYAOUI Othman <<othmanlatyaoui.pro@gmail.com>>
- Étudiant IR (ESP32) : MOUTTE Corentin <<corentinmoutte@gmail.com>>

---
©️ LaSalle Avignon 2024
