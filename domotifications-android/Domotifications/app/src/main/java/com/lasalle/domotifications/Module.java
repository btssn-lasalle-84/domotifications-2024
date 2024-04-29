package com.lasalle.domotifications;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Type;

public class Module
{
    private static final String TAG = "_Module"; //!< TAG pour les logs
    private int                 idModule;
    private String              nomModule;
    private TypeModule          typeModule;
    private boolean             etatActivation;
    private boolean             etatNotification;
    private String              couleur;
    private BaseDeDonnees       baseDeDonnees; //!< Association avec la base de donnees

    public enum TypeModule
    {
        BoiteAuxLettres,
        Machine,
        Poubelle
    }

    // Constructeur
    public Module(int           idModule,
                  String        nomModule,
                  TypeModule    typeModule,
                  boolean       etatActivation,
                  boolean       etatNotification,
                  BaseDeDonnees baseDeDonnees)

    {
        Log.d(TAG,
              "Module(" + idModule + "," + nomModule + "," + typeModule + "," + etatActivation +
                "," + etatNotification + ")");
        this.idModule         = idModule;
        this.nomModule        = nomModule;
        this.typeModule       = typeModule;
        this.etatActivation   = etatActivation;
        this.etatNotification = etatNotification;
        this.baseDeDonnees    = baseDeDonnees;
    }

    public int getIdModule()
    {
        return this.idModule;
    }

    public String getNomModule()
    {
        return this.nomModule;
    }

    public TypeModule getTypeModule()
    {
        return this.typeModule;
    }

    public boolean estActif()
    {
        return this.etatActivation;
    }

    public boolean estNotifie()
    {
        return this.etatNotification;
    }

    private String getCouleur()
    {
        return this.couleur;
    }

    public void setNomModule(String nomModule)
    {
        this.nomModule = nomModule;
    }

    public void setTypeModule(TypeModule typeModule)
    {
        this.typeModule = typeModule;
    }

    public void setEtatActivation(boolean etat)
    {
        if (this.etatActivation == !etat)
        {
            this.etatActivation = etat;
        }
        baseDeDonnees.mettreAJourEtatActivationModule(idModule, etat);
    }

    public void setActif(boolean actif)
    {
        this.setEtatActivation(actif);
    }

    public void setEtatNotification(boolean etat)
    {
        if (this.etatNotification == !etat)
        {
            this.etatNotification = etat;
        }
        baseDeDonnees.mettreAJourEtatNotificationModule(idModule, etat);
    }

    public void setCouleur(String couleur)
    {
        this.couleur = couleur;
    }
}
