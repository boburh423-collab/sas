package com.towerdefense.managers;

import com.towerdefense.entities.Enemy;
import java.util.ArrayList;
import java.util.List;

public class WaveManager {

    public static final int TOTAL_WAVES = 25;

    private int     currentWave   = 0;
    private boolean waveActive    = false;
    private float   spawnTimer    = 0f;
    private int     spawnIndex    = 0;

    private final List<Enemy.Type> spawnQueue = new ArrayList<>();
    private final List<Enemy>      enemies;
    private final List<com.badlogic.gdx.math.Vector2> path;

    public WaveManager(List<Enemy> enemies,
                       List<com.badlogic.gdx.math.Vector2> path) {
        this.enemies = enemies;
        this.path    = path;
    }

    public void startNextWave() {
        if (waveActive || currentWave >= TOTAL_WAVES) return;
        currentWave++;
        buildQueue(currentWave);
        spawnIndex = 0;
        spawnTimer = 0f;
        waveActive = true;
    }

    private void buildQueue(int wave) {
        spawnQueue.clear();
        int count = 8 + wave * 2;
        for (int i = 0; i < count; i++) {
            spawnQueue.add(pickType(wave, i));
        }
    }

    private Enemy.Type pickType(int wave, int idx) {
        // Boss every 5 waves at end
        if (wave % 5 == 0 && idx == spawnQueue.size() - 1 && wave > 0)
            return Enemy.Type.BOSS;

        float rand = (float)Math.random();
        if (wave >= 15) {
            if (rand < 0.15f) return Enemy.Type.BOSS;
            if (rand < 0.35f) return Enemy.Type.FLYING;
            if (rand < 0.55f) return Enemy.Type.TANK;
            if (rand < 0.75f) return Enemy.Type.FAST;
            return Enemy.Type.BASIC;
        } else if (wave >= 8) {
            if (rand < 0.15f) return Enemy.Type.FLYING;
            if (rand < 0.35f) return Enemy.Type.TANK;
            if (rand < 0.60f) return Enemy.Type.FAST;
            return Enemy.Type.BASIC;
        } else if (wave >= 4) {
            if (rand < 0.20f) return Enemy.Type.TANK;
            if (rand < 0.45f) return Enemy.Type.FAST;
            return Enemy.Type.BASIC;
        } else {
            if (rand < 0.25f) return Enemy.Type.FAST;
            return Enemy.Type.BASIC;
        }
    }

    public void update(float dt) {
        if (!waveActive) return;
        spawnTimer -= dt;

        if (spawnTimer <= 0 && spawnIndex < spawnQueue.size()) {
            spawnTimer = 0.6f;
            Enemy e = new Enemy(spawnQueue.get(spawnIndex), path, currentWave);
            enemies.add(e);
            spawnIndex++;
        }

        if (spawnIndex >= spawnQueue.size()) {
            boolean allDead = enemies.stream().noneMatch(Enemy::isAlive);
            if (allDead) waveActive = false;
        }
    }

    public boolean isWaveActive()    { return waveActive; }
    public int     getCurrentWave()  { return currentWave; }
    public boolean isLastWave()      { return currentWave >= TOTAL_WAVES; }

    public int getRemainingInQueue() {
        return Math.max(0, spawnQueue.size() - spawnIndex);
    }
    public int getAliveCount() {
        return (int)enemies.stream().filter(Enemy::isAlive).count();
    }
}
