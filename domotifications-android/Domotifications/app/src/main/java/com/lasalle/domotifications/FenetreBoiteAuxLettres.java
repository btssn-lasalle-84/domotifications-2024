package com.lasalle.domotifications;

import android.Manifest;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class FenetreBoiteAuxLettres extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG              = "_FenetreBoiteAuxLettres"; //!< TAG pour les logs
    private static final String API_PATCH_BOITES = "/boites"; //!< Pour une requête PATCH
    private static final String API_POST_BOITES  = "/boites"; //!< Pour une requête POST

    private static final int INTERVALLE            = 1000; //!< Intervalle d'interrogation en ms
    private static final int NB_MODULES_BOITES_MAX = 4;
    private static final int PARAMETRAGE_MODULE =
      1; //!< Code pour l'activité de paramètrage du module

    /**
     * Attributs
     */
    private BaseDeDonnees  baseDeDonnees;             //!< Association avec la base de donnees
    private Vector<Module> modulesBoitesAuxLettres;   //!< Conteneur des modules boites aux Lettres
    private int            nbModulesBoitesAuxLettres; //!< le nombre de boites gérées
    private int            idNotification = 0; //!< Identifiant unique pour chaque notification
    private Communication  communication;      //!< Association avec la classe Communication
    private Handler        handler =
      null; //!< Handler permettant la communication entre la classe Communication et l'activité
    private Timer minuteur =
      null; //!< Pour gérer la récupération des états des modules boites aux lettres
    private TimerTask tacheRecuperationEtats =
      null; //!< Pour effectuer la récupération des états des modules boites aux lettres
    private boolean               erreurCommunication = false;
    private Map<Integer, Boolean> notificationsEnvoyees =
      new HashMap<>(); //<! Pour la signalisation des notifications
    private int    numeroBoiteAcquittement = -1;
    private String nomAjoutModule;

    /**
     * GUI
     */

    public static final int IMAGE_BOITES =
      R.drawable
        .boite_aux_lettres; //!< Id de l'image de la boîte au lettre dans les ressources Android
    private ImageView[] imagesBoites;             //!< Images des boites aux lettres
    private ImageView[] imagesNotificationBoites; //!< Images des notifications des boites aux
    //!< lettres
    private Switch[] boutonsActivation; //!< Boutons d'activation/désactivation des modules
    //!< boitesAuxLettres
    private ImageButton boutonAccueil;         //!< Boutons d'activation/désactivation des modules
                                               //!< boites
    private ImageView boutonAjouterModule;     //!!< Boutons d'ajout des modules
    private ImageView[] boutonSupprimerModule; //!!< Boutons de suppression des modules
    private ImageView[] imagesParametres;      //!< Images des couleurs des modules

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        initialiserBaseDeDonnees();
        initialiserModulesBoites();
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

    private void initialiserModulesBoites()
    {
        Log.d(TAG, "initialiserModulesBoites()");
        modulesBoitesAuxLettres   = baseDeDonnees.getBoites();
        nbModulesBoitesAuxLettres = baseDeDonnees.getNbModulesBoites();
        Log.d(TAG, "nbModulesBoitesAuxLettres = " + nbModulesBoitesAuxLettres);
    }

    /**
     * @brief Initialise les ressources graphiques de l'activité
     */
    private void initialiserGUI()
    {
        // contenu bord à bord
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_boite_aux_lettres);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.machine1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        boutonAccueil = (ImageButton)findViewById(R.id.boutonAccueil);

        imagesBoites    = new ImageView[NB_MODULES_BOITES_MAX];
        imagesBoites[0] = (ImageView)findViewById(R.id.boiteAuxLettres0);
        imagesBoites[1] = (ImageView)findViewById(R.id.boiteAuxLettres1);
        imagesBoites[2] = (ImageView)findViewById(R.id.boiteAuxLettres2);
        imagesBoites[3] = (ImageView)findViewById(R.id.boiteAuxLettres3);

        imagesNotificationBoites    = new ImageView[NB_MODULES_BOITES_MAX];
        imagesNotificationBoites[0] = (ImageView)findViewById(R.id.notificationBoite0);
        imagesNotificationBoites[1] = (ImageView)findViewById(R.id.notificationBoite1);
        imagesNotificationBoites[2] = (ImageView)findViewById(R.id.notificationBoite2);
        imagesNotificationBoites[3] = (ImageView)findViewById(R.id.notificationBoite3);

        boutonsActivation    = new Switch[NB_MODULES_BOITES_MAX];
        boutonsActivation[0] = (Switch)findViewById(R.id.activationBoite0);
        boutonsActivation[1] = (Switch)findViewById(R.id.activationBoite1);
        boutonsActivation[2] = (Switch)findViewById(R.id.activationBoite2);
        boutonsActivation[3] = (Switch)findViewById(R.id.activationBoite3);

        imagesParametres    = new ImageView[NB_MODULES_BOITES_MAX];
        imagesParametres[0] = (ImageView)findViewById(R.id.couleurBoite0);
        imagesParametres[1] = (ImageView)findViewById(R.id.couleurBoite1);
        imagesParametres[2] = (ImageView)findViewById(R.id.couleurBoite2);
        imagesParametres[3] = (ImageView)findViewById(R.id.couleurBoite3);

        boutonSupprimerModule    = new ImageView[NB_MODULES_BOITES_MAX];
        boutonSupprimerModule[0] = (ImageView)findViewById(R.id.boutonSupprimerBoite0);
        boutonSupprimerModule[1] = (ImageView)findViewById(R.id.boutonSupprimerBoite1);
        boutonSupprimerModule[2] = (ImageView)findViewById(R.id.boutonSupprimerBoite2);
        boutonSupprimerModule[3] = (ImageView)findViewById(R.id.boutonSupprimerBoite3);

        boutonAjouterModule = (ImageView)findViewById(R.id.boutonAjouterModule);

        for(int i = 0; i < NB_MODULES_BOITES_MAX; ++i)
        {
            final int numeroBoite = i;

            imagesParametres[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(FenetreBoiteAuxLettres.this, ParametresModule.class);
                    intent.putExtra("idModule",
                                    modulesBoitesAuxLettres.get(numeroBoite).getIdModule());
                    intent.putExtra("nom", modulesBoitesAuxLettres.get(numeroBoite).getNomModule());
                    intent.putExtra("couleur",
                                    modulesBoitesAuxLettres.get(numeroBoite).getCouleur());
                    startActivityForResult(intent, PARAMETRAGE_MODULE);
                }
            });

            imagesBoites[i].setImageResource(IMAGE_BOITES);

            imagesNotificationBoites[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    gererClicBoutonNotification(numeroBoite);
                }
            });
            imagesBoites[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    gererClicBoutonNotification(numeroBoite);
                }
            });

            boutonSupprimerModule[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Log.d(TAG, "afficherBoiteDialogueSuppressionModule()");
                    afficherBoiteDialogueSuppressionModule(numeroBoite);
                }
            });
        }

        for(int i = 0; i < NB_MODULES_BOITES_MAX; ++i)
        {
            cacherBoiteAuxLettres(i);
        }

        for(int i = 0; i < nbModulesBoitesAuxLettres; ++i)
        {
            afficherBoiteAuxLettres(i);
        }

        for(int i = 0; i < nbModulesBoitesAuxLettres; i++)
        {
            final int numeroBoite = i;

            // initialise la GUI en fonction de l'état de notification
            if(modulesBoitesAuxLettres.get(i).estNotifie())
            {
                imagesNotificationBoites[i].setVisibility(View.VISIBLE);
            }
            else
            {
                imagesNotificationBoites[i].setVisibility(View.INVISIBLE);
            }

            // initialise la GUI en fonction de l'état d'activation
            if(modulesBoitesAuxLettres.get(i).estActif())
            {
                boutonsActivation[i].setChecked(true);
                imagesNotificationBoites[i].setEnabled(true);
                imagesBoites[i].setEnabled(true);
            }
            else
            {
                boutonsActivation[i].setChecked(false);
                imagesNotificationBoites[i].setEnabled(false);
                imagesBoites[i].setEnabled(false);
            }

            boutonsActivation[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    gererClicBoutonActivation(numeroBoite);
                }
            });
        }

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
                        //Log.d(TAG, "[Handler] REPONSE JSON");
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
                        validerAjoutBoite(message.obj.toString());
                        erreurCommunication = false;
                        break;
                    case Communication.CODE_HTTP_REPONSE_DELETE:
                        Log.d(TAG, "[Handler] REPONSE POST");
                        validerSuppressionBoite(message.obj.toString());
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

    private void gererClicBoutonNotification(int numeroBoite)
    {
        if(modulesBoitesAuxLettres.get(numeroBoite) == null)
        {
            Log.e(TAG, "gererClicBoutonNotification() Aucune boîte aux lettres !");
            return;
        }
        Log.d(TAG,
              "gererClicBoutonNotification() numeroBoite = " + numeroBoite +
                " idBoite = " + modulesBoitesAuxLettres.get(numeroBoite).getIdModule() +
                " notification = " + modulesBoitesAuxLettres.get(numeroBoite).estNotifie() +
                " activation = " + modulesBoitesAuxLettres.get(numeroBoite).estActif());

        if(modulesBoitesAuxLettres.get(numeroBoite).estActif())
        {
            if(modulesBoitesAuxLettres.get(numeroBoite).estNotifie())
            {
                /*
                Exemple :
                $ curl --location 'http://station-lumineuse.local:80/boites/1' --request PATCH
                --header 'Content-Type: application/json' --data '{"idBoite": "1","etat": false}'
                */
                numeroBoiteAcquittement = numeroBoite;
                String api =
                  API_PATCH_BOITES + "/" + modulesBoitesAuxLettres.get(numeroBoite).getIdModule();
                String json = "{\"idBoite\": \"" +
                              modulesBoitesAuxLettres.get(numeroBoite).getIdModule() +
                              "\",\"etat\": false}";
                communication.emettreRequetePATCH(api, json, handler);
            }
        }
    }

    private void gererClicBoutonActivation(int numeroBoite)
    {
        if(modulesBoitesAuxLettres.get(numeroBoite) == null)
        {
            Log.e(TAG, "gererClicBoutonActivation() Aucune boîte aux lettres !");
            return;
        }
        Log.d(TAG,
              "gererClicBoutonActivation() numeroBoite = " + numeroBoite +
                " activation = " + boutonsActivation[numeroBoite].isChecked());

        String api =
          API_PATCH_BOITES + "/" + modulesBoitesAuxLettres.get(numeroBoite).getIdModule();

        String json = "{\"idBoite\": \"" + modulesBoitesAuxLettres.get(numeroBoite).getIdModule() +
                      "\",\"actif\": " + boutonsActivation[numeroBoite].isChecked() + "}";

        communication.emettreRequetePATCH(api, json, handler);

        modulesBoitesAuxLettres.get(numeroBoite)
          .setActif(boutonsActivation[numeroBoite].isChecked());

        if(modulesBoitesAuxLettres.get(numeroBoite).estActif())
        {
            imagesBoites[numeroBoite].setEnabled(true);
            imagesNotificationBoites[numeroBoite].setEnabled(true);
        }
        else
        {
            imagesBoites[numeroBoite].setEnabled(false);
            imagesNotificationBoites[numeroBoite].setEnabled(false);
        }
    }

    private void recupererEtats()
    {
        Log.d(TAG, "recupererEtats()");
        tacheRecuperationEtats = new TimerTask() {
            public void run()
            {
                communication.emettreRequeteGET(Communication.API_GET_BOITES, handler);
            }
        };

        // toutes les secondes (en attente de l'implémentation de la websocket)
        minuteur.schedule(tacheRecuperationEtats, INTERVALLE, INTERVALLE);
    }

    public void traiterReponseJSON(String reponse)
    {
        // Log.d(TAG, "traiterReponseJSON() reponse = " + reponse);
        /*
            Exemple de réponsee : pour la requête GET /boites
            body =
            [
                {"idBoite":1,"couleur":"#FF0000","etat":false,"actif":true},
                {"idBoite":2,"couleur":"#FFFF00","etat":false,"actif":true},
                {"idBoite":3,"couleur":"#0000FF","etat":false,"actif":true},
                {"idBoite":4,"couleur":"#F0F0F2","etat":false,"actif":true},
            ]
        */
        JSONArray json = null;

        try
        {
            json = new JSONArray(reponse);
            for(int i = 0; i < json.length(); ++i)
            {
                JSONObject boiteAuxLettres = json.getJSONObject(i);
                int        idBoite         = boiteAuxLettres.getInt("idBoite");
                String     couleur         = boiteAuxLettres.getString("couleur");
                Boolean    etat            = boiteAuxLettres.getBoolean("etat");
                Boolean    actif           = boiteAuxLettres.getBoolean("actif");
                /*Log.d(TAG,
                      "traiterReponseJSON() idBoite = " + idBoite + " couleur = " + couleur +
                        " etat = " + etat + " actif = " + actif);*/
                for(int j = 0; j < modulesBoitesAuxLettres.size(); ++j)
                {
                    Module module = modulesBoitesAuxLettres.get(j);
                    if(module.getIdModule() == idBoite &&
                       module.getTypeModule() == Module.TypeModule.BoiteAuxLettres)
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

    public void validerAjoutBoite(String reponse)
    {
        Log.d(TAG, "validerAjoutBoite() reponse = " + reponse);
        /*
            Exemple de réponse : pour la requête POST
            body =
            [
                {"idBoite":5,"couleur":"#FF0000","etat":false,"actif":false}
            ]
        */
        JSONArray json = null;

        try
        {
            json               = new JSONArray(reponse);
            JSONObject boite   = json.getJSONObject(0);
            int        idBoite = boite.getInt("idBoite");
            String     couleur = boite.getString("couleur");
            Boolean    etat    = boite.getBoolean("etat");
            Boolean    actif   = boite.getBoolean("actif");
            Log.d(TAG,
                  "validerAjoutBoite() idBoite = " + idBoite + " couleur = " + couleur +
                    " etat = " + etat + " actif = " + actif);
            Module module = new Module(idBoite,
                                       nomAjoutModule,
                                       Module.TypeModule.BoiteAuxLettres,
                                       actif,
                                       etat,
                                       couleur,
                                       baseDeDonnees);
            modulesBoitesAuxLettres.add(module);
            baseDeDonnees.insererModule(module.getIdModule(),
                                        module.getTypeModule().ordinal() + 1,
                                        module.getNomModule(),
                                        module.estActif(),
                                        module.getCouleur());
            afficherBoiteAuxLettres(nbModulesBoitesAuxLettres);
            int numeroBoite = getNumeroBoite(module.getIdModule());
            mettreAJourModule(numeroBoite);
            nomAjoutModule            = "";
            nbModulesBoitesAuxLettres = modulesBoitesAuxLettres.size();
            Log.d(TAG,
                  "validerAjoutBoite() nbModulesBoitesAuxLettres = " + nbModulesBoitesAuxLettres);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void validerSuppressionBoite(String reponse)
    {
        Log.d(TAG, "validerSuppressionBoite() reponse = " + reponse);
        /*
            Exemple de réponse : pour la requête DELETE
            body =
            [
                {"idBoite":5}
            ]
        */
        JSONArray json = null;

        try
        {
            json               = new JSONArray(reponse);
            JSONObject boites  = json.getJSONObject(0);
            int        idBoite = boites.getInt("idBoite");
            Log.d(TAG, "validerSuppressionBoite() idBoite = " + idBoite);
            int numeroBoite = getNumeroBoite(idBoite);
            if(numeroBoite == -1)
            {
                Log.d(TAG, "validerSuppressionBoite() idBoite introuvable !");
                return;
            }
            Module module = modulesBoitesAuxLettres.get(numeroBoite);
            baseDeDonnees.supprimerModule(idBoite, module.getTypeModule().ordinal() + 1);
            modulesBoitesAuxLettres.remove(module);
            cacherBoiteAuxLettres(numeroBoite);
            nbModulesBoitesAuxLettres = modulesBoitesAuxLettres.size();
            Log.d(TAG,
                  "validerSuppressionBoite() nbModulesBoitesAuxLettres = " +
                    nbModulesBoitesAuxLettres);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void enregistrerAcquittementNotification(String reponse)
    {
        if(numeroBoiteAcquittement == -1)
        {
            return;
        }
        if(modulesBoitesAuxLettres.get(numeroBoiteAcquittement) == null)
        {
            Log.e(TAG, "enregistrerAcquittementNotification() Aucune boîte aux lettres !");
            return;
        }
        Log.d(TAG, "enregistrerAcquittementNotification() reponse = " + reponse);
        /*
            Exemple de réponsee : pour la requête PATCH /boites/1
            body =
            [
                {"idBoite":1,"couleur":"#FF0000","etat":false,"actif":true}
            ]
        */
        JSONArray json = null;

        try
        {
            json = new JSONArray(reponse);
            if(json.length() > 0)
            {
                JSONObject boitesAuxLettres = json.getJSONObject(0);
                int        idBoite          = boitesAuxLettres.getInt("idBoite");
                String     couleur          = boitesAuxLettres.getString("couleur");
                Boolean    etat             = boitesAuxLettres.getBoolean("etat");
                Boolean    actif            = boitesAuxLettres.getBoolean("actif");
                Log.d(TAG,
                      "enregistrerAcquittementNotification() idBoitesAuxLettres = " + idBoite +
                        " couleur = " + couleur + " etat = " + etat + " actif = " + actif);

                Module module = modulesBoitesAuxLettres.get(numeroBoiteAcquittement);
                if(module.getIdModule() == idBoite &&
                   module.getTypeModule() == Module.TypeModule.BoiteAuxLettres && !etat)
                {
                    baseDeDonnees.enregistrerAcquittementNotification(
                      module.getIdModule(),
                      module.getTypeModule().ordinal(),
                      true);
                    numeroBoiteAcquittement = -1;
                }
            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void mettreAJourModule(int numeroBoite)
    {
        if(modulesBoitesAuxLettres.get(numeroBoite) == null)
        {
            Log.e(TAG, "Aucune boite");
            return;
        }

        Module module  = modulesBoitesAuxLettres.get(numeroBoite);
        int    idBoite = module.getIdModule();

        if(module.estActif())
        {
            if(module.estNotifie())
            {
                imagesNotificationBoites[numeroBoite].setVisibility(View.VISIBLE);
                Boolean notificationEnvoyee = notificationsEnvoyees.get(idBoite);
                if(notificationEnvoyee == null || !notificationEnvoyee)
                {
                    // On signale une notification sur la tablette Android
                    creerNotification("Le module " + module.getNomModule() +
                                      " a une notification.");
                    notificationsEnvoyees.put(idBoite, true);
                }
            }
            else
            {
                imagesNotificationBoites[numeroBoite].setVisibility(View.INVISIBLE);
                notificationsEnvoyees.put(idBoite, false);
            }

            boutonsActivation[numeroBoite].setChecked(true);
            imagesNotificationBoites[numeroBoite].setEnabled(true);
            imagesBoites[numeroBoite].setEnabled(true);

            // Toast.makeText(getApplicationContext(), "Module activé", Toast.LENGTH_SHORT).show();
        }
        else
        {
            boutonsActivation[numeroBoite].setChecked(false);
            imagesNotificationBoites[numeroBoite].setEnabled(false);
            imagesBoites[numeroBoite].setEnabled(false);

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
        if(ContextCompat.checkSelfPermission(this,
                                             android.Manifest.permission.POST_NOTIFICATIONS) ==
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
                        if(idModule >= 0 && idModule < modulesBoitesAuxLettres.size())
                        {
                            int    numeroModule = getNumeroBoite(idModule);
                            Module module = modulesBoitesAuxLettres.get(numeroModule);
                            if(!module.getNomModule().equals(nomModule))
                            {
                                module.setNomModule(nomModule);
                                baseDeDonnees.modifierNomModule(module.getIdModule(),
                                                                module.getTypeModule().ordinal(),
                                                                nomModule);
                            }
                            module.setCouleur(couleurModule);
                        }
                    }
                    String api = API_PATCH_BOITES + "/" + idModule;
                    String json =
                      "{\"idBoite\": \"" + idModule + "\",\"couleur\": \"" + couleurModule + "\"}";
                    communication.emettreRequetePATCH(api, json, handler);
                }
            }
        }
    }

    private void afficherBoiteDialogueAjoutModule()
    {
        Log.d(TAG, "afficherBoiteDialogueAjoutModule()");
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
                    nomAjoutModule = nomModule.getText().toString();
                else
                    nomAjoutModule = "boite";
                Log.d(TAG, "afficherBoiteDialogueAjoutModule() nomModule = " + nomAjoutModule);

                // @todo ajouter le choix de la couleur (cf. R.layout.ajout_module)

                // Emettre la requête POST à la station

                int idBoite = rechercherIdDisponible();
                Log.d(TAG, "afficherBoiteDialogueAjoutModule() idBoite = " + idBoite);
                // si idBoite= 0 alors la station choisira l'idBoite à ajouter sinon c'est
                // l'application qui le détermine

                // Mode démo
                if(idBoite > 0)
                {
                    String api  = API_PATCH_BOITES;
                    String json = "{\"idBoite\": " + idBoite +
                                  ", \"couleur\": \"#00FF00\", \"actif\": true"
                                  + "}";
                    communication.emettreRequetePOST(api, json, handler);

                    // Notifier l'utilisateur
                    Toast
                      .makeText(getApplicationContext(),
                                "Demande d'ajout du module '" + nomAjoutModule + "' envoyée",
                                Toast.LENGTH_SHORT)
                      .show();

                    // Mode démo
                    String reponseJson =
                      "{\"idBoite\": " + idBoite +
                      ", \"couleur\": \"#00FF00\", \"etat\": false, \"actif\": true"
                      + "}";
                    validerAjoutBoite("[" + reponseJson + "]");
                }
            }
        });
        ajoutModule.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });

        ajoutModule.show();
    }

    private void afficherBoiteDialogueSuppressionModule(int numeroBoite)
    {
        if(modulesBoitesAuxLettres.get(numeroBoite) == null)
        {
            Log.e(TAG, "afficherBoiteDialogueSuppressionModule() Aucune boîte aux lettres !");
            return;
        }
        String nomModule = modulesBoitesAuxLettres.get(numeroBoite).getNomModule();

        AlertDialog.Builder boiteSuppression = new AlertDialog.Builder(this);
        boiteSuppression.setMessage("Vous êtes sur le point de supprimer le module '" + nomModule +
                                    "'.");
        boiteSuppression.setPositiveButton("SUPPRIMER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                // Emettre la requête DELETE à la station
                String api =
                  API_PATCH_BOITES + "/" + modulesBoitesAuxLettres.get(numeroBoite).getIdModule();
                String json =
                  "{\"idBoite\": " + modulesBoitesAuxLettres.get(numeroBoite).getIdModule() + "}";
                communication.emettreRequeteDELETE(api, json, handler);

                // Notifier l'utilisateur
                Toast
                  .makeText(getApplicationContext(),
                            "Demande de suppression du module '" + nomModule + "' envoyée",
                            Toast.LENGTH_SHORT)
                  .show();

                // Mode démo
                validerSuppressionBoite("[" + json + "]");
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

    private void cacherBoiteAuxLettres(int numeroBoite)
    {
        imagesBoites[numeroBoite].setVisibility(View.INVISIBLE);
        imagesNotificationBoites[numeroBoite].setVisibility(View.INVISIBLE);
        boutonsActivation[numeroBoite].setVisibility(View.INVISIBLE);
        imagesParametres[numeroBoite].setVisibility(View.INVISIBLE);
        boutonSupprimerModule[numeroBoite].setVisibility(View.INVISIBLE);
    }

    private void afficherBoiteAuxLettres(int numeroBoite)
    {
        imagesBoites[numeroBoite].setVisibility(View.VISIBLE);
        imagesNotificationBoites[numeroBoite].setVisibility(View.VISIBLE);
        boutonsActivation[numeroBoite].setVisibility(View.VISIBLE);
        imagesParametres[numeroBoite].setVisibility(View.VISIBLE);
        boutonSupprimerModule[numeroBoite].setVisibility(View.VISIBLE);
    }

    private int getNumeroBoite(int idBoite)
    {
        for(int i = 0; i < modulesBoitesAuxLettres.size(); ++i)
        {
            Module module = modulesBoitesAuxLettres.get(i);
            if(module.getIdModule() == idBoite)
            {
                return i;
            }
        }
        return -1;
    }

    private int rechercherIdDisponible()
    {
        for(int id = 1; id <= NB_MODULES_BOITES_MAX; id++)
        {
            if(!estPresent(id))
                return id;
        }
        return 0; // pas d'id de disponible
    }

    private boolean estPresent(int id)
    {
        for(int i = 0; i < modulesBoitesAuxLettres.size(); i++)
        {
            if(id == modulesBoitesAuxLettres.get(i).getIdModule())
                return true;
        }
        return false;
    }
}