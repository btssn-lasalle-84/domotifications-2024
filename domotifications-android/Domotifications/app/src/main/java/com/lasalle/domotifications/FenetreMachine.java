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

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class FenetreMachine extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG                = "_FenetreMachine"; //!< TAG pour les logs
    private static final String API_PATCH_MACHINES = "/machines";       //!< Pour une requête PATCH
    private static final String API_POST_MACHINES  = "/machines";       //!< Pour une requête POST
    private static final int    INTERVALLE         = 1000; //!< Intervalle d'interrogation en ms
    public static final int     NB_MACHINES_MAX    = 6;    //!< Nombre max de machines
    private static final int    PARAMETRAGE_MODULE =
      1; //!< Code pour l'activité de paramètrage du module

    /**
     * Attributs
     */
    private BaseDeDonnees    baseDeDonnees;      //!< Association avec la base de donnees
    protected Vector<Module> modulesMachines;    //!< Conteneur des modules machines
    private int              nbModulesMachines;  //!< le nombre de machines gérées
    private int              idNotification = 0; //!< Identifiant unique pour chaque notification
    private Communication    communication;      //!< Association avec la classe Communication
    private Handler          handler =
      null; //!< Handler permettant la communication entre la classe Communication et l'activité
    private Timer minuteur = null; //!< Pour gérer la récupération des états des modules machines
    private TimerTask tacheRecuperationEtats =
      null; //!< Pour effectuer la récupération des états des modules machines
    private boolean               erreurCommunication = false;
    private Map<Integer, Boolean> notificationsEnvoyees =
      new HashMap<>(); //<! Pour la signalisation des notifications
    private int    numeroMachineAcquittement = -1;
    private String nomAjoutModule;

    /**
     * GUI
     */
    private ImageButton boutonAccueil;

    public static final int[] IMAGE_MACHINES = {
        R.drawable.machine, R.drawable.lavevaisselle, R.drawable.machine,
        R.drawable.machine, R.drawable.machine,       R.drawable.machine
    };                                  //!< Id de l'image de la machine dans les ressources Android
    private ImageView[] imagesMachines; //!< Images des machines
    private ImageView[] imagesNotificationMachines; //!< Images des notifications des machines
    private Switch[] boutonsActivation; //!< Boutons d'activation/désactivation des modules
    //!< machines
    private ImageView boutonAjouterModule;     //!< Bouton pour ajouter les modules machines
    private ImageView[] boutonSupprimerModule; //!< Boutons pour supprimer les modules machines
    private ImageView[] imagesParametres;      //!< Images des couleurs des modules
    //!< machines

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        initialiserBaseDeDonnees();
        initialiserModulesMachines();
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

    private void initialiserModulesMachines()
    {
        Log.d(TAG, "initialiserModulesMachines()");
        modulesMachines = baseDeDonnees.getMachines();
        // nbModulesMachines = baseDeDonnees.getNbModulesMachines();
        nbModulesMachines = modulesMachines.size();
        Log.d(TAG, "nbModulesMachines = " + nbModulesMachines);
    }

    private void initialiserGUI()
    {
        // contenu bord à bord
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_machine);
        ViewCompat.setOnApplyWindowInsetsListener(
          findViewById(R.id.fenetreMachine),
          (v, insets) -> {
              Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
              v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
              return insets;
          });

        boutonAccueil = (ImageButton)findViewById(R.id.boutonAccueil);

        imagesMachines    = new ImageView[NB_MACHINES_MAX];
        imagesMachines[0] = (ImageView)findViewById(R.id.machine0);
        imagesMachines[1] = (ImageView)findViewById(R.id.machine1);
        imagesMachines[2] = (ImageView)findViewById(R.id.machine2);
        imagesMachines[3] = (ImageView)findViewById(R.id.machine3);
        imagesMachines[4] = (ImageView)findViewById(R.id.machine4);
        imagesMachines[5] = (ImageView)findViewById(R.id.machine5);

        imagesNotificationMachines    = new ImageView[NB_MACHINES_MAX];
        imagesNotificationMachines[0] = (ImageView)findViewById(R.id.notificationMachine0);
        imagesNotificationMachines[1] = (ImageView)findViewById(R.id.notificationMachine1);
        imagesNotificationMachines[2] = (ImageView)findViewById(R.id.notificationMachine2);
        imagesNotificationMachines[3] = (ImageView)findViewById(R.id.notificationMachine3);
        imagesNotificationMachines[4] = (ImageView)findViewById(R.id.notificationMachine4);
        imagesNotificationMachines[5] = (ImageView)findViewById(R.id.notificationMachine5);

        boutonsActivation    = new Switch[NB_MACHINES_MAX];
        boutonsActivation[0] = (Switch)findViewById(R.id.activationMachine0);
        boutonsActivation[1] = (Switch)findViewById(R.id.activationMachine1);
        boutonsActivation[2] = (Switch)findViewById(R.id.activationMachine2);
        boutonsActivation[3] = (Switch)findViewById(R.id.activationMachine3);
        boutonsActivation[4] = (Switch)findViewById(R.id.activationMachine4);
        boutonsActivation[5] = (Switch)findViewById(R.id.activationMachine5);

        imagesParametres    = new ImageView[NB_MACHINES_MAX];
        imagesParametres[0] = (ImageView)findViewById(R.id.couleurMachine0);
        imagesParametres[1] = (ImageView)findViewById(R.id.couleurMachine1);
        imagesParametres[2] = (ImageView)findViewById(R.id.couleurMachine2);
        imagesParametres[3] = (ImageView)findViewById(R.id.couleurMachine3);
        imagesParametres[4] = (ImageView)findViewById(R.id.couleurMachine4);
        imagesParametres[5] = (ImageView)findViewById(R.id.couleurMachine5);

        boutonSupprimerModule    = new ImageView[NB_MACHINES_MAX];
        boutonSupprimerModule[0] = (ImageView)findViewById(R.id.boutonSupprimerMachine0);
        boutonSupprimerModule[1] = (ImageView)findViewById(R.id.boutonSupprimerMachine1);
        boutonSupprimerModule[2] = (ImageView)findViewById(R.id.boutonSupprimerMachine2);
        boutonSupprimerModule[3] = (ImageView)findViewById(R.id.boutonSupprimerMachine3);
        boutonSupprimerModule[4] = (ImageView)findViewById(R.id.boutonSupprimerMachine4);
        boutonSupprimerModule[5] = (ImageView)findViewById(R.id.boutonSupprimerMachine5);

        boutonAjouterModule = (ImageView)findViewById(R.id.boutonAjouterModule);

        for(int i = 0; i < NB_MACHINES_MAX; ++i)
        {
            int idMachine = i;
            imagesParametres[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(FenetreMachine.this, ParametresModule.class);
                    intent.putExtra("idModule", modulesMachines.get(idMachine).getIdModule());
                    intent.putExtra("nom", modulesMachines.get(idMachine).getNomModule());
                    intent.putExtra("couleur", modulesMachines.get(idMachine).getCouleur());
                    startActivityForResult(intent, PARAMETRAGE_MODULE);
                }
            });

            imagesMachines[i].setImageResource(IMAGE_MACHINES[i]);

            imagesNotificationMachines[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    gererClicBoutonNotification(idMachine);
                }
            });

            imagesMachines[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    gererClicBoutonNotification(idMachine);
                }
            });

            boutonSupprimerModule[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Log.d(TAG, "afficherBoiteDialogueSuppressionModule()");
                    afficherBoiteDialogueSuppressionModule(idMachine);
                }
            });
        }

        for(int i = 0; i < NB_MACHINES_MAX; ++i)
        {
            cacherMachine(i);
        }

        for(int i = 0; i < nbModulesMachines; ++i)
        {
            afficherMachine(i);
        }

        for(int i = 0; i < nbModulesMachines; i++)
        {
            final int numeroMachine = i;

            // initialise la GUI en fonction de l'état de notification
            if(modulesMachines.get(i).estNotifie())
            {
                imagesNotificationMachines[i].setVisibility(View.VISIBLE);
            }
            else
            {
                imagesNotificationMachines[i].setVisibility(View.INVISIBLE);
            }

            // initialise la GUI en fonction de l'état d'activation
            if(modulesMachines.get(i).estActif())
            {
                boutonsActivation[i].setChecked(true);
                imagesNotificationMachines[i].setEnabled(true);
                imagesMachines[i].setEnabled(true);
            }
            else
            {
                boutonsActivation[i].setChecked(false);
                imagesNotificationMachines[i].setEnabled(false);
                imagesMachines[i].setEnabled(false);
            }

            boutonsActivation[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    gererClicBoutonActivation(numeroMachine);
                }
            });
        }

        boutonAccueil.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonAccueil");
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
                        validerAjoutMachine(message.obj.toString());
                        erreurCommunication = false;
                        break;
                    case Communication.CODE_HTTP_REPONSE_DELETE:
                        Log.d(TAG, "[Handler] REPONSE DELETE");
                        validerSuppressionMachine(message.obj.toString());
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

    private void gererClicBoutonNotification(int numeroMachine)
    {
        if(modulesMachines.get(numeroMachine) == null)
        {
            Log.e(TAG, "gererClicBoutonNotification() Aucune machine !");
            return;
        }
        Log.d(TAG,
              "gererClicBoutonNotification() numeroMachine = " + numeroMachine +
                " idMachine = " + modulesMachines.get(numeroMachine).getIdModule() +
                " notification = " + modulesMachines.get(numeroMachine).estNotifie() +
                " activation = " + modulesMachines.get(numeroMachine).estActif());

        if(modulesMachines.get(numeroMachine).estActif())
        {
            if(modulesMachines.get(numeroMachine).estNotifie())
            {
                /*
                Exemple :
                $ curl --location 'http://station-lumineuse.local:80/machines/3' --request PATCH
                --header 'Content-Type: application/json' --data '{"idMachine": "1","etat": false}'
                */
                numeroMachineAcquittement = numeroMachine;
                String api =
                  API_PATCH_MACHINES + "/" + modulesMachines.get(numeroMachine).getIdModule();
                String json = "{\"idMachine\": \"" +
                              modulesMachines.get(numeroMachine).getIdModule() +
                              "\",\"etat\": false}";
                communication.emettreRequetePATCH(api, json, handler);
            }
        }
    }

    private void gererClicBoutonActivation(int numeroMachine)
    {
        if(modulesMachines.get(numeroMachine) == null)
        {
            Log.e(TAG, "gererClicBoutonActivation() Aucune machine !");
            return;
        }
        Log.d(TAG,
              "gererClicBoutonActivation() numeroMachine = " + numeroMachine +
                " activation = " + boutonsActivation[numeroMachine].isChecked());

        String api = API_PATCH_MACHINES + "/" + modulesMachines.get(numeroMachine).getIdModule();

        String json = "{\"idMachine\": \"" + modulesMachines.get(numeroMachine).getIdModule() +
                      "\",\"actif\": " + boutonsActivation[numeroMachine].isChecked() + "}";

        communication.emettreRequetePATCH(api, json, handler);

        modulesMachines.get(numeroMachine).setActif(boutonsActivation[numeroMachine].isChecked());

        if(modulesMachines.get(numeroMachine).estActif())
        {
            imagesMachines[numeroMachine].setEnabled(true);
            imagesNotificationMachines[numeroMachine].setEnabled(true);
        }
        else
        {
            imagesMachines[numeroMachine].setEnabled(false);
            imagesNotificationMachines[numeroMachine].setEnabled(false);
        }
    }

    private void recupererEtats()
    {
        Log.d(TAG, "recupererEtats()");
        tacheRecuperationEtats = new TimerTask() {
            public void run()
            {
                communication.emettreRequeteGET(Communication.API_GET_MACHINES, handler);
            }
        };

        // toutes les secondes (en attente de l'implémentation de la websocket)
        minuteur.schedule(tacheRecuperationEtats, INTERVALLE, INTERVALLE);
    }

    public void traiterReponseJSON(String reponse)
    {
        // Log.d(TAG, "traiterReponseJSON() reponse = " + reponse);
        /*
            Exemple de réponse : pour la requête GET /machines
            body =
            [
                {"idMachine":1,"couleur":"#FF0000","etat":false,"actif":true},
                {"idMachine":2,"couleur":"#FFFF00","etat":false,"actif":true},
                {"idMachine":3,"couleur":"#0000FF","etat":false,"actif":true},
                {"idMachine":4,"couleur":"#F0F0F2","etat":false,"actif":true},
            ]
        */
        JSONArray json = null;

        try
        {
            json = new JSONArray(reponse);
            for(int i = 0; i < json.length(); ++i)
            {
                JSONObject machines  = json.getJSONObject(i);
                int        idMachine = machines.getInt("idMachine");
                String     couleur   = machines.getString("couleur");
                Boolean    etat      = machines.getBoolean("etat");
                Boolean    actif     = machines.getBoolean("actif");
                // Log.d(TAG, "traiterReponseJSON() idMachine = " + idMachine + " couleur = " +
                // couleur + " etat = " + etat + " actif = " + actif);
                for(int j = 0; j < modulesMachines.size(); ++j)
                {
                    Module module = modulesMachines.get(j);
                    if(module.getIdModule() == idMachine &&
                       module.getTypeModule() == Module.TypeModule.Machine)
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

    public void validerAjoutMachine(String reponse)
    {
        Log.d(TAG, "validerAjoutMachine() reponse = " + reponse);
        /*
            Exemple de réponse : pour la requête POST
            body =
            [
                {"idMachine":5,"couleur":"#FF0000","etat":false,"actif":false}
            ]
        */
        JSONArray json = null;

        try
        {
            json                 = new JSONArray(reponse);
            JSONObject machine   = json.getJSONObject(0);
            int        idMachine = machine.getInt("idMachine");
            String     couleur   = machine.getString("couleur");
            Boolean    etat      = machine.getBoolean("etat");
            Boolean    actif     = machine.getBoolean("actif");
            Log.d(TAG,
                  "validerAjoutMachine() idMachine = " + idMachine + " couleur = " + couleur +
                    " etat = " + etat + " actif = " + actif);
            Module module = new Module(idMachine,
                                       nomAjoutModule,
                                       Module.TypeModule.Machine,
                                       actif,
                                       etat,
                                       couleur,
                                       baseDeDonnees);
            modulesMachines.add(module);
            baseDeDonnees.insererModule(module.getIdModule(),
                                        module.getTypeModule().ordinal() + 1,
                                        module.getNomModule(),
                                        module.estActif(),
                                        module.getCouleur());
            afficherMachine(nbModulesMachines);
            int numeroMachine = getNumeroMachine(module.getIdModule());
            mettreAJourModule(numeroMachine);
            nomAjoutModule    = "";
            nbModulesMachines = modulesMachines.size();
            Log.d(TAG, "validerAjoutMachine() nbModulesMachines = " + nbModulesMachines);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void validerSuppressionMachine(String reponse)
    {
        Log.d(TAG, "validerSuppressionMachine() reponse = " + reponse);
        /*
            Exemple de réponse : pour la requête DELETE
            body =
            [
                {"idMachine":5}
            ]
        */
        JSONArray json = null;

        try
        {
            json                 = new JSONArray(reponse);
            JSONObject machines  = json.getJSONObject(0);
            int        idMachine = machines.getInt("idMachine");
            Log.d(TAG, "validerSuppressionMachine() idMachine = " + idMachine);
            int numeroMachine = getNumeroMachine(idMachine);
            if(numeroMachine == -1)
            {
                Log.d(TAG, "validerSuppressionMachine() idMachine introuvable !");
                return;
            }
            Module module = modulesMachines.get(numeroMachine);
            baseDeDonnees.supprimerModule(idMachine, module.getTypeModule().ordinal() + 1);
            modulesMachines.remove(module);
            cacherMachine(numeroMachine);
            nbModulesMachines = modulesMachines.size();
            Log.d(TAG, "validerSuppressionMachine() nbModulesMachines = " + nbModulesMachines);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void enregistrerAcquittementNotification(String reponse)
    {
        if(numeroMachineAcquittement == -1)
        {
            return;
        }
        if(modulesMachines.get(numeroMachineAcquittement) == null)
        {
            Log.e(TAG, "enregistrerAcquittementNotification() : Aucune machine !");
            return;
        }
        Log.d(TAG, "enregistrerAcquittementNotification() reponse = " + reponse);
        /*
            Exemple de réponsee : pour la requête PATCH /machines/3
            body =
            [
                {"idMachine":1,"couleur":"#FF0000","etat":false,"actif":true}
            ]
        */
        JSONArray json = null;

        try
        {
            json = new JSONArray(reponse);
            if(json.length() > 0)
            {
                JSONObject machines  = json.getJSONObject(0);
                int        idMachine = machines.getInt("idMachine");
                String     couleur   = machines.getString("couleur");
                Boolean    etat      = machines.getBoolean("etat");
                Boolean    actif     = machines.getBoolean("actif");
                Log.d(TAG,
                      "enregistrerAcquittementNotification() idMachine = " + idMachine +
                        " couleur = " + couleur + " etat = " + etat + " actif = " + actif);

                Module module = modulesMachines.get(numeroMachineAcquittement);
                if(module.getIdModule() == idMachine &&
                   module.getTypeModule() == Module.TypeModule.Machine && !etat)
                {
                    baseDeDonnees.enregistrerAcquittementNotification(
                      module.getIdModule(),
                      module.getTypeModule().ordinal(),
                      true);
                    numeroMachineAcquittement = -1;
                }
            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void mettreAJourModule(int numeroMachine)
    {
        if(modulesMachines.get(numeroMachine) == null)
        {
            Log.e(TAG, "Aucune machine");
            return;
        }

        Module module    = modulesMachines.get(numeroMachine);
        int    idMachine = module.getIdModule();

        if(module.estActif())
        {
            if(module.estNotifie())
            {
                imagesNotificationMachines[numeroMachine].setVisibility(View.VISIBLE);
                Boolean notificationEnvoyee = notificationsEnvoyees.get(idMachine);
                if(notificationEnvoyee == null || !notificationEnvoyee)
                {
                    // On signale une notification sur la tablette Android
                    creerNotification("Le module " + module.getNomModule() +
                                      " a une notification.");
                    notificationsEnvoyees.put(idMachine, true);
                }
            }
            else
            {
                imagesNotificationMachines[numeroMachine].setVisibility(View.INVISIBLE);
                notificationsEnvoyees.put(idMachine, false);
            }

            boutonsActivation[numeroMachine].setChecked(true);
            imagesNotificationMachines[numeroMachine].setEnabled(true);
            imagesMachines[numeroMachine].setEnabled(true);

            // Toast.makeText(getApplicationContext(), "Module activé", Toast.LENGTH_SHORT).show();
        }
        else
        {
            boutonsActivation[numeroMachine].setChecked(false);
            imagesNotificationMachines[numeroMachine].setEnabled(false);
            imagesMachines[numeroMachine].setEnabled(false);

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
                        int    numeroModule = getNumeroMachine(idModule);
                        Module module       = modulesMachines.get(numeroModule);
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

                    String api  = API_PATCH_MACHINES + "/" + idModule;
                    String json = "{\"idMachine\": \"" + idModule + "\",\"couleur\": \"" +
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
                    nomAjoutModule = "machine";
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
                int idMachine = rechercherIdDisponible();
                Log.d(TAG, "afficherBoiteDialogueAjoutModule() idMachine = " + idMachine);
                // si idMachine = 0 alors la station choisira l'idMachine à ajouter sinon c'est
                // l'application qui le détermine

                // Mode démo
                if(idMachine > 0)
                {
                    String api  = API_PATCH_MACHINES;
                    String json = "{\"idMachine\": " + idMachine + ", \"couleur\": \"" +
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
                    String reponseJson = "{\"idMachine\": " + idMachine + ", \"couleur\": \"" +
                                         couleurAjoutModule + "\", \"etat\": false, \"actif\": true"
                                         + "}";
                    validerAjoutMachine("[" + reponseJson + "]");
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

    /**
     * @brief Méthode pour afficher la boîte de dialogue de suppression de module
     */
    private void afficherBoiteDialogueSuppressionModule(int numeroMachine)
    {
        if(modulesMachines.get(numeroMachine) == null)
        {
            Log.e(TAG, "afficherBoiteDialogueSuppressionModule() Aucune machine !");
            return;
        }

        String nomModule = modulesMachines.get(numeroMachine).getNomModule();

        AlertDialog.Builder boiteSuppression = new AlertDialog.Builder(this);
        boiteSuppression.setMessage("Vous êtes sur le point de supprimer le module '" + nomModule +
                                    "'.");
        boiteSuppression.setPositiveButton("SUPPRIMER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                // Emettre la requête DELETE à la station
                String api =
                  API_PATCH_MACHINES + "/" + modulesMachines.get(numeroMachine).getIdModule();
                String json =
                  "{\"idMachine\": " + modulesMachines.get(numeroMachine).getIdModule() + "}";
                communication.emettreRequeteDELETE(api, json, handler);

                // Notifier l'utilisateur
                Toast
                  .makeText(getApplicationContext(),
                            "Demande de suppression du module '" + nomModule + "' envoyée",
                            Toast.LENGTH_SHORT)
                  .show();

                // Mode démo
                validerSuppressionMachine("[" + json + "]");
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

    private void cacherMachine(int numeroMachine)
    {
        imagesMachines[numeroMachine].setVisibility(View.INVISIBLE);
        imagesNotificationMachines[numeroMachine].setVisibility(View.INVISIBLE);
        boutonsActivation[numeroMachine].setVisibility(View.INVISIBLE);
        imagesParametres[numeroMachine].setVisibility(View.INVISIBLE);
        boutonSupprimerModule[numeroMachine].setVisibility(View.INVISIBLE);
    }

    private void afficherMachine(int numeroMachine)
    {
        imagesMachines[numeroMachine].setVisibility(View.VISIBLE);
        imagesNotificationMachines[numeroMachine].setVisibility(View.VISIBLE);
        boutonsActivation[numeroMachine].setVisibility(View.VISIBLE);
        imagesParametres[numeroMachine].setVisibility(View.VISIBLE);
        boutonSupprimerModule[numeroMachine].setVisibility(View.VISIBLE);
    }

    private int getNumeroMachine(int idMachine)
    {
        for(int i = 0; i < modulesMachines.size(); ++i)
        {
            Module module = modulesMachines.get(i);
            if(module.getIdModule() == idMachine)
            {
                return i;
            }
        }
        return -1;
    }

    private int rechercherIdDisponible()
    {
        for(int id = 1; id <= NB_MACHINES_MAX; id++)
        {
            if(!estPresent(id))
                return id;
        }
        return 0; // pas d'id de disponible
    }

    private boolean estPresent(int id)
    {
        for(int i = 0; i < modulesMachines.size(); i++)
        {
            if(id == modulesMachines.get(i).getIdModule())
                return true;
        }
        return false;
    }
}
