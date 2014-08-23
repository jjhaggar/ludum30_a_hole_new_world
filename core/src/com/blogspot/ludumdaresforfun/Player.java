package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class Player extends Image {
    final float MAX_VELOCITY = 100f;
    final float JUMP_VELOCITY = 270f; // 210f;
    enum State {
        Standing, Walking, Jumping, StandingShooting
    }
    Vector2 desiredPosition = new Vector2();
    final Vector2 velocity = new Vector2();
    State state = State.Walking;
    boolean facesRight = true;
    boolean grounded = true;
    public boolean updateVelocity;
    public boolean shooting = false;
    public boolean invincible = false;
    public HUDCounter counter = new HUDCounter(5);

    public Rectangle rect = new Rectangle();
    protected Animation animation = null;
    float stateTime = 0;

    public Player(Animation animation) {
        super(animation.getKeyFrame(0));
        this.animation = animation;
    }

    public Rectangle getRect() {
        this.rect.set(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        return this.rect;

    }

    @Override
    public float getWidth(){
    	return 24f;		//taken from picture
    }

    public void beingHit() {
        if (!this.invincible) {
            this.invincible = true;
            int lifes = this.counter.lostLife();
            if (lifes <= 0) {
                this.die();
            }
            Timer.schedule(new Task() {
                @Override
                public void run() {
                    Player.this.invincible = false;
                }
            }, 1);
        }

    }

    public void die() {
        // TODO: animate, sound and reset game
        System.out.println("GAME OVER");
    }

    @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime+=delta, true));
        super.act(delta);
    }
}
