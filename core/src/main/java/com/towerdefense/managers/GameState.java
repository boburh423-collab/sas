package com.towerdefense.managers;

public class GameState {
    public int  gold  = 200;
    public int  lives = 20;
    public int  score = 0;
    public boolean paused = false;
    public boolean gameOver  = false;
    public boolean victory   = false;

    public void addGold(int amount) { gold  += amount; }
    public void addScore(int amount){ score += amount; }
    public boolean spendGold(int amount) {
        if (gold >= amount) { gold -= amount; return true; }
        return false;
    }
    public void loseLife(int amount) {
        lives -= amount;
        if (lives <= 0) { lives = 0; gameOver = true; }
    }
}
