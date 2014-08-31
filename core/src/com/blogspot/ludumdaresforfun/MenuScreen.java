package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MenuScreen extends BaseScreen{

    BGAnimated bg;
	private ConfigControllers configControllers;

    public MenuScreen() {

    	Assets.musicStage.setLooping(true);
    	Assets.musicStage.play();

    	this.bg = new BGAnimated(Assets.Intro);
    	this.stage.addActor(this.bg);

        this.configControllers = new ConfigControllers(this);
        this.configControllers.init();
    }

	@Override
	public void backButtonPressed() {
        Assets.dispose();
        Gdx.app.exit();
	}

    @Override
    public void enterButtonPressed() {
        this.configControllers.terminate();
        LD.getInstance().INTRO_SCREEN = new IntroScreen();
        LD.getInstance().setScreen(LD.getInstance().INTRO_SCREEN);
    }

	@Override
	public void resize (int width, int height) {
		this.stage.setViewport(new FitViewport(400, 240, this.stage.getCamera()));
		this.stage.getViewport().update(width, height, true);
	}

}
