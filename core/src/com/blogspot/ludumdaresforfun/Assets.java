package com.blogspot.ludumdaresforfun;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class Assets {
	static AssetManager assetManager;
	static Animation stand, walk, jump, standingShot, shotAnim;

	static void loadAnimation() {
        final String TEXTURE_ATLAS_OBJECTS = "rayaman.pack";
		assetManager = new AssetManager();
		assetManager.load(TEXTURE_ATLAS_OBJECTS, TextureAtlas.class);
		assetManager.finishLoading();

        TextureAtlas atlas = assetManager.get(TEXTURE_ATLAS_OBJECTS);
		Array<AtlasRegion> regions;

		regions = atlas.findRegions("rayaman_walking");
		walk = new Animation(0.15f, regions);
		walk.setPlayMode(Animation.PlayMode.LOOP);

		regions = atlas.findRegions("rayaman_standing");
		stand = new Animation(0, regions);

		regions = atlas.findRegions("rayaman_jumping");
		jump = new Animation(0, regions);

		regions = atlas.findRegions("rayaman_standing_shot");
		standingShot = new Animation(0.15f, regions);

		regions = atlas.findRegions("rayaman_shot");
		shotAnim = new Animation(0.15f, regions);
	}
}