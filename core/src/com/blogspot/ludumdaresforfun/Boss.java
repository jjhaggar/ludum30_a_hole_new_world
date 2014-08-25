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
    final int ACTIVATE_DISTANCE = 250;
    final int MAX_LIFES = 24;
    enum State {
        Standing, Walking, Jumping, Falling, Attack, Summon, Hurting, Die
    }

    enum FlowState {
    	WalkingLeft, WalkingRight, Jumping, Transition, Attack, Summon, BeingHurt, Die, Standing
    }

    Vector2 desiredPosition = new Vector2();
    final Vector2 velocity = new Vector2();
    State state = State.Standing;
    FlowState flowState = FlowState.WalkingLeft;
    boolean facesRight = true;
    public boolean updateVelocity;
    public boolean setToDie = false;
    public boolean grounded;
    public Counter counter = new Counter(this.MAX_LIFES);  //the same as megaman enemies

    public int lifesToGain = 0;
    public float lifesTimer = 0f;

    public enum Direction {
        Left, Right
    }
    public Direction dir = Direction.Right;

    public Rectangle rect = new Rectangle();

    public int diffInitialPos = 0;
    public final int RANGE = 100;
    public final int ATTACK_DISTANCE = 15 * 16;

    protected Animation animation = null;
    float stateTime = 0;
    float flowTime = 0;
    public float offSetX;
	public boolean invincible = false;
	public boolean toggle = false;
	public float offSetY;
	//public AtlasRegion actualFrame;
	public AtlasRegion actualFrame;

	public Boss(Animation animation) {
		super(animation.getKeyFrame(0));
		this.animation = animation;
		this.actualFrame = ((AtlasRegion)animation.getKeyFrame(0));
		this.offSetX = this.actualFrame.offsetX;
		this.offSetY = this.actualFrame.offsetY;
	}
	public Rectangle getRect() {
        this.rect.set((this.getX() + this.actualFrame.offsetX) - this.offSetX, (this.getY() + this.actualFrame.offsetY) - this.offSetY , this.actualFrame.packedWidth, this.actualFrame.packedHeight);
        return this.rect;

    }

    public void die(){
    	//animate, sound and set to die
    	if (!this.setToDie == true){
    		Assets.playSound("bossDead");
    		this.setToDie = true;
    		System.out.println("YOU KILL THE BOSS");
    		this.stateTime = 0;
    	}
    }

    public void beingHit() {
    	if (!this.setToDie){
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
