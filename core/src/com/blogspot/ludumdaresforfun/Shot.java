package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Shot extends Image {
    float SHOT_VELOCITY_X = 210f;
    float SHOT_VELOCITY_Y = 150f;
    float stateTime = 0;
    boolean shotGoesRight;
    boolean normalGravity;
    Vector2 desiredPosition = new Vector2();
    final Vector2 velocity = new Vector2();
    public Rectangle rect = new Rectangle();

    protected Animation animation = null;
    public float offSetX;
    public AtlasRegion actualFrame;

    public Shot(Animation animation) {
    	super(animation.getKeyFrame(0));
    	this.animation = animation;
    	actualFrame = ((AtlasRegion)animation.getKeyFrame(0));
    	this.offSetX = actualFrame.offsetX;
    }

    public Rectangle getRect() {
        this.rect.set(this.getX(), this.getY(),this.actualFrame.packedWidth, this.actualFrame.packedHeight);
        return this.rect;

    }

    public void Initialize(float x, float y, boolean facesRight, boolean normalGravity) {
        this.setPosition(x, y);
        this.shotGoesRight = facesRight;
        if (facesRight)
        	this.velocity.x = this.SHOT_VELOCITY_X;
        else
        	this.velocity.x = -this.SHOT_VELOCITY_X;

        if (normalGravity)
        	this.velocity.y = this.SHOT_VELOCITY_Y;
        else
        	this.velocity.y = -this.SHOT_VELOCITY_Y;

        this.normalGravity = normalGravity;
    }

    @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime+=delta, true));
        super.act(delta);
    }
}
