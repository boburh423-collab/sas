package com.towerdefense;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.towerdefense.screens.MenuScreen;
import com.towerdefense.managers.AssetLoader;

public class TowerDefenseGame extends Game {

    public static final String TITLE   = "Tower Defense";
    public static final int    V_WIDTH  = 1280;
    public static final int    V_HEIGHT = 720;

    public SpriteBatch  batch;
    public BitmapFont   font;
    public BitmapFont   bigFont;
    public AssetLoader  assets;

    @Override
    public void create() {
        batch  = new SpriteBatch();
        assets = new AssetLoader();
        assets.load();

        // Built-in LibGDX bitmap font – no external file needed
        font    = new BitmapFont();
        font.getData().setScale(1.5f);

        bigFont = new BitmapFont();
        bigFont.getData().setScale(3.2f);

        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        bigFont.dispose();
        assets.dispose();
    }
}
