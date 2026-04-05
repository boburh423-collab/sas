package com.towerdefense.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;

public class AssetLoader implements Disposable {

    private final Map<String, Texture> textures = new HashMap<>();

    public void load() {
        // Generate all textures programmatically – no external image files needed
        textures.put("bg",           makeColor(40, 40, 60, 1280, 720));
        textures.put("cell_empty",   makeColor(55, 100, 55, 64, 64));
        textures.put("cell_path",    makeColor(180, 140, 80, 64, 64));
        textures.put("cell_hover",   makeColor(90, 140, 90, 64, 64));
        textures.put("cell_blocked", makeColor(70, 50, 50, 64, 64));

        // Tower textures
        textures.put("tower_arrow",  makeTower(60, 120, 200, 64));
        textures.put("tower_cannon", makeTower(200, 80, 60, 64));
        textures.put("tower_ice",    makeTower(80, 200, 220, 64));
        textures.put("tower_laser",  makeTower(220, 220, 60, 64));
        textures.put("tower_bomb",   makeTower(220, 100, 30, 64));

        // Enemy textures
        textures.put("enemy_basic",  makeEnemy(200, 80, 80, 32));
        textures.put("enemy_fast",   makeEnemy(220, 160, 60, 24));
        textures.put("enemy_tank",   makeEnemy(120, 120, 200, 48));
        textures.put("enemy_flying", makeEnemy(180, 80, 220, 28));
        textures.put("enemy_boss",   makeEnemy(220, 40, 40, 64));

        // Projectile textures
        textures.put("proj_arrow",   makeCircle(200, 200, 60, 8));
        textures.put("proj_cannon",  makeCircle(200, 100, 40, 12));
        textures.put("proj_ice",     makeCircle(80, 200, 240, 10));
        textures.put("proj_laser",   makeCircle(240, 240, 60, 6));
        textures.put("proj_bomb",    makeCircle(240, 140, 20, 14));

        // UI textures
        textures.put("panel",        makeColor(20, 20, 40, 200, 720));
        textures.put("btn_normal",   makeRounded(60, 80, 160, 180, 50));
        textures.put("btn_hover",    makeRounded(80, 110, 200, 180, 50));
        textures.put("btn_pressed",  makeRounded(40, 60, 130, 180, 50));
        textures.put("hp_bar_bg",    makeColor(80, 20, 20, 32, 4));
        textures.put("hp_bar_fg",    makeColor(60, 200, 60, 32, 4));
        textures.put("gold_icon",    makeCircle(220, 180, 0, 16));
        textures.put("heart_icon",   makeColor(220, 40, 60, 16, 16));
        textures.put("wave_bg",      makeColor(30, 30, 60, 320, 60));
        textures.put("tower_range",  makeRange(200, 200, 255, 80, 256));
        textures.put("explosion",    makeExplosion(256));
        textures.put("logo",         makeLogo(500, 120));
        textures.put("tile_grass",   makeGrass(64, 64));
        textures.put("tile_path",    makePath(64, 64));
        textures.put("tile_water",   makeWater(64, 64));
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Texture makeColor(int r, int g, int b, int w, int h) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(r/255f, g/255f, b/255f, 1f);
        p.fill();
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture makeCircle(int r, int g, int b, int radius) {
        int s = radius * 2 + 2;
        Pixmap p = new Pixmap(s, s, Pixmap.Format.RGBA8888);
        p.setColor(r/255f, g/255f, b/255f, 1f);
        p.fillCircle(s/2, s/2, radius);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture makeTower(int r, int g, int b, int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        // Base
        p.setColor(r/255f * 0.6f, g/255f * 0.6f, b/255f * 0.6f, 1f);
        p.fillCircle(size/2, size/2, size/2 - 2);
        // Center
        p.setColor(r/255f, g/255f, b/255f, 1f);
        p.fillCircle(size/2, size/2, size/3);
        // Highlight
        p.setColor(1f, 1f, 1f, 0.3f);
        p.fillCircle(size/2 - size/6, size/2 - size/6, size/8);
        // Barrel
        p.setColor(r/255f * 0.8f, g/255f * 0.8f, b/255f * 0.8f, 1f);
        p.fillRectangle(size/2 - 3, 0, 6, size/2);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture makeEnemy(int r, int g, int b, int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        // Body
        p.setColor(r/255f, g/255f, b/255f, 1f);
        p.fillRectangle(4, 8, size-8, size-12);
        // Head
        p.setColor(r/255f * 1.2f > 1 ? 1f : r/255f * 1.2f,
                   g/255f * 1.2f > 1 ? 1f : g/255f * 1.2f,
                   b/255f * 1.2f > 1 ? 1f : b/255f * 1.2f, 1f);
        p.fillCircle(size/2, size/4, size/4);
        // Eyes
        p.setColor(0.1f, 0.1f, 0.1f, 1f);
        p.fillCircle(size/2 - size/8, size/4, 2);
        p.fillCircle(size/2 + size/8, size/4, 2);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture makeRounded(int r, int g, int b, int w, int h) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(r/255f, g/255f, b/255f, 1f);
        int rad = 10;
        p.fillRectangle(rad, 0, w - rad*2, h);
        p.fillRectangle(0, rad, w, h - rad*2);
        p.fillCircle(rad, rad, rad);
        p.fillCircle(w - rad, rad, rad);
        p.fillCircle(rad, h - rad, rad);
        p.fillCircle(w - rad, h - rad, rad);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture makeRange(int r, int g, int b, int alpha, int diameter) {
        Pixmap p = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        p.setColor(r/255f, g/255f, b/255f, alpha/255f * 0.15f);
        p.fillCircle(diameter/2, diameter/2, diameter/2 - 2);
        p.setColor(r/255f, g/255f, b/255f, alpha/255f * 0.6f);
        p.drawCircle(diameter/2, diameter/2, diameter/2 - 2);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture makeExplosion(int size) {
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        int cx = size/2, cy = size/2;
        for (int i = size/2; i > 0; i--) {
            float t = 1f - (float)i/(size/2);
            float r = 1f;
            float g = t < 0.5f ? t * 2f : 1f - (t - 0.5f) * 1.5f;
            float b = 0f;
            float a = (1f - t) * 0.8f;
            p.setColor(r, g, b, a);
            p.fillCircle(cx, cy, i);
        }
        Texture tx = new Texture(p); p.dispose(); return tx;
    }

    private Texture makeLogo(int w, int h) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        // gradient-like
        for (int y = 0; y < h; y++) {
            float t = (float)y / h;
            p.setColor(0.1f + t*0.1f, 0.1f + t*0.3f, 0.4f + t*0.4f, 1f);
            p.drawLine(0, y, w, y);
        }
        p.setColor(0f, 0.8f, 1f, 0.5f);
        p.drawLine(0, 0, w, 0);
        p.drawLine(0, h-1, w, h-1);
        Texture tx = new Texture(p); p.dispose(); return tx;
    }

    private Texture makeGrass(int w, int h) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(0.25f, 0.55f, 0.25f, 1f);
        p.fill();
        // texture dots
        p.setColor(0.2f, 0.5f, 0.2f, 0.5f);
        for (int i = 0; i < 12; i++) {
            int x = (i*17+3) % w, y = (i*23+7) % h;
            p.fillCircle(x, y, 3);
        }
        Texture tx = new Texture(p); p.dispose(); return tx;
    }

    private Texture makePath(int w, int h) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(0.72f, 0.58f, 0.36f, 1f);
        p.fill();
        p.setColor(0.68f, 0.54f, 0.32f, 0.6f);
        for (int i = 0; i < 8; i++) {
            int x = (i*19+5) % w, y = (i*13+3) % h;
            p.fillCircle(x, y, 2);
        }
        Texture tx = new Texture(p); p.dispose(); return tx;
    }

    private Texture makeWater(int w, int h) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(0.1f, 0.3f, 0.7f, 1f);
        p.fill();
        p.setColor(0.2f, 0.5f, 0.9f, 0.4f);
        p.drawLine(0, h/3, w, h/3);
        p.drawLine(0, 2*h/3, w, 2*h/3);
        Texture tx = new Texture(p); p.dispose(); return tx;
    }

    public Texture get(String key) {
        return textures.get(key);
    }

    @Override
    public void dispose() {
        textures.values().forEach(Texture::dispose);
        textures.clear();
    }
}
