package lego.robot.api;

/**
 * Private property.
 * User: jIRKA
 * Date: 30.9.2014
 * Time: 20:30
 */
public abstract class RobotStrategy implements Runnable{

    protected AbstractRobotInterface robotInterface = null;
    protected RobotEnvironment robotEnvironment = null;
    protected String strategyDescriptor;

    public abstract void stop();

    public RobotStrategy(AbstractRobotInterface robotInterface, String strategyDescriptor){
        this.robotInterface = robotInterface;
        robotEnvironment = robotInterface.getRobotEnvironment();
        this.strategyDescriptor = strategyDescriptor;
    }

    public RobotEnvironment getRobotEnvironment(){
        return robotEnvironment;
    }
    public String getStrategyDescriptor(){
        return strategyDescriptor;
    }


}
