package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class HUD extends Image {

    Animation animation;
    float OFFSET_LIFES_PLAYER = 2; // 2px
    float OFFSET_LIFES_BOSS = 1;
    float stateTime = 0;

    public HUD(Animation animation) {
    	super(animation.getKeyFrame(0));
    	this.animation = animation;
    }

    public void putHeadBoss() {
    }

    public void setLifesBoss(int lifes) {
    }

    public void setLifesPlayer(int lifes) {
    }

   @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime +=delta, true));
        super.act(delta);
    }
}
