package com.lasalle.domotifications;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FenetrePoubelle extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG               = "_FenetrePoubelle"; //!< TAG pour les logs
    private static final String API_GET_POUBELLES = "/poubelles";       //!< TAG pour les logs
    /**
     * Attributs
     */
    // Exemple d'accès à la base de données
    private BaseDeDonnees baseDeDonnees;      //!< Association avec la base de donnees
    private int           nbModulesPoubelles; //!< le nombre de poubelles gérées
    // Exemple d'accès à la base de données
    private Communication communication; //!< Association avec la classe Communication
    private Handler       handler =
      null; //!< Handler permettant la communication entre la classe Communication et l'activité
    /**
     * GUI
     */
    public static final int BLEUE = 0; //!< Poubelle bleue
    public static final int VERTE = 1; //!< Poubelle verte
    public static final int JAUNE = 2; //!< Poubelle jaune
    public static final int GRISE = 3; //!< Poubelle grise
    public static final int ROUGE = 4; //!< Poubelle rouge
    // pour les tests (cf. todo ci-dessous)
    public static final int NB_COULEURS_POUBELLE = 5; //!< Nombre de couleurs max pour les poubelles
    public static final int[] IMAGES_POUBELLES   = {
          R.drawable.poubelle_bleue,
          R.drawable.poubelle_verte,
          R.drawable.poubelle_jaune,
          R.drawable.poubelle_grise,
          R.drawable.poubelle_rouge
    }; //!< Id des images des poubelles dans les ressources Android

    private ImageView[] imagesPoubelles; //!< Images des poubelles de couleur
    private ImageButton boutonAccueil;

    private Button[] boutonsActiver;
    private Module[] modulesPoubelles;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        baseDeDonnees      = BaseDeDonnees.getInstance(this);
        nbModulesPoubelles = baseDeDonnees.getNbModulesPoubelles();
        Log.d(TAG, "nbModulesPoubelles = " + nbModulesPoubelles);

        System.out.println("nbModulesPoubelles : " + nbModulesPoubelles);

        initialiserHandler();

        recupererEtats();

        initialiserGUI();

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

        boutonAccueil = (ImageButton)findViewById(R.id.boutonAccueil);

        imagesPoubelles    = new ImageView[NB_COULEURS_POUBELLE];
        imagesPoubelles[0] = (ImageView)findViewById(R.id.poubelle0);
        imagesPoubelles[1] = (ImageView)findViewById(R.id.poubelle1);
        imagesPoubelles[2] = (ImageView)findViewById(R.id.poubelle2);
        imagesPoubelles[3] = (ImageView)findViewById(R.id.poubelle3);
        imagesPoubelles[4] = (ImageView)findViewById(R.id.poubelle4);

        for(int i = 0; i < NB_COULEURS_POUBELLE; ++i)
        {
            imagesPoubelles[i].setImageResource(IMAGES_POUBELLES[i]);
        }

        for(int i = 0; i < NB_COULEURS_POUBELLE; ++i)
        {
            imagesPoubelles[i].setVisibility(View.INVISIBLE);
        }

        for(int i = 0; i < nbModulesPoubelles; ++i)
        {
            imagesPoubelles[i].setVisibility(View.VISIBLE);
        }

        if(modulesPoubelles !=null) {
            for (int i = 0; i < modulesPoubelles.length; i++) {
                final int index = i;
                Button boutonActiver = boutonsActiver[i];
                boutonActiver.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gererClicSurBouton(index);
                    }
                });
            }
        }

        boutonAccueil = (ImageButton)findViewById(R.id.boutonAccueil);
        boutonAccueil.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonAccueil");
                finish();
            }
        });
    }

    private void recupererEtats()
    {
        Log.d(TAG, "recupererEtats()");
        // On récupère l'URL de la station dans la base de données
        //communication = Communication.getInstance(this);
        // ou on indique l'adresse de la station :
        communication = Communication.getInstance(Communication.ADRESSE_IP_STATION, this);

        communication.emettreRequeteGET(API_GET_POUBELLES, handler);
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
                // @todo Mettre à jour l'IHM

            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

    }

    private void initialiserHandler()
    {
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
                        break;
                }
            }
        };
    }

    private void gererClicSurBouton(int i) {
        if (modulesPoubelles[i] == null) {
            Log.e(TAG, "Module à l'index " + i + " est null");
            return;
        }

        modulesPoubelles[i].setActif(!modulesPoubelles[i].estActif());

        Button boutonActiver = boutonsActiver[i];

        if (boutonActiver == null) {
            Log.e(TAG, "Bouton à l'index " + i + " est null");
            return;
        }

        if (modulesPoubelles[i].estActif()) {
            boutonActiver.setText("Désactiver");
            baseDeDonnees.mettreAJourEtatActivationModule(modulesPoubelles[i].getIdModule(), true);
        } else {
            boutonActiver.setText("Activer");
            baseDeDonnees.mettreAJourEtatActivationModule(modulesPoubelles[i].getIdModule(), false);
        }
    }


}

