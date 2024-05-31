package com.lasalle.domotifications;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Message;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class SelecteurCouleur extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG = "_SelecteurCouleur";

    /**
     * Attributs
     */
    private int               idModule;
    private String            nomModule;
    private String            couleurModule;
    private String            couleurHTML;
    private ImageView         selecteur;
    private Bitmap            image;
    private TextInputEditText nouvelleCouleur;

    Communication   communication; //!< Association avec la classe Communication
    private Handler handler;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selecteur_couleur);
        // Gestion des événements
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Récupération des informations
        Intent intent = getIntent();
        idModule      = intent.getIntExtra("idModule", -1);
        nomModule     = intent.getStringExtra("nom");
        couleurModule = intent.getStringExtra("couleur");
        Log.d(TAG,
              "onCreate() idModule = " + idModule + " - nomModule = " + nomModule +
                " - couleurModule = " + couleurModule);

        // Affichage des informations
        TextView informationsModuleTextView = findViewById(R.id.informationsModule);
        informationsModuleTextView.setText("Module " + nomModule);

        TextView couleurModuleTextView = findViewById(R.id.couleurhexa);
        if(couleurModule != null)
            couleurModuleTextView.setText(couleurModule);
        else
            couleurModuleTextView.setText("");

        Button boutonValider = (Button)findViewById(R.id.boutonValider);
        boutonValider.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonValider");

                Intent intent = new Intent();
                intent.putExtra("idModule", idModule);
                intent.putExtra("nom", nomModule);
                intent.putExtra("couleur", nouvelleCouleur.getText().toString().trim());
                Log.d(TAG,
                      "onClick() idModule = " + idModule + " - nomModule = " + nomModule +
                        " - nouvelleCouleur = " + nouvelleCouleur.getText().toString().trim());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        Button boutonAnnuler = (Button)findViewById(R.id.boutonAnnuler);
        boutonAnnuler.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonAnnuler");
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        nouvelleCouleur = (TextInputEditText)findViewById(R.id.couleurhexa);
        selecteur       = findViewById(R.id.selecteur);
        selecteur.setDrawingCacheEnabled(true);
        selecteur.buildDrawingCache(true);
        selecteur.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN ||
                   event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    image     = selecteur.getDrawingCache();
                    int pixel = image.getPixel((int)event.getX(), (int)event.getY());

                    // Extraction des composantes RGB
                    int rouge = Color.red(pixel);
                    int vert  = Color.green(pixel);
                    int bleu  = Color.blue(pixel);
                    Log.d(TAG,
                          "rouge = " + String.format("%02X", rouge) + " - "
                            + "vert = " + String.format("%02X", vert) + " - "
                            + " bleu = " + String.format("%02X", bleu));

                    // Conversion au format #RRGGBB
                    couleurHTML = String.format("#%06X", (pixel & 0x00FFFFFF));
                    Log.d(TAG, "couleurHTML = " + couleurHTML);

                    // Vérification
                    /*
                    int couleurRGB =	Color.parseColor(couleurHTML);
                    Log.d(TAG, "couleurRGB = " + couleurRGB);
                    */

                    nouvelleCouleur.setText(couleurHTML);
                }
                return false;
            }
        });
    }
}
