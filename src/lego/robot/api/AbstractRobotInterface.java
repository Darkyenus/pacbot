package lego.robot.api;

/**
 * Private property.
 * User: jIRKA
 * Date: 30.9.2014
 * Time: 20:29
 */
public interface AbstractRobotInterface {

    public RobotEnvironment getRobotEnvironment();

    public void moveForward();
    public void moveLeft();
    public void moveRight();
    public void moveBackward();

    public void turnLeft();
    public void turnRight();

    public boolean scanLeft();
    public boolean scanRight();
    public boolean scanFront();
    public boolean scanBack();

}
