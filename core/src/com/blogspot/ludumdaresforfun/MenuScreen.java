package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MenuScreen extends BaseScreen{

    BGAnimated bg;

	public MenuScreen() {
	    Assets.loadMusicAndSound();
	    Assets.loadAnimation();
	    if (!Assets.musicStage.isPlaying()) {
	    	Assets.musicStage.setLooping(true);
	    	Assets.musicStage.play();
	    }
	    this.bg = new BGAnimated(Assets.Intro);
	    this.stage.addActor(this.bg);
	}

	@Override
	public void backButtonPressed() {
        Assets.dispose();
        Gdx.app.exit();
	}

    @Override
    public void enterButtonPressed() {
        LD.getInstance().MAIN_SCREEN = new MainScreen();
        LD.getInstance().setScreen(LD.getInstance().MAIN_SCREEN);
    }

	@Override
	public void resize (int width, int height) {
		this.stage.setViewport(new FitViewport(400, 240, this.stage.getCamera()));
		this.stage.getViewport().update(width, height, true);
	}

}
