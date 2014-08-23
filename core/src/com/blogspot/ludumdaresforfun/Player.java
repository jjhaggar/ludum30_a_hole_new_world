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
    final float JUMP_VELOCITY = 255f; // 210f;
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

    public void beingHit() {
        this.invincible = true;
        Timer.schedule(new Task() {
            @Override
            public void run() {
                Player.this.invincible = false;
            }
        }, 1);

    }

    @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime+=delta, true));
        super.act(delta);
    }
}
