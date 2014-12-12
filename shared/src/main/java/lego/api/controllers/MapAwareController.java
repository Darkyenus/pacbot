package lego.api.controllers;

import lego.api.BotController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Private property.
 * User: Darkyen
 * Date: 02/12/14
 * Time: 17:33
 */
public abstract class MapAwareController extends BotController {

    private static final byte FREE_BIT = (byte) 	0x80;//10_0_00000
    private static final byte VISITED_BIT = 		0x40;//01_0_00000

    public static final byte FREE_UNVISITED = FREE_BIT;					//10
    public static final byte FREE_VISITED 	= FREE_BIT | VISITED_BIT;	//11
    public static final byte OBSTACLE 		= 0x0;						//00
    public static final byte START 			= VISITED_BIT;				//01

    private static final byte TILE_TYPE_MASK = (byte) 0xC0;//0b11_00_0000;
    private static final byte TILE_BOOL_META_BIT = 0x20;//0b00_10_0000;
    private static final byte TILE_NUM_META_MASK = 0x1F;//0b00_01_1111;

    public static final byte mazeWidth = 9;
    public static final byte mazeHeight = 6;
    public static final byte startX = 4;
    public static final byte startY = 2;

    protected byte x = startX;
    protected byte y = startY;
    protected final byte[][] maze = new byte[mazeWidth][mazeHeight];

    protected int mapIndex = -1;

    public int getMapIndex(){
        return mapIndex;
    }

    public final byte[][] getMindMaze () {
        return maze;
    }

