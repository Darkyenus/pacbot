package lego.api.controllers;

import lego.api.BotController;
import lejos.nxt.Sound;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 10:57
 */
public abstract class EnvironmentController extends BotController {

    final public static byte mazeWidth = 9;
    final public static byte mazeHeight = 6;

    protected byte x = 4;
    protected byte y = 2;
    protected FieldStatus[][] maze = new FieldStatus[mazeWidth][mazeHeight];

    {
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
        maze[x-1][y] = FieldStatus.OBSTACLE;
        maze[x+1][y] = FieldStatus.OBSTACLE;
        maze[x][y+1] = FieldStatus.FREE_UNVISITED;
    }

    /**
     * @return Coordinate at which the robot currently is.
     */
    public byte getX(){
        return x;
    }

    /**
     * @return Coordinate at which the robot currently is.
     */
    public byte getY(){
        return y;
    }

    /**
     * @return status of given field. OBSTACLE if out of bounds.
     */
    public FieldStatus getField(byte x, byte y){
        if(x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight)return FieldStatus.OBSTACLE;
        else return maze[x][y];
    }

    /**
     * Updates information about given tile. That may be the same as already is.
     * Updating tile out of bounds gives beep sequence down and buzz, then does nothing.
     * Updating definitive tile gives beep sequence up and buzz, then updates as if nothing happened.
     */
    public void setField(byte x, byte y,FieldStatus to){
        if(x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight){
            Sound.beepSequence();
            Sound.buzz();
        }else{
            FieldStatus now = maze[x][y];
            if(to == now)return;
            else if(to.definitive){
                Sound.beepSequenceUp();
                Sound.buzz();
            }
            maze[x][y] = to;
        }
    }

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
     */
    public abstract MoveFieldsTask moveAsync(Direction in);

    public enum Direction {
        UP((byte)0,(byte)-1),
        DOWN((byte)0,(byte)1),
        LEFT((byte)-1,(byte)0),
        RIGHT((byte)1,(byte)0);

        public final byte x;
        public final byte y;

        Direction(byte x, byte y) {
            this.x = x;
            this.y = y;
        }
    }

    public enum FieldStatus {
        UNKNOWN('?', false),
        OBSTACLE('O', true),
        FREE_UNVISITED('.', false),
        FREE_VISITED(' ', true),
        START('S', true);

        /**
         * Char as which should be this block displayed in debug views without custom rendering.
         */
        public final char displayChar;
        /**
         * True means, that field with this status is fully known and that status will not change unless error happens.
         * For example, OBSTACLE is definitive, because obstacles can not be removed.
         * On the other hand FREE_UNVISITED will be changed to FREE_VISITED during program run, so that will change and
         * therefore is not definitive.
         */
        public final boolean definitive;

        FieldStatus(char displayChar, boolean definitive) {
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
