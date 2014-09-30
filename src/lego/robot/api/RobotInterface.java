package lego.robot.api;

import lego.robot.api.constants.RelativeHeading;

/**
 * Created by jIRKA on 30.9.2014.
 */
public abstract class RobotInterface implements AbstractRobotInterface {

    private RobotStrategy rs = null;

    public RobotInterface(RobotStrategy rs){
        this.rs = rs;
    }

    public abstract void moveRobotForward();
    public abstract void moveRobotLeft();
    public abstract void moveRobotRight();
    public abstract void moveRobotBackward();

    public abstract void rotateRobotLeft();
    public abstract void rotateRobotRight();


    @Override
    public void moveForward() {
        switch(rs.getRobotEnvironment().getHeading()) {
            case DOWN:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(0, 1);
                break;
            case RIGHT:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(1,0);
                break;
            case UP:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(0,-1);
                break;
            case LEFT:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(-1,0);
                break;
        }
        moveRobotForward();
    }

    @Override
    public void moveLeft() {
        switch(rs.getRobotEnvironment().getHeading()) {
            case DOWN:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(1,0);
                break;
            case RIGHT:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(0,-1);
                break;
            case UP:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(-1,0);
                break;
            case LEFT:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(0,1);
                break;
        }
        moveRobotLeft();
    }

    @Override
    public void moveRight() {
        switch(rs.getRobotEnvironment().getHeading()) {
            case DOWN:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(-1,0);
                break;
            case RIGHT:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(0,1);
                break;
            case UP:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(1,0);
                break;
            case LEFT:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(0,-1);
                break;
        }
        moveRobotRight();
    }

    @Override
    public void moveBackward() {
        switch(rs.getRobotEnvironment().getHeading()) {
            case DOWN:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(0,-1);
                break;
            case RIGHT:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(-1,0);
                break;
            case UP:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(0,1);
                break;
            case LEFT:
                rs.getRobotEnvironment().moveBySuppressOutOfBounds(1,0);
                break;
        }
        moveRobotBackward();
    }

    @Override
    public void rotateLeft() {
        rs.getRobotEnvironment().rotateBy(RelativeHeading.LEFT);
        rotateRobotLeft();
    }

    @Override
    public void rotateRight() {
        rs.getRobotEnvironment().rotateBy(RelativeHeading.RIGHT);
        rotateRobotRight();
    }

}
