package com.lasalle.domotifications;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SelecteurCouleur extends AppCompatActivity {

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
                }
                return false;
            }
        });
    }
}