    /** Details: Everything except specified chars are ignored. Name can be only one character. Use only ASCII chars (simple UTF8). */
    private boolean loadSavedMap (int mapName) {
        FileInputStream input = null;
        boolean readingName = false;
        boolean readingMap = false;
        byte fieldToRead = 0;

        try {
            File mapsFile = new File("maps");
            input = new FileInputStream(mapsFile);
            int i = input.read();

            final int PRENAME_CHAR = '#';
            final int OBSTACLE_CHAR = 'X';
            final int FREE_SPACE_CHAR = '_';
            final int START_CHAR = 'S';
            final byte FIELDS_TO_READ = mazeWidth * mazeHeight;
            while (i != -1) {
                if (readingMap) {
                    switch (i) {
                        case OBSTACLE_CHAR:
                            maze[fieldToRead % mazeWidth][fieldToRead / mazeWidth] = OBSTACLE;
                            fieldToRead++;
                            break;
                        case FREE_SPACE_CHAR:
                            maze[fieldToRead % mazeWidth][fieldToRead / mazeWidth] = FREE_UNVISITED;
                            fieldToRead++;
                            break;
                        case START_CHAR:
                            maze[fieldToRead % mazeWidth][fieldToRead / mazeWidth] = START;
                            fieldToRead++;
                            break;
                    }
                    if (fieldToRead == FIELDS_TO_READ) {
                        // Finalize
                        if (maze[startX][startY] != START) {
                            onError(ERROR_LOADING_INVALID_START);
                        }
                        return true;
                    }
                } else if (readingName) {
                    if (i == mapName) {
                        readingMap = true;
                    }
                } else {
                    if (i == PRENAME_CHAR) {
                        readingName = true;
                    }
                }
                i = input.read();
            }
        } catch (FileNotFoundException notFound) {
            onError(ERROR_LOADING_FILE_NOT_FOUND);
        } catch (IOException e) {
            onError(ERROR_LOADING_IOEXCEPTION);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable ignored) {
                }
            }
        }
        if (readingMap || readingName) {
            onError(ERROR_LOADING_MAP_CORRUPTED);
        } else {
            onError(ERROR_LOADING_MAP_NOT_FOUND);
        }
        return false;
    }

    private boolean loadMap () {
        FileInputStream input = null;
        File mapsFile = new File("mappointer");
        try {
            input = new FileInputStream(mapsFile);
            int mapName = input.read();
            if (mapName == -1) {
                onError(ERROR_LOADING_POINTER_FILE_CORRUPTED);
            } else {
                mapIndex = mapName;
                return loadSavedMap(mapName);
            }
        } catch (FileNotFoundException e) {
            onError(ERROR_LOADING_POINTER_FILE_MISSING);
        } catch (IOException e) {
            onError(ERROR_LOADING_MAP_CORRUPTED);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable ignored) {
                }
            }
        }
        return false;
    }

    {
        if (!loadMap()) {
            for (int x = 0; x < mazeWidth; x++) {
                for (int y = 0; y < mazeHeight; y++) {
                    maze[x][y] = FREE_UNVISITED;
                }
            }
            // Maze setup: Start and closest obstacles are given, everything else is FREE_UNVISITED.
            maze[x][y] = START;
            maze[x - 1][y] = OBSTACLE;
            maze[x + 1][y] = OBSTACLE;
        }
    }

    /** @return Coordinate at which the robot currently is. */
    public final byte getX () {
        return x;
    }

    /** @return Coordinate at which the robot currently is. */
    public final byte getY () {
        return y;
    }

    /** @return status of given field. OBSTACLE if out of bounds.
     * @deprecated Use isFree isObstacle and isStart instead */
    @Deprecated
    public final byte getField (byte x, byte y) {
        if (x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight)
            return OBSTACLE;
        else
            return maze[x][y];
    }

    /**
     * Returns char that should be drawn on screen for given coords.
     * WILL CRASH FOR INVALID COORDS!
     */
    public final String getFieldDisplay (byte x, byte y) {
        switch (maze[x][y] & TILE_TYPE_MASK){
            case FREE_UNVISITED:
                return ".";
            case FREE_VISITED:
                return " ";
            case OBSTACLE:
                return "O";
            case START:
                return "S";
            default:
                return null;//Will NEVER happen.
        }
    }

    public final boolean isFree(int x, int y) {
        return !(x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight) && (maze[x][y] & FREE_BIT) == FREE_BIT;
    }

    public final boolean isFreeUnvisited(int x, int y) {
        return !(x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight) && (maze[x][y] & TILE_TYPE_MASK) == FREE_UNVISITED;
    }

    public final boolean isFreeVisited(int x, int y) {
        return !(x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight) && (maze[x][y] & TILE_TYPE_MASK) == FREE_VISITED;
    }

    public final boolean isObstacle(int x, int y) {
        return x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight || (maze[x][y] & TILE_TYPE_MASK) == OBSTACLE;
    }

    /**
     * Check whether there is a start at given coordinate.
     * (Note: Unlike isFree and isObstacle, this is hardcoded for correct coordinate.
     */
    public final boolean isStart(int x, int y){
        return x == startX && y == startY;
    }

    /**
     * Gets state of bool meta at given coord. Returns false when invalid coords.
     */
    public final boolean getMetaBit(byte x, byte y) {
        return !(x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight) && (maze[x][y] & TILE_BOOL_META_BIT) == TILE_BOOL_META_BIT;
    }

    /** Same as getMetaBit but without bound checking */
    public final boolean getMetaBitUnsafe(int x, int y){
        return (maze[x][y] & TILE_BOOL_META_BIT) == TILE_BOOL_META_BIT;
    }

    /**
     * Sets the meta bit to TRUE on given coord. Does nothing when invalid coords.
     */
    public final void setMetaBit(byte x, byte y){
        if (x >= 0 && y >= 0 && x < mazeWidth && y < mazeHeight) {
            maze[x][y] |= TILE_BOOL_META_BIT;
        }
    }

    /**
     * Same as setMetaBit, but doesn't perform bounds checking.
     */
    public final void setMetaBitUnsafe(int x, int y){
        maze[x][y] |= TILE_BOOL_META_BIT;
    }

    /**
     * Sets the meta bit to FALSE on given coord. Does nothing when invalid coords.
     */
    public final void unsetMetaBit(byte x, byte y){
        if (x >= 0 && y >= 0 && x < mazeWidth && y < mazeHeight) {
            maze[x][y] &= ~TILE_BOOL_META_BIT;
        }
    }

    /**
     * Same as unsetMetaBit, but doesn't perform bounds checking.
     */
    public final void unsetMetaBitUnsafe(int x, int y){
        maze[x][y] &= ~TILE_BOOL_META_BIT;
    }

    public final byte getMetaNum(byte x,byte y){
        if (x >= 0 && y >= 0 && x < mazeWidth && y < mazeHeight) {
            return (byte) (maze[x][y] & TILE_NUM_META_MASK);
        }else return 0;
    }

    public final void setMetaNum(byte x,byte y,byte value){
        if (x >= 0 && y >= 0 && x < mazeWidth && y < mazeHeight) {
            byte now = maze[x][y];
            now &= ~TILE_NUM_META_MASK;
            now |= value & TILE_NUM_META_MASK;
            maze[x][y] = now;
        }
    }

    /** Safely sets field byte value, including type and all metadata. Does not produce errors.
     * You will probably not need this, unless doing magic. */
    public final void setFieldRaw (byte x, byte y, byte to) {
        if (x >= 0 && y >= 0 && x < mazeWidth && y < mazeHeight) {
            maze[x][y] = to;
        }
    }

    /** Updates information about given tile. That may be the same as already is. Updating tile out of bounds gives
     * ERROR_SET_OUT_OF_BOUNDS, then does nothing. Updating definitive tile gives ERROR_SET_DEFINITIVE,
     * then updates as if nothing happened. */
    public final void setField (byte x, byte y, byte to) {
        to &= TILE_TYPE_MASK;
        if (x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight) {
            if (to != OBSTACLE) {
                onError(ERROR_SET_OUT_OF_BOUNDS);
            }
        } else {
            byte nowTile = (byte) (maze[x][y] & TILE_TYPE_MASK);
            if (to == nowTile)
                return;
            else if (nowTile != FREE_UNVISITED) {
                if (nowTile != FREE_VISITED || to != FREE_UNVISITED) {
                    onError(ERROR_SET_DEFINITIVE);
                }
            }
            maze[x][y] &= ~TILE_TYPE_MASK;
            maze[x][y] |= to;
        }
    }

    public static final byte ERROR_SET_OUT_OF_BOUNDS = 0;
    public static final byte ERROR_SET_DEFINITIVE = 1;
    public static final byte ERROR_CAL_BLOCK_EXPECTED = 2;
    public static final byte ERROR_STUCK_IN_LOOP = 3;

    public static final byte ERROR_LOADING_INVALID_START = 4;
    public static final byte ERROR_LOADING_FILE_NOT_FOUND = 5;
    public static final byte ERROR_LOADING_MAP_NOT_FOUND = 6;
    public static final byte ERROR_LOADING_MAP_CORRUPTED = 7;
    public static final byte ERROR_LOADING_IOEXCEPTION = 8;
    public static final byte ERROR_LOADING_POINTER_FILE_MISSING = 9;
    public static final byte ERROR_LOADING_POINTER_FILE_CORRUPTED = 10;
    public static final byte ERROR_ON_LOST = 11;

    public static final byte WARNING_TOOK_TOO_LONG_TIME_TO_COMPUTE = -1;
    public static final byte WARNING_ALERT = -2;

    public static final byte SUCCESS_PATH_COMPUTED = -100;

    /** Called when controller encounters an error.
     * @param error one of this class static byte ERROR_* variable constants */
    public abstract void onError (byte error);
}
