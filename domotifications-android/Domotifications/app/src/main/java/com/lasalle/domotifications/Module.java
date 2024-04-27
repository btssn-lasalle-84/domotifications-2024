package com.lasalle.domotifications;

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

    public enum TypeModule
    {
        BoiteAuxLettres,
        Machine,
        Poubelle
    }

    // Constructeur
    public Module(int        idModule,
                  String     nomModule,
                  TypeModule typeModule,
                  boolean    etatActivation,
                  boolean    etatNotification)

    {
        Log.d(TAG,
              "Module(" + idModule + "," + nomModule + "," + typeModule + "," + etatActivation +
                "," + etatNotification + ")");
        this.idModule         = idModule;
        this.nomModule        = nomModule;
        this.typeModule       = typeModule;
        this.etatActivation   = etatActivation;
        this.etatNotification = etatNotification;
    }

    // Destructeur
    protected void finalize() throws Throwable
    {
    }

    public boolean estActif()
    {
        return this.etatActivation;
    }

    public int getIdModule()
    {
        return this.idModule;
    }

    public void setEtatActivation(boolean etat)
    {
        this.etatActivation = etat;
    }

    public void setActif(boolean actif)
    {
        this.setEtatActivation(actif);
    }

    public void setCouleur(String couleur){ this.couleur = couleur; }

    public void setTypeModule(TypeModule typeModule) { this.typeModule = typeModule; }

}
