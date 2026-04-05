package com.towerdefense.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.towerdefense.managers.AssetLoader;
import java.util.List;

public class Enemy {

    public enum Type {
        BASIC  (80,  60f, 10, 15, 1f,   "enemy_basic",  "Basic"),
        FAST   (40,  120f,5,  12, 0.7f, "enemy_fast",   "Fast"),
        TANK   (300, 30f, 25, 40, 1.3f, "enemy_tank",   "Tank"),
        FLYING (100, 90f, 15, 25, 0.8f, "enemy_flying", "Flying"),
        BOSS   (1000,40f, 50, 100,1.8f, "enemy_boss",   "Boss");

        public final int   baseHp;
        public final float speed;
        public final int   damage;   // lives damage on reach
        public final int   reward;   // gold
        public final float size;
        public final String texKey;
        public final String displayName;

        Type(int hp, float spd, int dmg, int rwd, float sz, String tk, String dn) {
            baseHp=hp; speed=spd; damage=dmg; reward=rwd; size=sz; texKey=tk; displayName=dn;
        }
    }

    public final Type    type;
    public       Vector2 position;
    public       float   pathProgress = 0f;  // index along path (float for lerp)

    private int          hp;
    private int          maxHp;
    private boolean      alive        = true;
    private float        slowTimer    = 0f;
    private float        slowFactor   = 1f;
    private float        angle        = 0f;

    // Particle-like hit flash
    private float        hitFlash     = 0f;

    private final List<Vector2> path;

    public Enemy(Type type, List<Vector2> path, int wave) {
        this.type     = type;
        this.path     = path;
        // Scale HP with wave
        float scale   = 1f + wave * 0.12f;
        this.maxHp    = (int)(type.baseHp * scale);
        this.hp       = maxHp;
        this.position = path.get(0).cpy();
    }

    public void update(float dt) {
        if (!alive) return;

        // Slow decay
        if (slowTimer > 0) {
            slowTimer -= dt;
            if (slowTimer <= 0) slowFactor = 1f;
        }
        if (hitFlash > 0) hitFlash -= dt * 3f;

        float effectiveSpeed = type.speed * slowFactor * dt;
        float remaining      = effectiveSpeed;

        while (remaining > 0 && pathProgress < path.size() - 1) {
            int    fromIdx  = (int)pathProgress;
            float  frac     = pathProgress - fromIdx;
            Vector2 from    = path.get(fromIdx);
            Vector2 to      = path.get(fromIdx + 1);
            float   segLen  = from.dst(to);
            float   fracLeft = (1f - frac);
            float   distLeft = fracLeft * segLen;

            if (remaining >= distLeft) {
                remaining    -= distLeft;
                pathProgress  = fromIdx + 1f;
            } else {
                pathProgress += remaining / segLen;
                remaining     = 0;
            }
        }

        // Interpolate position
        int   fromIdx = Math.min((int)pathProgress, path.size() - 2);
        float frac    = pathProgress - fromIdx;
        Vector2 from  = path.get(fromIdx);
        Vector2 to    = path.get(Math.min(fromIdx + 1, path.size() - 1));
        position.set(from).lerp(to, frac);

        // Angle
        float dx = to.x - from.x, dy = to.y - from.y;
        if (dx != 0 || dy != 0)
            angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
    }

    public void draw(SpriteBatch batch, AssetLoader assets) {
        if (!alive) return;

        float size = type.size * 58f;

        // Hit flash
        if (hitFlash > 0)
            batch.setColor(1f, 1f - hitFlash, 1f - hitFlash, 1f);

        TextureRegion region = new TextureRegion(assets.get(type.texKey));
        batch.draw(region,
            position.x - size/2, position.y - size/2,
            size/2, size/2,
            size, size,
            1f, 1f, angle - 90f);

        batch.setColor(Color.WHITE);

        // HP bar
        float barW = size * 1.1f;
        float barH = 5f;
        float barX = position.x - barW/2;
        float barY = position.y + size/2 + 3;

        batch.draw(assets.get("hp_bar_bg"), barX, barY, barW, barH);
        float frac = (float)hp / maxHp;
        batch.setColor(1f - frac, frac, 0f, 1f);
        batch.draw(assets.get("hp_bar_fg"), barX, barY, barW * frac, barH);
        batch.setColor(Color.WHITE);
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        hitFlash = 1f;
        if (hp <= 0) alive = false;
    }

    public void applySlow(float factor, float duration) {
        slowFactor = factor;
        slowTimer  = duration;
    }

    public boolean isAlive()       { return alive; }
    public boolean hasReachedEnd() { return pathProgress >= path.size() - 1; }
    public int     getReward()     { return type.reward; }
    public int     getDamage()     { return type.damage; }
    public float   getHpFraction() { return (float)hp / maxHp; }
}
