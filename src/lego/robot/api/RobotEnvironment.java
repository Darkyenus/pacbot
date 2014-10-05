package lego.robot.api;

import lego.util.Util;
import lego.robot.api.constants.AbsoluteHeading;
import lego.robot.api.constants.RelativeHeading;
import lego.util.TupleIntInt;

/**
 * Created by jIRKA on 30.9.2014.
 */
public class RobotEnvironment {

    final public static int mazeWidth = 9;
    final public static int mazeHeight = 6;

    private TupleIntInt actualPos = null;

    /**
     * @return Robot coord relative to upper left corner (That one is x = 0, y = 0)
     */
    public TupleIntInt getPos(){
        return actualPos;
    }

    private AbsoluteHeading heading = AbsoluteHeading.DOWN;

    public AbsoluteHeading getHeading() {
        return heading;
    }

    public void deploy(TupleIntInt where){
        actualPos = where;
    }

    public void moveTo(TupleIntInt newPos){
        if(!Util.isWithinMapBounds(newPos)){
            throw new IllegalArgumentException("Coordinates out of bounds. (x: "+newPos.getX()+", y: "+newPos.getY()+")");
        }
        actualPos = newPos;
    }

    public void moveBy(int x, int y){
        if(x + actualPos.getX()< 0 || x + actualPos.getX() >= mazeWidth || y + actualPos.getY() < 0 || y + actualPos.getY() >= mazeHeight){
            throw new IllegalArgumentException("Coordinates out of bounds. (x: "+x+", y: "+y+")");
        }
        actualPos = new TupleIntInt(actualPos.getX() + x, actualPos.getY() + y);
    }

    public void moveBySuppressOutOfBounds(int x, int y){
        actualPos = new TupleIntInt(
                Math.max(0, Math.min(mazeWidth-1, actualPos.getX() + x)),
                Math.max(0, Math.min(mazeWidth-1, actualPos.getY() + y))
        );
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
