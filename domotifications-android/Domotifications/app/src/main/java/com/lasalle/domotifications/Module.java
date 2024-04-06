package com.lasalle.domotifications;

public class Module
{
    private int        idModule;
    private String     nomModule;
    private TypeModule typeModule;
    private boolean    etatActivation;
    private boolean    etatModification;
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
                  boolean    etatModification)
    {
        this.idModule         = idModule;
        this.nomModule        = nomModule;
        this.typeModule       = typeModule;
        this.etatActivation   = etatActivation;
        this.etatModification = etatModification;
    }

    // Destructeur
    protected void finalize() throws Throwable
    {
    }
}
