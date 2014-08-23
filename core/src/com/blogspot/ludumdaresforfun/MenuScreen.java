package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class MenuScreen extends BaseScreen{

    Image bg;

	public MenuScreen() {
	    this.bg = new Image(Assets.getBg());
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

}
