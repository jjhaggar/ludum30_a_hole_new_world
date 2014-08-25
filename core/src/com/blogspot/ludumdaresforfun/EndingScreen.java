package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class EndingScreen extends BaseScreen{

    BGAnimated bg;

    public EndingScreen() {

    	this.bg = new BGAnimated(Assets.SequenceEnding);
    	this.stage.addActor(this.bg);
    	this.stage.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                EndingScreen.this.bg.act(delta);
                if (Assets.SequenceEnding.isAnimationFinished(EndingScreen.this.bg.stateTime)) {
                    Assets.SequenceEnding.getKeyFrame(delta);
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
        LD.getInstance().setScreen(new CreditsScreen());
    }

	@Override
	public void resize (int width, int height) {
		this.stage.setViewport(new FitViewport(400, 240, this.stage.getCamera()));
		this.stage.getViewport().update(width, height, true);
	}

}
