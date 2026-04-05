package com.towerdefense.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.towerdefense.TowerDefenseGame;
import com.towerdefense.entities.Enemy;
import com.towerdefense.entities.Tower;
import com.towerdefense.managers.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameScreen implements Screen {

    // ── refs ─────────────────────────────────────────────────────────────
    private final TowerDefenseGame game;

    // ── rendering ────────────────────────────────────────────────────────
    private final OrthographicCamera camera;
    private final FitViewport        viewport;
    private final ShapeRenderer      shapes = new ShapeRenderer();

    // ── game objects ──────────────────────────────────────────────────────
    private final GameMap        map     = new GameMap();
    private final GameState      state   = new GameState();
    private final List<Tower>    towers  = new ArrayList<>();
    private final List<Enemy>    enemies = new ArrayList<>();
    private final WaveManager    waveMgr;
    private final ParticleManager particles = new ParticleManager();

    // ── UI state ──────────────────────────────────────────────────────────
    private Tower.Type   selectedTowerType = Tower.Type.ARROW;
    private Tower        selectedTower     = null;   // for upgrade/sell panel
    private int          hoveredCol        = -1;
    private int          hoveredRow        = -1;

    // UI panel (right side)
    private static final float PANEL_X = 1080f;
    private static final float PANEL_W = 200f;

    // Tower selector buttons (right panel)
    private final List<TowerBtn> towerBtns = new ArrayList<>();

    // Wave button
    private final Rectangle btnNextWave = new Rectangle(PANEL_X + 10, 20, PANEL_W - 20, 45);
    private final Rectangle btnMenu     = new Rectangle(PANEL_X + 10, 70, PANEL_W - 20, 30);

    // Upgrade/Sell buttons (shown when tower selected)
    private final Rectangle btnUpgrade  = new Rectangle(PANEL_X + 10, 270, PANEL_W - 20, 40);
    private final Rectangle btnSell     = new Rectangle(PANEL_X + 10, 220, PANEL_W - 20, 40);

    // Speed toggle
    private boolean fastMode = false;
    private final Rectangle btnSpeed = new Rectangle(PANEL_X + 10, 110, PANEL_W - 20, 30);

    // Game-over / victory overlay time
    private float overlayTimer = 0f;

    // ── inner helper ──────────────────────────────────────────────────────
    private static class TowerBtn {
        Rectangle   rect;
        Tower.Type  type;
        TowerBtn(float x, float y, Tower.Type t) {
            rect = new Rectangle(x, y, 85, 100);
            type = t;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    public GameScreen(TowerDefenseGame game) {
        this.game = game;
        camera    = new OrthographicCamera();
        viewport  = new FitViewport(TowerDefenseGame.V_WIDTH, TowerDefenseGame.V_HEIGHT, camera);
        camera.setToOrtho(false, TowerDefenseGame.V_WIDTH, TowerDefenseGame.V_HEIGHT);

        waveMgr   = new WaveManager(enemies, map.getPathPoints());

        buildTowerButtons();
        setupInput();
    }

    private void buildTowerButtons() {
        Tower.Type[] types = Tower.Type.values();
        // Two columns
        for (int i = 0; i < types.length; i++) {
            float col = (i % 2 == 0) ? PANEL_X + 8 : PANEL_X + 103;
            float row = 580 - (i / 2) * 112;
            towerBtns.add(new TowerBtn(col, row, types[i]));
        }
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int sx, int sy, int ptr, int btn) {
                Vector3 world = camera.unproject(new Vector3(sx, sy, 0));
                float   wx    = world.x;
                float   wy    = world.y;

                // UI panel click
                if (wx >= PANEL_X) {
                    handlePanelClick(wx, wy);
                    return true;
                }

                // Map click
                int col = map.worldToCol(wx);
                int row = map.worldToRow(wy);

                // Check if clicking on existing tower
                Tower hit = towerAt(col, row);
                if (hit != null) {
                    selectedTower = (selectedTower == hit) ? null : hit;
                    return true;
                }

                // Try to place tower
                if (map.canBuild(col, row)) {
                    int cost = selectedTowerType.cost;
                    if (state.spendGold(cost)) {
                        Vector2 pos = map.cellCenter(col, row);
                        Tower t = new Tower(selectedTowerType, col, row, pos);
                        towers.add(t);
                        map.markBuilt(col, row);
                    }
                }
                selectedTower = null;
                return true;
            }

            @Override
            public boolean mouseMoved(int sx, int sy) {
                Vector3 world = camera.unproject(new Vector3(sx, sy, 0));
                hoveredCol = map.worldToCol(world.x);
                hoveredRow = map.worldToRow(world.y);
                return false;
            }

            @Override
            public boolean keyDown(int key) {
                if (key == Input.Keys.SPACE) waveMgr.startNextWave();
                if (key == Input.Keys.ESCAPE) { selectedTower = null; }
                if (key == Input.Keys.F)      fastMode = !fastMode;
                if (key == Input.Keys.NUM_1) selectedTowerType = Tower.Type.ARROW;
                if (key == Input.Keys.NUM_2) selectedTowerType = Tower.Type.CANNON;
                if (key == Input.Keys.NUM_3) selectedTowerType = Tower.Type.ICE;
                if (key == Input.Keys.NUM_4) selectedTowerType = Tower.Type.LASER;
                if (key == Input.Keys.NUM_5) selectedTowerType = Tower.Type.BOMB;
                return false;
            }
        });
    }

    private void handlePanelClick(float wx, float wy) {
        // Tower buttons
        for (TowerBtn tb : towerBtns) {
            if (tb.rect.contains(wx, wy)) {
                selectedTowerType = tb.type;
                selectedTower     = null;
                return;
            }
        }
        // Next wave
        if (btnNextWave.contains(wx, wy) && !waveMgr.isWaveActive())
            waveMgr.startNextWave();
        // Menu
        if (btnMenu.contains(wx, wy))
            game.setScreen(new MenuScreen(game));
        // Speed
        if (btnSpeed.contains(wx, wy))
            fastMode = !fastMode;
        // Upgrade / sell
        if (selectedTower != null) {
            if (btnUpgrade.contains(wx, wy) && selectedTower.canUpgrade()) {
                int cost = selectedTower.getUpgradeCost();
                if (state.spendGold(cost)) selectedTower.upgrade();
            }
            if (btnSell.contains(wx, wy)) {
                state.addGold(selectedTower.getSellValue());
                map.clearCell(selectedTower.col, selectedTower.row);
                towers.remove(selectedTower);
                selectedTower = null;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void render(float dt) {
        float step = fastMode ? dt * 2f : dt;

        if (!state.paused && !state.gameOver && !state.victory)
            update(step);

        draw();
    }

    private void update(float delta) {
        waveMgr.update(delta);

        // Update enemies
        Iterator<Enemy> eit = enemies.iterator();
        while (eit.hasNext()) {
            Enemy e = eit.next();
            e.update(delta);
            if (!e.isAlive()) {
                state.addGold(e.getReward());
                state.addScore(e.getReward() * 10);
                // tower kill credit
                towers.forEach(t -> { /* already tracked in projectile */ });
                particles.spawnDeath(e.position.x, e.position.y, new Color(0.9f,0.3f,0.1f,1f));
                particles.spawnGold(e.position.x, e.position.y);
                eit.remove();
            } else if (e.hasReachedEnd()) {
                state.loseLife(e.getDamage());
                particles.spawnDeath(e.position.x, e.position.y, new Color(1f,0f,0f,1f));
                eit.remove();
            }
        }

        // Update towers
        for (Tower t : towers) t.update(delta, enemies);

        particles.update(delta);

        // Victory check
        if (waveMgr.isLastWave() && !waveMgr.isWaveActive() && enemies.isEmpty())
            state.victory = true;

        if (state.gameOver || state.victory) overlayTimer += delta;
    }

    // ─────────────────────────────────────────────────────────────────────
    private void draw() {
        Gdx.gl.glClearColor(0.06f, 0.06f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        // ── Map ──────────────────────────────────────────────────────────
        drawMap();

        // ── Range overlay for selected tower ────────────────────────────
        game.batch.begin();
        if (selectedTower != null) selectedTower.drawRange(game.batch, game.assets);
        game.batch.end();

        // ── Towers ───────────────────────────────────────────────────────
        game.batch.begin();
        for (Tower t : towers) t.draw(game.batch, game.assets);
        game.batch.end();

        // ── Enemies ──────────────────────────────────────────────────────
        game.batch.begin();
        for (Enemy e : enemies) e.draw(game.batch, game.assets);
        game.batch.end();

        // ── Particles ────────────────────────────────────────────────────
        game.batch.begin();
        particles.draw(game.batch, game.assets);
        game.batch.end();

        // ── UI ───────────────────────────────────────────────────────────
        drawUI();

        // ── Overlays ─────────────────────────────────────────────────────
        if (state.gameOver) drawGameOver();
        else if (state.victory) drawVictory();
    }

    private void drawMap() {
        game.batch.begin();
        for (int c = 0; c < GameMap.COLS; c++) {
            for (int r = 0; r < GameMap.ROWS; r++) {
                float px = c * GameMap.CELL_SIZE;
                float py = r * GameMap.CELL_SIZE;
                float sz = GameMap.CELL_SIZE;

                GameMap.CellType cell = map.getCell(c, r);
                String texKey;
                switch (cell) {
                    case PATH:  texKey = "tile_path";  break;
                    case START: texKey = "tile_path";  break;
                    case END:   texKey = "tile_path";  break;
                    case WATER: texKey = "tile_water"; break;
                    default:    texKey = "tile_grass";
                }
                game.batch.draw(game.assets.get(texKey), px, py, sz, sz);

                // Hover highlight on buildable cell
                if (c == hoveredCol && r == hoveredRow
                        && cell == GameMap.CellType.EMPTY) {
                    game.batch.setColor(1f, 1f, 1f, 0.25f);
                    game.batch.draw(game.assets.get("cell_hover"), px, py, sz, sz);
                    game.batch.setColor(Color.WHITE);
                }
            }
        }

        // START / END markers
        drawMarker("START", 0, 5, new Color(0f,1f,0f,1f));
        drawMarker("END",  19, 8, new Color(1f,0.2f,0.2f,1f));

        game.batch.end();

        // Grid lines (shapes)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0f, 0f, 0f, 0.18f);
        for (int c = 0; c <= GameMap.COLS; c++)
            shapes.line(c * GameMap.CELL_SIZE, 0,
                        c * GameMap.CELL_SIZE, GameMap.ROWS * GameMap.CELL_SIZE);
        for (int r = 0; r <= GameMap.ROWS; r++)
            shapes.line(0, r * GameMap.CELL_SIZE,
                        GameMap.COLS * GameMap.CELL_SIZE, r * GameMap.CELL_SIZE);
        shapes.end();
    }

    private void drawMarker(String label, int col, int row, Color c) {
        float px = col * GameMap.CELL_SIZE + GameMap.CELL_SIZE * 0.5f;
        float py = row * GameMap.CELL_SIZE + GameMap.CELL_SIZE * 0.5f;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(c.r, c.g, c.b, 0.5f);
        shapes.circle(px, py, 14f);
        shapes.end();
        game.batch.begin();
        game.font.setColor(c);
        game.font.draw(game.batch, label, px - 18, py + 7);
        game.batch.end();
    }

    // ─── Right-side UI panel ──────────────────────────────────────────────
    private void drawUI() {
        // Panel BG
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.08f, 0.08f, 0.18f, 0.95f);
        shapes.rect(PANEL_X, 0, PANEL_W, TowerDefenseGame.V_HEIGHT);
        shapes.end();

        game.batch.begin();

        // Stats header
        game.font.setColor(Color.GOLD);
        game.font.draw(game.batch, "💰 " + state.gold,   PANEL_X + 10, 700);
        game.font.setColor(Color.RED);
        game.font.draw(game.batch, "❤  " + state.lives,  PANEL_X + 10, 675);
        game.font.setColor(new Color(0.5f,0.9f,1f,1f));
        game.font.draw(game.batch, "⭐ " + state.score,  PANEL_X + 10, 650);
        game.font.setColor(new Color(0.8f,0.8f,0.5f,1f));
        game.font.draw(game.batch, "Wave " + waveMgr.getCurrentWave()
            + "/" + WaveManager.TOTAL_WAVES, PANEL_X + 10, 625);

        // Enemy count
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Enemies: " + waveMgr.getAliveCount(),
            PANEL_X + 10, 605);

        game.batch.end();

        // Separator line
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.3f, 0.3f, 0.5f, 1f);
        shapes.rect(PANEL_X, 595, PANEL_W, 1);
        shapes.end();

        // Tower buttons
        for (TowerBtn tb : towerBtns) {
            boolean sel = (tb.type == selectedTowerType);
            drawTowerBtn(tb, sel);
        }

        // Wave / menu buttons
        drawRectButton(btnNextWave,
            waveMgr.isWaveActive() ? "⏳ Wave in progress" : "▶ Next Wave [SPACE]",
            waveMgr.isWaveActive()
                ? new Color(0.4f,0.4f,0.4f,1f)
                : new Color(0.1f,0.6f,0.1f,1f));
        drawRectButton(btnMenu, "↩ Menu",
            new Color(0.4f, 0.2f, 0.2f, 1f));
        drawRectButton(btnSpeed, fastMode ? "⏩ 2× Speed" : "▶  1× Speed",
            fastMode ? new Color(0.6f,0.4f,0f,1f)
                     : new Color(0.2f,0.2f,0.5f,1f));

        // Selected tower info panel
        if (selectedTower != null) {
            drawSelectedTowerPanel();
        } else {
            // Show hovered build preview
            if (hoveredCol >= 0 && map.canBuild(hoveredCol, hoveredRow)) {
                game.batch.begin();
                game.font.setColor(Color.LIGHT_GRAY);
                game.font.draw(game.batch, "Click to build:", PANEL_X+10, 210);
                game.font.setColor(Color.YELLOW);
                game.font.draw(game.batch, selectedTowerType.displayName, PANEL_X+10, 190);
                game.font.setColor(Color.GOLD);
                game.font.draw(game.batch, "Cost: " + selectedTowerType.cost + "g", PANEL_X+10, 170);
                game.font.setColor(Color.WHITE);
                game.font.draw(game.batch, selectedTowerType.description, PANEL_X+10, 150);
                game.batch.end();
            }
        }

        // Keyboard hints
        game.batch.begin();
        game.font.setColor(new Color(0.5f,0.5f,0.7f,1f));
        game.font.draw(game.batch, "[1-5] Select tower", PANEL_X+5, 160);
        game.font.draw(game.batch, "[F]   Toggle speed",  PANEL_X+5, 142);
        game.font.draw(game.batch, "[ESC] Deselect",      PANEL_X+5, 124);
        game.batch.end();
    }

    private void drawTowerBtn(TowerBtn tb, boolean selected) {
        Tower.Type t = tb.type;
        boolean canAfford = state.gold >= t.cost;

        // BG
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (selected) shapes.setColor(0.2f, 0.5f, 0.8f, 1f);
        else if (!canAfford) shapes.setColor(0.15f, 0.12f, 0.12f, 1f);
        else shapes.setColor(0.15f, 0.18f, 0.28f, 1f);
        shapes.rect(tb.rect.x, tb.rect.y, tb.rect.width, tb.rect.height, 6, 6, 6, 6);
        shapes.end();

        // Border
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(selected ? Color.CYAN : (canAfford ? Color.GRAY : Color.DARK_GRAY));
        shapes.rect(tb.rect.x, tb.rect.y, tb.rect.width, tb.rect.height);
        shapes.end();

        // Tower icon
        game.batch.begin();
        float iconSize = 36f;
        game.batch.setColor(canAfford ? Color.WHITE : new Color(0.5f,0.5f,0.5f,1f));
        game.batch.draw(game.assets.get(t.texKey),
            tb.rect.x + (tb.rect.width - iconSize)/2,
            tb.rect.y + tb.rect.height - iconSize - 4,
            iconSize, iconSize);
        game.batch.setColor(Color.WHITE);

        // Name
        game.font.setColor(canAfford ? Color.WHITE : Color.DARK_GRAY);
        float nameW = t.displayName.length() * 6.5f;
        game.font.draw(game.batch, t.displayName,
            tb.rect.x + (tb.rect.width - nameW)/2,
            tb.rect.y + tb.rect.height - iconSize - 8);

        // Cost
        game.font.setColor(canAfford ? Color.GOLD : Color.RED);
        String cost = t.cost + "g";
        float costW = cost.length() * 7f;
        game.font.draw(game.batch, cost,
            tb.rect.x + (tb.rect.width - costW)/2,
            tb.rect.y + 22);

        // Range stat
        game.font.setColor(new Color(0.6f,0.8f,1f,1f));
        game.font.draw(game.batch, "R:" + (int)t.range,
            tb.rect.x + 5, tb.rect.y + 36);

        // DMG stat
        game.font.setColor(new Color(1f,0.6f,0.6f,1f));
        game.font.draw(game.batch, "D:" + t.damage,
            tb.rect.x + 48, tb.rect.y + 36);
        game.batch.end();
    }

    private void drawSelectedTowerPanel() {
        Tower t = selectedTower;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.1f, 0.15f, 0.25f, 1f);
        shapes.rect(PANEL_X + 5, 195, PANEL_W - 10, 200);
        shapes.end();

        game.batch.begin();
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, t.type.displayName + " Lv." + t.getLevel(),
            PANEL_X + 10, 388);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Dmg:   " + t.getDamage(),  PANEL_X + 10, 368);
        game.font.draw(game.batch, "Range: " + (int)t.getRange(), PANEL_X + 10, 350);
        game.font.draw(game.batch, "Speed: " + String.format("%.1f", t.getFireRate()),
            PANEL_X + 10, 332);
        game.font.draw(game.batch, "Kills: " + t.getKills(),   PANEL_X + 10, 314);
        game.batch.end();

        // Upgrade button
        if (t.canUpgrade()) {
            drawRectButton(btnUpgrade,
                "⬆ Upgrade (" + t.getUpgradeCost() + "g)",
                state.gold >= t.getUpgradeCost()
                    ? new Color(0.1f, 0.5f, 0.8f, 1f)
                    : new Color(0.3f, 0.3f, 0.3f, 1f));
        } else {
            game.batch.begin();
            game.font.setColor(Color.GOLD);
            game.font.draw(game.batch, "✓ MAX LEVEL", PANEL_X + 35, 295);
            game.batch.end();
        }
        drawRectButton(btnSell,
            "💰 Sell (" + t.getSellValue() + "g)",
            new Color(0.6f, 0.3f, 0.1f, 1f));
    }

    private void drawRectButton(Rectangle r, String label, Color c) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(c);
        shapes.rect(r.x, r.y, r.width, r.height, 6, 6, 6, 6);
        shapes.end();
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        var layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font, label);
        game.font.draw(game.batch, label,
            r.x + (r.width  - layout.width)  / 2,
            r.y + (r.height + layout.height) / 2);
        game.batch.end();
    }

    // ─── Overlays ─────────────────────────────────────────────────────────
    private void drawGameOver() {
        drawDimOverlay(new Color(0.6f, 0.05f, 0.05f, 0.75f));
        game.batch.begin();
        game.bigFont.setColor(Color.RED);
        game.bigFont.draw(game.batch, "GAME OVER", 470, 420);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Score: " + state.score, 580, 370);
        game.font.draw(game.batch, "Wave reached: " + waveMgr.getCurrentWave(), 545, 345);
        if (overlayTimer > 1.5f) {
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "Tap anywhere to return to menu", 460, 300);
        }
        game.batch.end();
        if (overlayTimer > 1.5f && Gdx.input.justTouched())
            game.setScreen(new MenuScreen(game));
    }

    private void drawVictory() {
        drawDimOverlay(new Color(0.05f, 0.4f, 0.05f, 0.75f));
        game.batch.begin();
        game.bigFont.setColor(Color.YELLOW);
        game.bigFont.draw(game.batch, "VICTORY!", 490, 430);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "All 25 waves defeated!", 500, 380);
        game.font.draw(game.batch, "Final Score: " + state.score, 520, 355);
        if (overlayTimer > 1.5f) {
            game.font.setColor(Color.CYAN);
            game.font.draw(game.batch, "Tap anywhere to return to menu", 460, 310);
        }
        game.batch.end();
        if (overlayTimer > 1.5f && Gdx.input.justTouched())
            game.setScreen(new MenuScreen(game));
    }

    private void drawDimOverlay(Color c) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(c);
        shapes.rect(0, 0, TowerDefenseGame.V_WIDTH, TowerDefenseGame.V_HEIGHT);
        shapes.end();
    }

    // ─── helpers ──────────────────────────────────────────────────────────
    private Tower towerAt(int col, int row) {
        for (Tower t : towers)
            if (t.col == col && t.row == row) return t;
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────
    @Override public void show() {}
    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause()  { state.paused = true;  }
    @Override public void resume() { state.paused = false; }
    @Override public void hide()   {}
    @Override public void dispose() { shapes.dispose(); }
}
