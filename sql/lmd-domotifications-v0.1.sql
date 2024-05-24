--- LMD (langage de manipulation de données)

--- Contenu des tables (tests)

--- Table domotifications

INSERT INTO domotifications(nom, urlServeurWeb, urlServeurWebsocket, nbBoitesAuxLettres, nbPoubelles, nbMachines) VALUES ('BTS', 'http://station-lumineuse.local:80/', 'ws://station-lumineuse.local:5000', 1, 5, 6);

--- Table typesModules (BoiteAuxlettres, Poubelle, Machine)

INSERT INTO typesModules(type) VALUES ('BoiteAuxLettres');
INSERT INTO typesModules(type) VALUES ('Poubelle');
INSERT INTO typesModules(type) VALUES ('Machine');

--- Table modules

INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (1, 'boîte aux lettres', 1, 1, 1);
INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (1, 'bleue', 2, 1, 1);
INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (2, 'verte', 2, 1, 1);
INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (3, 'jaune', 2, 0, 1);
INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (4, 'grise', 2, 0, 1);
INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (5, 'rouge', 2, 0, 1);
INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (1, 'machine à laver', 3, 1, 1);
INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (2, 'lave-vaiselle', 3, 1, 1);
