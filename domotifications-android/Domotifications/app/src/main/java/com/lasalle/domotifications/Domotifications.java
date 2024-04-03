package com.lasalle.domotifications;

import java.util.List;

public class Domotifications {
    private List<Module> modules;
    private List<Module> poubelles;
    private List<Module> boites;
    private List<Module> machines;
    private Communication communication;

    public Domotifications()
    {
        communication = new Communication();
    }
}
