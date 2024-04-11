--- Exemples requêtes

--- Liste des système de domotifications

SELECT * FROM domotifications;

SELECT domotifications.nbPoubelles FROM domotifications;

--- Liste des modules

SELECT * FROM modules;

SELECT COUNT(*) AS NbModules FROM modules;

SELECT COUNT(*) AS NbPoubelles FROM modules WHERE modules.idTypesModules='2';

SELECT modules.id, modules.nom, typesModules.type, modules.actif, domotifications.urlServeurWeb, domotifications.urlServeurWebsocket FROM modules
INNER JOIN typesModules ON (modules.idTypesModules = typesModules.id)
INNER JOIN domotifications ON (modules.idDomotifications = domotifications.id);

SELECT modules.id, modules.nom, typesModules.type, modules.actif FROM modules
INNER JOIN typesModules ON (modules.idTypesModules = typesModules.id)
WHERE modules.id='2';

--- Liste des notifications

SELECT modules.id, modules.nom, typesModules.type, modules.actif, notifications.horodatage, notifications.acquittement FROM notifications
INNER JOIN modules ON (notifications.idModules = modules.id)
INNER JOIN typesModules ON (modules.idTypesModules = typesModules.id)
WHERE modules.id='2';

--- Mettre à jour une notification

UPDATE notifications SET acquittement=1 WHERE id='1';
