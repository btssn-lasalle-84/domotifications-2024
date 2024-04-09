package com.lasalle.domotifications;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lasalle.domotifications.R;

import java.util.Vector;

public class FenetrePoubelle extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG = "_FenetrePoubelle"; //!< TAG pour les logs
    /**
     * Attributs
     */
    // Exemple d'accès à la base de données
    private BaseDeDonnees baseDeDonnees;      //!< Association avec la base de donnees
    private int           nbModulesPoubelles; //!< le nombre de poubelles gérées
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
          R.drawable.poubelle,
          R.drawable.poubelle,
          R.drawable.poubelle,
          R.drawable.poubelle,
          R.drawable.poubelle
    }; //!< Id des images des poubelles dans les ressources Android
    // @todo jusqu'à 5 poubelles en couleurs
    /*
    public static final int[] IMAGES_POUBELLES   = {
          R.drawable.poubelle_bleue,
          R.drawable.poubelle_verte,
          R.drawable.poubelle_jaune,
          R.drawable.poubelle_grise,
          R.drawable.poubelle_rouge
    }; //!< Id des images des poubelles dans les ressources Android
    */
    private ImageView[] imagesPoubelles; //!< Images des poubelles de couleur
    private ImageButton boutonAccueil;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        baseDeDonnees      = BaseDeDonnees.getInstance(this);
        nbModulesPoubelles = baseDeDonnees.getNbModulesPoubelles();
        Log.d(TAG, "nbModulesPoubelles = " + nbModulesPoubelles);

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

        boutonAccueil = (ImageButton)findViewById(R.id.boutonAccueil);
        boutonAccueil.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonAccueil");
                finish();
            }
        });
    }
}