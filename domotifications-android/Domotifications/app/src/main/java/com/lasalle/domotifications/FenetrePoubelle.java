package com.lasalle.domotifications;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class FenetrePoubelle extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG                 = "_FenetrePoubelle"; //!< TAG pour les logs
    private static final String API_PATCH_POUBELLES = "/poubelles"; //!< Pour une requête PATCH
    private static final String API_POST_POUBELLES  = "/poubelles"; //!< Pour une requête PATCH
    private static final int    INTERVALLE          = 1000; //!< Intervalle d'interrogation en ms
    public static final int     NB_POUBELLES_MAX    = 5;    //!< Nombre max de poubelles
    private static final int    PARAMETRAGE_MODULE =
      1; //!< Code pour l'activité de paramètrage du module

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
    private boolean               erreurCommunication = false;
    private Map<Integer, Boolean> notificationsEnvoyees =
      new HashMap<>(); //<! Pour la signalisation des notifications
    int            numeroPoubelleAcquittement = -1;
    private String nomAjoutModule;

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
    private Switch[] boutonsActivation;        //!< Boutons d'activation/désactivation des modules
                                               //!< poubelles
    private ImageView boutonAjouterModule;     //!< Bouton pour ajouter les modules poubelles
    private ImageView[] boutonSupprimerModule; //!< Bouton pour supprimer les modules poubelles
    private ImageView[] imagesParametres;      //!< Images des couleurs des modules

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
        ViewCompat.setOnApplyWindowInsetsListener(
          findViewById(R.id.fenetrePoubelle),
          (v, insets) -> {
              Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
              v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
              return insets;
          });
        Log.d(TAG, "initialiserGUI()");

        boutonAccueil = (ImageButton)findViewById(R.id.boutonAccueil);

        imagesPoubelles    = new ImageView[NB_POUBELLES_MAX];
        imagesPoubelles[0] = (ImageView)findViewById(R.id.poubelle0);
        imagesPoubelles[1] = (ImageView)findViewById(R.id.poubelle1);
        imagesPoubelles[2] = (ImageView)findViewById(R.id.poubelle2);
        imagesPoubelles[3] = (ImageView)findViewById(R.id.poubelle3);
        imagesPoubelles[4] = (ImageView)findViewById(R.id.poubelle4);

        imagesNotificationPoubelles    = new ImageView[NB_POUBELLES_MAX];
        imagesNotificationPoubelles[0] = (ImageView)findViewById(R.id.notificationPoubelle0);
        imagesNotificationPoubelles[1] = (ImageView)findViewById(R.id.notificationPoubelle1);
        imagesNotificationPoubelles[2] = (ImageView)findViewById(R.id.notificationPoubelle2);
        imagesNotificationPoubelles[3] = (ImageView)findViewById(R.id.notificationPoubelle3);
        imagesNotificationPoubelles[4] = (ImageView)findViewById(R.id.notificationPoubelle4);

        boutonsActivation    = new Switch[NB_POUBELLES_MAX];
        boutonsActivation[0] = (Switch)findViewById(R.id.activationPoubelle0);
        boutonsActivation[1] = (Switch)findViewById(R.id.activationPoubelle1);
        boutonsActivation[2] = (Switch)findViewById(R.id.activationPoubelle2);
        boutonsActivation[3] = (Switch)findViewById(R.id.activationPoubelle3);
        boutonsActivation[4] = (Switch)findViewById(R.id.activationPoubelle4);

        imagesParametres    = new ImageView[NB_POUBELLES_MAX];
        imagesParametres[0] = (ImageView)findViewById(R.id.couleurPoubelle0);
        imagesParametres[1] = (ImageView)findViewById(R.id.couleurPoubelle1);
        imagesParametres[2] = (ImageView)findViewById(R.id.couleurPoubelle2);
        imagesParametres[3] = (ImageView)findViewById(R.id.couleurPoubelle3);
        imagesParametres[4] = (ImageView)findViewById(R.id.couleurPoubelle4);

        boutonSupprimerModule    = new ImageView[NB_POUBELLES_MAX];
        boutonSupprimerModule[0] = (ImageView)findViewById(R.id.boutonSupprimerPoubelle0);
        boutonSupprimerModule[1] = (ImageView)findViewById(R.id.boutonSupprimerPoubelle1);
        boutonSupprimerModule[2] = (ImageView)findViewById(R.id.boutonSupprimerPoubelle2);
        boutonSupprimerModule[3] = (ImageView)findViewById(R.id.boutonSupprimerPoubelle3);
        boutonSupprimerModule[4] = (ImageView)findViewById(R.id.boutonSupprimerPoubelle4);

        boutonAjouterModule = (ImageView)findViewById(R.id.boutonAjouterModule);

        for(int i = 0; i < NB_POUBELLES_MAX; ++i)
        {
            final int numeroPoubelle = i;

            imagesParametres[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(FenetrePoubelle.this, ParametresModule.class);
                    intent.putExtra("idModule", modulesPoubelles.get(numeroPoubelle).getIdModule());
                    intent.putExtra("nom", modulesPoubelles.get(numeroPoubelle).getNomModule());
                    intent.putExtra("couleur", modulesPoubelles.get(numeroPoubelle).getCouleur());
                    startActivityForResult(intent, PARAMETRAGE_MODULE);
                }
            });

            imagesPoubelles[i].setImageResource(IMAGES_POUBELLES[i]);

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

            boutonSupprimerModule[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Log.d(TAG, "afficherBoiteDialogueSuppressionModule()");
                    afficherBoiteDialogueSuppressionModule(numeroPoubelle);
                }
            });
        }

        for(int i = 0; i < NB_POUBELLES_MAX; ++i)
        {
            cacherPoubelle(i);
        }

        for(int i = 0; i < nbModulesPoubelles; ++i)
        {
            afficherPoubelle(i);
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

        boutonAjouterModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                afficherBoiteDialogueAjoutModule();
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
                        // Log.d(TAG, "[Handler] REPONSE JSON");
                        traiterReponseJSON(message.obj.toString());
                        erreurCommunication = false;
                        break;
                    case Communication.CODE_HTTP_REPONSE_PATCH:
                        Log.d(TAG, "[Handler] REPONSE PATCH");
                        traiterReponseJSON(message.obj.toString());
                        enregistrerAcquittementNotification(message.obj.toString());
                        erreurCommunication = false;
                        break;
                    case Communication.CODE_HTTP_REPONSE_POST:
                        Log.d(TAG, "[Handler] REPONSE POST");
                        validerAjoutPoubelle(message.obj.toString());
                        erreurCommunication = false;
                        break;
                    case Communication.CODE_HTTP_REPONSE_DELETE:
                        Log.d(TAG, "[Handler] REPONSE POST");
                        validerSuppressionPoubelle(message.obj.toString());
                        erreurCommunication = false;
                        break;
                    case Communication.CODE_HTTP_ERREUR:
                        if(!erreurCommunication)
                        {
                            Log.d(TAG, "[Handler] ERREUR HTTP");
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

        String api = API_PATCH_POUBELLES + "/" + modulesPoubelles.get(numeroPoubelle).getIdModule();

        String json = "{\"idPoubelle\": \"" + modulesPoubelles.get(numeroPoubelle).getIdModule() +
                      "\",\"actif\": " + boutonsActivation[numeroPoubelle].isChecked() + "}";

        communication.emettreRequetePATCH(api, json, handler);

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
                numeroPoubelleAcquittement = numeroPoubelle;
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
                communication.emettreRequeteGET(Communication.API_GET_POUBELLES, handler);
            }
        };

        // toutes les secondes (en attente de l'implémentation de la websocket)
        minuteur.schedule(tacheRecuperationEtats, INTERVALLE, INTERVALLE);
    }

    public void traiterReponseJSON(String reponse)
    {
        // Log.d(TAG, "traiterReponseJSON() reponse = " + reponse);
        /*
            Exemple de réponsee : pour la requête GET /poubelles
            body =
            [
                {"idPoubelle":1,"couleur":"#FF0000","etat":false,"actif":true},
                {"idPoubelle":2,"couleur":"#FFFF00","etat":false,"actif":true},
                {"idPoubelle":3,"couleur":"#0000FF","etat":false,"actif":true},
                {"idPoubelle":4,"couleur":"#F0F0F2","etat":false,"actif":true},
                {"idPoubelle":5,"couleur":"#00FF00","etat":false,"actif":true}
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
                /*Log.d(TAG,
                      "traiterReponseJSON() idPoubelle = " + idPoubelle + " couleur = " + couleur +
                        " etat = " + etat + " actif = " + actif);*/
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

    public void validerAjoutPoubelle(String reponse)
    {
        Log.d(TAG, "validerAjoutPoubelle() reponse = " + reponse);
        /*
            Exemple de réponse : pour la requête POST
            body =
            [
                {"idPoubelle":5,"couleur":"#FF0000","etat":false,"actif":false}
            ]
        */
        JSONArray json = null;

        try
        {
            json                  = new JSONArray(reponse);
            JSONObject poubelle   = json.getJSONObject(0);
            int        idPoubelle = poubelle.getInt("idPoubelle");
            String     couleur    = poubelle.getString("couleur");
            Boolean    etat       = poubelle.getBoolean("etat");
            Boolean    actif      = poubelle.getBoolean("actif");
            Log.d(TAG,
                  "validerAjoutPoubelle() idPoubelle = " + idPoubelle + " couleur = " + couleur +
                    " etat = " + etat + " actif = " + actif);
            Module module = new Module(idPoubelle,
                                       nomAjoutModule,
                                       Module.TypeModule.Poubelle,
                                       actif,
                                       etat,
                                       couleur,
                                       baseDeDonnees);
            modulesPoubelles.add(module);
            baseDeDonnees.insererModule(module.getIdModule(),
                                        module.getTypeModule().ordinal() + 1,
                                        module.getNomModule(),
                                        module.estActif(),
                                        module.getCouleur());
            afficherPoubelle(nbModulesPoubelles);
            int numeroPoubelle = getNumeroPoubelle(module.getIdModule());
            mettreAJourModule(numeroPoubelle);
            nomAjoutModule     = "";
            nbModulesPoubelles = modulesPoubelles.size();
            Log.d(TAG, "validerAjoutPoubelle() nbModulesPoubelles = " + nbModulesPoubelles);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void validerSuppressionPoubelle(String reponse)
    {
        Log.d(TAG, "validerSuppressionPoubelle() reponse = " + reponse);
        /*
            Exemple de réponse : pour la requête DELETE
            body =
            [
                {"idPoubelle":5}
            ]
        */
        JSONArray json = null;

        try
        {
            json                  = new JSONArray(reponse);
            JSONObject poubelles  = json.getJSONObject(0);
            int        idPoubelle = poubelles.getInt("idPoubelle");
            Log.d(TAG, "validerSuppressionPoubelle() idPoubelle = " + idPoubelle);
            int numeroPoubelle = getNumeroPoubelle(idPoubelle);
            if(numeroPoubelle == -1)
            {
                Log.d(TAG, "validerSuppressionPoubelle() idPoubelle introuvable !");
                return;
            }
            Module module = modulesPoubelles.get(numeroPoubelle);
            baseDeDonnees.supprimerModule(idPoubelle, module.getTypeModule().ordinal() + 1);
            modulesPoubelles.remove(module);
            cacherPoubelle(numeroPoubelle);
            nbModulesPoubelles = modulesPoubelles.size();
            Log.d(TAG, "validerSuppressionPoubelle() nbModulesPoubelles = " + nbModulesPoubelles);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void enregistrerAcquittementNotification(String reponse)
    {
        if(numeroPoubelleAcquittement == -1)
        {
            return;
        }
        if(modulesPoubelles.get(numeroPoubelleAcquittement) == null)
        {
            Log.e(TAG, "enregistrerAcquittementNotification() Aucune poubelle !");
            return;
        }
        Log.d(TAG, "enregistrerAcquittementNotification() reponse = " + reponse);
        /*
            Exemple de réponsee : pour la requête PATCH /poubelles/1
            body =
            [
                {"idPoubelle":1,"couleur":"#FF0000","etat":false,"actif":true}
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

                Module module = modulesPoubelles.get(numeroPoubelleAcquittement);
                if(module.getIdModule() == idPoubelle &&
                   module.getTypeModule() == Module.TypeModule.Poubelle && !etat)
                {
                    baseDeDonnees.enregistrerAcquittementNotification(
                      module.getIdModule(),
                      module.getTypeModule().ordinal(),
                      true);
                    numeroPoubelleAcquittement = -1;
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

        Module module     = modulesPoubelles.get(numeroPoubelle);
        int    idPoubelle = module.getIdModule();

        if(module.estActif())
        {
            if(module.estNotifie())
            {
                imagesNotificationPoubelles[numeroPoubelle].setVisibility(View.VISIBLE);
                Boolean notificationEnvoyee = notificationsEnvoyees.get(idPoubelle);
                if(notificationEnvoyee == null || !notificationEnvoyee)
                {
                    // On signale une notification sur la tablette Android
                    creerNotification("La poubelle " + module.getNomModule() +
                                      " a une notification.");
                    notificationsEnvoyees.put(idPoubelle, true);
                }
            }
            else
            {
                imagesNotificationPoubelles[numeroPoubelle].setVisibility(View.INVISIBLE);
                notificationsEnvoyees.put(idPoubelle, false);
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
        String titreNotification = getNomApplication(getApplicationContext());
        String texteNotification = message;

        NotificationManager notificationManager =
          (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // Si >= API26, obligation de créer un canal de notifications
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence        name        = getString(R.string.app_name);
            String              description = "Notification domotifications";
            int                 importance  = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel =
              new NotificationChannel("domotifications_id", name, importance);
            channel.setDescription(description);
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager = (NotificationManager)getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notification =
          new NotificationCompat.Builder(this, "domotifications_id")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titreNotification)
            .setContentText(texteNotification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // On pourrait ici crée une autre activité
        PendingIntent pendingIntent =
          PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);

        notification.setContentIntent(pendingIntent);
        notification.setAutoCancel(true);
        notification.setVibrate(new long[] { 0, 200, 100, 200, 100, 200 });

        // Si >= API26
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            notificationManager.notify(idNotification, notification.build());
        }
        else
        {
            NotificationManagerCompat.from(this).notify(idNotification, notification.build());
        }
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

    private boolean verifierPermissionNotification()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
           PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG,
                  "verifierPermissionNotification() permission notification = PERMISSION_GRANTED");
            return true;
        }
        else
        {
            Log.d(TAG,
                  "verifierPermissionNotification() permission notification = PERMISSION_DENIED");
            ActivityCompat.requestPermissions(
              this,
              new String[] { Manifest.permission.POST_NOTIFICATIONS },
              101);
        }
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,
              "onActivityResult() requestCode = " + requestCode + " - resultCode = " + resultCode);
        if(requestCode == PARAMETRAGE_MODULE)
        {
            if(resultCode == RESULT_OK)
            {
                if(data != null)
                {
                    int    idModule      = data.getIntExtra("idModule", -1);
                    String nomModule     = data.getStringExtra("nom");
                    String couleurModule = data.getStringExtra("couleur");
                    Log.d(TAG,
                          "onActivityResult() idModule = " + idModule +
                            " - nomModule : " + nomModule + " - couleurModule = " + couleurModule);

                    if(idModule != -1)
                    {
                        int    numeroModule = getNumeroPoubelle(idModule);
                        Module module       = modulesPoubelles.get(numeroModule);
                        if(module != null)
                        {
                            if(!module.getNomModule().equals(nomModule))
                            {
                                module.setNomModule(nomModule);
                                baseDeDonnees.modifierNomModule(module.getIdModule(),
                                                                module.getTypeModule().ordinal() +
                                                                  1,
                                                                nomModule);
                            }
                            module.setCouleur(couleurModule);
                        }
                    }

                    String api  = API_PATCH_POUBELLES + "/" + idModule;
                    String json = "{\"idPoubelle\": \"" + idModule + "\",\"couleur\": \"" +
                                  couleurModule + "\"}";
                    communication.emettreRequetePATCH(api, json, handler);
                }
            }
        }
    }

    private void afficherBoiteDialogueAjoutModule()
    {
        Log.d(TAG, "afficherBoiteDialogueAjoutModule()");
        AlertDialog.Builder ajoutModule     = new AlertDialog.Builder(this);
        LayoutInflater      factory         = LayoutInflater.from(this);
        final View          ajoutModuleView = factory.inflate(R.layout.ajout_module, null);
        ajoutModule.setView(ajoutModuleView);
        ajoutModule.setTitle("Ajouter un nouveau module");
        ajoutModule.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                // Lorsque l'on cliquera sur le bouton "OK", on récupère l'EditText correspondant à
                // la vue personnalisée (cad à ajoutModuleView)
                EditText nomModule = (EditText)ajoutModuleView.findViewById(R.id.editTextNom);
                if(!nomModule.getText().toString().isEmpty())
                    nomAjoutModule = nomModule.getText().toString().trim();
                else
                    nomAjoutModule = "poubelle";
                Log.d(TAG, "afficherBoiteDialogueAjoutModule() nomModule = " + nomAjoutModule);
                TextInputEditText couleurModule =
                  (TextInputEditText)ajoutModuleView.findViewById(R.id.editTextCouleurHTML);
                String couleurAjoutModule = "#00FF00";
                if(!couleurModule.getText().toString().trim().isEmpty())
                    couleurAjoutModule = couleurModule.getText().toString().trim();
                Log.d(TAG,
                      "afficherBoiteDialogueAjoutModule() couleurAjoutModule = " +
                        couleurAjoutModule);

                // Préparer et émettre la requête POST à la station
                int idPoubelle = rechercherIdDisponible();
                Log.d(TAG, "afficherBoiteDialogueAjoutModule() idPoubelle = " + idPoubelle);
                // si idPoubelle = 0 alors la station choisira l'idPoubelle à ajouter sinon c'est
                // l'application qui le détermine

                // Mode démo
                if(idPoubelle > 0)
                {
                    String api  = API_PATCH_POUBELLES;
                    String json = "{\"idPoubelle\": " + idPoubelle + ", \"couleur\": \"" +
                                  couleurAjoutModule + "\", \"actif\": true"
                                  + "}";
                    communication.emettreRequetePOST(api, json, handler);

                    // Notifier l'utilisateur
                    Toast
                      .makeText(getApplicationContext(),
                                "Demande d'ajout du module '" + nomAjoutModule + "' envoyée",
                                Toast.LENGTH_SHORT)
                      .show();

                    // Mode démo
                    String reponseJson = "{\"idPoubelle\": " + idPoubelle + ", \"couleur\": \"" +
                                         couleurAjoutModule + "\", \"etat\": false, \"actif\": true"
                                         + "}";
                    validerAjoutPoubelle("[" + reponseJson + "]");
                }
            }
        });
        ajoutModule.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });

        AlertDialog alert = ajoutModule.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void afficherBoiteDialogueSuppressionModule(int numeroPoubelle)
    {
        if(modulesPoubelles.get(numeroPoubelle) == null)
        {
            Log.e(TAG, "afficherBoiteDialogueSuppressionModule() Aucune poubelle !");
            return;
        }

        String nomModule = modulesPoubelles.get(numeroPoubelle).getNomModule();

        AlertDialog.Builder boiteSuppression = new AlertDialog.Builder(this);
        boiteSuppression.setMessage("Vous êtes sur le point de supprimer le module '" + nomModule +
                                    "'.");
        boiteSuppression.setPositiveButton("SUPPRIMER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                // Emettre la requête DELETE à la station
                String api =
                  API_PATCH_POUBELLES + "/" + modulesPoubelles.get(numeroPoubelle).getIdModule();
                String json =
                  "{\"idPoubelle\": " + modulesPoubelles.get(numeroPoubelle).getIdModule() + "}";
                communication.emettreRequeteDELETE(api, json, handler);

                // Notifier l'utilisateur
                Toast
                  .makeText(getApplicationContext(),
                            "Demande de suppression du module '" + nomModule + "' envoyée",
                            Toast.LENGTH_SHORT)
                  .show();

                // Mode démo
                validerSuppressionPoubelle("[" + json + "]");
            }
        });
        boiteSuppression.setNegativeButton("ANNULER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });

        AlertDialog alert = boiteSuppression.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void cacherPoubelle(int numeroPoubelle)
    {
        imagesPoubelles[numeroPoubelle].setVisibility(View.INVISIBLE);
        imagesNotificationPoubelles[numeroPoubelle].setVisibility(View.INVISIBLE);
        boutonsActivation[numeroPoubelle].setVisibility(View.INVISIBLE);
        imagesParametres[numeroPoubelle].setVisibility(View.INVISIBLE);
        boutonSupprimerModule[numeroPoubelle].setVisibility(View.INVISIBLE);
    }

    private void afficherPoubelle(int numeroPoubelle)
    {
        imagesPoubelles[numeroPoubelle].setVisibility(View.VISIBLE);
        imagesNotificationPoubelles[numeroPoubelle].setVisibility(View.VISIBLE);
        boutonsActivation[numeroPoubelle].setVisibility(View.VISIBLE);
        imagesParametres[numeroPoubelle].setVisibility(View.VISIBLE);
        boutonSupprimerModule[numeroPoubelle].setVisibility(View.VISIBLE);
    }

    private int getNumeroPoubelle(int idPoubelle)
    {
        for(int i = 0; i < modulesPoubelles.size(); ++i)
        {
            Module module = modulesPoubelles.get(i);
            if(module.getIdModule() == idPoubelle)
            {
                return i;
            }
        }
        return -1;
    }

    private int rechercherIdDisponible()
    {
        for(int id = 1; id <= NB_POUBELLES_MAX; id++)
        {
            if(!estPresent(id))
                return id;
        }
        return 0; // pas d'id de disponible
    }

    private boolean estPresent(int id)
    {
        for(int i = 0; i < modulesPoubelles.size(); i++)
        {
            if(id == modulesPoubelles.get(i).getIdModule())
                return true;
        }
        return false;
    }
}
