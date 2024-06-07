package com.lasalle.domotifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.util.Vector;

public class BoiteDeDialogue extends DialogFragment
{
    /**
     * Constantes
     */
    private static final String TAG = "_BoiteDeDialogue";

    /**
     * Attributs
     */
    private int               idModule;
    private String            nomModule;
    private Module.TypeModule typeModule;
    private BaseDeDonnees     baseDeDonnees;
    private Vector<Module>    modules;

    /**
     * Constructeur
     */
    public BoiteDeDialogue(BaseDeDonnees baseDeDonnees, Vector<Module> modules)
    {
        this.baseDeDonnees = baseDeDonnees;
        this.modules       = modules;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Récupérer les arguments passés à la boîte de dialogue
        Bundle args = getArguments();
        if(args != null)
        {
            idModule   = args.getInt("idModule", -1);
            nomModule  = args.getString("nomModule", "");
            typeModule = (Module.TypeModule)args.getSerializable("typeModule");
        }

        AlertDialog.Builder boiteSuppression = new AlertDialog.Builder(getActivity());
        boiteSuppression.setMessage("Vous êtes sur le point de supprimer le module '" + nomModule +
                                    "'.");
        boiteSuppression.setPositiveButton("SUPPRIMER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                Log.d(TAG, "Suppression du module '" + nomModule + "' avec idModule = " + idModule);

                // Supprimer le module de la base de données
                for(Module module: modules)
                {
                    if(module.getIdModule() == idModule && module.getNomModule().equals(nomModule))
                    {
                        baseDeDonnees.supprimerModule(idModule, typeModule.ordinal());
                        modules.remove(module);
                        break;
                    }
                }

                // Notifier l'utilisateur
                Toast
                  .makeText(getActivity(),
                            "Module '" + nomModule + "' supprimé.",
                            Toast.LENGTH_SHORT)
                  .show();

                Log.d(TAG, "Module supprimé de la base de données et de la liste des modules");
            }
        });
        boiteSuppression.setNegativeButton("ANNULER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                Log.d(TAG, "Annulation de la suppression du module '" + nomModule + "'");
                // On ferme la boîte de dialogue
                dismiss();
            }
        });
        return boiteSuppression.create();
    }
}
