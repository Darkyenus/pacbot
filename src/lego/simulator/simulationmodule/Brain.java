package lego.simulator.simulationmodule;

import lego.robot.api.AbstractRobotInterface;
import lego.robot.api.RobotStrategy;
import lego.robot.brain.random.RandomMain;
import lego.robot.brain.testificate.TestificateMain;

import java.util.HashMap;

/**
 * Private property.
 * User: jIRKA
 * Date: 8.10.2014
 * Time: 17:56
 */
public class Brain {

    public static final String[] supportedTypes = {"Random","Testificate"};

    private String type;
    private HashMap<String, String> data = new HashMap<>();

    public Brain(String type){
        this.type = type;
    }

    public void setData(HashMap<String, String> data){
        this.data = data;
    }

    public RobotStrategy getInstance(AbstractRobotInterface ari){
        RobotStrategy res = null;
        switch (type.toLowerCase()){
            case "random": res = new RandomMain(ari); break;
            case "testificate": res = new TestificateMain(ari); break;
        }
        return res;
    }

    public HashMap<String, String> getData(){
        return data;
    }

    public String getType(){
        return type;
    }

}
