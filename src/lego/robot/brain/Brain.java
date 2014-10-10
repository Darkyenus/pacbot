package lego.robot.brain;

import lego.robot.api.AbstractRobotInterface;
import lego.robot.api.RobotStrategy;
import lego.robot.brain.clever.CleverMain;
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

    public static final String[] supportedTypes = {"Random", "Testificate", "Clever"};

    private String type;
    private HashMap<String, String> data = new HashMap<String, String>();

    public Brain(String type){
        this.type = type;
    }

    public void setData(HashMap<String, String> data){
        this.data = data;
    }

    public RobotStrategy getInstance(AbstractRobotInterface ari){
        RobotStrategy res = null;

        if("Random".equalsIgnoreCase(type)){
            res = new RandomMain(ari);
        }else if("Testificate".equalsIgnoreCase(type)){
            res = new TestificateMain(ari);
        }else if("Clever".equalsIgnoreCase(type)){
            res = new CleverMain(ari);
        }

        if(res != null){
            res.setInitialData(data);
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
