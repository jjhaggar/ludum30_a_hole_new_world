package com.blogspot.ludumdaresforfun;


import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class Enemy extends Image{
    final float VELOCITY = 50f;
    final float ATTACK_VELOCITY = 130f;
    enum State {
        Walking, Running, Hurting, BeingInvoked
    }
    Vector2 desiredPosition = new Vector2();
    final Vector2 velocity = new Vector2();
    State state = State.Walking;
    boolean facesRight = false;
    public boolean updateVelocity;
    public boolean setToDie = false;

    public boolean running = false;
    public float attackHereX = 0;
    public boolean attackRight = false;

    public enum Direction {
        Left, Right
    }
    public Direction dir = Direction.Left;

    public Rectangle rect = new Rectangle();

    public int diffInitialPos = 0;
    public final int RANGE = 100;
    public final int ATTACK_DISTANCE = 100;

    protected Animation animation = null;
    float stateTime = 0;
	float offSetX;
	public boolean dying = false;
	public boolean inScreen = false;
	public AtlasRegion actualFrame;
	public boolean beingInvoked = false;

    public Enemy(Animation animation) {
        super(animation.getKeyFrame(0));
        this.animation = animation;
        this.actualFrame = ((AtlasRegion)animation.getKeyFrame(0));
    }

    public Rectangle getRect() {
        this.rect.set(this.getX(), this.getY(),this.actualFrame.packedWidth, this.actualFrame.packedHeight);
        return this.rect;

    }

    public void die(){
    	// sound and set to die
        Assets.playSound("enemyDead");
        this.state = State.Hurting;
        this.stateTime = 0;
    	this.dying = true;
    	this.velocity.x = 0;
    }

    public void run() {
    	if (this.state != Enemy.State.Running)
    	{
    		Assets.playSound("enemyAttack");

    		if (this.dir == Direction.Left) {
    			this.diffInitialPos -= 2;
    			this.velocity.x = -this.ATTACK_VELOCITY;
    		}
    		else {
    			this.diffInitialPos += 2;
    			this.velocity.x = this.ATTACK_VELOCITY;
    		}
    		this.state = Enemy.State.Running;
    		this.stateTime = 0;
    		this.running = true;

    	}
    }

    public void walk() {
        if (this.dir == Direction.Left) {
            this.diffInitialPos -= 1;
            this.velocity.x = -this.VELOCITY;
        }
        else {
            this.diffInitialPos += 1;
            this.velocity.x = this.VELOCITY;
        }
        this.state = Enemy.State.Walking;
    }

    @Override
    public void act(float delta) {
        ((TextureRegionDrawable)this.getDrawable()).setRegion(this.animation.getKeyFrame(this.stateTime+=delta, true));
        super.act(delta);
    }

}