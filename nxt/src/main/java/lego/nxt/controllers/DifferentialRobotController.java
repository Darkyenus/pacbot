package lego.nxt.controllers;

import lego.api.BotController;
import lego.nxt.Driver;

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
        Driver.TaskProcessor.appendTask(Driver.constructStraightDrive(BLOCK_DISTANCE, DEFAULT_SPEED));
    }

    public void queueAddMoveBackward() {
        Driver.TaskProcessor.appendTask(Driver.constructStraightDrive(-BLOCK_DISTANCE, DEFAULT_SPEED));
    }


    public void queueAddTurnLeft() {
        Driver.TaskProcessor.appendTask(Driver.constructTurnOnSpot(Driver.HALF_PI));
    }

    public void queueAddTurnRight() {
        Driver.TaskProcessor.appendTask(Driver.constructTurnOnSpot(-Driver.HALF_PI));
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
        return !Driver.TaskProcessor.isIdle();
    }

    public void waitUntilQueueIsEmpty() {
        Driver.TaskProcessor.waitUntilIdle();
    }
}
