package lego.robot.brain.darkyensbrain;

import lego.robot.api.AbstractRobotInterface;
import lego.robot.api.RobotStrategy;

/**
 * Private property.
 * User: jIRKA
 * Date: 16.10.2014
 * Time: 14:35
 */
public class DarkyensBrain extends RobotStrategy {


    public DarkyensBrain(AbstractRobotInterface robotInterface, String strategyDescriptor) {
        super(robotInterface, "Fully nothing");
    }

    @Override
    public void stop() {

    }

    @Override
    public void run() {
        while(true) {
            robotInterface.doNothing();
        }
    }
}

