package com.lasalle.domotifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class FenetrePoubelle extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG                 = "_FenetrePoubelle"; //!< TAG pour les logs
    private static final String API_GET_POUBELLES   = "/poubelles";       //!< Pour une requête GET
    private static final String API_PATCH_POUBELLES = "/poubelles"; //!< Pour une requête PATCH
    private static final int    INTERVALLE          = 1000; //!< Intervalle d'interrogation en ms
    /**
     * Attributs
     */
    private BaseDeDonnees  baseDeDonnees;      //!< Association avec la base de donnees
    private Vector<Module> modulesPoubelles;   //!< Conteneur des modules poubelles
    private int            nbModulesPoubelles; //!< le nombre de poubelles gérées
    private int            idNotification = 0; //!< Identifiant unique pour chaque notification
    private Communication  communication;      //!< Association avec la classe Communication
    private Handler        handler =
      null; //!< Handler permettant la communication entre la classe Communication et l'activité
    private Timer minuteur = null; //!< Pour gérer la récupération des états des modules poubelles
    private TimerTask tacheRecuperationEtats =
      null; //!< Pour effectuer la récupération des états des modules poubelles
    private boolean erreurCommunication = false;
    /**
     * GUI
     */
    public static final int BLEUE                = 0; //!< Poubelle bleue
    public static final int VERTE                = 1; //!< Poubelle verte
    public static final int JAUNE                = 2; //!< Poubelle jaune
    public static final int GRISE                = 3; //!< Poubelle grise
    public static final int ROUGE                = 4; //!< Poubelle rouge
    public static final int NB_COULEURS_POUBELLE = 5; //!< Nombre de couleurs max pour les poubelles
    public static final int[] IMAGES_POUBELLES   = {
          R.drawable.poubelle_bleue,
          R.drawable.poubelle_verte,
          R.drawable.poubelle_jaune,
          R.drawable.poubelle_grise,
          R.drawable.poubelle_rouge
    }; //!< Id des images des poubelles dans les ressources Android
    private ImageView[] imagesPoubelles;             //!< Images des poubelles de couleur
    private ImageButton boutonAccueil;               //!< Bouton pour revenir à l'accueil
    private ImageView[] imagesNotificationPoubelles; //!< Images des notifications des poubelles
    private Switch[] boutonsActivation; //!< Boutons d'activation/désactivation des modules
                                        //!< poubelles

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        initialiserBaseDeDonnees();
        initialiserModulesPoubelles();
        initialiserGUI();
        initialiserHandler();
        initialiserMinuteur();
        initialiserCommunication();
        recupererEtats();
    }

    private void initialiserBaseDeDonnees()
    {
        Log.d(TAG, "initialiserBaseDeDonnees()");
        baseDeDonnees = BaseDeDonnees.getInstance(this);
    }

    private void initialiserModulesPoubelles()
    {
        Log.d(TAG, "initialiserModulesPoubelles()");
        modulesPoubelles   = baseDeDonnees.getPoubelles();
        nbModulesPoubelles = baseDeDonnees.getNbModulesPoubelles();
        Log.d(TAG, "nbModulesPoubelles = " + nbModulesPoubelles);
    }

    /**
     * @brief Initialise les ressources graphiques de l'activité
     */
    private void initialiserGUI()
    {
        // contenu bord à bord
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_poubelle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.d(TAG, "initialiserGUI()");

        boutonAccueil = (ImageButton)findViewById(R.id.boutonAccueil);

        imagesPoubelles    = new ImageView[NB_COULEURS_POUBELLE];
        imagesPoubelles[0] = (ImageView)findViewById(R.id.poubelle0);
        imagesPoubelles[1] = (ImageView)findViewById(R.id.poubelle1);
        imagesPoubelles[2] = (ImageView)findViewById(R.id.poubelle2);
        imagesPoubelles[3] = (ImageView)findViewById(R.id.poubelle3);
        imagesPoubelles[4] = (ImageView)findViewById(R.id.poubelle4);

        imagesNotificationPoubelles    = new ImageView[NB_COULEURS_POUBELLE];
        imagesNotificationPoubelles[0] = (ImageView)findViewById(R.id.notificationPoubelle0);
        imagesNotificationPoubelles[1] = (ImageView)findViewById(R.id.notificationPoubelle1);
        imagesNotificationPoubelles[2] = (ImageView)findViewById(R.id.notificationPoubelle2);
        imagesNotificationPoubelles[3] = (ImageView)findViewById(R.id.notificationPoubelle3);
        imagesNotificationPoubelles[4] = (ImageView)findViewById(R.id.notificationPoubelle4);

        boutonsActivation    = new Switch[NB_COULEURS_POUBELLE];
        boutonsActivation[0] = (Switch)findViewById(R.id.activationPoubelle0);
        boutonsActivation[1] = (Switch)findViewById(R.id.activationPoubelle1);
        boutonsActivation[2] = (Switch)findViewById(R.id.activationPoubelle2);
        boutonsActivation[3] = (Switch)findViewById(R.id.activationPoubelle3);
        boutonsActivation[4] = (Switch)findViewById(R.id.activationPoubelle4);

        for(int i = 0; i < nbModulesPoubelles; ++i)
        {
            imagesPoubelles[i].setImageResource(IMAGES_POUBELLES[i]);
            final int numeroPoubelle = i;
            imagesNotificationPoubelles[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    gererClicBoutonNotification(numeroPoubelle);
                }
            });
            imagesPoubelles[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    gererClicBoutonNotification(numeroPoubelle);
                }
            });
        }

        for(int i = 0; i < NB_COULEURS_POUBELLE; ++i)
        {
            imagesPoubelles[i].setVisibility(View.INVISIBLE);
            boutonsActivation[i].setVisibility(View.INVISIBLE);
        }

        for(int i = 0; i < nbModulesPoubelles; ++i)
        {
            imagesPoubelles[i].setVisibility(View.VISIBLE);
            boutonsActivation[i].setVisibility(View.VISIBLE);
        }

        for(int i = 0; i < nbModulesPoubelles; i++)
        {
            final int numeroPoubelle = i;
            // initialise la GUI en fonction de l'état de notification
            if(modulesPoubelles.get(i).estNotifie())
            {
                imagesNotificationPoubelles[i].setVisibility(View.VISIBLE);
            }
            else
            {
                imagesNotificationPoubelles[i].setVisibility(View.INVISIBLE);
            }
            // initialise la GUI en fonction de l'état d'activation
            if(modulesPoubelles.get(i).estActif())
            {
                boutonsActivation[i].setChecked(true);
                imagesNotificationPoubelles[i].setEnabled(true);
                imagesPoubelles[i].setEnabled(true);
            }
            else
            {
                boutonsActivation[i].setChecked(false);
                imagesNotificationPoubelles[i].setEnabled(false);
                imagesPoubelles[i].setEnabled(false);
            }
            boutonsActivation[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    gererClicBoutonActivation(numeroPoubelle);
                }
            });
        }

        boutonAccueil = (ImageButton)findViewById(R.id.boutonAccueil);
        boutonAccueil.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonAccueil");
                if(minuteur != null)
                    minuteur.cancel();
                finish();
            }
        });
    }

    private void initialiserHandler()
    {
        Log.d(TAG, "initialiserHandler()");
        this.handler = new Handler(this.getMainLooper()) {
            @Override
            public void handleMessage(Message message)
            {
                // Log.d(TAG, "[Handler] message what = " + message.what);
                // Log.d(TAG, "[Handler] message obj = " + message.obj.toString());

                switch(message.what)
                {
                    case Communication.CODE_HTTP_REPONSE_JSON:
                        Log.d(TAG, "[Handler] REPONSE JSON");
                        traiterReponseJSON(message.obj.toString());
                        erreurCommunication = false;
                        break;
                    case Communication.CODE_HTTP_REPONSE_PATCH:
                        Log.d(TAG, "[Handler] REPONSE PATCH");
                        traiterReponseJSON(message.obj.toString());
                        enregistrerAcquittementNotification(message.obj.toString());
                        erreurCommunication = false;
                        break;
                    case Communication.CODE_HTTP_ERREUR:
                        Log.d(TAG, "[Handler] ERREUR HTTP");
                        if(erreurCommunication == false)
                        {
                            afficherErreur("Impossible de communiquer avec la station !");
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
        // On récupère l'URL de la station dans la base de données
        // communication = Communication.getInstance(this);
        // ou on indique l'adresse de la station :
        communication = Communication.getInstance(Communication.ADRESSE_IP_STATION, this);
    }

    private void gererClicBoutonActivation(int numeroPoubelle)
    {
        if(modulesPoubelles.get(numeroPoubelle) == null)
        {
            Log.e(TAG, "Aucune poubelle !");
            return;
        }
        Log.d(TAG,
              "gererClicBoutonActivation() numeroPoubelle = " + numeroPoubelle +
                " activation = " + boutonsActivation[numeroPoubelle].isChecked());

        modulesPoubelles.get(numeroPoubelle)
          .setActif(boutonsActivation[numeroPoubelle].isChecked());

        if(modulesPoubelles.get(numeroPoubelle).estActif())
        {
            imagesPoubelles[numeroPoubelle].setEnabled(true);
            imagesNotificationPoubelles[numeroPoubelle].setEnabled(true);
        }
        else
        {
            imagesPoubelles[numeroPoubelle].setEnabled(false);
            imagesNotificationPoubelles[numeroPoubelle].setEnabled(false);
        }
    }

    private void gererClicBoutonNotification(int numeroPoubelle)
    {
        if(modulesPoubelles.get(numeroPoubelle) == null)
        {
            Log.e(TAG, "Aucune poubelle !");
            return;
        }
        Log.d(TAG,
              "gererClicBoutonNotification() numeroPoubelle = " + numeroPoubelle +
                " idPoubelle = " + modulesPoubelles.get(numeroPoubelle).getIdModule() +
                " notification = " + modulesPoubelles.get(numeroPoubelle).estNotifie() +
                " activation = " + modulesPoubelles.get(numeroPoubelle).estActif());

        if(modulesPoubelles.get(numeroPoubelle).estActif())
        {
            if(modulesPoubelles.get(numeroPoubelle).estNotifie())
            {
                /*
                Exemple :
                $ curl --location 'http://station-lumineuse.local:80/poubelles/1' --request PATCH
                --header 'Content-Type: application/json' --data '{"idPoubelle": "1","etat": false}'
                */
                String api =
                  API_PATCH_POUBELLES + "/" + modulesPoubelles.get(numeroPoubelle).getIdModule();
                String json = "{\"idPoubelle\": \"" +
                              modulesPoubelles.get(numeroPoubelle).getIdModule() +
                              "\",\"etat\": false}";
                communication.emettreRequetePATCH(api, json, handler);
            }
        }
    }

    private void recupererEtats()
    {
        Log.d(TAG, "recupererEtats()");
        tacheRecuperationEtats = new TimerTask() {
            public void run()
            {
                communication.emettreRequeteGET(API_GET_POUBELLES, handler);
            }
        };

        // toutes les secondes (en attente de l'implémentation de la websocket)
        minuteur.schedule(tacheRecuperationEtats, INTERVALLE, INTERVALLE);
    }

    public void traiterReponseJSON(String reponse)
    {
        Log.d(TAG, "traiterReponseJSON() reponse = " + reponse);
        /*
            Exemple de réponsee : pour la requête GET /poubelles
            body =
            [
                {"idPoubelle":1,"couleur":"rouge","etat":false,"actif":true},
                {"idPoubelle":2,"couleur":"jaune","etat":false,"actif":true},
                {"idPoubelle":3,"couleur":"bleu","etat":false,"actif":true},
                {"idPoubelle":4,"couleur":"gris","etat":false,"actif":true},
                {"idPoubelle":5,"couleur":"vert","etat":false,"actif":true}
            ]
        */
        JSONArray json = null;

        try
        {
            json = new JSONArray(reponse);
            for(int i = 0; i < json.length(); ++i)
            {
                JSONObject poubelle   = json.getJSONObject(i);
                int        idPoubelle = poubelle.getInt("idPoubelle");
                String     couleur    = poubelle.getString("couleur");
                Boolean    etat       = poubelle.getBoolean("etat");
                Boolean    actif      = poubelle.getBoolean("actif");
                Log.d(TAG,
                      "traiterReponseJSON() idPoubelle = " + idPoubelle + " couleur = " + couleur +
                        " etat = " + etat + " actif = " + actif);
                for(int j = 0; j < modulesPoubelles.size(); ++j)
                {
                    Module module = modulesPoubelles.get(j);
                    if(module.getIdModule() == idPoubelle &&
                       module.getTypeModule() == Module.TypeModule.Poubelle)
                    {
                        module.setCouleur(couleur);
                        module.setActif(actif);
                        module.setEtatNotification(etat);
                        mettreAJourModule(j);
                        break;
                    }
                }
            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void enregistrerAcquittementNotification(String reponse)
    {
        Log.d(TAG, "enregistrerAcquittementNotification() reponse = " + reponse);
        /*
            Exemple de réponsee : pour la requête PATCH /poubelles/1
            body =
            [
                {"idPoubelle":1,"couleur":"rouge","etat":false,"actif":true}
            ]
        */
        JSONArray json = null;

        try
        {
            json = new JSONArray(reponse);
            if(json.length() > 0)
            {
                JSONObject poubelle   = json.getJSONObject(0);
                int        idPoubelle = poubelle.getInt("idPoubelle");
                String     couleur    = poubelle.getString("couleur");
                Boolean    etat       = poubelle.getBoolean("etat");
                Boolean    actif      = poubelle.getBoolean("actif");
                Log.d(TAG,
                      "enregistrerAcquittementNotification() idPoubelle = " + idPoubelle +
                        " couleur = " + couleur + " etat = " + etat + " actif = " + actif);
                for(int i = 0; i < modulesPoubelles.size(); ++i)
                {
                    Module module = modulesPoubelles.get(i);
                    if(module.getIdModule() == idPoubelle &&
                       module.getTypeModule() == Module.TypeModule.Poubelle)
                    {
                        baseDeDonnees.enregistrerAcquittementNotification(module.getIdModule(),
                                                                          module.getTypeModule().ordinal(),
                                                                          true);
                        break;
                    }
                }
            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void mettreAJourModule(int numeroPoubelle)
    {
        if(modulesPoubelles.get(numeroPoubelle) == null)
        {
            Log.e(TAG, "Aucune poubelle");
            return;
        }

        Module module = modulesPoubelles.get(numeroPoubelle);

        if(module.estActif())
        {
            if(module.estNotifie())
            {
                imagesNotificationPoubelles[numeroPoubelle].setVisibility(View.VISIBLE);

                // On signale une notification sur la tablette Android
                creerNotification("Le module " + module.getNomModule() + " a une notification.");
            }
            else
            {
                imagesNotificationPoubelles[numeroPoubelle].setVisibility(View.INVISIBLE);
            }

            boutonsActivation[numeroPoubelle].setChecked(true);
            imagesNotificationPoubelles[numeroPoubelle].setEnabled(true);
            imagesPoubelles[numeroPoubelle].setEnabled(true);

            // Toast.makeText(getApplicationContext(), "Module activé", Toast.LENGTH_SHORT).show();
        }
        else
        {
            boutonsActivation[numeroPoubelle].setChecked(false);
            imagesNotificationPoubelles[numeroPoubelle].setEnabled(false);
            imagesPoubelles[numeroPoubelle].setEnabled(false);

            // Toast.makeText(getApplicationContext(), "Module désactivé",
            // Toast.LENGTH_SHORT).show();
        }
    }

    private void creerNotification(String message)
    {
        NotificationManager notificationManager =
          (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        String titreNotification = getNomApplication(getApplicationContext());
        String texteNotification = message;

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                                                    .setSmallIcon(R.mipmap.ic_launcher)
                                                    .setContentTitle(titreNotification)
                                                    .setContentText(texteNotification);

        // On pourrait ici crée une autre activité
        PendingIntent pendingIntent =
          PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentIntent(pendingIntent);

        notification.setAutoCancel(true);

        notification.setVibrate(new long[] { 0, 200, 100, 200, 100, 200 });

        notificationManager.notify(idNotification++, notification.build());
    }

    public static String getNomApplication(Context context)
    {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    private void afficherErreur(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
