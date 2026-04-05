package com.towerdefense.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.towerdefense.managers.AssetLoader;
import java.util.ArrayList;
import java.util.List;

public class Tower {

    public enum Type {
        ARROW  (50,  80f, 1.2f, 15, "tower_arrow",  "proj_arrow",  "Arrow Tower",  "Fast attack, low dmg"),
        CANNON (100, 100f,0.6f, 45, "tower_cannon", "proj_cannon", "Cannon Tower", "Slow AoE damage"),
        ICE    (80,  90f, 0.9f, 20, "tower_ice",    "proj_ice",    "Ice Tower",    "Slows enemies"),
        LASER  (150, 110f,0.4f, 60, "tower_laser",  "proj_laser",  "Laser Tower",  "High single target"),
        BOMB   (120, 95f, 0.7f, 80, "tower_bomb",   "proj_bomb",   "Bomb Tower",   "Huge AoE splash");

        public final int     cost;
        public final float   range;
        public final float   fireRate;   // shots/sec
        public final int     damage;
        public final String  texKey;
        public final String  projKey;
        public final String  displayName;
        public final String  description;

        Type(int cost, float range, float fr, int dmg,
             String tk, String pk, String name, String desc) {
            this.cost=cost; this.range=range; this.fireRate=fr;
            this.damage=dmg; this.texKey=tk; this.projKey=pk;
            this.displayName=name; this.description=desc;
        }
    }

    public final Type      type;
    public final int       col, row;
    public final Vector2   position;

    private float          angle        = 0f;
    private float          fireCooldown = 0f;
    private int            level        = 1;
    private int            killCount    = 0;

    private final List<Projectile> projectiles = new ArrayList<>();

    public Tower(Type type, int col, int row, Vector2 pos) {
        this.type     = type;
        this.col      = col;
        this.row      = row;
        this.position = pos.cpy();
    }

    public void update(float dt, List<Enemy> enemies) {
        fireCooldown -= dt;

        // Find nearest enemy in range
        Enemy target = findTarget(enemies);
        if (target != null) {
            // Rotate toward target
            float dx = target.position.x - position.x;
            float dy = target.position.y - position.y;
            angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

            // Shoot
            if (fireCooldown <= 0f) {
                fireCooldown = 1f / getFireRate();
                Projectile proj = new Projectile(
                    type.projKey,
                    position.cpy(),
                    target,
                    getDamage(),
                    type == Type.ICE,
                    type == Type.CANNON || type == Type.BOMB,
                    type == Type.BOMB ? 60f : (type == Type.CANNON ? 40f : 0f)
                );
                projectiles.add(proj);
            }
        }

        // Update projectiles
        projectiles.removeIf(p -> {
            p.update(dt, enemies);
            return p.isDone();
        });
    }

    private Enemy findTarget(List<Enemy> enemies) {
        Enemy best = null;
        float bestProgress = -1f;
        float range = getRange();
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            if (position.dst(e.position) <= range) {
                if (e.pathProgress > bestProgress) {
                    bestProgress = e.pathProgress;
                    best = e;
                }
            }
        }
        return best;
    }

    public void draw(SpriteBatch batch, AssetLoader assets) {
        // Draw range circle when selected (done by GameScreen)
        // Draw tower
        var tex = assets.get(type.texKey);
        float size = 52f;
        batch.draw(tex,
            position.x - size/2, position.y - size/2,
            size/2, size/2,
            size, size,
            1f, 1f, angle);

        // Draw projectiles
        for (Projectile p : projectiles) p.draw(batch, assets);
    }

    public void drawRange(SpriteBatch batch, AssetLoader assets) {
        float r = getRange() * 2;
        var tex = assets.get("tower_range");
        batch.setColor(1f, 1f, 1f, 0.5f);
        batch.draw(tex, position.x - r/2, position.y - r/2, r, r);
        batch.setColor(Color.WHITE);
    }

    // ── upgrade ─────────────────────────────────────────────

    public int getUpgradeCost() {
        return type.cost / 2 * level;
    }

    public boolean canUpgrade() { return level < 3; }

    public void upgrade() {
        if (level < 3) level++;
    }

    public int getSellValue() {
        int total = type.cost;
        for (int l = 1; l < level; l++) total += type.cost / 2 * l;
        return total / 2;
    }

    // ── stats with level scaling ─────────────────────────────

    public float getRange()    { return type.range    * (1f + (level - 1) * 0.25f); }
    public float getFireRate() { return type.fireRate * (1f + (level - 1) * 0.30f); }
    public int   getDamage()   { return (int)(type.damage * (1f + (level - 1) * 0.50f)); }

    public void addKill() { killCount++; }
    public int getKills() { return killCount; }
    public int getLevel() { return level; }

    public List<Projectile> getProjectiles() { return projectiles; }
}
