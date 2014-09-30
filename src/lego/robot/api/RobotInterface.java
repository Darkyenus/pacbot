package lego.robot.api;

/**
 * Created by jIRKA on 30.9.2014.
 */
public interface RobotInterface {

    public void moveForward();
    public void moveLeft();
    public void moveRight();
    public void moveBackward();

    public void rotateLeft();
    public void rotateRight();

    public boolean scanLeft();
    public boolean scanRight();
    public boolean scanFront();
    public boolean scanBack();

}
