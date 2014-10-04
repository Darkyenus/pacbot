package lego.util;

import lego.robot.api.RobotEnvironment;
import lego.robot.api.constants.AbsoluteHeading;
import lego.robot.api.constants.RelativeMovement;
import lego.training.TupleIntInt;

/**
 * Created by jIRKA on 3.10.2014.
 */
public class Util {

    public static TupleIntInt getTransformedPos(TupleIntInt actualPos, AbsoluteHeading heading, RelativeMovement movement){
        TupleIntInt result = null;
        switch (heading){
            case UP:
                switch (movement){
                    case FORWARD:
                        result = new TupleIntInt(actualPos.getX(), actualPos.getY() - 1);
                        break;
                    case BACKWARD:
                        result = new TupleIntInt(actualPos.getX(), actualPos.getY() + 1);
                        break;
                    case LEFT:
                        result = new TupleIntInt(actualPos.getX() - 1, actualPos.getY());
                        break;
                    case RIGHT:
                        result = new TupleIntInt(actualPos.getX() + 1, actualPos.getY());
                        break;
                }
                break;
            case DOWN:
                switch (movement){
                    case FORWARD:
                        result = new TupleIntInt(actualPos.getX(), actualPos.getY() + 1);
                        break;
                    case BACKWARD:
                        result = new TupleIntInt(actualPos.getX(), actualPos.getY() - 1);
                        break;
                    case LEFT:
                        result = new TupleIntInt(actualPos.getX() + 1, actualPos.getY());
                        break;
                    case RIGHT:
                        result = new TupleIntInt(actualPos.getX() - 1, actualPos.getY());
                        break;
                }
                break;
            case LEFT:
                switch (movement){
                    case FORWARD:
                        result = new TupleIntInt(actualPos.getX() - 1, actualPos.getY());
                        break;
                    case BACKWARD:
                        result = new TupleIntInt(actualPos.getX() + 1, actualPos.getY());
                        break;
                    case LEFT:
                        result = new TupleIntInt(actualPos.getX(), actualPos.getY() + 1);
                        break;
                    case RIGHT:
                        result = new TupleIntInt(actualPos.getX(), actualPos.getY() - 1);
                        break;
                }
                break;
            case RIGHT:
                switch (movement){
                    case FORWARD:
                        result = new TupleIntInt(actualPos.getX() + 1, actualPos.getY());
                        break;
                    case BACKWARD:
                        result = new TupleIntInt(actualPos.getX() - 1, actualPos.getY());
                        break;
                    case LEFT:
                        result = new TupleIntInt(actualPos.getX(), actualPos.getY() - 1);
                        break;
                    case RIGHT:
                        result = new TupleIntInt(actualPos.getX(), actualPos.getY() + 1);
                        break;
                }
                break;
        }
        return result;
    }

    public static boolean isWithinMapBounds(TupleIntInt position){
        if(position.getX() >= 0 && position.getY() >= 0){
            if(position.getX() < RobotEnvironment.mazeWidth && position.getY() < RobotEnvironment.mazeHeight){
                return true;
            }
        }
        return false;
    }

}
