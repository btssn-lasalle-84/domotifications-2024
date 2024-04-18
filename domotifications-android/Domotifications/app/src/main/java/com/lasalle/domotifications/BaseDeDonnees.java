/**
 * @author Thierry VAIRA
 * @author LATYAOUI Othman
 * @file BaseDeDonnees.java
 * @brief La classe assurant la gestion de la base de données SQLite
 */

package com.lasalle.domotifications;

import static android.provider.MediaStore.getVersion;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
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
    private static final int VERSION_DOMOTIFICATIONS_BDD = 1; //!< Version de la base de données

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
          sqlite.rawQuery("SELECT domotifications.urlServeurWeb FROM domotifications;", null);

        String urlServeurWeb = null;
        if(curseur.moveToFirst())
        {
            urlServeurWeb = curseur.getString(0);
        }
        curseur.close();

        return urlServeurWeb;
    }

    /**
     * @brief Renvoie un vecteur de string contenant le noms des participants enregistrés
     */
    public Vector<String> getNomModules()
    {
        Log.d(TAG, "getNomModules()");

        Cursor curseur = sqlite.rawQuery("SELECT nom FROM modules ORDER BY nom ASC", null);

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
     * @brief Renvoie le nombre max de modules de type Poubelle
     */
    public int getNbMaxModulesPoubelles()
    {
        Log.d(TAG, "getNbMaxModulesPoubelles()");

        Cursor curseur =
          sqlite.rawQuery("SELECT domotifications.nbPoubelles FROM domotifications;", null);

        int nbPoubelles = 0;
        if(curseur.moveToFirst())
        {
            nbPoubelles = curseur.getInt(0);
        }
        curseur.close();

        return nbPoubelles;
    }

    /**
     * @brief Renvoie le nombre de modules installés de type Poubelle
     */
    public int getNbModulesPoubelles()
    {
        Log.d(TAG, "getNbModulesPoubelles()");

        Cursor curseur = sqlite.rawQuery(
          "SELECT COUNT(*) AS NbPoubelles FROM modules WHERE modules.idTypesModules='2';",
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
     * @brief Actualiser les données d'un enregistrement
     */
    private void actualiser(String id, String data)
    {
        sqlite.execSQL("UPDATE ... SET data = '" + data + "' WHERE id = '" + id + "'");
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
          "CREATE TABLE IF NOT EXISTS modules (id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT UNIQUE NOT NULL, idTypesModules INTEGER, actif BOOLEAN NOT NULL CHECK (actif IN (0, 1)) DEFAULT 0, idDomotifications INTEGER, FOREIGN KEY (idTypesModules) REFERENCES typesModules(id), FOREIGN KEY (idDomotifications) REFERENCES domotifications(id) ON DELETE CASCADE);");
        sqlite.execSQL(
          "CREATE TABLE IF NOT EXISTS notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, idDomotifications INTEGER, idModules INTEGER, horodatage DATETIME NOT NULL, acquittement BOOLEAN NOT NULL CHECK (acquittement IN (0, 1)) DEFAULT 0, FOREIGN KEY (idDomotifications) REFERENCES domotifications(id) ON DELETE CASCADE, FOREIGN KEY (idModules) REFERENCES modules(id) ON DELETE CASCADE);");

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
          "INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('boîte aux lettres', 1, 1, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('bleue', 2, 1, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('verte', 2, 0, 1);");
        // sqlite.execSQL("INSERT INTO modules (nom, idTypesModules, actif, idDomotifications)
        // VALUES ('jaune', 2, 0, 1);"); sqlite.execSQL("INSERT INTO modules (nom, idTypesModules,
        // actif, idDomotifications) VALUES ('grise', 2, 0, 1);"); sqlite.execSQL("INSERT INTO
        // modules (nom, idTypesModules, actif, idDomotifications) VALUES ('rouge', 2, 0, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('machine à laver', 3, 1, 1);");
        sqlite.execSQL(
          "INSERT INTO modules (nom, idTypesModules, actif, idDomotifications) VALUES ('lave-vaiselle', 3, 1, 1);");
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

        try {
            sqlite.execSQL("UPDATE domotifications SET urlServeurWeb = ?;", new String[]{urlServeurWeb});
        }

        catch(SQLiteConstraintException e)
        {
            Log.e(TAG, "Erreur de mise à jour de l'URL du Serveur Web");
        }
    }
}
