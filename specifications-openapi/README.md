# Domotifications 2024

- [Domotifications 2024](#domotifications-2024)
  - [Spécification API HTTP REST](#spécification-api-http-rest)
  - [Tests](#tests)
  - [Annexes](#annexes)
    - [API HTTP REST](#api-http-rest)
    - [OpenAPI](#openapi)

## Spécification API HTTP REST

La spécification utilisée ici est destinée à un serveur [ESP32](https://fr.wikipedia.org/wiki/ESP32). Elle permet gérer des modules (Boîtes aux lettres, poubelles, machines éléctriques, ...) qui communiquent en WiFi.

En résumé :

- pour la collection `poubelles` :

| Requête HTTP                       | Description                             |
|:----------------------------------:|:---------------------------------------:|
| **GET** /poubelles                 | Lister les poubelles                    |
| **POST** /poubelles                | Ajouter une poubelle                    |
| **DELETE** /poubelles/{idPoubelle} | Supprimer une poubelle                  |
| **GET** /poubelles/{idPoubelle}    | Obtenir les détails d&#x27;une poubelle |
| **PATCH** /poubelles/{idPoubelle}  | Mettre à jour une poubelle              |

- pour la collection `machines` :

| Requête HTTP                       | Description                             |
|:----------------------------------:|:---------------------------------------:|
| **GET** /machines                  | Lister les machines                     |
| **POST** /machines                 | Ajouter une machine                     |
| **DELETE** /machines/{idMachine}   | Supprimer une machine                   |
| **GET** /machines/{idMachine}      | Obtenir les détails d&#x27;une machine  |
| **PATCH** /machines/{idMachine}    | Mettre à jour une machine               |

- pour la collection `boites` :

| Requête HTTP                       | Description                             |
|:----------------------------------:|:---------------------------------------:|
| **GET** /boites                    | Lister les boites                       |
| **POST** /boites                   | Ajouter une poubelle                    |
| **DELETE** /boites/{idBoite}       | Supprimer une boite                     |
| **GET** /boites/{idBoite}          | Obtenir les détails d&#x27;une boite    |
| **PATCH** /boites/{idBoite}        | Mettre à jour une boite                 |

> cf. [API HTTP REST](#api-http-rest)

La spécification complète : [domotifications-v0.1.yaml](domotifications-v0.1.yaml)

> cf. [OpenAPI](#openapi)

Les propriétés `openapi` et `info` :

```yaml
openapi: 3.0.3
info:
  title: Domotifications 2024
  version: "0.1"
  description: Système de notifications visuelles des évènements domotiques
  contact:
    name: Thierry VAIRA
    email: tvaira@free.fr
    url: http://tvaira.free.fr
  license:
    name: Apache 2.0
    url: https://www.apache.org/licenses/LICENSE-2.0.html
```

La propriété `servers` :

```yaml
...
servers:
  - url: http://{adresseIPESP32}
    description: La station de notifications lumineuses
    variables:
      adresseIPESP32:
        default: 192.168.0.1
        description: |
          Aller sur http://station-lumineuse.local:80/
```

La propriété `paths` :

```yaml
...
tags:
  - name: poubelles
    description: Les modules Poubelle
  - name: machines
    description: Les modules Machine
  - name: boites
    description: Les modules Boite aux lettres
```

La propriété `components` :

- Exemple pour une requête `GET` sur `/poubelles` :

```yaml
...
paths:
  /poubelles:
    get:
      summary: Lister les modules Poubelle
      description: Lister toutes modules Poubelle disponibles
      operationId: getPoubelles
      tags:
        - poubelles
      responses:
        "200":
          description: Succès de l'opération
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/getPoubelles"
...
```

- Exemple de réponse à cette requête retourne un objet `getPoubelles` référencé dans la propriété `components` :

```yaml
...
components:
  schemas:
    getPoubelles:
      type: array
      items:
        $ref: "#/components/schemas/Poubelle"
      example:
        [
          { "idPoubelle": 1, "etat": false, "actif": true, "couleur": "bleue" },
          { "idPoubelle": 2, "etat": true, "actif": true, "couleur": "verte" },
        ]
...
    Poubelle:
      type: object
      description: Un module Poubelle
      required:
        - idPoubelle
        - etat
      properties:
        idPoubelle:
          type: integer
          format: int32
        etat:
          type: boolean
          description: |
            `true` si une notification sur le module Poubelle sinon `false`
        actif:
          type: boolean
          description: |
            `true` si le module est activé sinon `false`
        couleur:
          type: string
          enum:
            - bleue
            - verte
            - jaune
            - grise
            - rouge
...
```

On obtiendra alors une réponse en JSON de ce type :

```json
[
    { "idPoubelle": 1, "etat": false, "actif": true, "couleur": "bleue" },
    { "idPoubelle": 2, "etat": true, "actif": true, "couleur": "verte" },
]
```

> La spécification complète : [domotifications-v0.1.yaml](domotifications-v0.1.yaml)
> Avec l'[éditeur en ligne](http://editor.swagger.io/) : http://editor.swagger.io/

## Tests

Exemples avec `curl` :

- Lister les poubelles (`GET`) :

```bash
$ curl --location http://station-lumineuse.local:80/poubelles
[{"idPoubelle":1,"etat":false,"couleur":"bleu","actif":true},{"idPoubelle":2,"etat":true,"couleur":"verte","actif":true}]
```

- Modifier une poubelle (`PATCH`) :

```bash
$ curl --location 'http://station-lumineuse.local:80/poubelles/1' \
--header 'Content-Type: application/json' \
--data '{
  "idPoubelle": "1",
  "etat": true
}'
[{"idPoubelle":1,"etat":true,"couleur":"bleue","actif":true}]
```

- Obtenir les informations sur une poubelle (`GET`) :

```bash
$ curl --location http://station-lumineuse.local:80/poubelles/1
[{"idPoubelle":1,"etat":true,"couleur":"bleue","actif":true}]
```

## Annexes

### API HTTP REST

L'[API Web](https://fr.wikipedia.org/wiki/API_Web) est une interface de programmation d'application (API) entre la station de notifications lumineuses (serveur Web ESP32) et les clients Web (modules, application mobile, ...).

Elle se compose d'un ou plusieurs points d'accès exposés publiquement répondant avec des données, généralement exprimé en [XML](https://fr.wikipedia.org/wiki/Extensible_Markup_Language) ou [JSON](https://fr.wikipedia.org/wiki/JavaScript_Object_Notation).

Les points d'accès spécifient où se trouvent les ressources accessibles par les clients. Généralement l'accès se fait via une URI sur laquelle sont postées les requêtes HTTP, et dont la réponse est donc attendue. Les API Web peuvent être publiques ou privées, dans ce cas elles nécessitent un [jeton d'accès](https://fr.wikipedia.org/wiki/Jeton_d%27acc%C3%A8s) (_token_).

Les API Web Web 2.0 utilisent [REST](#rest) et [SOAP](https://fr.wikipedia.org/wiki/SOAP). Les API Web _RESTful_ utilisent des méthodes HTTP pour accéder aux ressources via des paramètres encodés en URL et utilisent JSON ou XML pour transmettre des données. En revanche, les protocoles SOAP sont normalisés par le W3C et imposent l'utilisation de XML.

Le tableau suivant indique comment les méthodes HTTP sont généralement utilisées dans une API REST :

|URI|GET|POST|PUT|PATCH|DELETE|
|---|---|---|---|---|---|
|`http://api.exemple.com/collection/`|Récupère les URI des ressources membres de la ressource `collection` dans le corps de la réponse.|Crée une ressource membre dans la ressource `collection` en utilisant les instructions du corps de la requête.|Remplace toutes les représentations des ressources membres de la ressource `collection` par la représentation dans le corps de la requête ou crée la ressource `collection` si elle n'existe pas.|Met à jour toutes les représentations des ressources membres de la ressource `collection` en utilisant les instructions du corps de la requête|Supprime toutes les représentations des ressources membres de la ressource `collection`.|
|`http://api.exemple.com/collection/item3`|Récupère une représentation de la ressource membre dans le corps de la réponse.|Crée une ressource membre dans la ressource membre en utilisant les instructions du corps de la requête.|Remplace toutes les représentations de la ressource membre ou crée la ressource membre si elle n'existe pas par la représentation dans le corps de la requête.|Met à jour toutes les représentations de la ressource membre ou crée éventuellement la ressource membre|Supprime toutes les représentations de la ressource membre.|

> [!CAUTION]
> Il n'y a pas de norme officielle pour les API REST, parce que REST est une architecture et non un protocole.

### OpenAPI

[OpenAPI](https://swagger.io/specification/) est une norme de description des API HTTP conformes à l’architecture REST.

> La spécification OpenAPI v3 actuelle découle d’un projet antérieur nommé [Swagger](https://swagger.io/) jusqu'à la v2.

Spécifications : https://swagger.io/specification/

Documentation : https://swagger.io/docs/specification/about/

La structure de base du fichier possède notamment les propriétés suivantes :

- `openapi` : indique la version des spécifications utilisées
- `info` : décrit des informations (métadonnées) sur l'API
- `servers` : définit les paramètres, comme l'[URL](https://fr.wikipedia.org/wiki/Uniform_Resource_Locator) de base, du (ou des) serveur(s)
- `paths` : définit les [URL](https://fr.wikipedia.org/wiki/Uniform_Resource_Locator)s et les opérations de l'API (`get`, `post`, ...)
- `components` : contient un ensemble d’objets réutilisables et explicitement référencés à partir des propriétés définies dans `paths`

---
©️ 2024 BTS LaSalle Avignon - Thierry VAIRA
