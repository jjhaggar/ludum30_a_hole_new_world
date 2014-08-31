package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameOverScreen extends BaseScreen{

    Image bg;
    public boolean bossCheckPoint = false;
	private ConfigControllers configControllers;

	public GameOverScreen(boolean checkPoint) {
		this.bossCheckPoint = checkPoint;

	    this.bg = new BGAnimated(Assets.GameOver);
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
    	Assets.musicStage.setLooping(true);
    	Assets.musicStage.play();
        LD.getInstance().MAIN_SCREEN = new MainScreen(this.bossCheckPoint);
        LD.getInstance().setScreen(LD.getInstance().MAIN_SCREEN);
    }

	@Override
	public void resize (int width, int height) {
		this.stage.setViewport(new FitViewport(400, 240, this.stage.getCamera()));
		this.stage.getViewport().update(width, height, true);
	}

}
