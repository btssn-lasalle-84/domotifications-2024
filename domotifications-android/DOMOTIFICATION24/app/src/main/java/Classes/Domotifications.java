package Classes;

import java.util.List;

public class Domotifications {

    private List<Modules> modules;
    private List<Modules> poubelles;
    private List<Modules> boites;
    private List<Modules> machines;
    private IHM ihm;
    private Communication communication;

    // Constructeur
    public Domotifications(List<Modules> modules, List<Modules> poubelles, List<Modules> boites, List<Modules> machines, IHM ihm, Communication communication) {
        this.modules = modules;
        this.poubelles = poubelles;
        this.boites = boites;
        this.machines = machines;
        this.ihm = ihm;
        this.communication = communication;
    }

    // Destructeur
    protected void finalize() throws Throwable {
    }
}
