package lego.robot.brain.testificate;

import lego.robot.api.AbstractRobotInterface;
import lego.robot.api.RobotStrategy;

/**
 * Private property.
 * User: jIRKA
 * Date: 3.10.2014
 * Time: 20:30
 */
public class TestificateMain extends RobotStrategy {

    public TestificateMain(AbstractRobotInterface ari){
        super(ari,"Hardcoded path");
    }

    @Override
    public void run() {
        robotInterface.moveForward();
        robotInterface.rotateRight();
        robotInterface.moveForward();
        robotInterface.moveForward();
        robotInterface.moveForward();
        robotInterface.moveForward();
        robotInterface.rotateRight();
        robotInterface.moveForward();
        robotInterface.rotateLeft();
        robotInterface.moveForward();
        robotInterface.moveForward();
        robotInterface.rotateLeft();
        robotInterface.moveForward();
    }

    @Override
    public void stop() {

    }
}
