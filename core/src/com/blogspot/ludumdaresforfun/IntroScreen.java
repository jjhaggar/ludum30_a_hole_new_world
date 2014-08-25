package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class IntroScreen extends BaseScreen{

    BGAnimated bg;

    public IntroScreen() {

    	Assets.musicStage.setLooping(true);
    	Assets.musicStage.play();

    	this.bg = new BGAnimated(Assets.SequenceIntro);
    	this.stage.addActor(this.bg);
    	this.stage.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                IntroScreen.this.bg.act(delta);
                if (Assets.SequenceIntro.isAnimationFinished(IntroScreen.this.bg.stateTime)) {
                    LD.getInstance().MAIN_SCREEN = new MainScreen(false);
                    LD.getInstance().setScreen(LD.getInstance().MAIN_SCREEN);
                    return true;
                }
                return false;
            }
        });
    }

	@Override
	public void backButtonPressed() {
        Assets.dispose();
        Gdx.app.exit();
	}

    @Override
    public void enterButtonPressed() {
    }

	@Override
	public void resize (int width, int height) {
		this.stage.setViewport(new FitViewport(400, 240, this.stage.getCamera()));
		this.stage.getViewport().update(width, height, true);
	}

}
