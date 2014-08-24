package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class Player extends Image {
    final float MAX_VELOCITY = 120f;
    final float JUMP_VELOCITY = 300f; // 210f;
    final int MAX_LIFES = 5;
    enum State {
        Standing, Walking, Jumping, StandingShooting, Attacking, Intro, BeingHit, Die
    }
    Vector2 desiredPosition = new Vector2();
    final Vector2 velocity = new Vector2();
    State state = State.Walking;
    boolean facesRight = true;
    boolean grounded = true;
    public boolean updateVelocity;
    public boolean shooting = false;
    public boolean invincible = false;
    public boolean noControl = false;
    public boolean dead = false;

    public HUDCounter counter = new HUDCounter(this.MAX_LIFES);


    public Rectangle rect = new Rectangle();
    protected Animation animation = null;
    float stateTime = 0;
    public float offSetX;
    public float offSetY;
    public float rightOffset = 0;
    public AtlasRegion actualFrame;

    public Player(Animation animation) {
        super(animation.getKeyFrame(0));
        this.animation = animation;
        actualFrame = ((AtlasRegion)Assets.playerWalk.getKeyFrame(0));
		offSetX = actualFrame.offsetX;
		offSetY = actualFrame.offsetY;
    }

    public Rectangle getRect() {
    	this.rect.set(this.getX() + actualFrame.offsetX - offSetX, this.getY() + actualFrame.offsetY - offSetY , this.actualFrame.packedWidth, this.actualFrame.packedHeight);
        return this.rect;
    }

 //   @Override
 //   public float getWidth(){
 //   	return 24f;		//taken from picture
 //   }

    public void beingHit() {
        if (!this.invincible) {
            Assets.playSound("playerHurt");
            this.invincible = true;

            this.state = Player.State.BeingHit;
            this.stateTime = 0;
            this.velocity.y = 150;
            this.noControl = true;

            int lifes = this.counter.lostLife();
            if (lifes <= 0) {
                this.die();
            }
            Timer.schedule(new Task() {
                @Override
                public void run() {
                    Player.this.invincible = false;
                }
            }, 1.8f);
        }

    }

    public void die() {
        Assets.playSound("playerDead");
        this.state = Player.State.Die;
        this.stateTime = 0;
        this.noControl = true;
        this.dead = true;
    }

    public int getLifes() {
        return this.counter.currentLifes;
    }

    @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime+=delta, true));
        super.act(delta);
    }
}
