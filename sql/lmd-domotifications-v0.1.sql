--- LMD (langage de manipulation de données)

--- Contenu des tables (tests)

--- Table domotifications

INSERT INTO domotifications(nom, urlServeurWeb, urlServeurWebsocket, nbBoitesAuxLettres, nbPoubelles, nbMachines) VALUES ('BTS', 'http://station-lumineuse.local:80/', 'ws://station-lumineuse.local:5000', 1, 5, 6);

--- Table typesModules (BoiteAuxlettres, Poubelle, Machine)

INSERT INTO typesModules(type) VALUES ('BoiteAuxLettres');
INSERT INTO typesModules(type) VALUES ('Poubelle');
INSERT INTO typesModules(type) VALUES ('Machine');

--- Table modules

INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('boîte aux lettres', 1, 1, 1);
INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('bleue', 2, 1, 1);
INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('verte', 2, 1, 1);
INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('jaune', 2, 0, 1);
INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('grise', 2, 0, 1);
INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('rouge', 2, 0, 1);
INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('machine à laver', 3, 1, 1);
INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('lave-vaiselle', 3, 1, 1);

--- Table notifications

INSERT INTO notifications (idDomotifications, idModules, horodatage, acquittement) VALUES (1, 1, DATETIME('now'), 0);
INSERT INTO notifications (idDomotifications, idModules, horodatage, acquittement) VALUES (1, 2, DATETIME('now'), 0);
INSERT INTO notifications (idDomotifications, idModules, horodatage, acquittement) VALUES (1, 3, DATETIME('now'), 1);
