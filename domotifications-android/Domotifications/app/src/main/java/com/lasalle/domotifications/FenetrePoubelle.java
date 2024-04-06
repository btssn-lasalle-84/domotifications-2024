package com.lasalle.domotifications;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lasalle.domotifications.R;

public class FenetrePoubelle extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG = "_FenetrePoubelle"; //!< TAG pour les logs

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // contenu bord à bord
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_poubelle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}