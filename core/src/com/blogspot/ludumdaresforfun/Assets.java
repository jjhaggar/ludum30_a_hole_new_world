package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Assets {
	static AssetManager assetManager;
    static Animation playerAttack, playerEmpty, playerIntro, playerStand, playerWalk, playerJump, playerStandShot;
    static Animation playerShot;
    static Animation enemyWalk, enemyRun, enemyHurt;
    static Animation bossGethit, bossStanding,  bossWalking;
	static Texture bg;
	static float offsetPlayer, offsetBoss, offsetShot, offsetEnemy;

	static void loadAnimation() {
        final String TEXTURE_ATLAS_OBJECTS = "characters.pack";
		assetManager = new AssetManager();
		assetManager.load(TEXTURE_ATLAS_OBJECTS, TextureAtlas.class);
		assetManager.finishLoading();

        TextureAtlas atlas = assetManager.get(TEXTURE_ATLAS_OBJECTS);
		Array<AtlasRegion> regions;

		// Player
		regions = atlas.findRegions("char_attack");
		playerAttack = new Animation(0.15f, regions);

		regions = atlas.findRegions("char_empty");
		playerEmpty = new Animation(0, regions);

		regions = atlas.findRegions("char_intro");
		playerIntro = new Animation(0, regions);

		regions = atlas.findRegions("char_standing");
		playerStand = new Animation(0, regions);

		regions = atlas.findRegions("char_walking");
		playerWalk = new Animation(0.15f, regions);
		playerWalk.setPlayMode(Animation.PlayMode.LOOP);

		regions = atlas.findRegions("char_standing");  //change to jumping
		playerJump = new Animation(0, regions);

		regions = atlas.findRegions("char_standing");	//change to shooting
		playerStandShot = new Animation(0.15f, regions);

		// Shot
		regions = atlas.findRegions("char_attack_holy_water");
		playerShot = new Animation(0, regions);
		offsetShot = regions.first().offsetX;

		// Enemy
		regions = atlas.findRegions("char_walking");//"enemy_walk"); // change
		enemyWalk = new Animation(0.15f, regions);
		enemyWalk.setPlayMode(Animation.PlayMode.LOOP);

		regions = atlas.findRegions("char_intro");//"enemy_run"); // change
		enemyRun = new Animation(0, regions);

		regions = atlas.findRegions("char_gethit");//"enemy_hurt"); // change
		enemyHurt = new Animation(0.15f, regions);

		// Boss
		regions = atlas.findRegions("boss_gethit");
		bossGethit = new Animation(0.15f, regions);

		regions = atlas.findRegions("boss_standing");
		bossStanding = new Animation(0.15f, regions);
		bossStanding.setPlayMode(PlayMode.LOOP);

		regions = atlas.findRegions("boss_walking");
		bossWalking = new Animation(0.15f, regions);
	}

	static TextureRegion getBg() {
		bg = new Texture(Gdx.files.internal("bg.png"));
		return new TextureRegion(bg, 0, 0, 400, 240);

	}

	static void dispose() {
	    bg.dispose();
	}
}