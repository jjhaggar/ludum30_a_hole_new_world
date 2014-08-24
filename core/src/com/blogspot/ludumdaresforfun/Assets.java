package com.blogspot.ludumdaresforfun;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Assets {
	static AssetManager assetManager;
    static Animation playerAttack, playerEmpty, playerIntro, playerStand, playerWalk, playerJump, playerBeingHit, playerDie;
    static Animation playerShot;
    static Animation enemyWalk, enemyRun, enemyHurt;
    static Animation bossGethit, bossStanding,  bossWalking, bossJumping, bossFalling, bossAttack, bossSummon, bossDie;
    static Animation GameOver, Intro;
	static Texture bg;
	static float offsetPlayer, offsetBoss, offsetShot, offsetEnemy;

	// Music and Sounds
	public static Music music;
    public static HashMap<String, Sound> sounds = new HashMap<String, Sound>();

	static void loadAnimation() {
        final String TEXTURE_ATLAS_OBJECTS = "characters.pack";
		assetManager = new AssetManager();
		assetManager.load(TEXTURE_ATLAS_OBJECTS, TextureAtlas.class);
		assetManager.finishLoading();

        TextureAtlas atlas = assetManager.get(TEXTURE_ATLAS_OBJECTS);
		Array<AtlasRegion> regions;

		//BG
		regions = atlas.findRegions("intro");
		Intro = new Animation(0.25f, regions);
		Intro.setPlayMode(PlayMode.LOOP);

		regions = atlas.findRegions("game_over");
		GameOver = new Animation(0.25f, regions);
		GameOver.setPlayMode(PlayMode.LOOP);

		// Player
		regions = atlas.findRegions("char_attack");
		playerAttack = new Animation(0.25f, regions);

		regions = atlas.findRegions("char_empty");
		playerEmpty = new Animation(0, regions);

		regions = atlas.findRegions("char_intro");
		playerIntro = new Animation(0.25f, regions);

		regions = atlas.findRegions("char_standing");
		playerStand = new Animation(0.15f, regions);

		regions = atlas.findRegions("char_walking");
		playerWalk = new Animation(0.15f, regions);
		playerWalk.setPlayMode(Animation.PlayMode.LOOP);

		regions = atlas.findRegions("char_jumping");  //change to jumping
		playerJump = new Animation(0, regions.get(1));

		regions = atlas.findRegions("char_gethit");  //change to jumping
		playerBeingHit = new Animation(0.8f, regions);

		regions = atlas.findRegions("char_dying");  //change to jumping
		playerDie = new Animation(0.25f, regions);

		// Shot
		regions = atlas.findRegions("char_attack_holy_water");
		playerShot = new Animation(0, regions);
		offsetShot = regions.first().offsetX;

		// Enemy
		regions = atlas.findRegions("enemy_walking");//"enemy_walk"); // change
		enemyWalk = new Animation(0.15f, regions);
		enemyWalk.setPlayMode(Animation.PlayMode.LOOP);

		regions = atlas.findRegions("enemy_attack");//"enemy_run"); // change
		enemyRun = new Animation(0.15f, regions);

		regions = atlas.findRegions("enemy_dying");//"enemy_hurt"); // change
		enemyHurt = new Animation(0.15f, regions);

		// Boss
		//regions = atlas.findRegions("boss_gethit");
		regions = atlas.findRegions("boss_empty");
		bossGethit = new Animation(0.15f, regions);
		bossGethit.setPlayMode(PlayMode.LOOP);


		regions = atlas.findRegions("boss_standing");
		bossStanding = new Animation(0.15f, regions);
		bossStanding.setPlayMode(PlayMode.LOOP);

		regions = atlas.findRegions("boss_walking");
		bossWalking = new Animation(0.15f, regions);
		bossWalking.setPlayMode(PlayMode.LOOP);

		regions = atlas.findRegions("boss_jump_a");
		bossJumping = new Animation(0.15f, regions);
		bossJumping.setPlayMode(PlayMode.LOOP);

		regions = atlas.findRegions("boss_jump_d");
		bossFalling = new Animation(0.15f, regions);
		bossFalling.setPlayMode(PlayMode.LOOP);

		regions = atlas.findRegions("boss_attack_close");
		bossAttack = new Animation(0.15f, regions);

		regions = atlas.findRegions("boss_attack_distance");
		bossSummon = new Animation(0.15f, regions);

		regions = atlas.findRegions("boss_dying");
		bossDie = new Animation(0.30f, regions);
	}

	static TextureRegion getBg() {
		bg = new Texture(Gdx.files.internal("bg.png"));
		return new TextureRegion(bg, 0, 0, 400, 240);

	}

	public static void loadMusicAndSound() {
		music = Gdx.audio.newMusic(Gdx.files.internal("music/mainTheme.ogg"));
		music = Gdx.audio.newMusic(Gdx.files.internal("music/finalBoss.ogg"));
		// Player
        addSound("playerAttack");
        addSound("playerHurt");
        addSound("playerDead");
        addSound("playerJump");
        addSound("playerShot");
		// Enemy
        addSound("enemyAttack");
        addSound("enemyDead");
		// Boss
        addSound("bossAttack");
        addSound("bossHurt");
        addSound("bossDead");
		// Others
        addSound("closeDoor");
        addSound("holyWaterBroken");
	}

    public static void addSound(final String name) {
        sounds.put(name, Gdx.audio.newSound(Gdx.files.internal("sounds/" + name + ".ogg")));
    }

    public static void playSound(final String name) {
        sounds.get(name).play();
    }

	static void dispose() {
	}
}