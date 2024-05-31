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

public class SelecteurCouleur extends AppCompatActivity {

    /**
     * Constantes
     */
    private static final String TAG = "_SelecteurCouleur";
    private static final int CHANGEMENT_COULEUR = 1;

    /**
     * Attributs
     */
    private String nomModule;
    private String couleurModule;
    ImageView selecteur;
    Bitmap image;

    Communication communication; //!< Association avec la classe Communication
    private Handler handler;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selecteur_couleur);
        communication = Communication.getInstance(Communication.ADRESSE_IP_STATION, this);
        initialiserHandler();

        Intent intent = getIntent();
        nomModule = intent.getStringExtra("nomModule");
        couleurModule = (String)getIntent().getStringExtra("couleur");
        Log.d(TAG, "onCreate() nomModule = " + nomModule + " couleurModule = " + couleurModule);

        // Affichage des informations

        TextView nomModuleTextView = findViewById(R.id.informationsModule);
        nomModuleTextView.setText(nomModule);

        TextView couleurModuleTextView = findViewById(R.id.couleurhexa);
        couleurModuleTextView.setText(couleurModule);

        // Gestion des événements
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selecteur = findViewById(R.id.selecteur);
        selecteur.setDrawingCacheEnabled(true);
        selecteur.buildDrawingCache(true);

        int idMachine = getIntent().getIntExtra("idMachine", -1);
        Module moduleMachine = FenetreMachine.modulesMachines.get(idMachine);

        selecteur.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    image = selecteur.getDrawingCache();
                    int pixel = image.getPixel((int) event.getX(), (int) event.getY());
                    //@todo Faire le lien entre la couleur de la LED et de la sélection

                    // Extraction des composantes RGB
                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);
                    Log.d(TAG, "rouge = " + red + " - " + "vert = " + green + " - " + " bleu = " + blue);
                    Log.d(TAG, "rouge = " + String.format("%02X", red) + " - " + "vert = " + String.format("%02X", green) + " - " + " bleu = " + String.format("%02X", blue));

                    // Conversion au format #RRGGBB
                    String couleurHTML = String.format("#%06X", (pixel & 0x00FFFFFF));
                    Log.d(TAG, "couleurHTML = " + couleurHTML);

                    // Vérification
                    int couleurRGB =	Color.parseColor(couleurHTML);
                    Log.d(TAG, "couleurRGB = " + couleurRGB);
                }
                return false;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,
                "onActivityResult() requestCode : " + requestCode + " - resultCode : " + resultCode);
        if(requestCode == CHANGEMENT_COULEUR)
        {
            if(resultCode == RESULT_OK)
            {
                if(data != null)
                {
                    String couleurModule = data.getStringExtra("couleur");
                    Log.d(TAG, "onActivityResult() couleurModule = " + couleurModule);
                    // @todo émettre une requête PATCH pour changer la couleur
                }
            }
        }
    }
    private void initialiserHandler() {
        this.handler = new Handler(this.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case Communication.CODE_HTTP_REPONSE_PATCH:
                        Log.d(TAG, "[Handler] REPONSE PATCH");
                        traiterReponsePATCH(message.obj.toString());
                        break;
                    case Communication.CODE_HTTP_ERREUR:
                        Log.d(TAG, "[Handler] ERREUR HTTP");
                        afficherErreur("Erreur de communication lors de la mise à jour de la couleur !");
                        break;
                }
            }
        };
    }

    private void traiterReponsePATCH(String reponse) {
        Log.d(TAG, "traiterReponsePATCH() reponse = " + reponse);
        // Traitez la réponse ici si nécessaire
    }

    private void afficherErreur(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
