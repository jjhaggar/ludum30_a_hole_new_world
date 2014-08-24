package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class Boss extends Image {
    final float VELOCITY = 50f;
    final float ATTACK_VELOCITY = 120f;
    enum State {
        Standing, Walking, Jumping, Falling, Attack, Summon, Hurting, Die
    }

    enum FlowState {
    	WalkingLeft, WalkingRight, Jumping, Transition, Attack, Summon, BeingHurt, Die
    }

    Vector2 desiredPosition = new Vector2();
    final Vector2 velocity = new Vector2();
    State state = State.Standing;
    FlowState flowState = FlowState.WalkingLeft;
    boolean facesRight = true;
    public boolean updateVelocity;
    public boolean setToDie = false;
    public boolean grounded;
    public HUDCounter counter = new HUDCounter(24);  //the same as megaman enemies

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
    float flowTime = 0;
    public float offSetX;
	public boolean invincible = false;
	public boolean toggle = false;

	public Boss(Animation animation) {
		super(animation.getKeyFrame(0));
		this.animation = animation;
		offSetX = ((AtlasRegion)animation.getKeyFrame(0)).offsetX;
	}
	public Rectangle getRect() {
        this.rect.set(this.getX() + offSetX , this.getY(), this.getWidth(), this.getHeight() - 10);
        return this.rect;

    }

    public void die(){
    	//animate, sound and set to die
        Assets.playSound("bossDead");
    	this.setToDie = true;
    	System.out.println("YOU KILL THE BOSS");
    }

    public void beingHit() {
        Assets.playSound("bossHurt");
        int lifes = this.counter.lostLife();
        if (lifes <= 0) {
            this.die();
        }
        Timer.schedule(new Task() {
            @Override
            public void run() {
                Boss.this.invincible = false;
            }
        }, 1);
    }

    @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime+=delta, true));
        super.act(delta);
    }

}
