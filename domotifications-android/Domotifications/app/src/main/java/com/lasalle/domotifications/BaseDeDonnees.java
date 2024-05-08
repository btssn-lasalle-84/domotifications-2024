/**
 * @author Thierry VAIRA
 * @author LATYAOUI Othman
 * @file BaseDeDonnees.java
 * @brief La classe assurant la gestion de la base de données SQLite
 */

package com.lasalle.domotifications;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Vector;

/**
 * @class BaseDeDonnees
 * @brief La classe assurant la gestion de la base de données SQLite "domotifications.db"
 */
public class BaseDeDonnees extends SQLiteOpenHelper
{
    /**
     * Constantes
     */
    private static final String TAG = "_BaseDeDonnees"; //!< TAG pour les logs
    private static final String DOMOTIFICATIONS_BDD =
      "domotifications.db";                                   //!< Nom de la base de données
    private static final int VERSION_DOMOTIFICATIONS_BDD = 2; //!< Version de la base de données
    public static final int  ID_DOMOTIFICATIONS          = 1;

    /**
     * Attributs
     */
    private static BaseDeDonnees baseDeDonnees =
      null;                               //!< Instance unique de BaseDeDonnees (singleton)
    private SQLiteDatabase sqlite = null; //<! Accès à la base de données SQLite

    /**
     * @brief Constructeur de la classe BaseDeDonnees
     */
    private BaseDeDonnees(Context context)
    {
        super(context, DOMOTIFICATIONS_BDD, null, VERSION_DOMOTIFICATIONS_BDD);
        Log.d(TAG, "BaseDeDonnees()");
        if(sqlite == null)
            sqlite = this.getWritableDatabase();
    }

    /**
     * @fn getInstance
     * @brief Retourne l'instance BaseDeDonnees
     */
    public synchronized static BaseDeDonnees getInstance(Context context)
    {
        if(baseDeDonnees == null)
        {
            baseDeDonnees = new BaseDeDonnees(context);
        }
        return baseDeDonnees;
    }

    // Exemples de requêtes SQL
    /**
     * @brief Renvoie l'url du serveur web
     */
    public String getURLServeurWeb()
    {
        Log.d(TAG, "getURLServeurWeb()");

        Cursor curseur =
          sqlite.rawQuery("SELECT domotifications.urlServeurWeb FROM domotifications WHERE id=" +
                            ID_DOMOTIFICATIONS + ";",
                          null);

        String urlServeurWeb = null;
        if(curseur.moveToFirst())
        {
            urlServeurWeb = curseur.getString(0);
        }
        curseur.close();

        return urlServeurWeb;
    }

    /**
     * @brief Renvoie un vecteur de string contenant le noms des modules
     */
    public Vector<String> getNomModules()
    {
        Log.d(TAG, "getNomModules()");

        Cursor curseur =
          sqlite.rawQuery("SELECT nom FROM modules  WHERE idDomotifications=" + ID_DOMOTIFICATIONS +
                            " ORDER BY nom ASC",
                          null);

        Vector<String> listeNomModules = new Vector<>();

        while(curseur.moveToNext())
        {
            String nom = curseur.getString(curseur.getColumnIndexOrThrow("nom"));
            listeNomModules.add(nom);
        }
        curseur.close();

        return listeNomModules;
    }

    /**
     * @brief Renvoie un vecteur de Module contenant le poubelles
     */
    public Vector<Module> getPoubelles()
    {
        String requete =
          "SELECT * FROM modules WHERE modules.idTypesModules='2' AND idDomotifications=" +
          ID_DOMOTIFICATIONS + ";";
        Log.d(TAG, "getPoubelles() requete = " + requete);
        Cursor         curseur      = sqlite.rawQuery(requete, null);
        Vector<Module> listeModules = new Vector<Module>();
        while(curseur.moveToNext())
        {
            String id     = curseur.getString(curseur.getColumnIndexOrThrow("id"));
            String nom    = curseur.getString(curseur.getColumnIndexOrThrow("nom"));
            String actif  = curseur.getString(curseur.getColumnIndexOrThrow("actif"));
            String etat   = curseur.getString(curseur.getColumnIndexOrThrow("etat"));
            Module module = new Module(Integer.parseInt(id),
                                       nom,
                                       Module.TypeModule.Poubelle,
                                       (Integer.parseInt(actif) == 1 ? true : false),
                                       (Integer.parseInt(etat) == 1 ? true : false),
                                       baseDeDonnees);
            listeModules.add(module);
        }
        curseur.close();

        return listeModules;
    }

