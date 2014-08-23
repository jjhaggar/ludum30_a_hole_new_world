package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Shot extends Image {
    float SHOT_VELOCITY = 200f;
    float stateTime = 0;
    boolean shotGoesRight;
    private float GRAVITY = -10f;
    Vector2 desiredPosition = new Vector2();
    final Vector2 velocity = new Vector2();

    protected Animation animation = null;

    public Shot(Animation animation) {
    	super(animation.getKeyFrame(0));
    	this.animation = animation;
    }

    public void Initialize(float x, float y, boolean facesRight, boolean normalGravity) {
        this.setPosition(x, y);
        this.shotGoesRight = facesRight;
        if (normalGravity)
        	this.velocity.y = 100;
        else
        	this.velocity.y = -100;
    }

    @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime+=delta, true));
        super.act(delta);
    }
}
