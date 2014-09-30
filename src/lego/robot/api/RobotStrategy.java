package lego.robot.api;

/**
 * Created by jIRKA on 30.9.2014.
 */
public abstract class RobotStrategy implements Runnable {

    protected AbstractRobotInterface robotInterface = null;
    protected RobotEnvironment robotEnvironment = null;

    public RobotStrategy(RobotEnvironment robotEnvironment){
        this.robotEnvironment = robotEnvironment;
    }

    public void setUpRobotInterface(AbstractRobotInterface robotInterface){
        this.robotInterface = robotInterface;
    }

    public RobotEnvironment getRobotEnvironment(){
        return robotEnvironment;
    }


}
