package lego.robot.api;

import lego.robot.api.constants.RelativeHeading;

/**
 * Private property.
 * User: jIRKA
 * Date: 30.9.2014
 * Time: 20:30
 */
public abstract class RobotInterface implements AbstractRobotInterface {

    private RobotEnvironment re = null;

    public RobotInterface(){
        this.re = new RobotEnvironment();
    }

    public RobotEnvironment getRobotEnvironment(){
        return re;
    }

    public abstract void moveRobotForward();
    public abstract void moveRobotLeft();
    public abstract void moveRobotRight();
    public abstract void moveRobotBackward();

    public abstract void turnRobotLeft();
    public abstract void turnRobotRight();


    @Override
    public void moveForward() {
        switch(re.getHeading()) {
            case DOWN:
                re.moveBySuppressOutOfBounds(0, 1);
                break;
            case RIGHT:
                re.moveBySuppressOutOfBounds(1,0);
                break;
            case UP:
                re.moveBySuppressOutOfBounds(0,-1);
                break;
            case LEFT:
                re.moveBySuppressOutOfBounds(-1,0);
                break;
        }
        moveRobotForward();
    }

    @Override
    public void moveLeft() {
        switch(re.getHeading()) {
            case DOWN:
                re.moveBySuppressOutOfBounds(1,0);
                break;
            case RIGHT:
                re.moveBySuppressOutOfBounds(0,-1);
                break;
            case UP:
                re.moveBySuppressOutOfBounds(-1,0);
                break;
            case LEFT:
                re.moveBySuppressOutOfBounds(0,1);
                break;
        }
        moveRobotLeft();
    }

    @Override
    public void moveRight() {
        switch(re.getHeading()) {
            case DOWN:
                re.moveBySuppressOutOfBounds(-1,0);
                break;
            case RIGHT:
                re.moveBySuppressOutOfBounds(0,1);
                break;
            case UP:
                re.moveBySuppressOutOfBounds(1,0);
                break;
            case LEFT:
                re.moveBySuppressOutOfBounds(0,-1);
                break;
        }
        moveRobotRight();
    }

    @Override
    public void moveBackward() {
        switch(re.getHeading()) {
            case DOWN:
                re.moveBySuppressOutOfBounds(0,-1);
                break;
            case RIGHT:
                re.moveBySuppressOutOfBounds(-1,0);
                break;
            case UP:
                re.moveBySuppressOutOfBounds(0,1);
                break;
            case LEFT:
                re.moveBySuppressOutOfBounds(1,0);
                break;
        }
        moveRobotBackward();
    }

    @Override
    public void turnLeft() {
        re.rotateBy(RelativeHeading.LEFT);
        turnRobotLeft();
    }

    @Override
    public void turnRight() {
        re.rotateBy(RelativeHeading.RIGHT);
        turnRobotRight();
    }

}
