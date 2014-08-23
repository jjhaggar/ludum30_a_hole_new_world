package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

	ConfigControllers configControllers;
	Rectangle playerRect;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private Player player;
	private ShapeRenderer shapeRenderer;
	private TiledMap map;
	private boolean normalGravity = true;

	private Array<Enemy> enemies = new Array<Enemy>();
	private Array<Rectangle> tiles = new Array<Rectangle>();
	private Array<Shot> shotArray = new Array<Shot>();
	private Array<Vector2> spawns = new Array<Vector2>();

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


	public MainScreen() {
		this.shapeRenderer = new ShapeRenderer();
		Assets.loadAnimation();

		this.map = new TmxMapLoader().load("aholenewworld.tmx");

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

		Gdx.graphics.setDisplayMode(400, 240, false);
		this.camera = new OrthographicCamera();
		this.camera.setToOrtho(false, 400, 240);
		this.camera.position.y = this.yPosUpperWorld;
		this.camera.update();

		this.player = new Player(Assets.stand);
		this.player.setPosition(0, 380);

        this.configControllers = new ConfigControllers();
		this.configControllers.init();
	}


	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.updatePlayer(delta);
		this.player.act(delta);

		this.camera.position.x = this.player.getX(); //200;//raya.position.x;
		this.camera.update();
		if (this.spawns.size > 0) {
            Vector2 auxNextSpawn = this.spawns.first();
            if ((this.camera.position.x + this.DISTANCESPAWN) >= auxNextSpawn.x) {
                Enemy auxShadow = new Enemy(Assets.jump);
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
	}

	private void renderShot(Shot shot, float deltaTime){
		TextureRegion frame = null;
		frame = Assets.shotAnim.getKeyFrame(shot.stateTime);

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
        shot.desiredPosition.y = shot.getY();

        shot.stateTime += deltaTime;

		if (this.normalGravity)
			shot.velocity.add(0, this.GRAVITY);
		else
			shot.velocity.add(0, -this.GRAVITY);

		shot.velocity.scl(deltaTime);

		//collision (destroy if necessary)
		boolean collided = this.collisionShot(shot);
		collided = this.collisionShotEnemy(shot);

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
		// based on the koala state, get the animation frame
		TextureRegion frame = null;
		switch (this.player.state) {
		case Standing:
			frame = Assets.stand.getKeyFrame(this.player.stateTime);
			break;
		case Walking:
			frame = Assets.walk.getKeyFrame(this.player.stateTime);
			break;
		case Jumping:
			frame = Assets.jump.getKeyFrame(this.player.stateTime);
			break;
		case StandingShooting:
			frame = Assets.standingShot.getKeyFrame(this.player.stateTime);
			break;
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
        batch.draw(frame, this.player.getX(), this.player.getY());

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
            TextureRegion frame = null;
            frame = Assets.walk.getKeyFrame(enemy.stateTime);
            switch (enemy.state) {
            case Walking:
                //frame = Assets.enemyWalk.getKeyFrame(enemy.stateTime);
                break;
            case Running:
                //frame = Assets.enemyRun.getKeyFrame(enemy.stateTime);
                break;
            case Hurting:
                //frame = Assets.enemyHurt.getKeyFrame(enemy.stateTime);
                break;
            }

            Batch batch = this.renderer.getSpriteBatch();
            batch.begin();
            if (enemy.facesRight) {
                if (frame.isFlipX())
                    frame.flip(true, false);
                batch.draw(frame, enemy.getX(), enemy.getY());
            } else {
                if (!frame.isFlipX())
                    frame.flip(true, false);
                batch.draw(frame, enemy.getX(), enemy.getY());
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
                        enemy.diffInitialPos += 2;
                        enemy.velocity.x = enemy.ATTACK_VELOCITY;
                    }
	            }
	            else {
                    if ((enemy.getX() + enemy.ATTACK_DISTANCE) >= this.player.getX()) {
                        enemy.dir = Enemy.Direction.Left;
                        enemy.diffInitialPos -= 2;
                        enemy.velocity.x = -enemy.ATTACK_VELOCITY;
                    }
	            }
	        }

            else if (enemy.dir == Enemy.Direction.Left) {
                if (-enemy.RANGE < enemy.diffInitialPos) {
                    enemy.diffInitialPos -= 1;
                    enemy.velocity.x = -enemy.VELOCITY;
                }
                else {
                    enemy.dir = Enemy.Direction.Right;
                }
	        }
	        else if (enemy.dir == Enemy.Direction.Right) {
                if (enemy.diffInitialPos < enemy.RANGE) {
                    enemy.diffInitialPos += 1;
                    enemy.velocity.x = enemy.VELOCITY;
                }
                else {
                    enemy.dir = Enemy.Direction.Left;
                }
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
	}

	private void updatePlayer(float deltaTime) {

		if (deltaTime == 0)
			return;
		this.player.stateTime += deltaTime;

		this.player.desiredPosition.x = this.player.getX();
		this.player.desiredPosition.y = this.player.getY();

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
			Shot shot = new Shot(Assets.shotAnim);
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

		if (Assets.standingShot.isAnimationFinished(this.player.stateTime))
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
			if (this.player.grounded && !this.player.shooting)
				this.player.state = Player.State.Standing;
		}

		this.player.velocity.scl(deltaTime);

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

		// unscale the velocity by the inverse delta time and set
		// the latest position
		this.player.desiredPosition.add(this.player.velocity);
		this.player.velocity.scl(1 / deltaTime);

		// Apply damping to the velocity on the x-axis so we don't
		// walk infinitely once a key was pressed
		this.player.velocity.x *= 0;		//0 is totally stopped if not pressed

		this.player.setPosition(this.player.desiredPosition.x, this.player.desiredPosition.y);
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
    }


}
