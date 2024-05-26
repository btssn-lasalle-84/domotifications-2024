/**
 * @file IHM.java
 * @brief Déclaration de l'activité principale
 * @author LATYAOUI Othman
 */

package com.lasalle.domotifications;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * @class EcranPrincipal
 * @brief L'activité principale
 */
public class IHM extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG        = "_IHM"; //!< TAG pour les logs (cf. Logcat)
    private static final int    INTERVALLE = 1000;   //!< Intervalle d'interrogation en ms
    private Handler             handler =
      null; //!< Handler permettant la communication entre la classe Communication et les activités
    private Timer minuteur = null; //!< Pour gérer la récupération des états des différents modules
    private Communication communication; //!< Association avec la classe Communication
    private BaseDeDonnees baseDeDonnees; //!< Association avec la classe BaseDeDonnee

    /**
     * Attributs
     */
    private int     nbNotificationsPoubelles;
    private int     nbNotificationsMachines;
    private int     nbNotificationsBoites;
    private boolean erreurCommunication = false;

    /**
     * GUI
     */
    private ImageButton boutonPoubelle;
    private ImageButton boutonMachine;
    private ImageButton boutonBoiteAuxLettres;
    private TextView    notificationPoubelle;
    private TextView    notificationMachine;
    private TextView    notificationBoiteAuxLettres;

    /**
     * @brief Méthode appelée à la création de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        initialiserGUI();

        initialiserBaseDeDonnees();
        initialiserHandler();
        initialiserMinuteur();
        initialiserCommunication();
        recupererNotifications();
    }

    /**
     * @brief Méthode appelée au démarrage après le onCreate() ou un restart
     * après un onStop()
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    /**
     * @brief Méthode appelée après onStart() ou après onPause()
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    /**
     * @brief Méthode appelée après qu'une boîte de dialogue s'est affichée (on
     * reprend sur un onResume()) ou avant onStop() (activité plus visible)
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    /**
     * @brief Méthode appelée lorsque l'activité n'est plus visible
     */
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    /**
     * @brief Méthode appelée à la destruction de l'application (après onStop()
     * et détruite par le système Android)
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    /**
     * @brief Initialise les ressources graphiques de l'activité
     */
    private void initialiserGUI()
    {
        setContentView(R.layout.activity_main);

        boutonPoubelle        = (ImageButton)findViewById(R.id.boutonPoubelle);
        boutonMachine         = (ImageButton)findViewById(R.id.boutonMachine);
        boutonBoiteAuxLettres = (ImageButton)findViewById(R.id.boutonBoiteAuxLettres);

        notificationPoubelle        = (TextView)findViewById(R.id.notificationPoubelle);
        notificationMachine         = (TextView)findViewById(R.id.notificationMachine);
        notificationBoiteAuxLettres = (TextView)findViewById(R.id.notificationBoiteAuxLettres);

        initialiserBouton(boutonPoubelle, FenetrePoubelle.class);
        initialiserBouton(boutonMachine, FenetreMachine.class);
        initialiserBouton(boutonBoiteAuxLettres, FenetreBoiteAuxLettres.class);
    }

    public void initialiserBouton(ImageButton bouton, Class<?> typeDeClasse)
    {
        bouton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d(TAG, "clic " + typeDeClasse.getName());
                Intent fenetre = new Intent(IHM.this, typeDeClasse);
                startActivity(fenetre);
            }
        });
    }

    private void initialiserBaseDeDonnees()
    {
        Log.d(TAG, "initialiserBaseDeDonnees()");
        baseDeDonnees = BaseDeDonnees.getInstance(this);
    }

    private void initialiserHandler()
    {
        Log.d(TAG, "initialiserHandler()");
        this.handler = new Handler(this.getMainLooper()) {
            @Override
            public void handleMessage(Message message)
            {
                switch(message.what)
                {
                    case Communication.CODE_HTTP_REPONSE_JSON:
                        Log.d(TAG, "[Handler] REPONSE JSON");
                        traiterReponseJSON(message.obj.toString());
                        erreurCommunication = false;
                        break;
                    case Communication.CODE_HTTP_ERREUR:
                        if(!erreurCommunication)
                        {
                            Log.d(TAG, "[Handler] ERREUR HTTP");
                            erreurCommunication = true;
                        }
                        break;
                }
            }
        };
    }

    private void initialiserMinuteur()
    {
        Log.d(TAG, "initialiserMinuteur()");
        minuteur = new Timer();
    }

    private void initialiserCommunication()
    {
        Log.d(TAG, "initialiserCommunication()");
        communication = Communication.getInstance(Communication.ADRESSE_IP_STATION, this);
    }

    private void recupererNotifications()
    {
        Log.d(TAG, "recupererNotifications()");
        nbNotificationsPoubelles = 0;
        nbNotificationsMachines  = 0;
        nbNotificationsBoites    = 0;

        TimerTask tacheRecuperationEtatsPoubelles = new TimerTask() {
            public void run()
            {
                communication.emettreRequeteGET(Communication.API_GET_POUBELLES, handler);
            }
        };

        TimerTask tacheRecuperationEtatsMachines = new TimerTask() {
            public void run()
            {
                communication.emettreRequeteGET(Communication.API_GET_MACHINES, handler);
            }
        };

        TimerTask tacheRecuperationEtatsBoites = new TimerTask() {
            public void run()
            {
                communication.emettreRequeteGET(Communication.API_GET_BOITES, handler);
            }
        };

        minuteur.schedule(tacheRecuperationEtatsPoubelles, INTERVALLE, INTERVALLE);
        minuteur.schedule(tacheRecuperationEtatsMachines, INTERVALLE, INTERVALLE);
        minuteur.schedule(tacheRecuperationEtatsBoites, INTERVALLE, INTERVALLE);
    }

    public void traiterReponseJSON(String reponse)
    {
        Log.d(TAG, "traiterReponseJSON() reponse = " + reponse);

        int     nouvellesNotificationsPoubelles = 0;
        boolean estModulesPoubelles             = false;
        int     nouvellesNotificationsMachines  = 0;
        boolean estModulesMachines              = false;
        int     nouvellesNotificationsBoites    = 0;
        boolean estModulesBoites                = false;

        /*
            Exemple de réponsee : pour la requête GET /poubelles
            [
                {"idPoubelle":1,"couleur":"rouge","etat":false,"actif":true},
                {"idPoubelle":2,"couleur":"jaune","etat":false,"actif":true},
                {"idPoubelle":3,"couleur":"bleu","etat":false,"actif":true},
                {"idPoubelle":4,"couleur":"gris","etat":false,"actif":true},
                {"idPoubelle":5,"couleur":"vert","etat":false,"actif":true}
            ]
            Exemple de réponsee : pour la requête GET /machines
            [
                {"idMachine":1,"couleur":"rouge","etat":false,"actif":true},
                {"idMachine":2,"couleur":"jaune","etat":false,"actif":true},
                {"idMachine":3,"couleur":"bleu","etat":false,"actif":true},
                {"idMachine":4,"couleur":"gris","etat":false,"actif":true},
            ]
            Exemple de réponsee : pour la requête GET /boites
            [
                {"idBoite":1,"couleur":"rouge","etat":false,"actif":true},
                {"idBoite":2,"couleur":"jaune","etat":false,"actif":true},
                {"idBoite":3,"couleur":"bleu","etat":false,"actif":true},
                {"idBoite":4,"couleur":"gris","etat":false,"actif":true},
            ]
        */

        try
        {
            JSONArray jsonArray = new JSONArray(reponse);
            for(int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Boolean    etat       = jsonObject.getBoolean("etat");
                Boolean    actif      = jsonObject.getBoolean("actif");
                if(jsonObject.has("idPoubelle"))
                {
                    if(actif && etat)
                    {
                        nouvellesNotificationsPoubelles++;
                    }
                    estModulesPoubelles = true;
                }
                else if(jsonObject.has("idMachine"))
                {
                    if(actif && etat)
                    {
                        nouvellesNotificationsMachines++;
                    }
                    estModulesMachines = true;
                }
                else if(jsonObject.has("idBoite"))
                {
                    if(actif && etat)
                    {
                        nouvellesNotificationsBoites++;
                    }
                    estModulesBoites = true;
                }
            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

        Log.d(TAG,
              "traiterReponseJSON() nouvellesNotificationsPoubelles = " +
                nouvellesNotificationsPoubelles +
                " nouvellesNotificationsMachines = " + nouvellesNotificationsMachines +
                " nouvellesNotificationsBoites = " + nouvellesNotificationsBoites);

        if(estModulesPoubelles && nouvellesNotificationsPoubelles != nbNotificationsPoubelles)
        {
            nbNotificationsPoubelles = nouvellesNotificationsPoubelles;
            mettreAJourNotificationsPoubelles();
        }
        if(estModulesMachines && nouvellesNotificationsMachines != nbNotificationsMachines)
        {
            nbNotificationsMachines = nouvellesNotificationsMachines;
            mettreAJourNotificationsMachines();
        }
        if(estModulesBoites && nouvellesNotificationsBoites != nbNotificationsBoites)
        {
            nbNotificationsBoites = nouvellesNotificationsBoites;
            mettreAJourNotificationsBoites();
        }
    }

    private void mettreAJourNotificationsPoubelles()
    {
        if(nbNotificationsPoubelles > 0)
        {
            Log.d(TAG,
                  "mettreAJourNotificationsPoubelles() nbNotificationsPoubelles = " +
                    nbNotificationsPoubelles);
            notificationPoubelle.setVisibility(View.VISIBLE);
            notificationPoubelle.setText(String.valueOf(nbNotificationsPoubelles));
        }
        else
        {
            notificationPoubelle.setVisibility(View.INVISIBLE);
        }
    }

    private void mettreAJourNotificationsMachines()
    {
        if(nbNotificationsMachines > 0)
        {
            Log.d(TAG,
                  "mettreAJourNotificationsMachines() nbNotificationsMachines = " +
                    nbNotificationsMachines);
            notificationMachine.setVisibility(View.VISIBLE);
            notificationMachine.setText(String.valueOf(nbNotificationsMachines));
        }
        else
        {
            notificationMachine.setVisibility(View.INVISIBLE);
        }
    }

    private void mettreAJourNotificationsBoites()
    {
        if(nbNotificationsBoites > 0)
        {
            Log.d(TAG,
                  "mettreAJourNotificationsBoites nbNotificationsBoites = " +
                    nbNotificationsBoites);
            notificationBoiteAuxLettres.setVisibility(View.VISIBLE);
            notificationBoiteAuxLettres.setText(String.valueOf(nbNotificationsBoites));
        }
        else
        {
            notificationBoiteAuxLettres.setVisibility(View.INVISIBLE);
        }
    }
}