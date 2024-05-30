package com.lasalle.domotifications;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

    /**
     * Attributs
     */
    ImageView selecteur;
    Bitmap image;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selecteur_couleur);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selecteur = findViewById(R.id.selecteur);
        selecteur.setDrawingCacheEnabled(true);
        selecteur.buildDrawingCache(true);

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

                    TextView textView = findViewById(R.id.couleurhexa);
                    textView.setText(couleurHTML);
                }
                return false;
            }
        });


    }
}