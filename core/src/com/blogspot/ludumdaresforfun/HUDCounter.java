package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class HUDCounter extends Image {

    final int LIFES;
    private int currentLifes;

    public HUDCounter(int lifes) {
        this.LIFES =  lifes;
        this.currentLifes = lifes;
    }

    public int lostLife() {
        this.currentLifes -= 1;
        return this.currentLifes;
    }



}
