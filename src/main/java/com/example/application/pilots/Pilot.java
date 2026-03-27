package com.example.application.pilots;

public class Pilot {
    private int id;
    public String name;
    public boolean isFlying;

    protected Pilot() {
    }

    public Pilot(int id, String name) {
        this.id = id;
        this.name = name;
        this.isFlying = false;
    }
}
