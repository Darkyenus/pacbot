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
        super(ari,"'Testificate' - Hardcoded path");
    }

    @Override
    public void run() {
        robotInterface.debugRender(new String[]{"Some text","multi line"});
        robotInterface.queueMoveForward();
        robotInterface.queueTurnRight();
        robotInterface.debugRender(new String[]{"Some text2","multi line2"});
        robotInterface.queueMoveForward();
        robotInterface.queueMoveForward();
        robotInterface.queueMoveForward();
        robotInterface.debugRender(new String[]{"Some text","multi line","Some text","multi line","Some text","multi line","Some text","multi line","Some text","multi line","Some text","multi line","Some text","multi end"});
        robotInterface.queueMoveForward();
        robotInterface.queueTurnRight();
        robotInterface.queueMoveForward();
        robotInterface.queueTurnLeft();
        robotInterface.debugRender(new String[]{"Some text2","multi line2"});
        robotInterface.queueMoveForward();
        robotInterface.debugRender(new String[]{"Some text2","multi line2"});
        robotInterface.queueMoveForward();
        robotInterface.debugRender(new String[]{"Some text2","multi line2"});
        robotInterface.queueTurnLeft();
        robotInterface.queueMoveForward();
        robotInterface.debugRender(new String[]{"Some text2","multi line2"});
    }

    @Override
    public void stop() {

    }
}
