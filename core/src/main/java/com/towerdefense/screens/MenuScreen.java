package com.towerdefense.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.towerdefense.TowerDefenseGame;

public class MenuScreen implements Screen {

    private final TowerDefenseGame game;
    private final ShapeRenderer    shapes = new ShapeRenderer();

    private final Rectangle btnPlay   = new Rectangle(540, 310, 200, 55);
    private final Rectangle btnExit   = new Rectangle(540, 235, 200, 55);

    private float time = 0f;

    public MenuScreen(TowerDefenseGame game) {
        this.game = game;
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int sx, int sy, int ptr, int btn) {
                float x = sx, y = TowerDefenseGame.V_HEIGHT - sy;
                if (btnPlay.contains(x, y)) game.setScreen(new GameScreen(game));
                if (btnExit.contains(x, y)) Gdx.app.exit();
                return true;
            }
        });
    }

    @Override
    public void render(float dt) {
        time += dt;
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Animated stars BG
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 80; i++) {
            float fx = ((i * 137.508f) % TowerDefenseGame.V_WIDTH);
            float fy = ((i * 293.74f)  % TowerDefenseGame.V_HEIGHT);
            float brightness = 0.5f + 0.5f * (float)Math.sin(time * 1.5f + i);
            shapes.setColor(brightness, brightness, brightness, 1f);
            shapes.circle(fx, fy, 1.5f);
        }

        // Ground gradient
        for (int y = 0; y < 120; y++) {
            float t = y / 120f;
            shapes.setColor(0.05f + t*0.1f, 0.15f + t*0.2f, 0.05f, 1f);
            shapes.rect(0, y, TowerDefenseGame.V_WIDTH, 1);
        }
        shapes.end();

        // Logo texture + text
        game.batch.begin();
        var logoTex = game.assets.get("logo");
        game.batch.draw(logoTex, 390, 450, 500, 120);

        // Title
        game.bigFont.setColor(0f, 0.9f, 1f, 1f);
        game.bigFont.draw(game.batch, "TOWER", 490, 540);
        game.bigFont.setColor(1f, 0.7f, 0f, 1f);
        game.bigFont.draw(game.batch, "DEFENSE", 480, 495);

        game.font.setColor(0.7f, 0.7f, 0.7f, 1f);
        game.font.draw(game.batch, "Defend your base from waves of enemies!", 380, 420);
        game.font.draw(game.batch, "Build & upgrade towers to survive 25 waves.", 380, 395);

        // Buttons
        drawButton(btnPlay, "▶  PLAY",   new Color(0.2f, 0.7f, 0.2f, 1f));
        drawButton(btnExit, "✖  EXIT",   new Color(0.7f, 0.2f, 0.2f, 1f));

        // Tower info
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "Tower Types:", 100, 440);
        game.font.setColor(Color.WHITE);
        String[] types = {"🏹 Arrow – Fast, low damage",
                          "💣 Cannon – Slow, AoE blast",
                          "❄  Ice    – Slows enemies",
                          "⚡ Laser  – High single target",
                          "💥 Bomb   – Massive AoE"};
        for (int i = 0; i < types.length; i++)
            game.font.draw(game.batch, types[i], 90, 415 - i * 28);

        game.batch.end();
    }

    private void drawButton(Rectangle r, String label, Color c) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(c);
        shapes.rect(r.x, r.y, r.width, r.height, 8, 8, 8, 8);
        shapes.end();

        game.font.setColor(Color.WHITE);
        // center text inside button
        var layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, label);
        game.batch.begin();
        game.font.draw(game.batch, label,
            r.x + (r.width  - layout.width)  / 2,
            r.y + (r.height + layout.height) / 2);
        game.batch.end();
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { shapes.dispose(); }
}
