package com.towerdefense.managers;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class GameMap {

    public enum CellType { EMPTY, PATH, START, END, WATER }

    public static final int COLS       = 20;
    public static final int ROWS       = 11;
    public static final int CELL_SIZE  = 58;
    public static final int MAP_OFFSET_X = 0;
    public static final int MAP_OFFSET_Y = 0;

    private final CellType[][] grid = new CellType[COLS][ROWS];
    private final List<Vector2> pathPoints = new ArrayList<>();

    // Predefined path: list of (col,row) waypoints
    private static final int[][] PATH_DEF = {
        {0,5},{1,5},{2,5},{3,5},{4,5},{4,4},{4,3},{4,2},{5,2},{6,2},
        {7,2},{8,2},{8,3},{8,4},{8,5},{8,6},{8,7},{8,8},{9,8},{10,8},
        {11,8},{12,8},{12,7},{12,6},{12,5},{12,4},{12,3},{13,3},{14,3},
        {15,3},{15,4},{15,5},{15,6},{15,7},{15,8},{16,8},{17,8},{18,8},
        {19,8}
    };

    public GameMap() {
        init();
    }

    private void init() {
        // Fill everything as EMPTY
        for (int c = 0; c < COLS; c++)
            for (int r = 0; r < ROWS; r++)
                grid[c][r] = CellType.EMPTY;

        // Mark path
        for (int[] pt : PATH_DEF) {
            int c = pt[0], r = pt[1];
            grid[c][r] = CellType.PATH;
        }
        grid[0][5]  = CellType.START;
        grid[19][8] = CellType.END;

        // Build world-space path waypoints (center of each cell)
        pathPoints.clear();
        for (int[] pt : PATH_DEF) {
            pathPoints.add(cellCenter(pt[0], pt[1]));
        }
    }

    public CellType getCell(int col, int row) {
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return CellType.WATER;
        return grid[col][row];
    }

    public boolean canBuild(int col, int row) {
        return getCell(col, row) == CellType.EMPTY;
    }

    public void markBuilt(int col, int row) {
        grid[col][row] = CellType.WATER; // occupied
    }

    public void clearCell(int col, int row) {
        grid[col][row] = CellType.EMPTY;
    }

    /** World position of cell center */
    public Vector2 cellCenter(int col, int row) {
        return new Vector2(
            MAP_OFFSET_X + col * CELL_SIZE + CELL_SIZE / 2f,
            MAP_OFFSET_Y + row * CELL_SIZE + CELL_SIZE / 2f
        );
    }

    /** Cell coordinates from world position */
    public int worldToCol(float x) {
        return (int)((x - MAP_OFFSET_X) / CELL_SIZE);
    }
    public int worldToRow(float y) {
        return (int)((y - MAP_OFFSET_Y) / CELL_SIZE);
    }

    public List<Vector2> getPathPoints() { return pathPoints; }

    public Vector2 getStartPoint() { return pathPoints.get(0); }
    public Vector2 getEndPoint()   { return pathPoints.get(pathPoints.size() - 1); }
}
