package lego.robot.api;

/**
 * Created by jIRKA on 30.9.2014.
 */
public abstract class RobotStrategy implements Runnable {

    protected AbstractRobotInterface robotInterface = null;
    protected RobotEnvironment robotEnvironment = null;

    public abstract void stop();

    public RobotStrategy(AbstractRobotInterface robotInterface){
        this.robotInterface = robotInterface;
        robotEnvironment = robotInterface.getRobotEnvironment();
    }

    public RobotEnvironment getRobotEnvironment(){
        return robotEnvironment;
    }


}
