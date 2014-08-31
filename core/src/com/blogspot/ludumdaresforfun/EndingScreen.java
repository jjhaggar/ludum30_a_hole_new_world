package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class EndingScreen extends BaseScreen{

    BGAnimated bg;
    boolean lighningAlreadyPlaying;
    private ConfigControllers configControllers;

    public EndingScreen() {

    	this.bg = new BGAnimated(Assets.SequenceEnding);
    	this.stage.addActor(this.bg);
    	if(Assets.musicBoss.isLooping())
    		Assets.musicBoss.stop();

    	this.stage.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                EndingScreen.this.bg.act(delta);
                if ((Assets.SequenceEnding.getKeyFrameIndex(EndingScreen.this.bg.stateTime) == 20) && (!EndingScreen.this.lighningAlreadyPlaying)){
                	Assets.playSound("lightning");
                	lighningAlreadyPlaying = true;
                }
                if (Assets.SequenceEnding.isAnimationFinished(EndingScreen.this.bg.stateTime))
                	bg.animation = new Animation(0, Assets.SequenceEnding.getKeyFrames()[Assets.SequenceEnding.getKeyFrames().length - 1]);

                return false;
            }
        });
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
        LD.getInstance().setScreen(new CreditsScreen());
    }

	@Override
	public void resize (int width, int height) {
		this.stage.setViewport(new FitViewport(400, 240, this.stage.getCamera()));
		this.stage.getViewport().update(width, height, true);
	}

}