    /**
     * @brief Renvoie un vecteur de Module contenant les boîtes
     */
    public Vector<Module> getBoites()
    {
        String requete =
          "SELECT * FROM modules WHERE modules.idTypesModules='1' AND idDomotifications=" +
          ID_DOMOTIFICATIONS + ";";
        Log.d(TAG, "getBoites() requete = " + requete);
        Cursor         curseur      = sqlite.rawQuery(requete, null);
        Vector<Module> listeModules = new Vector<Module>();
        while(curseur.moveToNext())
        {
            String id     = curseur.getString(curseur.getColumnIndexOrThrow("id"));
            String nom    = curseur.getString(curseur.getColumnIndexOrThrow("nom"));
            String actif  = curseur.getString(curseur.getColumnIndexOrThrow("actif"));
            String etat   = curseur.getString(curseur.getColumnIndexOrThrow("etat"));
            Module module = new Module(Integer.parseInt(id),
                                       nom,
                                       Module.TypeModule.BoiteAuxLettres,
                                       (Integer.parseInt(actif) == 1 ? true : false),
                                       (Integer.parseInt(etat) == 1 ? true : false),
                                       baseDeDonnees);
            listeModules.add(module);
        }
        curseur.close();

        return listeModules;
    }

    /**
     * @brief Renvoie un vecteur de Module contenant les machines
     */
    public Vector<Module> getMachines()
    {
        String requete =
                "SELECT * FROM modules WHERE modules.idTypesModules='3' AND idDomotifications=" +
                        ID_DOMOTIFICATIONS + ";";
        Log.d(TAG, "getMachines() requete = " + requete);
        Cursor         curseur      = sqlite.rawQuery(requete, null);
        Vector<Module> listeModules = new Vector<Module>();
        while(curseur.moveToNext())
        {
            String id     = curseur.getString(curseur.getColumnIndexOrThrow("id"));
            String nom    = curseur.getString(curseur.getColumnIndexOrThrow("nom"));
            String actif  = curseur.getString(curseur.getColumnIndexOrThrow("actif"));
            String etat   = curseur.getString(curseur.getColumnIndexOrThrow("etat"));
            Module module = new Module(Integer.parseInt(id),
                    nom,
                    Module.TypeModule.Machine,
                    (Integer.parseInt(actif) == 1 ? true : false),
                    (Integer.parseInt(etat) == 1 ? true : false),
                    baseDeDonnees);
            listeModules.add(module);
        }
        curseur.close();

        return listeModules;
    }

    /**
     * @brief Renvoie le nombre max de modules de type Poubelle
     */
    public int getNbMaxModulesPoubelles()
    {
        Log.d(TAG, "getNbMaxModulesPoubelles()");

        Cursor curseur = sqlite.rawQuery(
          "SELECT domotifications.nbPoubelles FROM domotifications WHERE id=" + ID_DOMOTIFICATIONS +
            ";",
          null);

        int nbPoubelles = 0;
        if(curseur.moveToFirst())
        {
            nbPoubelles = curseur.getInt(0);
        }
        curseur.close();

        return nbPoubelles;
    }

    /**
     * @brief Renvoie le nombre max de modules de type Boîte
     */
    public int getNbMaxModulesBoites()
    {
        Log.d(TAG, "getNbMaxModulesBoites()");

        Cursor curseur = sqlite.rawQuery(
          "SELECT domotifications.nbBoitesAuxLettres FROM domotifications WHERE id=" +
            ID_DOMOTIFICATIONS + ";",
          null);

        int nbBoitesAuxLettres = 0;
        if(curseur.moveToFirst())
        {
            nbBoitesAuxLettres = curseur.getInt(0);
        }
        curseur.close();

        return nbBoitesAuxLettres;
    }

    /**
     * @brief Renvoie le nombre max de modules de type Machines
     */
    public int getNbMaxModulesMachines()
    {
        Log.d(TAG, "getNbMaxModulesMachines()");

        Cursor curseur = sqlite.rawQuery(
                "SELECT domotifications.nbMachines FROM domotifications WHERE id=" + ID_DOMOTIFICATIONS +
                        ";",
                null);

        int nbMachines = 0;
        if(curseur.moveToFirst())
        {
            nbMachines = curseur.getInt(0);
        }
        curseur.close();

        return nbMachines;
    }

    /**
     * @brief Renvoie le nombre de modules installés de type Poubelle
     */
    public int getNbModulesPoubelles()
    {
        Log.d(TAG, "getNbModulesPoubelles()");

        Cursor curseur = sqlite.rawQuery(
          "SELECT COUNT(*) AS NbPoubelles FROM modules WHERE modules.idTypesModules='2' AND idDomotifications=" +
            ID_DOMOTIFICATIONS + ";",
          null);

        int nbPoubelles = 0;
        if(curseur.moveToFirst())
        {
            nbPoubelles = curseur.getInt(0);
        }
        curseur.close();

        return nbPoubelles;
    }

