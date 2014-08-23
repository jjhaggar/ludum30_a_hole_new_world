package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.blogspot.ludumdaresforfun.Enemy.Direction;
import com.blogspot.ludumdaresforfun.Enemy.State;

public class Boss extends Image {
    final float VELOCITY = 50f;
    final float ATTACK_VELOCITY = 120f;
    enum State {
        Walking, Running, Hurting
    }
    Vector2 desiredPosition = new Vector2();
    final Vector2 velocity = new Vector2();
    State state = State.Walking;
    boolean facesRight = true;
    public boolean updateVelocity;
    public boolean setToDie = false;

    public enum Direction {
        Left, Right
    }
    public Direction dir = Direction.Right;

    public Rectangle rect = new Rectangle();

    public int diffInitialPos = 0;
    public final int RANGE = 100;
    public final int ATTACK_DISTANCE = 50;

    protected Animation animation = null;
    float stateTime = 0;

	public Boss(Animation animation) {
		super(animation.getKeyFrame(0));
		this.animation = animation;
	}
	public Rectangle getRect() {
        this.rect.set(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        return this.rect;

    }

    public void die(){
    	//animate, sound and set to die
    	setToDie = true;
    }

    @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime+=delta, true));
        super.act(delta);
    }


}
