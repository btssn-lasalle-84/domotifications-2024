package com.lasalle.domotifications;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lasalle.domotifications.R;

public class FenetreMachine extends AppCompatActivity

{
    /**
     * Constantes
     */
    private static final String TAG = "_FenetreMachine"; //!< TAG pour les logs
    /**
     * GUI
     */
    private ImageButton boutonAccueil;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // contenu bord à bord
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_machine);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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