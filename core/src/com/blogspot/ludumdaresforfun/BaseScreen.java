package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class BaseScreen implements Screen{

	Stage stage;

	//public LudumGame game;

	public BaseScreen() {
		//this.game = LudumGame.getInstance();
		this.stage = new Stage(new ScreenViewport());

		Gdx.input.setInputProcessor(this.stage);

		this.stage.addListener(new InputListener(){
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if((keycode == Keys.BACK) || (keycode == Keys.ESCAPE)){
					BaseScreen.this.backButtonPressed();
				}
				return super.keyDown(event, keycode);
			}
		});

	}

	@Override
	public void resize (int width, int height) {
		//this.stage.setViewport(new FitViewport(LudumGame.getInstance().WIDTH, LudumGame.getInstance().HEIGHT, this.stage.getCamera()));
		this.stage.getViewport().update(width, height, true);
	}

	@Override
	public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		this.stage.act(delta);
		this.stage.draw();
	}

	@Override
	public void dispose() {
		this.stage.dispose();
	}


	@Override
	public void show() {
		Gdx.input.setInputProcessor(this.stage);
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	public abstract void backButtonPressed();
}
