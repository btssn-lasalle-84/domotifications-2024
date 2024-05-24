# Station de notifications lumineuses

- [Station de notifications lumineuses](#station-de-notifications-lumineuses)
  - [Présentation](#présentation)
  - [Configuration du projet](#configuration-du-projet)
  - [Configuration WiFi](#configuration-wifi)

## Présentation

La station de notifications lumineuses permet de notifier visuellement des évènements domotiques (quand et quelle poubelle sortir, quand est-ce qu’un colis a été livré dans la boîte aux lettres et quand est-ce qu’une des machines à laver ou sécher le linge a terminé, ...).

La station de notifications lumineuses doit permettre d’avoir d’un simple coup d’œil les informations concernant les poubelles, les livraisons et les machines de linge. Cela a pour but d’apporter confort et sérénité.

Elle est composée d’un bandeau circulaire à leds (WS2812B GRB) et d'un micro-contôleur D1 mini ESP32.

Le bandeau circulaire à leds est découpé en trois groupes distincts pour les notifications :

- en vert : la notification d’une machine terminée (le système peut gérer jusqu’à 6 machines individuellement),
- en rouge/bleu/vert/gris/jaune : la notification d'une poubelle de tri à sortir (5 types de poubelle),
- en rouge : la présence d’un colis dans la boîte aux lettres.

## Configuration du projet

Le fichier `platformio.ini` de configuration du projet :

```ini
[env:wemos_d1_mini32]
platform = espressif32
board = wemos_d1_mini32
framework = arduino
lib_deps =
    adafruit/Adafruit NeoPixel @ ^1.10.5
    bblanchon/ArduinoJson @ ^6.19.4
    https://github.com/tzapu/WiFiManager.git
upload_port = /dev/ttyUSB0
upload_speed = 115200
monitor_port = /dev/ttyUSB0
monitor_speed = 115200
```

## Configuration WiFi

1. Allumer la station de notifications lumineuses

2. Avec un terminal WiFi, il faut se connecter au point d'accès de la station de notifications lumineuses (`ESP32_XXYYZZ`)

3. À partir de la page wb de configuration, il faut renseigner le SSID et le mot de passe Wifi puis sauvegarder

4. Redémarrer la station de notifications lumineuses et vérifier son adresse IP à partir du terminal, par exemple :

```
...
*wm:[1] connectTimeout not set, ESP waitForConnectResult...
*wm:[2] Connection result: WL_CONNECTED
*wm:[1] AutoConnect: SUCCESS
*wm:[2] Connected in 929 ms
*wm:[1] STA IP Address: 192.168.52.34
Serveur HTTP Rest ok
Adresse IP : 192.168.52.34
...
```

---
Auteur : Corentin MOUTTE 2024