    /**
     * @brief Renvoie le nombre de modules installés de type Boîtes
     */
    public int getNbModulesBoites()
    {
        Log.d(TAG, "getNbModulesBoites()");

        Cursor curseur = sqlite.rawQuery(
          "SELECT COUNT(*) AS NbBoites FROM modules WHERE modules.idTypesModules='1' AND idDomotifications=" +
            ID_DOMOTIFICATIONS + ";",
          null);

        int nbBoitesAuxLettres = 0;
        if(curseur.moveToFirst())
        {
            nbBoitesAuxLettres = curseur.getInt(0);
        }
        curseur.close();

        return nbBoitesAuxLettres;
    }

    /**
     * @brief Renvoie le nombre de modules installés de type Machines
     */
    public int getNbModulesMachines()
    {
        Log.d(TAG, "getNbModulesMachines()");

        Cursor curseur = sqlite.rawQuery(
                "SELECT COUNT(*) AS NbMachines FROM modules WHERE modules.idTypesModules='3' AND idDomotifications=" +
                        ID_DOMOTIFICATIONS + ";",
                null);

        int nbMachines = 0;
        if(curseur.moveToFirst())
        {
            nbMachines = curseur.getInt(0);
        }
        curseur.close();

        return nbMachines;
    }

    /**
     * @brief Inserer un enregistrement dans une table
     */
    public void inserer(String data)
    {
        try
        {
            Log.d(TAG, "inserer(" + data + ")");
            sqlite.execSQL("INSERT INTO ... VALUES ('" + data + "')");
        }
        catch(SQLiteConstraintException e)
        {
            Log.e(TAG, "Erreur insertion !");
        }
    }

    /**
     * @brief Supprime un enregistrement
     */
    public void supprimerJoueur(String nom)
    {
        sqlite.execSQL("DELETE FROM ... WHERE nom = ?", new String[] { nom });
    }

    /**
     * @brief Crée les différentes tables de la base de données
     */
    @Override
    public void onCreate(SQLiteDatabase sqlite)
    {
        Log.d(TAG, "onCreate()");
        sqlite.execSQL(
          "CREATE TABLE IF NOT EXISTS domotifications (id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT UNIQUE NOT NULL, urlServeurWeb varchar(255) NOT NULL, urlServeurWebsocket varchar(255) NOT NULL, nbBoitesAuxLettres INTEGER, nbPoubelles INTEGER, nbMachines INTEGER);");
        sqlite.execSQL(
          "CREATE TABLE IF NOT EXISTS typesModules (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL CHECK(type IN ('BoiteAuxLettres','Poubelle','Machine')));");
        sqlite.execSQL(
          "CREATE TABLE IF NOT EXISTS modules (id INTEGER, nom TEXT UNIQUE NOT NULL, idTypesModules INTEGER, actif BOOLEAN NOT NULL CHECK (actif IN (0, 1)) DEFAULT 0, etat BOOLEAN NOT NULL CHECK (etat IN (0, 1)) DEFAULT 0, idDomotifications INTEGER, PRIMARY KEY(id, idTypesModules), FOREIGN KEY (idTypesModules) REFERENCES typesModules(id), FOREIGN KEY (idDomotifications) REFERENCES domotifications(id) ON DELETE CASCADE);");
        sqlite.execSQL(
          "CREATE TABLE IF NOT EXISTS notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, idDomotifications INTEGER, idModules INTEGER, idTypesModules INTEGER, horodatage DATETIME NOT NULL, acquittement BOOLEAN NOT NULL CHECK (acquittement IN (0, 1)) DEFAULT 0, FOREIGN KEY (idDomotifications) REFERENCES domotifications(id) ON DELETE CASCADE, FOREIGN KEY (idModules,idTypesModules) REFERENCES modules(id,idTypesModules) ON DELETE CASCADE);");

        initialiserBaseDeDonnees(sqlite);
    }

    /**
     * @brief Ajoute des données initiales dans la base de données
     */
    private void initialiserBaseDeDonnees(SQLiteDatabase sqlite)
    {
        Log.d(TAG, "initialiserBaseDeDonnees()");
        sqlite.execSQL("INSERT INTO typesModules(type) VALUES ('BoiteAuxLettres');");
        sqlite.execSQL("INSERT INTO typesModules(type) VALUES ('Poubelle');");
        sqlite.execSQL("INSERT INTO typesModules(type) VALUES ('Machine');");

        // pour les tests seulement
        initialiserTestBaseDeDonnees(sqlite);
    }

