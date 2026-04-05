package com.towerdefense.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.towerdefense.managers.AssetLoader;
import java.util.List;

public class Projectile {

    private static final float SPEED   = 320f;
    private static final float EXPLODE_TIME = 0.25f;

    private final String  texKey;
    private final Vector2 position;
    private final int     damage;
    private final boolean isSlowing;
    private final boolean isAoE;
    private final float   aoeRadius;

    private Enemy   target;
    private boolean done        = false;
    private boolean exploding   = false;
    private float   explodeTimer = 0f;

    public Projectile(String texKey, Vector2 start, Enemy target,
                      int damage, boolean isSlowing, boolean isAoE, float aoeRadius) {
        this.texKey    = texKey;
        this.position  = start.cpy();
        this.target    = target;
        this.damage    = damage;
        this.isSlowing = isSlowing;
        this.isAoE     = isAoE;
        this.aoeRadius = aoeRadius;
    }

    public void update(float dt, List<Enemy> enemies) {
        if (done) return;

        if (exploding) {
            explodeTimer += dt;
            if (explodeTimer >= EXPLODE_TIME) done = true;
            return;
        }

        // Re-target if target died
        if (!target.isAlive()) { done = true; return; }

        Vector2 dir = target.position.cpy().sub(position);
        float dist = dir.len();

        if (dist < SPEED * dt) {
            // Hit
            position.set(target.position);
            hitTarget(enemies);
        } else {
            dir.scl(SPEED * dt / dist);
            position.add(dir);
        }
    }

    private void hitTarget(List<Enemy> enemies) {
        if (isAoE) {
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                if (e.position.dst(position) <= aoeRadius) {
                    e.takeDamage(damage);
                    if (isSlowing) e.applySlow(0.4f, 2.5f);
                }
            }
            exploding = true;
        } else {
            target.takeDamage(damage);
            if (isSlowing) target.applySlow(0.4f, 2.5f);
            done = true;
        }
    }

    public void draw(SpriteBatch batch, AssetLoader assets) {
        if (done) return;

        if (exploding) {
            float alpha = 1f - explodeTimer / EXPLODE_TIME;
            float r     = aoeRadius * 2 * (1f - alpha * 0.3f);
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(assets.get("explosion"),
                position.x - r/2, position.y - r/2, r, r);
            batch.setColor(Color.WHITE);
            return;
        }

        float size = 12f;
        batch.draw(assets.get(texKey),
            position.x - size/2, position.y - size/2, size, size);
    }

    public boolean isDone() { return done; }
}
