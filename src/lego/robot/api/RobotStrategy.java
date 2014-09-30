package lego.robot.api;

/**
 * Created by jIRKA on 30.9.2014.
 */
public abstract class RobotStrategy implements Runnable {

    protected RobotInterface robotInterface = null;
    protected RobotEnvironment robotEnvironment = null;

    public RobotStrategy(RobotInterface robotInterface, RobotEnvironment robotEnvironment){
        this.robotInterface = robotInterface;
        this.robotEnvironment = robotEnvironment;
    }


}