    /**
     * @brief Ajoute des données initiales dans la base de données pour les tests
     */
    private void initialiserTestBaseDeDonnees(SQLiteDatabase sqlite)
    {
        Log.d(TAG, "initialiserTestBaseDeDonnees()");
        // Pour les tests
        sqlite.execSQL(
          "INSERT INTO domotifications(nom, urlServeurWeb, urlServeurWebsocket, nbBoitesAuxLettres, nbPoubelles, nbMachines) VALUES ('BTS', 'http://station-lumineuse.local:80', 'ws://station-lumineuse.local:5000', 1, 5, 6);");
        sqlite.execSQL(
          "INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (1, 'boîte aux lettres', 1, 1, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (1, 'bleue', 2, 1, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (2, 'verte', 2, 0, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (3, 'jaune', 2, 0, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (4, 'grise', 2, 0, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (5, 'rouge', 2, 0, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (1, 'machine à laver', 3, 1, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (id, nom, idTypesModules, actif, idDomotifications) VALUES (2, 'lave-vaiselle', 3, 1, 1);");
    }

    /**
     * @brief Supprime les tables existantes pour en recréer des vierges
     */
    public void effacer()
    {
        Log.d(TAG, "effacer()");
        onUpgrade(sqlite, sqlite.getVersion(), sqlite.getVersion() + 1);
    }

    /**
     * @brief Supprimer les tables existantes pour en recréer des vierges
     * @warning le plus simple est de supprimer l'application puis de la réinstaller !
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqlite, int oldVersion, int newVersion)
    {
        Log.d(TAG, "onUpgrade()");
        // sqlite.execSQL("DROP TABLE IF EXISTS ...");
        sqlite.execSQL("DROP TABLE IF EXISTS notifications;");
        sqlite.execSQL("DROP TABLE IF EXISTS modules;");
        sqlite.execSQL("DROP TABLE IF EXISTS typesModules;");
        sqlite.execSQL("DROP TABLE IF EXISTS domotifications;");
        sqlite.setVersion(newVersion);
        onCreate(sqlite);
    }

    public void sauvegarderURLServeurWeb(String urlServeurWeb)
    {
        Log.d(TAG, "sauvegarderURLServeurWeb()");

        try
        {
            sqlite.execSQL("UPDATE domotifications SET urlServeurWeb = ?;",
                           new String[] { urlServeurWeb });
        }
        catch(SQLiteConstraintException e)
        {
            Log.e(TAG, "Erreur de mise à jour de l'URL du Serveur Web");
        }
    }

    /**
     * @brief Met à jour l'état d'activation du module dans la base de données
     */
    public void mettreAJourEtatActivationModule(int idModule, int idTypesModules, boolean actif)
    {
        Log.d(TAG,
              "mettreAJourEtatActivationModule() idModule = " + idModule + " actif = " + actif);

        try
        {
            String requete = "UPDATE modules SET actif = '" + (actif ? 1 : 0) + "' WHERE id = '" +
                             idModule + "' AND idTypesModules = '" + (idTypesModules + 1)  + "'";
            Log.d(TAG, "mettreAJourEtatActivationModule() requete = " + requete);
            sqlite.execSQL(requete);
        }
        catch(SQLiteConstraintException e)
        {
            Log.e(TAG, "Erreur de mise à jour de l'état d'activation du module");
        }
    }

    /**
     * @brief Met à jour l'état de notification du module dans la base de données
     */
    public void mettreAJourEtatNotificationModule(int idModule, int idTypesModules, boolean etat)
    {
        Log.d(TAG,
              "mettreAJourEtatNotificationModule() idModule = " + idModule + " etat = " + etat);

        try
        {
            String requete = "UPDATE modules SET etat = '" + (etat ? 1 : 0) + "' WHERE id = '" +
                             idModule + "' AND idTypesModules = '" + (idTypesModules + 1) + "'";
            Log.d(TAG, "mettreAJourEtatNotificationModule() requete = " + requete);
            sqlite.execSQL(requete);
        }
        catch(SQLiteConstraintException e)
        {
            Log.e(TAG, "Erreur de mise à jour de l'état de notification du module");
        }
    }

    /**
     * @brief Enregistre l'acquittement de la notification dans la base de données
     */
    public void enregistrerAcquittementNotification(int     idModule,
                                                    int     idTypesModules,
                                                    boolean acquittement)
    {
        Log.d(TAG,
              "enregistrerAcquittementNotification() idModule = " + idModule +
                " idTypesModules = " + idTypesModules + " acquittement = " + acquittement);

        try
        {
            String requete =
              "INSERT INTO notifications (idDomotifications, idModules, idTypesModules, horodatage, acquittement) VALUES (" +
              ID_DOMOTIFICATIONS + ", " + idModule + ", " + (idTypesModules + 1) + ", "
              + "datetime('now'), " + (acquittement ? 1 : 0) + ");";
            Log.d(TAG, "enregistrerAcquittementNotification() requete = " + requete);
            sqlite.execSQL(requete);
        }
        catch(SQLiteConstraintException e)
        {
            Log.e(TAG, "Erreur d'enregistrement de l'acquittement de la notification");
        }
    }
}