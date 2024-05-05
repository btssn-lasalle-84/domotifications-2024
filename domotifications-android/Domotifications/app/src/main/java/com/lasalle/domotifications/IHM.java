/**
 * @file IHM.java
 * @brief Déclaration de l'activité principale
 * @author LATYAOUI Othman
 */

package com.lasalle.domotifications;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

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
        // @todo Initialiser un handler pour la récupération des notifications
    }

    private void initialiserMinuteur()
    {
        Log.d(TAG, "initialiserMinuteur()");
        // @todo Initialiser un minuteur pour la récupération des notifications
    }

    private void initialiserCommunication()
    {
        Log.d(TAG, "initialiserCommunication()");
        // @todo Initialiser une communication pour la récupération des notifications
    }

    private void recupererNotifications()
    {
        Log.d(TAG, "recupererNotifications()");
        nbNotificationsPoubelles = 0;
        nbNotificationsMachines  = 0;
        nbNotificationsBoites    = 0;
        // @todo Effectuer les requêtes pour récupérer les notifications de tous les modules
    }

    private void mettreAJourNotificationsPoubelles()
    {
        // @todo Afficher, si nécessaire, le nombre de notifications des modules Poubelle
    }

    private void mettreAJourNotificationsMachines()
    {
        // @todo Afficher, si nécessaire, le nombre de notifications des modules Machine
    }

    private void mettreAJourNotificationsBoites()
    {
        // @todo Afficher, si nécessaire, le nombre de notifications des modules Boite
    }
}