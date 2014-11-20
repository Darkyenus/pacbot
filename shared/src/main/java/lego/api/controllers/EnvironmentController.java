package lego.api.controllers;

import lego.api.BotController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static lego.api.controllers.EnvironmentController.FieldStatus.*;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 10:57
 */
public abstract class EnvironmentController extends BotController {

    public static final byte mazeWidth = 9;
    public static final byte mazeHeight = 6;
    public static final byte startX = 4;
    public static final byte startY = 2;

    private static final byte OBSTACLES = 13;
    private static final byte FREE = 40;
    private byte obstaclesDiscovered = 0;
    private byte freeDiscovered = 0;

    protected byte x = startX;
    protected byte y = startY;
    protected final FieldStatus[][] maze = //new FieldStatus[mazeWidth][mazeHeight];
    /*{ //Some map (Unfinished)
     {FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED},
     {FREE_UNVISITED,OBSTACLE,OBSTACLE,OBSTACLE,OBSTACLE,FREE_UNVISITED},
     {FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED},
     {FREE_UNVISITED,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED},
     {FREE_UNVISITED,FREE_UNVISITED,START,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED},
     {FREE_UNVISITED,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED},
     {FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED},
     {FREE_UNVISITED,OBSTACLE,OBSTACLE,OBSTACLE,OBSTACLE,FREE_UNVISITED},
     {FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED}
    };*/
            {
                    {FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED},
                    {FREE_UNVISITED,OBSTACLE,FREE_UNVISITED,OBSTACLE,OBSTACLE,FREE_UNVISITED},
                    {FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED},
                    {OBSTACLE,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED},
                    {FREE_UNVISITED,FREE_UNVISITED,START,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED},
                    {OBSTACLE,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED},
                    {FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED},
                    {FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED},
                    {FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,FREE_UNVISITED,OBSTACLE,FREE_UNVISITED},
            };


    public final FieldStatus[][] getMindMaze(){
        return maze;
    }

