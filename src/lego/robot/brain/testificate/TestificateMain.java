package lego.robot.brain.testificate;

import lego.robot.api.AbstractRobotInterface;
import lego.robot.api.RobotStrategy;

/**
 * Created by jIRKA on 3.10.2014.
 */
public class TestificateMain extends RobotStrategy {

    public TestificateMain(AbstractRobotInterface ari){
        super(ari);
    }

    @Override
    public void run() {
        robotInterface.moveForward();
        robotInterface.rotateRight();
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
