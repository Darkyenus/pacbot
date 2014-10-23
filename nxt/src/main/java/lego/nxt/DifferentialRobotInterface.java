package lego.nxt;

import lego.nxt.util.TaskProcessor;
import lego.robot.api.RobotInterface;

/**
 * Private property.
 * User: jIRKA
 * Date: 9.10.2014
 * Time: 17:34
 *
 * @deprecated This uses Driver class and is old api. Will be deleted soon, probably.
 */
@Deprecated
public class DifferentialRobotInterface extends RobotInterface{

    public static final float BLOCK_DISTANCE = 28.5f;
    public static final int DEFAULT_SPEED = 800;

    @Override
    public void queueAddMoveForward() {
        TaskProcessor.appendTask(Driver.constructStraightDrive(BLOCK_DISTANCE, DEFAULT_SPEED));
    }

    @Override
    public void queueAddMoveBackward() {
        TaskProcessor.appendTask(Driver.constructStraightDrive(-BLOCK_DISTANCE, DEFAULT_SPEED));
    }


    @Override
    public void queueAddTurnLeft() {
        TaskProcessor.appendTask(Driver.constructTurnOnSpot(Driver.HALF_PI));
    }

    @Override
    public void queueAddTurnRight() {
        TaskProcessor.appendTask(Driver.constructTurnOnSpot(-Driver.HALF_PI));
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
        return !TaskProcessor.isIdle();
    }

    @Override
    public void waitUntilQueueIsEmpty() {
        TaskProcessor.waitUntilIdle();
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
