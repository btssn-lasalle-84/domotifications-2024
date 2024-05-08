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
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.List;
import java.util.Timer;
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
    private Handler handler =
            null; //!< Handler permettant la communication entre la classe Communication et les activités
    private Timer minuteur = null; //!< Pour gérer la récupération des états des différents modules
    private Communication communication; //!< Association avec la classe Communication
    private BaseDeDonnees baseDeDonnees; //!< Association avec la classe BaseDeDonnee
    private FenetrePoubelle fenetrePoubelle; //!< Association avec la classe FenetrePoubelle


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

    /**
     * @brief Méthode appelée à la création de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        initialiserGUI();

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

    private void initialiserHandler()
    {
        Log.d(TAG, "initialiserHandler()");
        handler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                recupererNotifications();
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

        Vector<Module> modules = new Vector<>();
        modules.addAll(baseDeDonnees.getPoubelles());
        modules.addAll(baseDeDonnees.getBoites());
        modules.addAll(baseDeDonnees.getMachines());

        for (Module module : modules)
        {
            if (module.estNotifie())
            {
                switch (module.getTypeModule())
                {
                    case Poubelle:
                        nbNotificationsPoubelles++;
                        break;
                    case BoiteAuxLettres:
                        nbNotificationsBoites++;
                    case Machine:
                        nbNotificationsMachines++;
                        break;

                    default:
                        break;
                }
            }
        }

        mettreAJourNotificationsPoubelles();
        mettreAJourNotificationsBoites();
        mettreAJourNotificationsMachines();

    }

    private void mettreAJourNotificationsPoubelles()
    {
        if(nbNotificationsPoubelles > 0)
        {
            Log.d(TAG, "Nombre de notifications poubelles : " + nbNotificationsPoubelles);
        }
    }

    private void mettreAJourNotificationsMachines()
    {
        if(nbNotificationsMachines > 0)
        {
            Log.d(TAG, "Nombre de notifications machines : " + nbNotificationsMachines);
        }
    }

    private void mettreAJourNotificationsBoites()
    {
        if (nbNotificationsBoites > 0)
        {
            Log.d(TAG, "Nombre de notifications boites : " + nbNotificationsBoites);
        }
    }
}