package com.example.domotification;

public class IHM {

    private String nomInterface;
    private String adresseIP;

    // Constructeur
    public IHM(String nomInterface, String adresseIP) {
        this.nomInterface = nomInterface;
        this.adresseIP = adresseIP;
    }

    // Destructeur

    protected void finalize() throws Throwable {
    }
}
