package com.example.domotification;
import java.net.InetAddress;
public class Communication {
    private Communication communication;
    private InetAddress adresseIP;

    // Constructeur
    public Communication(InetAddress adresseIP) {
        this.adresseIP = adresseIP;
    }

    // Destructeur

    protected void finalize() throws Throwable {

    }


}
