package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class Assets {
	static AssetManager assetManager;
	static Animation stand, walk, jump, standingShot;
	static Animation shotAnim;
	static Animation bossStanding, bossGetHit;

	static void loadAnimation() {
        final String TEXTURE_ATLAS_OBJECTS = "characters.pack";
		assetManager = new AssetManager();
		assetManager.load(TEXTURE_ATLAS_OBJECTS, TextureAtlas.class);
		assetManager.finishLoading();

        TextureAtlas atlas = assetManager.get(TEXTURE_ATLAS_OBJECTS);
		Array<AtlasRegion> regions;

		regions = atlas.findRegions("char_walking");
		walk = new Animation(0.15f, regions);
		walk.setPlayMode(Animation.PlayMode.LOOP);

		regions = atlas.findRegions("char_standing");
		stand = new Animation(0, regions);

		regions = atlas.findRegions("char_standing");  //change to jumping
		jump = new Animation(0, regions);

		regions = atlas.findRegions("char_standing");	//change to shooting
		standingShot = new Animation(0.15f, regions);

		regions = atlas.findRegions("char_standing");
		shotAnim = new Animation(0.15f, regions);

		regions = atlas.findRegions("boss_standing");
		bossStanding = new Animation(0.15f, regions);

		regions = atlas.findRegions("boss_gethit");
		bossGetHit = new Animation(0.15f, regions);
	}
}