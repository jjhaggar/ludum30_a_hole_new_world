package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class Counter extends Image {

    final int LIFES;
    public int currentLifes;

    public Counter(int lifes) {
        this.LIFES =  lifes;
        this.currentLifes = lifes;
    }

    public int lostLife() {
        this.currentLifes -= 1;
        return this.currentLifes;
    }

    public int gainLife(int lifes) {
    	this.currentLifes += lifes;
    	return this.currentLifes;
    }



}
