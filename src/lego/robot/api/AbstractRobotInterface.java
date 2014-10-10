package lego.robot.api;

/**
 * Private property.
 * User: jIRKA
 * Date: 30.9.2014
 * Time: 20:29
 */
public interface AbstractRobotInterface {

    public RobotEnvironment getRobotEnvironment();

    public void queueMoveForward();
    public void queueMoveLeft();
    public void queueMoveRight();
    public void queueMoveBackward();

    public void queueTurnLeft();
    public void queueTurnRight();

    public boolean scanLeft();
    public boolean scanRight();
    public boolean scanFront();
    public boolean scanBack();

    public boolean isQueueRunning();
    public void waitUntilQueueIsEmpty();

    public void debugRender(String[] lines);

}
