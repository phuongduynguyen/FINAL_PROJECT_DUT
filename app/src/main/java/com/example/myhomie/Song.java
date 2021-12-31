package com.example.myhomie;

import java.io.Serializable;

public class Song implements Serializable {
    private int resource;

    public Song(int resource){
        this.resource = resource;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }
}
