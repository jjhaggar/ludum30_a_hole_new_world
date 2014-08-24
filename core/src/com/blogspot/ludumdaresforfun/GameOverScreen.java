package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameOverScreen extends BaseScreen{

    Image bg;

	public GameOverScreen() {
		if (Assets.musicBoss.isPlaying())
			Assets.musicBoss.stop();
		else if (Assets.musicStage.isPlaying())
			Assets.musicBoss.stop();

	    this.bg = new BGAnimated(Assets.GameOver);
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
