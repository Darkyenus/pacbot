package lego.nxt;

import lego.robot.api.RobotInterface;

/**
 * Private property.
 * User: jIRKA
 * Date: 9.10.2014
 * Time: 17:34
 */
public class DifferentialRobotInterface extends RobotInterface{

    public static final float BLOCK_DISTANCE = 28.5f;
    public static final int DEFAULT_SPEED = 800;

    @Override
    public void queueAddMoveForward() {
        Driver.TaskProcessor.appendHead(Driver.constructStraightDrive(BLOCK_DISTANCE, DEFAULT_SPEED));
    }

    @Override
    public void queueAddMoveBackward() {
        Driver.TaskProcessor.appendHead(Driver.constructStraightDrive(-BLOCK_DISTANCE, DEFAULT_SPEED));
    }


    @Override
    public void queueAddTurnLeft() {
        Driver.TaskProcessor.appendHead(Driver.constructTurnOnSpot(Driver.HALF_PI));
    }

    @Override
    public void queueAddTurnRight() {
        Driver.TaskProcessor.appendHead(Driver.constructTurnOnSpot(-Driver.HALF_PI));
    }


    @Override
    public boolean scanLeft() {
        //TODO need to enhance robot hardware
        return false;
    }

    @Override
    public boolean scanRight() {
        //TODO need to enhance robot hardware
        return false;
    }

    @Override
    public boolean scanFront() {
        //TODO need to enhance robot hardware
        return false;
    }

    @Override
    public boolean scanBack() {
        //TODO need to enhance robot hardware
        return false;
    }

    @Override
    public boolean isQueueRunning() {
        return !Driver.TaskProcessor.isIdle();
    }

    @Override
    public void waitUntilQueueIsEmpty() {
        Driver.TaskProcessor.waitUntilIdle();
    }

    @Override
    public void queueAddMoveLeft() {
        throw new UnsupportedOperationException("Differential robot cannot move sideways");
    }

    @Override
    public void queueAddMoveRight() {
        throw new UnsupportedOperationException("Differential robot cannot move sideways");
    }
}
