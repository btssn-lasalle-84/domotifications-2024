package com.lasalle.domotifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

public class BoiteDeDialogue extends DialogFragment
{
    /**
     * Constantes
     */
    private static final String TAG = "_BoiteDeDialogue";

    /**
     * Attributs
     */
    private int    idModule;
    private String nomModule;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Récupérer les arguments passés à la boîte de dialogue
        Bundle args = getArguments();
        if(args != null)
        {
            idModule  = args.getInt("idModule", -1);
            nomModule = args.getString("nomModule", "");
        }

        AlertDialog.Builder boiteSuppression = new AlertDialog.Builder(getActivity());
        boiteSuppression.setMessage("Vous êtes sur le point de supprimer le module '" + nomModule +
                                    "'.");
        boiteSuppression.setPositiveButton("SUPPRIMER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                Toast
                  .makeText(getActivity(),
                            "Module '" + nomModule + "' supprimé.",
                            Toast.LENGTH_SHORT)
                  .show();
            }
        });
        boiteSuppression.setNegativeButton("ANNULER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                // On ferme la boîte de dialogue
                dismiss();
            }
        });
        return boiteSuppression.create();
    }
}
