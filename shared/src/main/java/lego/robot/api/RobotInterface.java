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

    public abstract void queueAddMoveForward();
    public abstract void queueAddMoveLeft();
    public abstract void queueAddMoveRight();
    public abstract void queueAddMoveBackward();

    public abstract void queueAddTurnLeft();
    public abstract void queueAddTurnRight();


    @Override
    public void queueMoveForward() {
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
        queueAddMoveForward();
    }

    @Override
    public void queueMoveLeft() {
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
        queueAddMoveLeft();
    }

    @Override
    public void queueMoveRight() {
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
        queueAddMoveRight();
    }

    @Override
    public void queueMoveBackward() {
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
        queueAddMoveBackward();
    }

    @Override
    public void queueTurnLeft() {
        re.rotateBy(RelativeHeading.LEFT);
        queueAddTurnLeft();
    }

    @Override
    public void queueTurnRight() {
        re.rotateBy(RelativeHeading.RIGHT);
        queueAddTurnRight();
    }

    @Override
    public void doNothing(){

    }


    //Makes it not mandatory to override
    @Override
    public void debugRender(String[] lines) {}
}
