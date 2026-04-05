package com.towerdefense.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.towerdefense.managers.AssetLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleManager {

    private static class Particle {
        Vector2 pos, vel;
        float   life, maxLife;
        float   size;
        Color   color;

        Particle(float x, float y, float vx, float vy,
                 float life, float size, Color c) {
            pos     = new Vector2(x, y);
            vel     = new Vector2(vx, vy);
            this.life = this.maxLife = life;
            this.size = size;
            color   = c.cpy();
        }
    }

    private final List<Particle> particles = new ArrayList<>();

    public void spawnDeath(float x, float y, Color c) {
        for (int i = 0; i < 10; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(30f, 90f);
            particles.add(new Particle(x, y,
                MathUtils.cosDeg(angle)*speed,
                MathUtils.sinDeg(angle)*speed,
                MathUtils.random(0.4f, 0.8f),
                MathUtils.random(4f, 10f), c));
        }
    }

    public void spawnGold(float x, float y) {
        for (int i = 0; i < 5; i++) {
            float angle = MathUtils.random(60f, 120f);
            float speed = MathUtils.random(40f, 80f);
            particles.add(new Particle(x, y,
                MathUtils.cosDeg(angle)*speed,
                MathUtils.sinDeg(angle)*speed,
                0.7f, 6f, new Color(1f, 0.85f, 0f, 1f)));
        }
    }

    public void update(float dt) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.life -= dt;
            if (p.life <= 0) { it.remove(); continue; }
            p.pos.x += p.vel.x * dt;
            p.pos.y += p.vel.y * dt;
            p.vel.y -= 120f * dt; // gravity
        }
    }

    public void draw(SpriteBatch batch, AssetLoader assets) {
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            batch.setColor(p.color.r, p.color.g, p.color.b, alpha);
            batch.draw(assets.get("proj_arrow"),
                p.pos.x - p.size/2, p.pos.y - p.size/2, p.size, p.size);
        }
        batch.setColor(Color.WHITE);
    }
}
