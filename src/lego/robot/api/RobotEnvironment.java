package lego.robot.api;

import lego.robot.api.constants.AbsoluteHeading;
import lego.robot.api.constants.RelativeHeading;

/**
 * Created by jIRKA on 30.9.2014.
 */
public class RobotEnvironment {

    final public int mazeWidth = 9;
    final public int mazeHeight = 6;

    private int x = 0;
    private int y = 0;

    /**
     * @return X coord relative to upper left corner (That one is x = 0)
     */
    public int getX(){
        return x;
    }

    /**
     * @return Y coord relative to upper left corner (That one is y = 0)
     */
    public int getY(){
        return y;
    }

    private AbsoluteHeading heading = AbsoluteHeading.DOWN;

    private AbsoluteHeading getHeading() {
        return heading;
    }

    public void deploy(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void moveTo(int x, int y){
        if(x < 0 || x >= mazeWidth || y < 0 || y >= mazeHeight){
            throw new IllegalArgumentException("Coordinates out of bounds. (x: "+x+", y: "+y+")");
        }
        this.x = x;
        this.y = y;
    }

    public void moveBy(int x, int y){
        if(x + this.x< 0 || x + this.x >= mazeWidth || y + this.y < 0 || y + this.y >= mazeHeight){
            throw new IllegalArgumentException("Coordinates out of bounds. (x: "+x+", y: "+y+")");
        }
        this.x += x;
        this.y += y;
    }

    public void rotateTo(AbsoluteHeading heading){
        this.heading = heading;
    }
    public void rotateBy(RelativeHeading headingDelta){
        if(headingDelta == RelativeHeading.LEFT){
            switch(heading) {
                case DOWN:
                    heading = AbsoluteHeading.RIGHT;
                    break;
                case RIGHT:
                    heading = AbsoluteHeading.UP;
                    break;
                case UP:
                    heading = AbsoluteHeading.LEFT;
                    break;
                case LEFT:
                    heading = AbsoluteHeading.DOWN;
                    break;
            }
        }else{
            switch(heading) {
                case DOWN:
                    heading = AbsoluteHeading.LEFT;
                    break;
                case LEFT:
                    heading = AbsoluteHeading.UP;
                    break;
                case UP:
                    heading = AbsoluteHeading.RIGHT;
                    break;
                case RIGHT:
                    heading = AbsoluteHeading.DOWN;
                    break;
            }
        }
    }

}