    /**
     * Format:
     *
     * #<Name_Char>
     * _ _ _ _ _ _ _
     * O _ O _ O _ O
     * _ _ O S O _ _
     * _ _ _ _ _ _ _
     * O O O _ O O O
     * _ _ _ _ _ _ _
     *
     * Details:
     * Everything except specified chars are ignored.
     * Name can be only one character.
     * Use only ASCII chars (simple UTF8).
     *
     */
    private boolean loadSavedMap(int mapName){
        FileInputStream input = null;
        boolean readingName = false;
        boolean readingMap = false;
        byte fieldToRead = 0;

        try{
            File mapsFile = new File("maps");
            input = new FileInputStream(mapsFile);
            int i = input.read();

            final int PRENAME_CHAR = '#';
            final int OBSTACLE_CHAR = 'O';
            final int FREE_SPACE_CHAR = '_';
            final int START_CHAR = 'S';
            final byte FIELDS_TO_READ = mazeWidth * mazeHeight;
            while(i != -1){
                if(readingMap){
                    switch (i){
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
                    if(fieldToRead == FIELDS_TO_READ){
                        //Finalize
                        if(maze[startX][startY] != START){
                            onError(ERROR_LOADING_INVALID_START);
                        }
                        return true;
                    }
                }else if(readingName){
                    if(i == mapName){
                        readingMap = true;
                    }
                }else{
                    if(i == PRENAME_CHAR){
                        readingName = true;
                    }
                }
                i = input.read();
            }
        } catch (FileNotFoundException notFound){
            onError(ERROR_LOADING_FILE_NOT_FOUND);
        } catch (IOException e) {
            onError(ERROR_LOADING_IOEXCEPTION);
        } finally {
            if(input != null){
                try{
                    input.close();
                } catch (Throwable ignored){}
            }
        }
        if(readingMap || readingName){
            onError(ERROR_LOADING_MAP_CORRUPTED);
        }else {
            onError(ERROR_LOADING_MAP_NOT_FOUND);
        }
        return false;
    }

    private boolean loadMap(){
        FileInputStream input = null;
        File mapsFile = new File("maps");
        try {
            input = new FileInputStream(mapsFile);
            int mapName = input.read();
            if(mapName == -1){
                onError(ERROR_LOADING_MAP_CORRUPTED);
            }else{
                return loadSavedMap(mapName);
            }
        } catch (FileNotFoundException e) {
            onError(ERROR_LOADING_POINTER_FILE_MISSING);
        } catch (IOException e) {
            onError(ERROR_LOADING_MAP_CORRUPTED);
        } finally {
            if(input != null){
                try{
                    input.close();
                } catch (Throwable ignored){}
            }
        }
        return false;
    }

    {
        if(!loadMap()) {
            for (int x = 0; x < mazeWidth; x++) {
                for (int y = 0; y < mazeHeight; y++) {
                    maze[x][y] = FieldStatus.UNKNOWN;
                }
            }
            //Maze setup
            /*
                OSO
                 F

                 Everything else unknown
            */
            maze[x][y] = FieldStatus.START;
            maze[x - 1][y] = FieldStatus.OBSTACLE;
            maze[x + 1][y] = FieldStatus.OBSTACLE;
            maze[x][y + 1] = FieldStatus.FREE_UNVISITED;
            obstaclesDiscovered = 2;
            freeDiscovered = 1;
        }
    }

    /**
     * @return Coordinate at which the robot currently is.
     */
    public final byte getX(){
        return x;
    }

    /**
     * @return Coordinate at which the robot currently is.
     */
    public final byte getY(){
        return y;
    }

    /**
     * @return status of given field. OBSTACLE if out of bounds.
     */
    public final FieldStatus getField(byte x, byte y){
        if(x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight)return FieldStatus.OBSTACLE;
        else return maze[x][y];
    }

    /**
     * Updates information about given tile. That may be the same as already is.
     * Updating tile out of bounds gives ERROR_SET_OUT_OF_BOUNDS, then does nothing.
     * Updating definitive tile gives ERROR_SET_DEFINITIVE, then updates as if nothing happened.
     */
    public final void setField(byte x, byte y,FieldStatus to){
        if(x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight){
            if(to != FieldStatus.OBSTACLE){
                onError(ERROR_SET_OUT_OF_BOUNDS);
            }
        }else{
            FieldStatus now = maze[x][y];
            if(to == now)return;
            else if(now.definitive){
                onError(ERROR_SET_DEFINITIVE);
            }
            maze[x][y] = to;
            if(now == FieldStatus.UNKNOWN){
                if(to == FieldStatus.OBSTACLE){
                    obstaclesDiscovered++;
                    predictRestFree();
                }else if(to == FieldStatus.FREE_UNVISITED || to == FieldStatus.FREE_VISITED){
                    freeDiscovered++;
                    predictRestObstacles();
                }
            }
        }
    }

    private void predictRestFree(){
        if(obstaclesDiscovered == OBSTACLES){
            //Rest undiscovered must be free
            obstaclesDiscovered = 0;//So this doesn't get calculated again
            freeDiscovered = 0;
            for (int x = 0; x < mazeWidth; x++) {
                for (int y = 0; y < mazeHeight; y++) {
                    if(maze[x][y] == FieldStatus.UNKNOWN){
                        maze[x][y] = FieldStatus.FREE_UNVISITED;
                    }
                }
            }
        }
    }

    private void predictRestObstacles(){
        if(freeDiscovered == FREE){
            //Rest undiscovered must be obstacles
            obstaclesDiscovered = 0;//So this doesn't get calculated again
            freeDiscovered = 0;
            for (int x = 0; x < mazeWidth; x++) {
                for (int y = 0; y < mazeHeight; y++) {
                    if(maze[x][y] == FieldStatus.UNKNOWN){
                        maze[x][y] = FieldStatus.OBSTACLE;
                    }
                }
            }
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

    public static final byte WARNING_TOOK_TOO_LONG_TO_COMPUTE = -1;
    public static final byte WARNING_ALERT = -2;

    /**
     * Called when controller encounters an error.
     * @param error one of this class static byte ERROR_* variable constants
     */
    public abstract void onError(byte error);

    /**
     * Instruct robot to move by given amount of fields.
     * @param x direction and distance
     * @return amount of actually travelled fields
     */
    public byte moveByX(byte x){
        return moveByXAsync(x).moved();
    }

    /**
     * Same as moveByX but asynchronous.
     */
    public abstract MoveFieldsTask moveByXAsync(byte x);

    /**
     * Instruct robot to move by given amount of fields.
     * @param y direction and distance
     * @return amount of actually travelled fields
     */
    public byte moveByY(byte y){
        return moveByYAsync(y).moved();
    }

    /**
     * Same as moveByY but asynchronous.
     */
    public abstract MoveFieldsTask moveByYAsync(byte y);

    /**
     * Instruct robot to move in given direction until it encounters an obstacle
     * @param in direction
     * @return amount of actually travelled fields
     */
    public byte move(Direction in){
        return moveAsync(in).moved();
    }

    /**
     * Same as move but asynchronous.
     * You may want to override this, if you need better implementation.
     */
    public MoveFieldsTask moveAsync(Direction in) {
        switch (in){
            case UP:
                return moveByYAsync((byte)-100);
            case DOWN:
                return moveByYAsync((byte)100);
            case LEFT:
                return moveByXAsync((byte)-100);
            case RIGHT:
                return moveByXAsync((byte)100);
            default:
                //This will never return, above match is exhaustive. Unless in is null. Then you have bigger problems.
                throw new Error();
        }
    }

    public static enum Direction {
        UP((byte)0,(byte)-1),
        RIGHT((byte)1,(byte)0),
        DOWN((byte)0,(byte)1),
        LEFT((byte)-1,(byte)0);

        public final byte x;
        public final byte y;
        public Direction right;
        public Direction left;

        Direction(byte x, byte y) {
            this.x = x;
            this.y = y;
        }

        static { //I am not 100% sure this will work on nxt. But it should.
            Direction[] values = values();
            int length = values.length;
            for (int i = 0; i < length; i++) {
                values[i].right = values()[(i+1)%length];
                values[i].left = values()[(i+length-1)%length];
            }
        }
    }

    public enum FieldStatus {
        UNKNOWN("?", false),
        OBSTACLE("O", true),
        FREE_UNVISITED(".", false),
        FREE_VISITED(" ", true),
        START("S", true);

        /**
         * Char as which should be this block displayed in debug views without custom rendering.
         *
         * NOTE: It is actually string, because it would have to be transformed to string during rendering anyway, which is wasteful.
         */
        public final String displayChar;
        /**
         * True means, that field with this status is fully known and that status will not change unless error happens.
         * For example, OBSTACLE is definitive, because obstacles can not be removed.
         * On the other hand FREE_UNVISITED will be changed to FREE_VISITED during program run, so that will change and
         * therefore is not definitive.
         */
        public final boolean definitive;

        FieldStatus(String displayChar, boolean definitive) {
            this.displayChar = displayChar;
            this.definitive = definitive;
        }
    }

    public static interface MoveFieldsTask {

        /**
         * Returns whether or not is this task already done.
         */
        public boolean isDone();

        /**
         * Blocks until task is done, then returns by how much tiles it did move.
         */
        public byte moved();

        /**
         * Blocks until isDone returns true.
         */
        public void waitUntilDone();
    }
}
