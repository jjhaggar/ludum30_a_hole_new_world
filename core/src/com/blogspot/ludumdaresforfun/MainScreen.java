package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class MainScreen extends BaseScreen {

    public boolean pause = false;
    public boolean toggle = false;
	ConfigControllers configControllers;
	Rectangle playerRect;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private Player player;
	private ShapeRenderer shapeRenderer;
	private TiledMap map;
	private boolean normalGravity = true;
	private boolean bossActive = false;

	private Array<Enemy> enemies = new Array<Enemy>();
	private Array<Rectangle> tiles = new Array<Rectangle>();
	private Array<Rectangle> spikes = new Array<Rectangle>();
	private Array<Shot> shotArray = new Array<Shot>();
	private Array<Vector2> spawns = new Array<Vector2>();
	private Array<Vector2> lifes = new Array<Vector2>();

	private Boss boss;
	private Vector2 door;

	private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject () {
            return new Rectangle();
		}
    };
    HUD hud;

	private final float GRAVITY = -10f;
	final int SCREEN_HEIGHT = 240;
	final int SCREEN_WIDTH = 400;
	final int MAP_HEIGHT;
	final int MAP_WIDTH;
	final int POS_UPPER_WORLD;
	final int POS_LOWER_WORLD;
	final int DISTANCESPAWN = 410;

	final int TILED_SIZE;
	final float activateBossXPosition = 420;
	private float xRightBossWall = 420 + 200;
	private float xLeftBossWall = 420;

	public boolean bossCheckPoint = false;

	float UpOffset = 0;

	public MainScreen(boolean checkPoint) {
		this.shapeRenderer = new ShapeRenderer();

		this.map = new TmxMapLoader().load("newtiles.tmx");
		this.MAP_HEIGHT = (Integer) this.map.getProperties().get("height");
		this.MAP_WIDTH = (Integer) this.map.getProperties().get("width");
		this.TILED_SIZE = (Integer) this.map.getProperties().get("tileheight");
		this.POS_LOWER_WORLD = ((this.MAP_HEIGHT / 2) * this.TILED_SIZE) - this.TILED_SIZE;
		this.POS_UPPER_WORLD = this.MAP_HEIGHT  * this.TILED_SIZE ;

		this.renderer = new OrthogonalTiledMapRenderer(this.map, 1);

		//Assets.dispose(); //TODO: for debugging

		this.camera = new OrthographicCamera();
		this.camera.setToOrtho(false, this.SCREEN_WIDTH, this.SCREEN_HEIGHT);
		this.camera.position.y = this.POS_UPPER_WORLD - this.MAP_HEIGHT;
		this.camera.update();

		this.player = new Player(Assets.playerStand);
        this.boss = new Boss(Assets.bossStanding);

        this.configControllers = new ConfigControllers();
		this.configControllers.init();

		TiledMapTileLayer layerSpawn = (TiledMapTileLayer)(this.map.getLayers().get("Spawns"));
		this.rectPool.freeAll(this.tiles);
		this.tiles.clear();
        for (int x = 0; x <= layerSpawn.getWidth(); x++) {
            for (int y = 0; y <= layerSpawn.getHeight(); y++) {
				Cell cell = layerSpawn.getCell(x, y);
				if (cell != null) {
				    String type = (String) cell.getTile().getProperties().get("type");
				    if (type != null) {
				        if (type.equals("enemy")) {
                            this.spawns.add(new Vector2(x * this.TILED_SIZE, y * this.TILED_SIZE));
                        }
				        else if (type.equals("pollo")) {
                            this.lifes.add(new Vector2(x * this.TILED_SIZE, y * this.TILED_SIZE));
                        }
				        else if (type.equals("player")) {
                            this.player.setPosition(x * this.TILED_SIZE, y * this.TILED_SIZE);
				        }
				        else if (type.equals("boss")) {
                            this.boss.setPosition(x * this.TILED_SIZE, y * this.TILED_SIZE);
                        }
				        else if (type.equals("door")) {
                            this.door = new Vector2(x, y);
                        }
				    }
                }
            }
        }

        this.hud = new HUD(Assets.hudBase);
        if (checkPoint)
        	this.player.setPosition(765*16, 62*16);

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(.9f, .9f, .9f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.updatePlayer(delta);
		this.player.act(delta);

		this.activateBoss();

		if (!this.bossActive){
            //update x
            if ((this.player.getX() - (this.SCREEN_WIDTH / 2)) < this.TILED_SIZE)
            	this.camera.position.x = (this.SCREEN_WIDTH / 2) + this.TILED_SIZE;
            else if ((this.player.getX() + (this.SCREEN_WIDTH / 2)) > (this.MAP_WIDTH * this.TILED_SIZE))
            	this.camera.position.x =  (this.MAP_WIDTH * 16) - (this.SCREEN_WIDTH / 2);
            else
            	this.camera.position.x = this.player.getX();

            //update y
			if ((this.player.getY() - (this.SCREEN_HEIGHT / 2)) >= this.POS_LOWER_WORLD)
                this.camera.position.y = this.player.getY();
            else if (this.player.getY() > this.POS_LOWER_WORLD)
                this.camera.position.y = this.POS_LOWER_WORLD + (this.SCREEN_HEIGHT / 2);
            else if ((this.player.getY() + (this.SCREEN_HEIGHT / 2)) >= this.POS_LOWER_WORLD)
				this.camera.position.y = this.POS_LOWER_WORLD - (this.SCREEN_HEIGHT / 2);
			else
                this.camera.position.y = this.player.getY();

			this.camera.update();
		}

		if (this.spawns.size > 0) {
            Vector2 auxNextSpawn = this.spawns.first();
            if ((this.camera.position.x + this.DISTANCESPAWN) >= auxNextSpawn.x) {
                Enemy auxShadow = new Enemy(Assets.enemyWalk);
                if (auxNextSpawn.y < 240) {
                    auxNextSpawn.y -= 5; // Offset fixed collision
                }
                auxShadow.setPosition(auxNextSpawn.x, auxNextSpawn.y);
                auxShadow.state = Enemy.State.BeingInvoked;
                auxShadow.stateTime = 0;
                auxShadow.beingInvoked = true;
                this.enemies.add(auxShadow);
                this.spawns.removeIndex(0);
            }
		}

		this.collisionLifes(delta);
		this.updateEnemies(delta);
		this.renderer.setView(this.camera);
		this.renderer.render(new int[]{0, 1, 3});

		this.renderEnemies(delta);
		this.renderPlayer(delta);
		for (Shot shot : this.shotArray){
			if (shot != null)
				this.renderShot(shot, delta);
		}
		if (this.bossActive && (this.boss != null)) {
			this.updateBoss(delta);
			if (this.boss != null)
				this.renderBoss(delta);
		}
		this.renderHUD(delta);

	}

	private void updateBoss(float delta) {
		if (this.boss.state.equals(Boss.State.Jumping) || this.boss.state.equals(Boss.State.Falling)){
			if (this.player.getRect().overlaps(new Rectangle (this.boss.getX(), this.boss.getY(), this.boss.getWidth(), this.boss.getHeight()))) {
				this.player.beingHit();
			}
		}
		else{
			if (this.player.getRect().overlaps(this.boss.getRect())) {
				this.player.beingHit();
			}
		}

		this.boss.desiredPosition.y = this.boss.getY();

		this.boss.stateTime += delta;

		if (this.normalGravity)
			this.boss.velocity.add(0, this.GRAVITY);
		else
			this.boss.velocity.add(0, -this.GRAVITY);

		this.boss.velocity.scl(delta);

		this.collisionForBoss(this.boss);

		// unscale the velocity by the inverse delta time and set the latest position
		this.boss.desiredPosition.add(this.boss.velocity);
		this.boss.velocity.scl(1 / delta);
		this.flowBoss(delta);

		this.boss.setPosition(this.boss.desiredPosition.x, this.boss.desiredPosition.y);

		if (this.boss.setToDie && Assets.bossDie.isAnimationFinished(this.boss.stateTime))
			this.boss = null;
	}


	private void flowBoss(float delta) {


		this.changeOfStatesInCaseOfAnimationFinish();

		if (this.boss.flowState == Boss.FlowState.WalkingLeft){
			this.boss.velocity.x = -100;
			this.boss.flowState = Boss.FlowState.Transition;
			this.boss.state = Boss.State.Walking;
		}
		else if (this.boss.flowState == Boss.FlowState.WalkingRight){
			this.boss.velocity.x = 100;
			this.boss.flowState = Boss.FlowState.Transition;
			this.boss.state = Boss.State.Walking;
		}
		else if (this.boss.flowState == Boss.FlowState.Jumping){
			if ((this.boss.getX() - this.player.getX()) > 0)
				this.boss.velocity.x = -200;
			else
				this.boss.velocity.x = 200;

			this.boss.velocity.y = 400;
			this.boss.flowState = Boss.FlowState.Transition;
			this.boss.state = Boss.State.Jumping;

			if (this.boss.getX() > this.xRightBossWall)		 //if going to hit wall turns back
				this.boss.velocity.x = -100;
			else if (this.boss.getX() < this.xLeftBossWall)							//same for other wall
				this.boss.velocity.x = 100;
		}
		else if (this.boss.flowState == Boss.FlowState.Attack){
			if ((this.boss.getX() - this.player.getX()) > 0)
				this.boss.facesRight = false;
			else
				this.boss.facesRight = true;

			this.boss.velocity.x = 0;
			//attack to character(detect position and collision)
			this.boss.flowState = Boss.FlowState.Transition;
			this.boss.state = Boss.State.Attack;
			this.boss.stateTime = 0;
		}
		else if (this.boss.flowState == Boss.FlowState.Summon){
			this.boss.velocity.x = 0;
			this.Summon();
			this.boss.flowState = Boss.FlowState.Transition;
			this.boss.state = Boss.State.Summon;
			this.boss.stateTime = 0;
		}
		else if (this.boss.flowState == Boss.FlowState.Standing){
			this.boss.velocity.x = 0;
			this.boss.flowState = Boss.FlowState.Transition;
			this.boss.counter.gainLife(3);
			//addSound(“gainLife”);
			this.boss.state = Boss.State.Standing;
		}
		else if (this.boss.flowState == Boss.FlowState.Die){
			this.boss.velocity.x = 0;
			this.boss.velocity.y = 0;
		}
		else if (this.boss.flowState == Boss.FlowState.Transition){ //door.x is the left side of the tiles

			if (!this.boss.state.equals(Boss.State.Die)){
				if (this.boss.getX() > this.xRightBossWall)		 //if going to hit wall turns back
					this.boss.flowState = Boss.FlowState.WalkingLeft;
				else if (this.boss.getX() < this.xLeftBossWall)							//same for other wall
					this.boss.flowState = Boss.FlowState.WalkingRight;
				else if (this.boss.flowTime > 2){							//takes pseudo-random action
					int nextState = (int)Math.round(Math.random() * 7);

					if ((Math.abs(this.boss.getX() -
							this.player.getX()) < 48) && ((nextState % 2) == 0))	//3 tiles far: attacks 50% time
						this.boss.flowState = Boss.FlowState.Attack;
					else if ((nextState == 0) || (nextState == 1))										//one possibility is jump
						this.boss.flowState = Boss.FlowState.Jumping;
					else if ((nextState == 2) || (nextState == 3))
						this.boss.flowState = Boss.FlowState.Summon;				//another summon
					else if ((nextState == 4) || (nextState == 5)){															//or move in your direction
						if ((this.boss.getX() - this.player.getX()) > 0)
							this.boss.flowState = Boss.FlowState.WalkingLeft;
						else
							this.boss.flowState = Boss.FlowState.WalkingRight;
					}
					else
						this.boss.flowState = Boss.FlowState.Standing;

					this.boss.flowTime = 0;
				}
			}
		}
		this.boss.flowTime += delta;
	}


	private void Summon() {
		this.spawns.add(new Vector2(this.boss.getX(), this.boss.getY() + 60));
		if ((this.boss.getX() + 30) < this.xRightBossWall)
			this.spawns.add(new Vector2(this.boss.getX() + 10 + this.boss.getWidth(), this.boss.getY() + 5));
		if ((this.boss.getX() - 30) > this.xLeftBossWall)
			this.spawns.add(new Vector2(this.boss.getX() - 30, this.boss.getY() + 5));
	}

	private void changeOfStatesInCaseOfAnimationFinish() {
		if ((this.boss.state == Boss.State.Jumping) && (this.boss.velocity.y < 0))
			this.boss.state = Boss.State.Falling;
		if (this.boss.setToDie)
			this.boss.flowState = Boss.FlowState.Die;
	}

	private void renderBoss(float delta) {
		this.boss.actualFrame = null;

		if (this.boss.velocity.x > 0)
			this.boss.facesRight = true;
		else if (this.boss.velocity.x < 0)
			this.boss.facesRight = false;

		if (this.boss.state == Boss.State.Standing)
			this.boss.actualFrame = (AtlasRegion)Assets.bossStanding.getKeyFrame(this.boss.stateTime);
		else if (this.boss.state == Boss.State.Walking)
			this.boss.actualFrame = (AtlasRegion)Assets.bossWalking.getKeyFrame(this.boss.stateTime);
		else if (this.boss.state == Boss.State.Attack)
			this.boss.actualFrame = (AtlasRegion)Assets.bossAttack.getKeyFrame(this.boss.stateTime);
		else if (this.boss.state == Boss.State.Jumping)
			this.boss.actualFrame = (AtlasRegion)Assets.bossJumping.getKeyFrame(this.boss.stateTime);
		else if (this.boss.state == Boss.State.Falling)
			this.boss.actualFrame = (AtlasRegion)Assets.bossFalling.getKeyFrame(this.boss.stateTime);
		else if (this.boss.state == Boss.State.Hurting)
			this.boss.actualFrame = (AtlasRegion)Assets.bossGethit.getKeyFrame(this.boss.stateTime);
		else if (this.boss.state == Boss.State.Die)
			this.boss.actualFrame = (AtlasRegion)Assets.bossDie.getKeyFrame(this.boss.stateTime);
		else if (this.boss.state == Boss.State.Summon)
			this.boss.actualFrame = (AtlasRegion)Assets.bossSummon.getKeyFrame(this.boss.stateTime);

		if (this.boss.invincible && this.boss.toggle) {
			this.boss.actualFrame = (AtlasRegion)Assets.bossGethit.getKeyFrame(this.player.stateTime);
		    this.boss.toggle = !this.boss.toggle;
		}
		else if (this.boss.invincible && !this.boss.toggle) {
		    this.boss.toggle = !this.boss.toggle;
		}
		else if (!this.boss.invincible) {
		    this.boss.toggle = false;
		}

		Batch batch = this.renderer.getSpriteBatch();
		batch.begin();
		if (this.boss.facesRight) {
			if (this.boss.actualFrame.isFlipX())
				this.boss.actualFrame.flip(true, false);
			batch.draw(this.boss.actualFrame, (this.boss.getX() + this.boss.actualFrame.offsetX) - this.boss.offSetX,
					(this.boss.getY() + this.boss.actualFrame.offsetY) - this.boss.offSetY);
		} else {
			if (!this.boss.actualFrame.isFlipX())
				this.boss.actualFrame.flip(true, false);
			batch.draw(this.boss.actualFrame, (this.boss.getX() + this.boss.actualFrame.offsetX) - this.boss.offSetX,
					(this.boss.getY() + this.boss.actualFrame.offsetY) - this.boss.offSetY);
		}

		batch.end();
	}


	private void renderShot(Shot shot, float deltaTime){
		AtlasRegion frame = null;
		if (shot.state == Shot.State.Normal)
			frame = (AtlasRegion) Assets.playerShot.getKeyFrame(shot.stateTime);
		else if (shot.state == Shot.State.Exploding)
			frame = (AtlasRegion) Assets.playerShotHit.getKeyFrame(shot.stateTime);

		if (!this.normalGravity) {
		    if (!frame.isFlipY())
                frame.flip(false, true);
		}
		else {
		    if (frame.isFlipY())
                frame.flip(false, true);
		}

		Batch batch = this.renderer.getSpriteBatch();
		batch.begin();
		if (shot.shotGoesRight) {
			if (frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, shot.getX(), shot.getY());
		} else {
			if (!frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, shot.getX(), shot.getY());
		}

		batch.end();
	}

    public boolean updateShot(Shot shot, float deltaTime){
    	boolean killMe = false;
        shot.desiredPosition.y = shot.getY();

        shot.stateTime += deltaTime;

		if (this.normalGravity && !shot.state.equals(Shot.State.Exploding))
			shot.velocity.add(0, this.GRAVITY);
		else
			shot.velocity.add(0, -this.GRAVITY);

		shot.velocity.scl(deltaTime);

		//collision (destroy if necessary)
		boolean collided = this.collisionShotEnemy(shot);

		if (!collided)
			collided = this.collisionShot(shot);

		// unscale the velocity by the inverse delta time and set
		// the latest position
		if (shot != null){
			shot.desiredPosition.add(shot.velocity);
			shot.velocity.scl(1 / deltaTime);

			shot.setPosition(shot.desiredPosition.x, shot.desiredPosition.y);
			if (shot.normalGravity && (shot.getY() < this.POS_LOWER_WORLD))
				collided = true;	//dont traspass to the other world
			else if (!shot.normalGravity && (shot.getY() >= this.POS_LOWER_WORLD))
				collided = true;
			else if ((shot.getY() > (this.MAP_HEIGHT * this.TILED_SIZE)) || (shot.getY() < 0))
				collided = true;
		}

		if (collided && !shot.state.equals(Shot.State.Exploding)){
            Assets.playSound("holyWaterBroken");
            shot.state = Shot.State.Exploding;
            shot.stateTime = 0;
            shot.velocity.x = 0f;
            shot.velocity.y = 0f;
		}

		if (Assets.playerShotHit.isAnimationFinished(shot.stateTime) && shot.state.equals(Shot.State.Exploding))
			killMe = true;

		return killMe;
    }


	private boolean collisionShotEnemy(Shot shot) {
		boolean collided = false;

		this.playerRect = this.rectPool.obtain();

		shot.desiredPosition.y = Math.round(shot.getY());
		shot.desiredPosition.x = Math.round(shot.getX());

		this.playerRect = shot.getRect();

		for (Enemy enemy : this.enemies){
			if (this.playerRect.overlaps(enemy.getRect())) {
				if (!enemy.dying){
					enemy.die();
					collided = true;
					break;
				}
			}
		}

		if ((this.boss != null) && this.playerRect.overlaps(this.boss.getRect())) {

			if (!this.boss.invincible)
				this.boss.beingHit();

		    if (!this.boss.setToDie){
		    	this.boss.invincible = true;		//activates also the flickering
		    }
		    else if (this.boss.state != Boss.State.Die){
		    	this.boss.state = Boss.State.Die;
		    }
		    collided = true;
		}


		return collided;
	}


	private boolean collisionShot(Shot shot) {
		this.playerRect = this.rectPool.obtain();

		shot.desiredPosition.y = Math.round(shot.getY());
		shot.desiredPosition.x = Math.round(shot.getX());

		this.playerRect = shot.getRect();

		int startX, startY, endX, endY;

		if (shot.velocity.x > 0) {	//this.raya.velocity.x > 0
			startX = endX = (int)((shot.desiredPosition.x + shot.velocity.x + shot.actualFrame.packedWidth) / 16);
		}
		else {
			startX = endX = (int)((shot.desiredPosition.x + shot.velocity.x) / 16);
		}

		startY = (int)((shot.desiredPosition.y) / 16);
		endY = (int)((shot.desiredPosition.y + shot.actualFrame.packedHeight) / 16);

		this.getTiles(startX, startY, endX, endY, this.tiles);

		this.playerRect.x += shot.velocity.x;

		for (Rectangle tile : this.tiles){
			if (this.playerRect.overlaps(tile)) {
				shot = null;
				return true;
				}
		}

		this.playerRect.x = shot.desiredPosition.x;

		if (this.normalGravity){
			if (shot.velocity.y > 0) {
				startY = endY = (int)((shot.desiredPosition.y + shot.velocity.y + shot.actualFrame.packedHeight) / 16f);
			}
			else {
				startY = endY = (int)((shot.desiredPosition.y + shot.velocity.y) / 16f);
			}
		}
		else{
			if (shot.velocity.y < 0) {

				startY = endY = (int)((shot.desiredPosition.y + shot.velocity.y) / 16f);
			}
			else {
				startY = endY = (int)((shot.desiredPosition.y + shot.velocity.y + shot.actualFrame.packedHeight ) / 16f);
			}
		}

		startX = (int)((shot.desiredPosition.x + shot.offSetX) / 16);					//16 tile size
		endX = (int)((shot.desiredPosition.x + shot.actualFrame.packedWidth) / 16);


		// System.out.println(startX + " " + startY + " " + endX + " " + endY);

		this.getTiles(startX, startY, endX, endY, this.tiles);

		shot.desiredPosition.y += (int)(shot.velocity.y);

		for (Rectangle tile : this.tiles) {
			if (this.playerRect.overlaps(tile)) {
				shot = null;
				return true;
				}
			}
		return false;
	}

	private void renderPlayer (float deltaTime) {
		AtlasRegion frame = null;
		switch (this.player.state) {
		case Standing:
			frame = (AtlasRegion)Assets.playerStand.getKeyFrame(this.player.stateTime);
			break;
		case Walking:
			frame = (AtlasRegion)Assets.playerWalk.getKeyFrame(this.player.stateTime);
			break;
		case Jumping:
			frame = (AtlasRegion)Assets.playerJump.getKeyFrame(this.player.stateTime);
			break;
		case Intro:
			frame = (AtlasRegion)Assets.playerIntro.getKeyFrame(this.player.stateTime);
			break;
		case Attacking:
			frame = (AtlasRegion)Assets.playerAttack.getKeyFrame(this.player.stateTime);
			break;
		case Die:
			frame = (AtlasRegion)Assets.playerDie.getKeyFrame(this.player.stateTime);
			break;
		case BeingHit:
			frame = (AtlasRegion)Assets.playerBeingHit.getKeyFrame(this.player.stateTime);
			break;
		}
		if (this.player.invincible && this.toggle) {
			frame = (AtlasRegion)Assets.playerEmpty.getKeyFrame(this.player.stateTime);
		    this.toggle = !this.toggle;
		}
		else if (this.player.invincible && !this.toggle) {
		    this.toggle = !this.toggle;
		}
		else if (!this.player.invincible) {
		    this.toggle = false;
		}
		// draw the koala, depending on the current velocity
		// on the x-axis, draw the koala facing either right
		// or left
		Batch batch = this.renderer.getSpriteBatch();
		batch.begin();
		if (this.player.facesRight && frame.isFlipX()) {
            frame.flip(true, false);
            this.player.rightOffset = 1f;	//fix differences
		}
		else if (!this.player.facesRight && !frame.isFlipX()) {
			frame.flip(true, false);
			this.player.rightOffset = -4f;   //fix differences
		}

		if (this.normalGravity && frame.isFlipY()) {
			frame.flip(false, true);
			this.UpOffset = 0;
		}
		else if (!this.normalGravity && !frame.isFlipY()){
			frame.flip(false, true);
			this.UpOffset = -2;
		}

		//batch.draw(frame, this.player.getX() + frame.offsetX, this.player.getY() + frame.offsetY + this.UpOffset);
		batch.draw(frame, (this.player.getX() + this.player.actualFrame.offsetX) - this.player.offSetX, (this.player.getY() + this.player.actualFrame.offsetY) - this.player.offSetY);

		batch.end();
		this.shapeRenderer.begin(ShapeType.Filled);

		this.shapeRenderer.setColor(Color.BLACK);

		this.getTiles(0, 0, 25, 15, this.tiles);
		//for (Rectangle tile : this.tiles) {
		//	shapeRenderer.rect(tile.x * 1.6f, tile.y * 2, tile.width * 2, tile.height * 2);
		//}
		this.shapeRenderer.setColor(Color.RED);
		//shapeRenderer.rect(playerRect.x * 1.6f, playerRect.y * 2, playerRect.width * 2, playerRect.height * 2);

        this.shapeRenderer.end();
    }

	private void renderEnemies(float deltaTime) {
	    for (Enemy enemy : this.enemies) {
	    		enemy.actualFrame = null;
            switch (enemy.state) {
            case Walking:
            	enemy.actualFrame = (AtlasRegion)Assets.enemyWalk.getKeyFrame(enemy.stateTime);
                break;
            case Running:
            	enemy.actualFrame = (AtlasRegion)Assets.enemyRun.getKeyFrame(enemy.stateTime);
                break;
            case Hurting:
            	enemy.actualFrame = (AtlasRegion)Assets.enemyHurt.getKeyFrame(enemy.stateTime);
                break;
            case BeingInvoked:
            	enemy.actualFrame = (AtlasRegion)Assets.enemyAppearing.getKeyFrame(enemy.stateTime);
                break;
            }

            Batch batch = this.renderer.getSpriteBatch();
            batch.begin();
            if (enemy.facesRight) {
                if (enemy.actualFrame.isFlipX())
                	enemy.actualFrame.flip(true, false);
                batch.draw(enemy.actualFrame, enemy.getX(), enemy.getY());
            } else {
                if (!enemy.actualFrame.isFlipX())
                	enemy.actualFrame.flip(true, false);
                batch.draw(enemy.actualFrame, enemy.getX(), enemy.getY());
            }
            batch.end();

            this.shapeRenderer.begin(ShapeType.Filled);
            this.shapeRenderer.setColor(Color.BLACK);
            this.getTiles(0, 0, 25, 15, this.tiles);
            this.shapeRenderer.setColor(Color.RED);
            this.shapeRenderer.end();
	    }
	}

	private void renderHUD(float deltaTime) {
        AtlasRegion frame = null;
        AtlasRegion playerLife = null;
        AtlasRegion bossLife = null;
        AtlasRegion bossHead = null;
        frame = (AtlasRegion)Assets.hudBase.getKeyFrame(this.hud.stateTime);
        playerLife = (AtlasRegion)Assets.hudLifePlayer.getKeyFrame(this.hud.stateTime);
        bossLife = (AtlasRegion)Assets.hudLifeBoss.getKeyFrame(this.hud.stateTime);
        bossHead = (AtlasRegion)Assets.hudBossHead.getKeyFrame(this.hud.stateTime);

        Batch batch = this.renderer.getSpriteBatch();
        batch.begin();
        if (this.normalGravity) {
            batch.draw(frame, (this.camera.position.x - (this.SCREEN_WIDTH / 2)),
                    (this.camera.position.y + (this.SCREEN_HEIGHT / 2)) - this.TILED_SIZE);
            for (int pl=0; pl < this.player.getLifes(); pl++) {
                batch.draw(playerLife,
                        ((this.camera.position.x - (this.SCREEN_WIDTH / 2)) + Assets.offsetLifePlayer.x + (pl * (playerLife.getRegionWidth() + this.hud.OFFSET_LIFES_PLAYER))),
                        (((this.camera.position.y + (this.SCREEN_HEIGHT / 2)) - this.TILED_SIZE - Assets.offsetLifePlayer.y) + playerLife.getRegionHeight()));
            }
            if (this.bossActive && (this.boss != null)) {
                batch.draw(bossHead, ((this.camera.position.x - (this.SCREEN_WIDTH / 2)) + Assets.offsetBoosHead),
                    (this.camera.position.y + (this.SCREEN_HEIGHT / 2)) - this.TILED_SIZE);
                for (int bl=0; bl < this.boss.getLifes(); bl++) {
                    batch.draw(bossLife,
                            ((this.camera.position.x - (this.SCREEN_WIDTH / 2)) + Assets.offsetLifeBoss.x + (bl * (bossLife.getRegionWidth() + this.hud.OFFSET_LIFES_BOSS))),
                            (((this.camera.position.y + (this.SCREEN_HEIGHT / 2)) - this.TILED_SIZE - Assets.offsetLifeBoss.y) + bossLife.getRegionHeight()));
                }
            }
        } else {
            batch.draw(frame, this.camera.position.x - (this.SCREEN_WIDTH / 2),
                    this.camera.position.y - (this.SCREEN_HEIGHT / 2));
            for (int pl=0; pl < this.player.getLifes(); pl++) {
                batch.draw(playerLife,
                        ((this.camera.position.x - (this.SCREEN_WIDTH / 2)) + Assets.offsetLifePlayer.x + (pl * (playerLife.getRegionWidth() + this.hud.OFFSET_LIFES_PLAYER))),
                        (((this.camera.position.y - (this.SCREEN_HEIGHT / 2)) + Assets.offsetLifePlayer.y)));
            }
        }
        batch.end();

        this.shapeRenderer.begin(ShapeType.Filled);
        this.shapeRenderer.setColor(Color.BLACK);
        this.getTiles(0, 0, 25, 15, this.tiles);
        this.shapeRenderer.setColor(Color.RED);
        this.shapeRenderer.end();
	}

	private void collisionLifes(float deltaTime) {
        Array<Vector2> obtainLifes = new Array<Vector2>();
	    for (Vector2 life : this.lifes) {
	        if (this.normalGravity) {
                if ((life.dst(this.player.getX(), this.player.getCenterY()) < this.player.getWidth()) &&
                        (this.player.getLifes() < this.player.MAX_LIFES)) {
                    this.player.counter.gainLife(1);
                    obtainLifes.add(life);
                    // Remove life in map
                    TiledMapTileLayer layerPlantfs = (TiledMapTileLayer)(this.map.getLayers().get("Platfs"));
                    layerPlantfs.setCell((int)life.x / this.TILED_SIZE, (int)life.y / this.TILED_SIZE, null);
                }
	        }
	        else {
                if ((life.dst(this.player.getX(), this.player.getY()) < this.player.getWidth()) &&
                        (this.player.getLifes() < this.player.MAX_LIFES)) {
                    this.player.counter.gainLife(1);
                    obtainLifes.add(life);
                    // Remove life in map
                    TiledMapTileLayer layerPlantfs = (TiledMapTileLayer)(this.map.getLayers().get("Platfs"));
                    layerPlantfs.setCell((int)life.x / this.TILED_SIZE, (int)life.y / this.TILED_SIZE, null);
                }
	        }
	    }
	    this.lifes.removeAll(obtainLifes, false);
	}

	private void updateEnemies(float deltaTime) {
	    for (Enemy enemy : this.enemies) {

	    	this.isEnemyInScreen(enemy);
	    	this.isEnemyFinishedInvoking(enemy);

	        // Collision between player vs enemy
	    	if (!enemy.dying){
	    		if (this.player.getX() > enemy.getX()){
	    			if (this.player.getRect().overlaps(enemy.getRect())) {
	    				this.player.beingHit();
	    			}
	    		}
	    		else{
	    			if (this.player.getRect().overlaps(enemy.getRect())) {
	    				this.player.beingHit();
	    			}
	    		}
	    	}

	        enemy.stateTime += deltaTime;
	        // Check if player is invincible and check distance to player for attack him.
	        if (!enemy.running && !enemy.dying && !enemy.beingInvoked && enemy.inScreen){
	        	if (!this.player.invincible &&
	        	        (Math.abs(((enemy.getY() + (enemy.getHeight() / 2))
                        - (this.player.getY() + (this.player.getHeight() / 2)))) <= this.player.getHeight())) {
	        		if (enemy.getX() < this.player.getX()) {
	        			if ((enemy.getX() + enemy.ATTACK_DISTANCE) >= (this.player.getX() + this.player.getWidth())) {
	        				enemy.dir = Enemy.Direction.Right;
	        				enemy.facesRight = true;
	        				enemy.run();
	        				enemy.attackHereX = this.player.getX();
	        				enemy.attackRight = true;
	        			}
	        		}
	        		else {
	        			if ((enemy.getX() - enemy.ATTACK_DISTANCE) <= this.player.getX()) {
	        				enemy.dir = Enemy.Direction.Left;
	        				enemy.facesRight = false;
	        				enemy.run();
	        				enemy.attackHereX = this.player.getX();
	        				enemy.attackRight = false;
	        			}
	        		}
	        	}

	        	else if (enemy.dir == Enemy.Direction.Left) {
	        		if (-enemy.RANGE >= enemy.diffInitialPos) {
	        			enemy.dir = Enemy.Direction.Right;
	        			enemy.facesRight = true;
	        		}
	        		enemy.walk();
	        	}
	        	else if (enemy.dir == Enemy.Direction.Right) {
	        		if (enemy.diffInitialPos >= enemy.RANGE) {
	        			enemy.dir = Enemy.Direction.Left;
	        			enemy.facesRight = false;
	        		}
	        		enemy.walk();
	        	}
	        }
	        else if ((enemy.getX() > enemy.attackHereX) && enemy.attackRight)
	        	enemy.running = false;
	        else if ((enemy.getX() < enemy.attackHereX) && !enemy.attackRight)
	        	enemy.running = false;

            enemy.velocity.scl(deltaTime);

            // Enviroment collision
            enemy.desiredPosition.y = Math.round(enemy.getY());
            enemy.desiredPosition.x = Math.round(enemy.getX());
            int startX, startY, endX, endY;
            if (enemy.velocity.x > 0) {
                startX = endX = (int)((enemy.desiredPosition.x + enemy.velocity.x + enemy.getWidth()) / this.TILED_SIZE);
            }
            else {
                startX = endX = (int)((enemy.desiredPosition.x + enemy.velocity.x) / this.TILED_SIZE);
            }
            startY = (int) enemy.getY() / this.TILED_SIZE;
            endY =  (int) (enemy.getY() + enemy.getHeight()) / this.TILED_SIZE;
            this.getTiles(startX, startY, endX, endY, this.tiles);

            enemy.getRect();
            enemy.rect.x += enemy.velocity.x;

            for (Rectangle tile : this.tiles) {
                if (enemy.rect.overlaps(tile)) {
                    enemy.velocity.x = 0;
                    enemy.running = false;
                    break;
                }
            }

            enemy.rect.x = enemy.desiredPosition.x;

            enemy.desiredPosition.add(enemy.velocity);
            enemy.velocity.scl(1 / deltaTime);

            enemy.setPosition(enemy.desiredPosition.x, enemy.desiredPosition.y);

            if (Assets.playerDie.isAnimationFinished(enemy.stateTime) && enemy.dying){
    			enemy.setToDie = true;
    		}

        }

	    int i = 0;
		boolean[] toBeDeleted = new boolean[this.enemies.size];
		for (Enemy enemy : this.enemies){
			if (enemy != null){
				if(enemy.setToDie == true) //&& animation finished
					toBeDeleted[i] = true;
			}
			i++;
		}

		for(int j = 0; j < toBeDeleted.length; j++){
			if (toBeDeleted[j] && (this.enemies.size >= (j + 1)))
				this.enemies.removeIndex(j);
		}
	}

	private void isEnemyFinishedInvoking(Enemy enemy) {

		if (Assets.enemyAppearing.isAnimationFinished(enemy.stateTime) && enemy.state.equals(Enemy.State.BeingInvoked)){
			enemy.beingInvoked = false;
			enemy.state = Enemy.State.Walking;
		}

	}

	private void isEnemyInScreen(Enemy enemy) {
		//TODO: Maybe change so that they activate a little bit before they enter the screen
		if ((enemy.getX() > (this.camera.position.x - (this.SCREEN_WIDTH / 2)))
				&& (enemy.getX() < (this.camera.position.x + (this.SCREEN_WIDTH / 2)))
				&& ((enemy.getY() > (this.camera.position.y - (this.SCREEN_HEIGHT / 2)))
				&& (enemy.getX() < (this.camera.position.x + (this.SCREEN_HEIGHT / 2))))){
			enemy.inScreen = true;
		}
	}

	private void updatePlayer(float deltaTime) {

		if (deltaTime == 0)
			return;
		this.player.stateTime += deltaTime;

		this.player.desiredPosition.x = this.player.getX();
		this.player.desiredPosition.y = this.player.getY();

		this.movingShootingJumping(deltaTime);
		this.gravityAndClamping();

		this.player.velocity.scl(deltaTime);

		//retreat if noControl //velocity y is changed in beingHit
		if (this.player.noControl){
			if (this.player.facesRight)
				this.player.velocity.x = -120f * deltaTime;
			else
				this.player.velocity.x = 120 * deltaTime;
		}

		boolean collisionSpike = this.collisionWallsAndSpike();

		// unscale the velocity by the inverse delta time and set the latest position
		this.player.desiredPosition.add(this.player.velocity);
		this.player.velocity.scl(1 / deltaTime);

		if (Assets.playerBeingHit.isAnimationFinished(this.player.stateTime) && !this.player.dead)
			this.player.noControl = false;

		if (this.player.noControl == false)
			this.player.velocity.x *= 0;		//0 is totally stopped if not pressed

        this.player.setPosition(this.player.desiredPosition.x, this.player.desiredPosition.y);

		if (Assets.playerDie.isAnimationFinished(this.player.stateTime) && this.player.dead){
			this.gameOver();
		}
		if (collisionSpike) {
		    this.player.beingHit();
		}
	}

	private void gameOver() {
		LD.getInstance().GAMEOVER_SCREEN = new GameOverScreen(this.bossCheckPoint);
        LD.getInstance().setScreen(LD.getInstance().GAMEOVER_SCREEN);
	}

	private void activateBoss() {
		if (this.boss == null)
			return;
		if ((this.player.getX() >= (this.boss.getX() - this.boss.ACTIVATE_DISTANCE)) && !this.bossActive) {
			this.bossActive = true;

			this.bossCheckPoint = true;

			this.camera.position.x = (this.MAP_WIDTH * this.TILED_SIZE) - (this.SCREEN_WIDTH / 2);
			this.camera.update();

			this.xRightBossWall = (((this.door.x * this.TILED_SIZE) + this.SCREEN_WIDTH) -  (this.TILED_SIZE * 4) - 16);
			this.xLeftBossWall = ((this.door.x * this.TILED_SIZE) + (this.TILED_SIZE * 2)) + 8;

			Assets.musicStage.stop();
			Assets.musicBoss.setLooping(true);
			Assets.musicBoss.play();

			//close door
			TiledMapTileLayer layerSpawn = null;
			Cell cell = null;
			//door = new Vector2(789 - 25, 61);

			layerSpawn = (TiledMapTileLayer)(this.map.getLayers().get("Platfs"));
			cell = layerSpawn.getCell((int)this.door.x, (int)this.door.y); //has to be solid block

	        layerSpawn.setCell((int)this.door.x, (int)this.door.y + 1, cell);
	        layerSpawn.setCell((int)this.door.x, (int)this.door.y + 2, cell);
	        layerSpawn.setCell((int)this.door.x + 1, (int)this.door.y + 1, cell);
	        layerSpawn.setCell((int)this.door.x + 1, (int)this.door.y + 2, cell);

	        layerSpawn = (TiledMapTileLayer)(this.map.getLayers().get("Collisions"));
	        cell = layerSpawn.getCell((int)this.door.x, (int)this.door.y);
	        layerSpawn.setCell((int)this.door.x, (int)this.door.y + 1, cell);
	        layerSpawn.setCell((int)this.door.x, (int)this.door.y + 2, cell);
	        layerSpawn.setCell((int)this.door.x + 1, (int)this.door.y + 1, cell);
	        layerSpawn.setCell((int)this.door.x + 1, (int)this.door.y + 2, cell);


		}
	}


	private void gravityAndClamping() {
		if (this.normalGravity)
			this.player.velocity.add(0, this.GRAVITY);
		else
			this.player.velocity.add(0, -this.GRAVITY);

		if (this.player.getY() < this.POS_LOWER_WORLD){
			//this.camera.position.y = this.POS_LOWER_WORLD;
			if (this.normalGravity == true){
				this.normalGravity = false;
				this.player.velocity.y = -this.player.JUMP_VELOCITY * 1.01f;	//3 tiles in both
			}
		}
		else {
			//this.camera.position.y = 0;//this.yPosUpperWorld;
			if (this.normalGravity == false){
				this.normalGravity = true;
				this.player.velocity.y = this.player.JUMP_VELOCITY / 1.3f;		//3 tiles in both
			}
		}

		// clamp the velocity to the maximum, x-axis only
		if (Math.abs(this.player.velocity.x) > this.player.MAX_VELOCITY) {
			this.player.velocity.x = Math.signum(this.player.velocity.x) * this.player.MAX_VELOCITY;
		}

		// clamp the velocity to 0 if it's < 1, and set the state to standign
		if (Math.abs(this.player.velocity.x) < 1) {
			this.player.velocity.x = 0;
			if (this.player.grounded && Assets.playerAttack.isAnimationFinished(this.player.stateTime) &&
					Assets.playerBeingHit.isAnimationFinished(this.player.stateTime) && !this.player.invincible)
				this.player.state = Player.State.Standing;
		}
	}


	private void movingShootingJumping(float deltaTime) {

		if (this.player.noControl == false){
			if ((Gdx.input.isKeyJustPressed(Keys.S) || this.configControllers.jumpPressed) && this.player.grounded){
				Assets.playSound("playerJump");
				if (this.normalGravity)
					this.player.velocity.y = this.player.JUMP_VELOCITY;
				else
					this.player.velocity.y = -this.player.JUMP_VELOCITY;
				this.player.grounded = false;
				this.player.state = Player.State.Jumping;
				//this.player.stateTime = 0;
			}

			if (Gdx.input.isKeyPressed(Keys.LEFT) || this.configControllers.leftPressed){
				this.player.velocity.x = -this.player.MAX_VELOCITY;
				if (this.player.grounded && Assets.playerAttack.isAnimationFinished(this.player.stateTime)
						&& Assets.playerBeingHit.isAnimationFinished(this.player.stateTime)){
					this.player.state = Player.State.Walking;
					//this.player.stateTime = 0;
				}
				this.player.facesRight = false;
			}

			if (Gdx.input.isKeyPressed(Keys.RIGHT) || this.configControllers.rightPressed){
				this.player.velocity.x = this.player.MAX_VELOCITY;
				if (this.player.grounded && Assets.playerAttack.isAnimationFinished(this.player.stateTime)
						&& Assets.playerBeingHit.isAnimationFinished(this.player.stateTime)){
					this.player.state = Player.State.Walking;
					//this.player.stateTime = 0;
				}
				this.player.facesRight = true;
			}

			if ((Gdx.input.isKeyJustPressed(Keys.D) || this.configControllers.shootPressed) && (this.shotArray.size < 3)){
				Assets.playSound("playerAttack");
				Shot shot = new Shot(Assets.playerShot);
				if (this.player.facesRight){
					//-1 necessary to be exactly the same as the other facing
					shot.Initialize((this.player.getCenterX()), ((this.player.getY() + (this.player.getHeight() / 2)) - 10), this.player.facesRight, this.normalGravity);
				}
				else {
					shot.Initialize((this.player.getCenterX()), ((this.player.getY() + (this.player.getHeight() / 2)) - 10), this.player.facesRight, this.normalGravity);
				}
				this.shotArray.add(shot);

				this.player.state = Player.State.Attacking;
				this.player.stateTime = 0;
				this.player.shooting = true;
			}
		}

		if (Assets.playerAttack.isAnimationFinished(this.player.stateTime))
			this.player.shooting = false;

		int i = 0;
		boolean[] toBeDeleted = new boolean[3];
		for (Shot shot : this.shotArray){
			if (shot != null){
				if(this.updateShot(shot, deltaTime) == true)
					toBeDeleted[i] = true;
					//pool of shots?
			}
			i++;
		}

		for(int j = 0; j < toBeDeleted.length; j++){
			if (toBeDeleted[j] && (this.shotArray.size >= (j + 1)))
				this.shotArray.removeIndex(j);
		}
	}

	private boolean collisionForBoss(Boss boss) {
		//collision detection
		// perform collision detection & response, on each axis, separately
		// if the raya is moving right, check the tiles to the right of it's
		// right bounding box edge, otherwise check the ones to the left
		this.playerRect = this.rectPool.obtain();

		boss.desiredPosition.y = Math.round(boss.getY());
		boss.desiredPosition.x = Math.round(boss.getX());

		this.playerRect = this.boss.getRect();

		int startX, startY, endX, endY;

		if (this.player.velocity.x > 0) {
			startX = endX = (int)((boss.desiredPosition.x + boss.velocity.x + this.boss.actualFrame.getRegionWidth()) / this.TILED_SIZE);
		}
		else {
			startX = endX = (int)((boss.desiredPosition.x + boss.velocity.x) / this.TILED_SIZE);
		}

		if (boss.grounded && this.normalGravity){
			startY = (int)((boss.desiredPosition.y) / this.TILED_SIZE);
			endY = (int)((boss.desiredPosition.y + this.boss.actualFrame.getRegionHeight()) / this.TILED_SIZE);
		}
		else if (boss.grounded && !this.normalGravity){
			startY = (int)((boss.desiredPosition.y) / this.TILED_SIZE);
			endY = (int)((boss.desiredPosition.y + this.boss.actualFrame.getRegionHeight()) / this.TILED_SIZE);
		}
		else{
			startY = (int)((boss.desiredPosition.y) / this.TILED_SIZE);
			endY = (int)((boss.desiredPosition.y + this.boss.actualFrame.getRegionHeight()) / this.TILED_SIZE);
		}

		this.getTiles(startX, startY, endX, endY, this.tiles);

		this.playerRect.x += boss.velocity.x;

		for (Rectangle tile : this.tiles) {
			if (this.playerRect.overlaps(tile)) {
				this.player.velocity.x = 0;
				break;
				}
		}

		this.playerRect.x = boss.desiredPosition.x;

		// if the koala is moving upwards, check the tiles to the top of it's
		// top bounding box edge, otherwise check the ones to the bottom

		if (this.normalGravity){
			if (boss.velocity.y > 0) {
				startY = endY = (int)((boss.desiredPosition.y + boss.velocity.y + this.boss.actualFrame.getRegionHeight()) / this.TILED_SIZE);
			}
			else {
				startY = endY = (int)((boss.desiredPosition.y + boss.velocity.y) / this.TILED_SIZE);
			}
		}
		else{
			if (this.player.velocity.y < 0) {
				startY = endY = (int)((boss.desiredPosition.y + boss.velocity.y) / this.TILED_SIZE);
			}
			else {
				startY = endY = (int)((boss.desiredPosition.y + boss.velocity.y + this.boss.actualFrame.getRegionHeight() ) / this.TILED_SIZE);
			}
		}


		startX = (int)(boss.desiredPosition.x / this.TILED_SIZE);					//16 tile size
		endX = (int)((boss.desiredPosition.x + this.boss.actualFrame.getRegionWidth()) / this.TILED_SIZE);

		// System.out.println(startX + " " + startY + " " + endX + " " + endY);

		this.getTiles(startX, startY, endX, endY, this.tiles);

		this.playerRect.y += (int)(boss.velocity.y);

		boolean grounded = boss.grounded;

		for (Rectangle tile : this.tiles) {
			// System.out.println(playerRect.x + " " + playerRect.y + " " + tile.x + " " + tile.y);
			if (this.playerRect.overlaps(tile)) {
				// we actually reset the koala y-position here
				// so it is just below/above the tile we collided with
				// this removes bouncing :)

				if (this.normalGravity){
					if (boss.velocity.y > 0) {
						boss.desiredPosition.y = tile.y - this.boss.actualFrame.getRegionHeight() - 1;
						// we hit a block jumping upwards, let's destroy it!
					}
					else {
						boss.desiredPosition.y = (tile.y + tile.height) - 1;	//in this way he is in the ground
						// if we hit the ground, mark us as grounded so we can jump
						grounded = true;
					}
				}
				else{
					if (boss.velocity.y > 0) {
						//this.player.desiredPosition.y = tile.y - tile.height- 1;
						// if we hit the ground, mark us as grounded so we can jump
						grounded = true;
					}
					else {
						boss.desiredPosition.y = (tile.y + tile.height) - 1;
						// we hit a block jumping upwards, let's destroy it!
					}
				}

				boss.velocity.y = 0;
				break;
				}
			}

		if (this.tiles.size == 0)
			grounded = false;

		//goes together with get
		this.rectPool.free(this.playerRect);

		return grounded;
	}


	private boolean collisionWallsAndSpike() {
		//collision detection
		// perform collision detection & response, on each axis, separately
		// if the raya is moving right, check the tiles to the right of it's
		// right bounding box edge, otherwise check the ones to the left
		this.playerRect = this.rectPool.obtain();

		this.player.desiredPosition.y = Math.round(this.player.getY());
		this.player.desiredPosition.x = Math.round(this.player.getX());

		this.playerRect.set(this.player.getRect());

		int startX, startY, endX, endY;

		if (this.player.velocity.x > 0) {
			startX = endX = (int)((this.player.desiredPosition.x + this.player.velocity.x + this.player.actualFrame.getRegionWidth()) / this.TILED_SIZE);
		}
		else {
			startX = endX = (int)(((this.player.desiredPosition.x + this.player.velocity.x) - 1) / this.TILED_SIZE);
		}

		if (this.player.grounded && this.normalGravity){
			startY = (int)((this.player.desiredPosition.y) / this.TILED_SIZE) + 1;
			endY = (int)((this.player.desiredPosition.y + this.player.actualFrame.getRegionHeight()) / this.TILED_SIZE) + 1;
		}
		else if (this.player.grounded && !this.normalGravity){
			startY = (int)((this.player.desiredPosition.y) / this.TILED_SIZE) - 1;
			endY = (int)((this.player.desiredPosition.y + this.player.actualFrame.getRegionHeight()) / this.TILED_SIZE) - 1;
		}
		else{
			startY = (int)((this.player.desiredPosition.y) / this.TILED_SIZE);
			endY = (int)((this.player.desiredPosition.y + this.player.actualFrame.getRegionHeight()) / this.TILED_SIZE);
		}

		this.getTiles(startX, startY, endX, endY, this.tiles, this.spikes);

		this.playerRect.x += this.player.velocity.x;

		for (Rectangle tile : this.tiles) {
			if (this.playerRect.overlaps(tile)) {
				this.player.velocity.x = 0;
				break;
            }
		}
		for (Rectangle spike : this.spikes) {
			if (this.playerRect.overlaps(spike)) {
				this.player.velocity.x = 0;
				break;
            }
		}

		this.playerRect.x = this.player.desiredPosition.x;

		// if the koala is moving upwards, check the tiles to the top of it's
		// top bounding box edge, otherwise check the ones to the bottom

		if (this.normalGravity){
			if (this.player.velocity.y > 0) {
				startY = endY = (int)((this.player.desiredPosition.y + this.player.velocity.y + this.player.actualFrame.getRegionHeight()) / this.TILED_SIZE);
			}
			else {
				startY = endY = (int)((this.player.desiredPosition.y + this.player.velocity.y) / this.TILED_SIZE);
			}
		}
		else{
			if (this.player.velocity.y < 0) {
				startY = endY = (int)((this.player.desiredPosition.y + this.player.velocity.y) / this.TILED_SIZE);
			}
			else {
				startY = endY = (int)((this.player.desiredPosition.y + this.player.velocity.y + this.player.actualFrame.getRegionHeight() ) / this.TILED_SIZE);
			}
		}

		if (!this.player.facesRight)
			startX = (int)((this.player.desiredPosition.x + 2)/ this.TILED_SIZE);					//16 tile size
		else
			startX =  (int)((this.player.desiredPosition.x)/ this.TILED_SIZE);					//16 tile size

		endX = (int)((this.player.desiredPosition.x + this.player.actualFrame.getRegionWidth()) / this.TILED_SIZE);

		// System.out.println(startX + " " + startY + " " + endX + " " + endY);

		this.getTiles(startX, startY, endX, endY, this.tiles, this.spikes);

		this.playerRect.y += (int)(this.player.velocity.y);

        boolean collisionSpike = false;

		for (Rectangle spike : this.spikes) {
			if (this.playerRect.overlaps(spike)) {
				if (this.normalGravity){
					if (this.player.velocity.y > 0) // we hit a block jumping upwards
						this.player.desiredPosition.y = spike.y - this.player.actualFrame.getRegionHeight();
					else {
						// if we hit the ground, mark us as grounded so we can jump
						this.player.desiredPosition.y = (spike.y + spike.height);
						this.player.grounded = true;
					}
				}
				else{	//upside down
					if (this.player.velocity.y > 0) {
						this.player.desiredPosition.y = (spike.y - this.player.actualFrame.getRegionHeight());
						this.player.grounded = true;
					}
					else
						this.player.desiredPosition.y = (spike.y + spike.height);
				}
			    collisionSpike = true;
				break;
            }
		}

		for (Rectangle tile : this.tiles) {
			if (this.playerRect.overlaps(tile)) {
				if (this.normalGravity){
					if (this.player.velocity.y > 0) // we hit a block jumping upwards
						this.player.desiredPosition.y = tile.y - this.player.actualFrame.getRegionHeight() ;
					else {
						// if we hit the ground, mark us as grounded so we can jump
						this.player.desiredPosition.y = (tile.y + tile.height) - 1;
						this.player.grounded = true;
					}
				}
				else{	//upside down
					if (this.player.velocity.y > 0) {
						this.player.desiredPosition.y = (tile.y - this.player.actualFrame.getRegionHeight()) + 3;
						this.player.grounded = true;
					}
					else
						this.player.desiredPosition.y = (tile.y + tile.height);
				}

				this.player.velocity.y = 0;
				break;
            }
        }

		if (this.tiles.size == 0)
			this.player.grounded = false;

		//goes together with get
		this.rectPool.free(this.playerRect);

		return collisionSpike;
	}


	private void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
	    this.getTiles(startX, startY, endX, endY, tiles, null);
    }

	private void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles, Array<Rectangle> spikes) {
		TiledMapTileLayer layer = (TiledMapTileLayer)(this.map.getLayers().get("Collisions"));
		TiledMapTileLayer layer2 = (TiledMapTileLayer)(this.map.getLayers().get("Spikes"));
		this.rectPool.freeAll(tiles);
		tiles.clear();
        if (spikes != null) {
            this.rectPool.freeAll(spikes);
            spikes.clear();
        }
		for (int y = startY; y <= endY; y++) {
			for (int x = startX; x <= endX; x++) {
				Cell cell = layer.getCell(x, y);
				if (cell != null) {
					Rectangle rect = this.rectPool.obtain();
					rect.set(x * this.TILED_SIZE, y  * this.TILED_SIZE, this.TILED_SIZE, this.TILED_SIZE);
					tiles.add(rect);
                }
				if (spikes != null) {
                    Cell cell2 = layer2.getCell(x, y);
                    if (cell2 != null) {
                        Rectangle rect = this.rectPool.obtain();
                        rect.set(x * this.TILED_SIZE, y  * this.TILED_SIZE, this.TILED_SIZE, this.TILED_SIZE);
                        spikes.add(rect);
                        tiles.add(rect);
                    }
				}
            }
        }
    }

    @Override
    public void backButtonPressed() {
        LD.getInstance().MENU_SCREEN = new MenuScreen();
        LD.getInstance().setScreen(LD.getInstance().MENU_SCREEN);
    }


    @Override
    public void enterButtonPressed() {
        if (!this.pause) {
            this.pause();
        }
        else {
            this.resume();
        }
    }

	@Override
	public void pause() {
		this.pause = true;
	}

	@Override
	public void resume() {
		this.pause = false;
	}

}
