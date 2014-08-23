package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Shot extends Image {
    float SHOT_VELOCITY = 200f;
    float stateTime = 0;
    boolean shotGoesRight;

    protected Animation animation = null;

    public Shot(float x, float y, boolean facesRight, Animation animation) {
        super(animation.getKeyFrame(0));
        this.animation = animation;
        this.setPosition(x, y);
        this.shotGoesRight = facesRight;
    }

    public void updateShot(float deltaTime){
        if (this.shotGoesRight)
            this.setX(this.getX() + (deltaTime * this.SHOT_VELOCITY));
        else
            this.setX(this.getX() - (deltaTime * this.SHOT_VELOCITY));

        this.stateTime += deltaTime;
    }

    @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime+=delta, true));
        super.act(delta);
    }
}
