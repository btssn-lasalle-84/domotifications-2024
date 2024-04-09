--- LDD (langage de définition de données)

--- Supprime les tables

DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS modules;
DROP TABLE IF EXISTS typesModules;
DROP TABLE IF EXISTS domotifications;

--- Table domotifications

CREATE TABLE IF NOT EXISTS domotifications (id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT UNIQUE NOT NULL, urlServeurWeb varchar(255) NOT NULL, urlServeurWebsocket varchar(255) NOT NULL, nbBoitesAuxLettres INTEGER, nbPoubelles INTEGER, nbMachines INTEGER);

--- Table typesModules (BoiteAuxlettres, Poubelle, Machine)

CREATE TABLE IF NOT EXISTS typesModules (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL CHECK(type IN ('BoiteAuxLettres','Poubelle','Machine')));

--- Table modules

CREATE TABLE IF NOT EXISTS modules (id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT UNIQUE NOT NULL, idTypesModules INTEGER, actif BOOLEAN NOT NULL CHECK (actif IN (0, 1)) DEFAULT 0, idDomotifications INTEGER, FOREIGN KEY (idTypesModules) REFERENCES typesModules(id), FOREIGN KEY (idDomotifications) REFERENCES domotifications(id) ON DELETE CASCADE);

--- Table notifications

CREATE TABLE IF NOT EXISTS notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, idDomotifications INTEGER, idModules INTEGER, horodatage DATETIME NOT NULL, acquittement BOOLEAN NOT NULL CHECK (acquittement IN (0, 1)) DEFAULT 0, FOREIGN KEY (idDomotifications) REFERENCES domotifications(id) ON DELETE CASCADE, FOREIGN KEY (idModules) REFERENCES modules(id) ON DELETE CASCADE);
