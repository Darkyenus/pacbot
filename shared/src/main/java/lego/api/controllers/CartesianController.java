package lego.api.controllers;

import lego.api.BotController;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 10:27
 */
public abstract class CartesianController extends BotController {

    public abstract boolean isInVerticalMode();

    public abstract void setInVerticalMode(boolean inVerticalMode);

    public abstract void queueAddMoveForward();

    public abstract void queueAddMoveBackward();

    public abstract void queueAddMoveLeft();

    public abstract void queueAddMoveRight();

    public abstract boolean scanLeft();

    public abstract boolean scanRight();

    public abstract boolean scanFront();

    public abstract boolean scanBack();

    public abstract boolean isQueueRunning();

    public abstract void waitUntilQueueIsEmpty();
}
