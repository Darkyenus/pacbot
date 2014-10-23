package lego.nxt.controllers;

import lego.api.BotController;
import lego.nxt.Driver;
import lego.nxt.util.TaskProcessor;

/**
 * Private property.
 * User: Darkyen
 * Date: 21/10/14
 * Time: 20:33
 */
public final class DifferentialRobotController extends BotController {

    @Override
    protected void initialize() {
        Driver.initialize();
    }

    public static final float BLOCK_DISTANCE = 28.5f;
    public static final int DEFAULT_SPEED = 800;

    public void queueAddMoveForward() {
        TaskProcessor.appendTask(Driver.constructStraightDrive(BLOCK_DISTANCE, DEFAULT_SPEED));
    }

    public void queueAddMoveBackward() {
        TaskProcessor.appendTask(Driver.constructStraightDrive(-BLOCK_DISTANCE, DEFAULT_SPEED));
    }


    public void queueAddTurnLeft() {
        TaskProcessor.appendTask(Driver.constructTurnOnSpot(Driver.HALF_PI));
    }

    public void queueAddTurnRight() {
        TaskProcessor.appendTask(Driver.constructTurnOnSpot(-Driver.HALF_PI));
    }


    public boolean scanLeft() {
        //TODO need to enhance robot hardware
        return false;
    }

    public boolean scanRight() {
        //TODO need to enhance robot hardware
        return false;
    }

    public boolean scanFront() {
        //TODO need to enhance robot hardware
        return false;
    }

    public boolean scanBack() {
        //TODO need to enhance robot hardware
        return false;
    }

    public boolean isQueueRunning() {
        return !TaskProcessor.isIdle();
    }

    public void waitUntilQueueIsEmpty() {
        TaskProcessor.waitUntilIdle();
    }
}
