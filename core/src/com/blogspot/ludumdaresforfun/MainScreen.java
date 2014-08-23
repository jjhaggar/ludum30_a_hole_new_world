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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class MainScreen extends BaseScreen {

	private ShapeRenderer shapeRenderer;
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private float GRAVITY = -10f;
	private Array<Rectangle> tiles = new Array<Rectangle>();
	Rectangle rayaRect;

	ConfigControllers configControllers = new ConfigControllers();

	private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject () {
            return new Rectangle();
		}
    };

	private RayaMan raya;
	private Shot shot;


	public MainScreen() {
		this.shapeRenderer = new ShapeRenderer();
		Assets.loadAnimation();

		this.map = new TmxMapLoader().load("prueba_scroll.tmx");

		this.renderer = new OrthogonalTiledMapRenderer(this.map, 1);

		Gdx.graphics.setDisplayMode(400, 240, false);
		this.camera = new OrthographicCamera();
		this.camera.setToOrtho(false, 400, 240);
		this.camera.update();

		this.raya = new RayaMan(Assets.stand);
		this.raya.setPosition(0, 64);

		//this.stage.addActor(this.raya);

		this.configControllers.init();
	}


	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.updateRaya(delta);

		this.camera.position.x = this.raya.getX(); //200;//raya.position.x;
		this.camera.update();

		this.renderer.setView(this.camera);
		this.renderer.render();

		this.renderRayaMan(delta);
		if (this.shot != null)
			this.renderShot(delta);
		//this.stage.act(delta);
		//this.stage.draw();

	}

	private void renderShot(float deltaTime){
		TextureRegion frame = null;
		frame = Assets.shotAnim.getKeyFrame(this.shot.stateTime);

		Batch batch = this.renderer.getSpriteBatch();
		batch.begin();
		if (this.shot.shotGoesRight) {
			if (frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, this.shot.getX(), this.shot.getY());
		} else {
			if (!frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, this.shot.getX(), this.shot.getY());
		}

		batch.end();
	}

	private void renderRayaMan (float deltaTime) {
		// based on the koala state, get the animation frame
		TextureRegion frame = null;
		switch (this.raya.state) {
		case Standing:
			frame = Assets.stand.getKeyFrame(this.raya.stateTime);
			break;
		case Walking:
			frame = Assets.walk.getKeyFrame(this.raya.stateTime);
			break;
		case Jumping:
			frame = Assets.jump.getKeyFrame(this.raya.stateTime);
			break;
		case StandingShooting:
			frame = Assets.standingShot.getKeyFrame(this.raya.stateTime);
			break;
		}
		// draw the koala, depending on the current velocity
		// on the x-axis, draw the koala facing either right
		// or left
		Batch batch = this.renderer.getSpriteBatch();
		batch.begin();
		if (this.raya.facesRight) {
			if (frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, this.raya.getX(), this.raya.getY());
		} else {
			if (!frame.isFlipX())
				frame.flip(true, false);
			batch.draw(frame, this.raya.getX(), this.raya.getY());
		}

		batch.end();
		this.shapeRenderer.begin(ShapeType.Filled);

		this.shapeRenderer.setColor(Color.BLACK);

		this.getTiles(0, 0, 25, 15, this.tiles);
		//for (Rectangle tile : this.tiles) {
		//	shapeRenderer.rect(tile.x * 1.6f, tile.y * 2, tile.width * 2, tile.height * 2);
		//}
		this.shapeRenderer.setColor(Color.RED);
		//shapeRenderer.rect(rayaRect.x * 1.6f, rayaRect.y * 2, rayaRect.width * 2, rayaRect.height * 2);


        this.shapeRenderer.end();
		}

	private void updateRaya(float deltaTime) {

		if (deltaTime == 0)
			return;
		this.raya.stateTime += deltaTime;

		this.raya.desiredPosition.x = this.raya.getX();
		this.raya.desiredPosition.y = this.raya.getY();

		if ((Gdx.input.isKeyPressed(Keys.S) || this.configControllers.jumpPressed) && this.raya.grounded){
			this.raya.velocity.y = this.raya.JUMP_VELOCITY;
			this.raya.grounded = false;
			this.raya.state = RayaMan.State.Jumping;
			//this.raya.stateTime = 0;
		}

		if (Gdx.input.isKeyPressed(Keys.LEFT) || this.configControllers.leftPressed){
			this.raya.velocity.x = -this.raya.MAX_VELOCITY;
			if (this.raya.grounded){
				this.raya.state = RayaMan.State.Walking;
				//this.raya.stateTime = 0;
			}
			this.raya.facesRight = false;
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT) || this.configControllers.rightPressed){
			this.raya.velocity.x = this.raya.MAX_VELOCITY;
			if (this.raya.grounded){
				this.raya.state = RayaMan.State.Walking;
				//this.raya.stateTime = 0;
			}
			this.raya.facesRight = true;
		}

		if (Gdx.input.isKeyPressed(Keys.D)){
			if (this.raya.facesRight){
				//-1 necessary to be exactly the same as the other facing
				this.shot = new Shot((this.raya.getX() + (this.raya.getHeight() / 2)) - 1, (this.raya.getY() + (this.raya.getWidth() / 2)), this.raya.facesRight, Assets.shotAnim);
			}
			else {
				this.shot = new Shot(this.raya.getX(), (this.raya.getY() + (this.raya.getWidth() / 2)), this.raya.facesRight, Assets.shotAnim);
			}
			this.shot.setY(this.shot.getY() + (this.shot.getWidth() / 2));

			if (this.raya.grounded){	//&& raya.velocity.x == 0)
				this.raya.state = RayaMan.State.StandingShooting;
				this.raya.stateTime = 0;
			}
			this.raya.shooting = true;
		}

		if (Assets.standingShot.isAnimationFinished(this.raya.stateTime))
			this.raya.shooting = false;

		if (this.shot != null)
			this.shot.updateShot(deltaTime);		//pool of shots?

		this.raya.velocity.add(0, this.GRAVITY);

		// clamp the velocity to the maximum, x-axis only
		if (Math.abs(this.raya.velocity.x) > this.raya.MAX_VELOCITY) {
			this.raya.velocity.x = Math.signum(this.raya.velocity.x) * this.raya.MAX_VELOCITY;
		}

		// clamp the velocity to 0 if it's < 1, and set the state to standign
		if (Math.abs(this.raya.velocity.x) < 1) {
			this.raya.velocity.x = 0;
			if (this.raya.grounded && !this.raya.shooting)
				this.raya.state = RayaMan.State.Standing;
		}

		this.raya.velocity.scl(deltaTime);

		//collision detection
		// perform collision detection & response, on each axis, separately
		// if the raya is moving right, check the tiles to the right of it's
		// right bounding box edge, otherwise check the ones to the left
		this.rayaRect = this.rectPool.obtain();

		this.raya.desiredPosition.y = Math.round(this.raya.getY());
		this.raya.desiredPosition.x = Math.round(this.raya.getX());

		this.rayaRect.set(this.raya.desiredPosition.x, (this.raya.desiredPosition.y), this.raya.getWidth(), this.raya.getHeight());

		int startX, startY, endX, endY;

		if (this.raya.velocity.x > 0) {
			startX = endX = (int)((this.raya.desiredPosition.x + this.raya.velocity.x + this.raya.getWidth()) / 16);
		}
		else {
			startX = endX = (int)((this.raya.desiredPosition.x + this.raya.velocity.x) / 16);
		}
		if (this.raya.grounded){
			startY = (int)((this.raya.desiredPosition.y) / 16) + 1;
			endY = (int)((this.raya.desiredPosition.y + this.raya.getHeight()) / 16) + 1;
		}
		else{
			startY = (int)((this.raya.desiredPosition.y) / 16);
			endY = (int)((this.raya.desiredPosition.y + this.raya.getHeight()) / 16);
		}
		this.getTiles(startX, startY, endX, endY, this.tiles);

		this.rayaRect.x += this.raya.velocity.x;

		for (Rectangle tile : this.tiles) {
			if (this.rayaRect.overlaps(tile)) {
				this.raya.velocity.x = 0;
				break;
				}
		}

		this.rayaRect.x = this.raya.desiredPosition.x;

		// if the koala is moving upwards, check the tiles to the top of it's
		// top bounding box edge, otherwise check the ones to the bottom

		if (this.raya.velocity.y > 0) {
			startY = endY = (int)((this.raya.desiredPosition.y + this.raya.velocity.y + this.raya.getHeight()) / 16f);
		}
		else {
			startY = endY = (int)((this.raya.desiredPosition.y + this.raya.velocity.y) / 16f);
		}

		startX = (int)(this.raya.desiredPosition.x / 16);					//16 tile size
		endX = (int)((this.raya.desiredPosition.x + this.raya.getWidth()) / 16);

		// System.out.println(startX + " " + startY + " " + endX + " " + endY);

		this.getTiles(startX, startY, endX, endY, this.tiles);

		this.rayaRect.y += (int)(this.raya.velocity.y);

		for (Rectangle tile : this.tiles) {
			// System.out.println(rayaRect.x + " " + rayaRect.y + " " + tile.x + " " + tile.y);
			if (this.rayaRect.overlaps(tile)) {
				// we actually reset the koala y-position here
				// so it is just below/above the tile we collided with
				// this removes bouncing :)

				if (this.raya.velocity.y > 0) {
					this.raya.desiredPosition.y = tile.y - this.raya.getHeight() - 1;
					// we hit a block jumping upwards, let's destroy it!
					}
				else {
					this.raya.desiredPosition.y = (tile.y + tile.height) - 1;
					// if we hit the ground, mark us as grounded so we can jump
					this.raya.grounded = true;
					}
				this.raya.velocity.y = 0;
				break;
				}
			}

		if (this.tiles.size == 0)
			this.raya.grounded = false;

		//goes together with get
		this.rectPool.free(this.rayaRect);

		// unscale the velocity by the inverse delta time and set
		// the latest position
		this.raya.desiredPosition.add(this.raya.velocity);
		this.raya.velocity.scl(1 / deltaTime);

		// Apply damping to the velocity on the x-axis so we don't
		// walk infinitely once a key was pressed
		this.raya.velocity.x *= 0;		//0 is totally stopped if not pressed

		this.raya.setPosition(this.raya.desiredPosition.x, this.raya.desiredPosition.y);

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
					rect.set(x * 16, y  * 16, 16, 16);
					tiles.add(rect);
                }
            }
        }
    }

    @Override
    public void backButtonPressed() {
    }


}
