--- LMD (langage de manipulation de données)

--- Contenu des tables (tests)

--- Table domotifications

INSERT INTO domotifications(nom, urlServeurWeb, urlServeurWebsocket, nbBoitesAuxLettres, nbPoubelles, nbMachines) VALUES ('BTS', 'http://station-lumineuse.local:80', 'ws://station-lumineuse.local:5000', 1, 5, 6);

--- Table typesModules (BoiteAuxlettres, Poubelle, Machine)

INSERT INTO typesModules(type) VALUES ('BoiteAuxLettres');
INSERT INTO typesModules(type) VALUES ('Poubelle');
INSERT INTO typesModules(type) VALUES ('Machine');

--- Table modules

INSERT INTO modules (id, nom, idTypesModules, actif, couleur, idDomotifications) VALUES (1, 'boîte aux lettres 1', 1, 1, '#FF0000', 1);
INSERT INTO modules (id, nom, idTypesModules, actif, couleur, idDomotifications) VALUES (1, 'bleue', 2, 1, '#0000FF', 1);
INSERT INTO modules (id, nom, idTypesModules, actif, couleur, idDomotifications) VALUES (2, 'verte', 2, 0, '#00FF00', 1);
INSERT INTO modules (id, nom, idTypesModules, actif, couleur, idDomotifications) VALUES (3, 'jaune', 2, 0, '#FFFF00', 1);
INSERT INTO modules (id, nom, idTypesModules, actif, couleur, idDomotifications) VALUES (4, 'grise', 2, 0, '#F0F0F2', 1);
INSERT INTO modules (id, nom, idTypesModules, actif, couleur, idDomotifications) VALUES (5, 'rouge', 2, 0, '#FF0000', 1);
INSERT INTO modules (id, nom, idTypesModules, actif, couleur, idDomotifications) VALUES (1, 'machine à laver', 3, 1, '#FF7F00', 1);
INSERT INTO modules (id, nom, idTypesModules, actif, couleur, idDomotifications) VALUES (2, 'lave-vaisselle', 3, 1, '#EEC4C9', 1);
