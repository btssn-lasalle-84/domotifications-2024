package com.lasalle.domotifications;

public class Module
{
    private int        idModule;
    private String     nomModule;
    private TypeModule typeModule;
    private boolean    etatActivation;
    private boolean    etatModification;

    private boolean actif; // Représenter l'état d'activation du module

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
                  boolean    etatModification,
                  boolean    actif)
    {
        this.idModule         = idModule;
        this.nomModule        = nomModule;
        this.typeModule       = typeModule;
        this.etatActivation   = etatActivation;
        this.etatModification = etatModification;
        this.actif            = actif;
    }

    // Destructeur
    protected void finalize() throws Throwable
    {
    }


    public boolean estActif() {
        return this.etatActivation;
    }

    public int getIdModule() {
        return this.idModule;
    }

    public void setEtatActivation(boolean etat) {
        this.etatActivation = etat;
    }

    public void setActif(boolean actif) {
        this.setEtatActivation(actif);
    }

}
