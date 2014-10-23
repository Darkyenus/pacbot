package lego.robot.api;

import java.util.HashMap;

/**
 * Private property.
 * User: jIRKA
 * Date: 30.9.2014
 * Time: 20:30
 *
 * @deprecated Old API
 */
@Deprecated
public abstract class RobotStrategy implements Runnable {

    protected AbstractRobotInterface robotInterface = null;
    protected RobotEnvironment robotEnvironment = null;
    protected String strategyDescriptor;

    protected HashMap<String, String> initialData = new HashMap<String, String>();

    public abstract void stop();

    public RobotStrategy(AbstractRobotInterface robotInterface, String strategyDescriptor){
        this.robotInterface = robotInterface;
        robotEnvironment = robotInterface.getRobotEnvironment();
        this.strategyDescriptor = strategyDescriptor;
    }

    public RobotEnvironment getRobotEnvironment(){
        return robotEnvironment;
    }
    public AbstractRobotInterface getRobotInterface(){
        return robotInterface;
    }
    public String getStrategyDescriptor(){
        return strategyDescriptor;
    }


    public void setInitialData(HashMap<String,String> data){
        initialData = data;
    }


}
