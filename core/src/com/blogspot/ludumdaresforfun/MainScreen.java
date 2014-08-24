package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
	private Array<Shot> shotArray = new Array<Shot>();
	private Array<Vector2> spawns = new Array<Vector2>();

	private Boss boss;

	private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject () {
            return new Rectangle();
		}
    };

	private final float GRAVITY = -10f;
	private final float yPosUpperWorld = 360;
	private final float yPosLowerWorld = 120;
	final int DISTANCESPAWN = 410;
	final int TILED_SIZE = 16;
	final float activateBossXPosition = 420;


	public MainScreen() {
		this.shapeRenderer = new ShapeRenderer();
		Assets.loadAnimation();

		this.map = new TmxMapLoader().load("aholenewworld_enemy.tmx");

		this.renderer = new OrthogonalTiledMapRenderer(this.map, 1);

		TiledMapTileLayer layerSpawn = (TiledMapTileLayer)(this.map.getLayers().get(2));
		this.rectPool.freeAll(this.tiles);
		this.tiles.clear();
        for (int x = 0; x <= layerSpawn.getHeight(); x++) {
            for (int y = 0; y <= layerSpawn.getWidth(); y++) {
				Cell cell = layerSpawn.getCell(x, y);
				if (cell != null) {
                    this.spawns.add(new Vector2(x * this.TILED_SIZE, y * this.TILED_SIZE));
                }
            }
        }

        this.renderer = new OrthogonalTiledMapRenderer(this.map, 1);

		Gdx.graphics.setDisplayMode(400, 240, false);
		this.camera = new OrthographicCamera();
		this.camera.setToOrtho(false, 400, 240);
		this.camera.position.y = this.yPosUpperWorld;
		this.camera.update();

		this.player = new Player(Assets.playerStand);
		this.player.setPosition(360, 380);

        this.configControllers = new ConfigControllers();
		this.configControllers.init();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.updatePlayer(delta);
		this.player.act(delta);

		if (!this.bossActive){
			this.camera.position.x = this.player.getX(); //200;//raya.position.x;
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
                this.enemies.add(auxShadow);
                this.spawns.removeIndex(0);
            }
		}

		this.updateEnemies(delta);
		this.renderer.setView(this.camera);
		this.renderer.render(new int[]{0,1});

		this.renderEnemies(delta);
		this.renderPlayer(delta);
		for (Shot shot : this.shotArray){
			if (shot != null)
				this.renderShot(shot, delta);
		}
		if (this.boss != null){
			this.updateBoss(delta);
			this.renderBoss(delta);
		}

	}

	private void updateBoss(float delta) {
		if (this.player.getRect().overlaps(this.boss.getRect())) {
            this.player.beingHit();
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
	}


	private void flowBoss(float delta) {

		if (this.boss.flowState == Boss.FlowState.WalkingLeft){
			this.boss.velocity.x = -100;
			this.boss.flowState = Boss.FlowState.Transition;
		}
		else if (this.boss.flowState == Boss.FlowState.WalkingRight){
			this.boss.velocity.x = 100;
			this.boss.flowState = Boss.FlowState.Transition;
		}
		else if (this.boss.flowState == Boss.FlowState.Jumping){
			if ((this.boss.getX() - this.player.getX()) > 0)
				this.boss.velocity.x = -200;
			else
				this.boss.velocity.x = 200;

			this.boss.velocity.y = 400;
			this.boss.flowState = Boss.FlowState.Transition;
		}
		else if (this.boss.flowState == Boss.FlowState.Attack){
			if ((this.boss.getX() - this.player.getX()) > 0)
				this.boss.facesRight = false;
			else
				this.boss.facesRight = true;

			this.boss.velocity.x = 0;
			//attack to character(detect position and collision)
			this.boss.flowState = Boss.FlowState.Transition;
		}
		else if (this.boss.flowState == Boss.FlowState.Summon){
			this.boss.velocity.x = 0;
			//summon
			this.boss.flowTime = 0;
			this.boss.velocity.y = 200;  //only to differentiate right now
			this.boss.flowState = Boss.FlowState.Transition;
		}
		else if (this.boss.flowState == Boss.FlowState.Transition){
			if (this.boss.getX() > (420 + 300))
				this.boss.flowState = Boss.FlowState.WalkingLeft;
			else if (this.boss.getX() < 420)
				this.boss.flowState = Boss.FlowState.WalkingRight;
			else if (this.boss.flowTime > 2){
				int nextState = (int)Math.round(Math.random() * 3);

				if ((Math.abs(this.boss.getX() - this.player.getX()) < 48) && ((nextState % 2) == 0))	//2 tiles
					this.boss.flowState = Boss.FlowState.Attack;
				else if (nextState == 0)
					this.boss.flowState = Boss.FlowState.Jumping;
				else if (nextState == 1)
					this.boss.flowState = Boss.FlowState.Summon;
				else{
					if ((this.boss.getX() - this.player.getX()) > 0)
						this.boss.flowState = Boss.FlowState.WalkingLeft;
					else
						this.boss.flowState = Boss.FlowState.WalkingRight;
				}
				this.boss.flowTime = 0;
			}
		}
		this.boss.flowTime += delta;
	}


	private void renderBoss(float delta) {
		AtlasRegion frame = null;

		if (this.boss.velocity.x > 0)
			this.boss.facesRight = true;
		else if (this.boss.velocity.x < 0)
			this.boss.facesRight = false;

		if (this.boss.state == Boss.State.Standing)
			frame = (AtlasRegion)Assets.bossStanding.getKeyFrame(this.boss.stateTime);

		Batch batch = this.renderer.getSpriteBatch();
		batch.begin();
		if (this.boss.facesRight) {
			if (frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, this.boss.getX() + frame.offsetX, this.boss.getY() + frame.offsetY);
			System.out.println(this.boss.getX() + "");
		} else {
			if (!frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, this.boss.getX() + frame.offsetX, this.boss.getY() + frame.offsetY);
		}

		batch.end();

	}


	private void renderShot(Shot shot, float deltaTime){
		AtlasRegion frame = null;
		frame = (AtlasRegion) Assets.playerShot.getKeyFrame(shot.stateTime);

		Batch batch = this.renderer.getSpriteBatch();
		batch.begin();
		if (shot.shotGoesRight) {
			if (frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, shot.getX() + frame.offsetX, shot.getY() + frame.offsetY);
		} else {
			if (!frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, shot.getX() + frame.offsetX, shot.getY() + frame.offsetY);
		}

		batch.end();
	}

    public boolean updateShot(Shot shot, float deltaTime){
        shot.desiredPosition.y = shot.getY();

        shot.stateTime += deltaTime;

		if (this.normalGravity)
			shot.velocity.add(0, this.GRAVITY);
		else
			shot.velocity.add(0, -this.GRAVITY);

		shot.velocity.scl(deltaTime);

		//collision (destroy if necessary)
		boolean collided = this.collisionShotEnemy(shot);
		if (collided == false)
			collided = this.collisionShot(shot);



		// unscale the velocity by the inverse delta time and set
		// the latest position
		if (shot != null){
			shot.desiredPosition.add(shot.velocity);
			shot.velocity.scl(1 / deltaTime);

			shot.setPosition(shot.desiredPosition.x, shot.desiredPosition.y);
			if (shot.normalGravity && (shot.getY() < 240))
				collided = true;	//dont traspass to the other world
			else if (!shot.normalGravity && (shot.getY() >= 240))
				collided = true;
			else if ((shot.getY() > 480) || (shot.getY() < 0))
				collided = true;
		}

		return collided;
    }


	private boolean collisionShotEnemy(Shot shot) {
		boolean collided = false;

		this.playerRect = this.rectPool.obtain();

		shot.desiredPosition.y = Math.round(shot.getY());
		shot.desiredPosition.x = Math.round(shot.getX());

		this.playerRect.set(shot.desiredPosition.x, (shot.desiredPosition.y), shot.getWidth(), shot.getHeight());

		for (Enemy enemy : this.enemies){
			if (this.playerRect.overlaps(enemy.rect)) {
				enemy.die();
				collided = true;
				break;
			}
		}

		if (boss != null && this.playerRect.overlaps(this.boss.rect)) {
			this.boss.beingHit();
			collided = true;
		}


		return collided;
	}


	private boolean collisionShot(Shot shot) {
		this.playerRect = this.rectPool.obtain();

		shot.desiredPosition.y = Math.round(shot.getY());
		shot.desiredPosition.x = Math.round(shot.getX());

		this.playerRect.set(shot.desiredPosition.x, (shot.desiredPosition.y), shot.getWidth(), shot.getHeight());

		int startX, startY, endX, endY;

		if (shot.velocity.x > 0) {	//this.raya.velocity.x > 0
			startX = endX = (int)((shot.desiredPosition.x + shot.velocity.x + shot.getWidth()) / 16);
		}
		else {
			startX = endX = (int)((shot.desiredPosition.x + shot.velocity.x) / 16);
		}

		startY = (int)((shot.desiredPosition.y) / 16);
		endY = (int)((shot.desiredPosition.y + shot.getHeight()) / 16);

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
				startY = endY = (int)((shot.desiredPosition.y + shot.velocity.y + shot.getHeight()) / 16f);
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
				startY = endY = (int)((shot.desiredPosition.y + shot.velocity.y + shot.getHeight() ) / 16f);
			}
		}

		startX = (int)(shot.desiredPosition.x / 16);					//16 tile size
		endX = (int)((shot.desiredPosition.x + shot.getWidth()) / 16);


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
		case StandingShooting:
			frame = (AtlasRegion)Assets.playerStandShot.getKeyFrame(this.player.stateTime);
			break;
		case Intro:
			frame = (AtlasRegion)Assets.playerIntro.getKeyFrame(this.player.stateTime);
			break;
		case Attacking:
			frame = (AtlasRegion)Assets.playerAttack.getKeyFrame(this.player.stateTime);
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
		}
		else if (!this.player.facesRight && !frame.isFlipX()) {
			frame.flip(true, false);
		}

		if (this.normalGravity && frame.isFlipY())
			frame.flip(false, true);
		else if (!this.normalGravity && !frame.isFlipY())
			frame.flip(false, true);

		batch.draw(frame, this.player.getX() + frame.offsetX, this.player.getY() + frame.offsetY);

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
            AtlasRegion frame = null;
            switch (enemy.state) {
            case Walking:
                frame = (AtlasRegion)Assets.enemyWalk.getKeyFrame(enemy.stateTime);
                break;
            case Running:
                frame = (AtlasRegion)Assets.enemyRun.getKeyFrame(enemy.stateTime);
                break;
            case Hurting:
                frame = (AtlasRegion)Assets.enemyHurt.getKeyFrame(enemy.stateTime);
                break;
            }

            Batch batch = this.renderer.getSpriteBatch();
            batch.begin();
            if (enemy.facesRight) {
                if (frame.isFlipX())
                    frame.flip(true, false);
                batch.draw(frame, enemy.getX() + frame.offsetX, enemy.getY() + frame.offsetY);
            } else {
                if (!frame.isFlipX())
                    frame.flip(true, false);
                batch.draw(frame, enemy.getX() + frame.offsetX, enemy.getY() + frame.offsetY);
            }
            batch.end();

            this.shapeRenderer.begin(ShapeType.Filled);
            this.shapeRenderer.setColor(Color.BLACK);
            this.getTiles(0, 0, 25, 15, this.tiles);
            this.shapeRenderer.setColor(Color.RED);
            this.shapeRenderer.end();
	    }
	}

	private void updateEnemies(float deltaTime) {
	    for (Enemy enemy : this.enemies) {
	        // Collision between player vs enemy
	        if (this.player.getRect().overlaps(enemy.getRect())) {
	            this.player.beingHit();
	        }
	        // Check if player is invincible and check distance to player for attack him.
	        if (!this.player.invincible &&
	                ((enemy.getY() - (enemy.getWidth() / 2)) <= this.player.getY()) &&
	                (this.player.getY() <= (enemy.getY() + (enemy.getWidth() / 2)))) {
	            if (enemy.getX() < this.player.getX()) {
                    if ((enemy.getX() - enemy.ATTACK_DISTANCE) <= (this.player.getX() + this.player.getHeight())) {
                        enemy.dir = Enemy.Direction.Right;
                        enemy.run();
                    }
	            }
	            else {
                    if ((enemy.getX() + enemy.ATTACK_DISTANCE) >= this.player.getX()) {
                        enemy.dir = Enemy.Direction.Left;
                        enemy.run();
                    }
	            }
	        }

            else if (enemy.dir == Enemy.Direction.Left) {
                if (-enemy.RANGE >= enemy.diffInitialPos) {
                    enemy.dir = Enemy.Direction.Right;
                }
                enemy.walk();
	        }
	        else if (enemy.dir == Enemy.Direction.Right) {
                if (enemy.diffInitialPos >= enemy.RANGE) {
                    enemy.dir = Enemy.Direction.Left;
                }
                enemy.walk();
	        }

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
                    break;
                }
            }

            enemy.rect.x = enemy.desiredPosition.x;

            enemy.desiredPosition.add(enemy.velocity);
            enemy.velocity.scl(1 / deltaTime);

            enemy.setPosition(enemy.desiredPosition.x, enemy.desiredPosition.y);

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

	private void updatePlayer(float deltaTime) {

		if (deltaTime == 0)
			return;
		this.player.stateTime += deltaTime;

		this.player.desiredPosition.x = this.player.getX();
		this.player.desiredPosition.y = this.player.getY();

		this.movingShootingJumping(deltaTime);
		this.gravityAndClamping();

		this.player.velocity.scl(deltaTime);

		this.collisionWalls();

		// unscale the velocity by the inverse delta time and set
		// the latest position
		this.player.desiredPosition.add(this.player.velocity);
		this.player.velocity.scl(1 / deltaTime);

		// Apply damping to the velocity on the x-axis so we don't
		// walk infinitely once a key was pressed
		this.player.velocity.x *= 0;		//0 is totally stopped if not pressed
		this.player.setPosition(this.player.desiredPosition.x, this.player.desiredPosition.y);

		this.activateBoss();

	}

	private void activateBoss() {
		if ((this.player.getX() > this.activateBossXPosition) && !this.bossActive){		//boss final trigger
			this.bossActive = true;

			this.boss = new Boss(Assets.bossStanding);
			this.boss.setPosition(600, 350);

			this.camera.position.x = 600;		//boss final position camera
			this.camera.update();

			//close door
			TiledMapTileLayer layerSpawn = (TiledMapTileLayer)(this.map.getLayers().get(0));
			Cell cell = layerSpawn.getCell(25, 16); //has to be solid block

	        layerSpawn.setCell(25, 17, cell);
	        layerSpawn.setCell(25, 18, cell);
	        layerSpawn.setCell(25, 19, cell);

	        layerSpawn = (TiledMapTileLayer)(this.map.getLayers().get(1));
	        layerSpawn.setCell(25, 17, cell);
	        layerSpawn.setCell(25, 18, cell);
	        layerSpawn.setCell(25, 19, cell);
		}
	}


	private void gravityAndClamping() {
		if (this.normalGravity)
			this.player.velocity.add(0, this.GRAVITY);
		else
			this.player.velocity.add(0, -this.GRAVITY);

		if (this.player.getY() < 240){
			this.camera.position.y = this.yPosLowerWorld;
			if (this.normalGravity == true){
				this.normalGravity = false;
				this.player.velocity.y = -this.player.JUMP_VELOCITY;
			}
		}
		else{
			this.camera.position.y = this.yPosUpperWorld;
			if (this.normalGravity == false){
				this.normalGravity = true;
				this.player.velocity.y = this.player.JUMP_VELOCITY;
			}
		}

		// clamp the velocity to the maximum, x-axis only
		if (Math.abs(this.player.velocity.x) > this.player.MAX_VELOCITY) {
			this.player.velocity.x = Math.signum(this.player.velocity.x) * this.player.MAX_VELOCITY;
		}

		// clamp the velocity to 0 if it's < 1, and set the state to standign
		if (Math.abs(this.player.velocity.x) < 1) {
			this.player.velocity.x = 0;
			if (this.player.grounded && !this.player.shooting && !this.player.invincible)
				this.player.state = Player.State.Standing;
		}
	}


	private void movingShootingJumping(float deltaTime) {
		if ((Gdx.input.isKeyJustPressed(Keys.S) || this.configControllers.jumpPressed) && this.player.grounded){
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
			if (this.player.grounded){
				this.player.state = Player.State.Walking;
				//this.player.stateTime = 0;
			}
			this.player.facesRight = false;
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT) || this.configControllers.rightPressed){
			this.player.velocity.x = this.player.MAX_VELOCITY;
			if (this.player.grounded){
				this.player.state = Player.State.Walking;
				//this.player.stateTime = 0;
			}
			this.player.facesRight = true;
		}

		if (Gdx.input.isKeyJustPressed(Keys.D) && (this.shotArray.size < 3)){
			Shot shot = new Shot(Assets.playerShot);
			if (this.player.facesRight){
				//-1 necessary to be exactly the same as the other facing
				shot.Initialize((this.player.getX() + (this.player.getHeight() / 2)) - 1, (this.player.getY() + (this.player.getWidth() / 2)), this.player.facesRight, this.normalGravity);
			}
			else {
				shot.Initialize(this.player.getX(), (this.player.getY() + (this.player.getWidth() / 2)), this.player.facesRight, this.normalGravity);
			}
			this.shotArray.add(shot);

			if (this.player.grounded){	//&& raya.velocity.x == 0)
				this.player.state = Player.State.StandingShooting;
				this.player.stateTime = 0;
			}
			this.player.shooting = true;
		}

		if (Assets.playerStandShot.isAnimationFinished(this.player.stateTime))
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

		this.playerRect.set(boss.desiredPosition.x, boss.desiredPosition.y, boss.getWidth(), boss.getHeight());

		int startX, startY, endX, endY;

		if (this.player.velocity.x > 0) {
			startX = endX = (int)((boss.desiredPosition.x + boss.velocity.x + boss.getWidth()) / this.TILED_SIZE);
		}
		else {
			startX = endX = (int)((boss.desiredPosition.x + boss.velocity.x) / this.TILED_SIZE);
		}

		if (boss.grounded && this.normalGravity){
			startY = (int)((boss.desiredPosition.y) / this.TILED_SIZE) + 1;
			endY = (int)((boss.desiredPosition.y + boss.getHeight()) / this.TILED_SIZE) + 1;
		}
		else if (boss.grounded && !this.normalGravity){
			startY = (int)((boss.desiredPosition.y) / this.TILED_SIZE) - 1;
			endY = (int)((boss.desiredPosition.y + boss.getHeight()) / this.TILED_SIZE) - 1;
		}
		else{
			startY = (int)((boss.desiredPosition.y) / this.TILED_SIZE);
			endY = (int)((boss.desiredPosition.y + boss.getHeight()) / this.TILED_SIZE);
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
				startY = endY = (int)((boss.desiredPosition.y + boss.velocity.y + boss.getHeight()) / this.TILED_SIZE);
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
				startY = endY = (int)((boss.desiredPosition.y + boss.velocity.y + boss.getHeight() ) / this.TILED_SIZE);
			}
		}


		startX = (int)(boss.desiredPosition.x / this.TILED_SIZE);					//16 tile size
		endX = (int)((boss.desiredPosition.x + boss.getWidth()) / this.TILED_SIZE);

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
						boss.desiredPosition.y = tile.y - boss.getHeight() - 1;
						// we hit a block jumping upwards, let's destroy it!
					}
					else {
						boss.desiredPosition.y = (tile.y + tile.height) - 4;	//in this way he is in the ground
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


	private void collisionWalls() {
		//collision detection
		// perform collision detection & response, on each axis, separately
		// if the raya is moving right, check the tiles to the right of it's
		// right bounding box edge, otherwise check the ones to the left
		this.playerRect = this.rectPool.obtain();

		this.player.desiredPosition.y = Math.round(this.player.getY());
		this.player.desiredPosition.x = Math.round(this.player.getX());

		this.playerRect.set(this.player.desiredPosition.x, (this.player.desiredPosition.y), this.player.getWidth(), this.player.getHeight());

		int startX, startY, endX, endY;

		if (this.player.velocity.x > 0) {
			startX = endX = (int)((this.player.desiredPosition.x + this.player.velocity.x + this.player.getWidth()) / this.TILED_SIZE);
		}
		else {
			startX = endX = (int)((this.player.desiredPosition.x + this.player.velocity.x) / this.TILED_SIZE);
		}

		if (this.player.grounded && this.normalGravity){
			startY = (int)((this.player.desiredPosition.y) / this.TILED_SIZE) + 1;
			endY = (int)((this.player.desiredPosition.y + this.player.getHeight()) / this.TILED_SIZE) + 1;
		}
		else if (this.player.grounded && !this.normalGravity){
			startY = (int)((this.player.desiredPosition.y) / this.TILED_SIZE) - 1;
			endY = (int)((this.player.desiredPosition.y + this.player.getHeight()) / this.TILED_SIZE) - 1;
		}
		else{
			startY = (int)((this.player.desiredPosition.y) / this.TILED_SIZE);
			endY = (int)((this.player.desiredPosition.y + this.player.getHeight()) / this.TILED_SIZE);
		}

		this.getTiles(startX, startY, endX, endY, this.tiles);

		this.playerRect.x += this.player.velocity.x;

		for (Rectangle tile : this.tiles) {
			if (this.playerRect.overlaps(tile)) {
				this.player.velocity.x = 0;
				break;
				}
		}

		this.playerRect.x = this.player.desiredPosition.x;

		// if the koala is moving upwards, check the tiles to the top of it's
		// top bounding box edge, otherwise check the ones to the bottom

		if (this.normalGravity){
			if (this.player.velocity.y > 0) {
				startY = endY = (int)((this.player.desiredPosition.y + this.player.velocity.y + this.player.getHeight()) / this.TILED_SIZE);
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
				startY = endY = (int)((this.player.desiredPosition.y + this.player.velocity.y + this.player.getHeight() ) / this.TILED_SIZE);
			}
		}


		startX = (int)(this.player.desiredPosition.x / this.TILED_SIZE);					//16 tile size
		endX = (int)((this.player.desiredPosition.x + this.player.getWidth()) / this.TILED_SIZE);

		// System.out.println(startX + " " + startY + " " + endX + " " + endY);

		this.getTiles(startX, startY, endX, endY, this.tiles);

		this.playerRect.y += (int)(this.player.velocity.y);

		for (Rectangle tile : this.tiles) {
			// System.out.println(playerRect.x + " " + playerRect.y + " " + tile.x + " " + tile.y);
			if (this.playerRect.overlaps(tile)) {
				// we actually reset the koala y-position here
				// so it is just below/above the tile we collided with
				// this removes bouncing :)

				if (this.normalGravity){
					if (this.player.velocity.y > 0) {
						this.player.desiredPosition.y = tile.y - this.player.getHeight() - 1;
						// we hit a block jumping upwards, let's destroy it!
					}
					else {
						this.player.desiredPosition.y = (tile.y + tile.height) - 1;
						// if we hit the ground, mark us as grounded so we can jump
						this.player.grounded = true;
					}
				}
				else{
					if (this.player.velocity.y > 0) {
						//this.player.desiredPosition.y = tile.y - tile.height- 1;
						// if we hit the ground, mark us as grounded so we can jump
						this.player.grounded = true;
					}
					else {
						this.player.desiredPosition.y = (tile.y + tile.height) - 1;
						// we hit a block jumping upwards, let's destroy it!
					}
				}

				this.player.velocity.y = 0;
				break;
				}
			}

		if (this.tiles.size == 0)
			this.player.grounded = false;

		//goes together with get
		this.rectPool.free(this.playerRect);
	}


	private void getTiles (int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {

		TiledMapTileLayer layer = (TiledMapTileLayer)(this.map.getLayers().get(1));
		this.rectPool.freeAll(tiles);
		tiles.clear();
		for (int y = startY; y <= endY; y++) {
			for (int x = startX; x <= endX; x++) {
				Cell cell = layer.getCell(x, y);
				if (cell != null) {
					Rectangle rect = this.rectPool.obtain();
					rect.set(x * this.TILED_SIZE, y  * this.TILED_SIZE, this.TILED_SIZE, this.TILED_SIZE);
					tiles.add(rect);
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
