package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Game;

public class LD extends Game {

    static LD instance;
    public MenuScreen MENU_SCREEN;
    public GameOverScreen GAMEOVER_SCREEN;

    public MainScreen MAIN_SCREEN;

    @Override
    public void create() {
        instance = this;
        this.setScreen(new MenuScreen());
    }

    @Override
    public void render() {
        super.render();
    }

    public static LD getInstance() {
        return instance;
    }

}
