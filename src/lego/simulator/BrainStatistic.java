package lego.simulator;

import java.util.LinkedHashMap;

/**
 * Private property.
 * User: jIRKA
 * Date: 3.10.2014
 * Time: 20:26
 */
public class BrainStatistic {

    private LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();

    public BrainStatistic(LinkedHashMap<String, String> data){
        this.data = data;
    }

    public String getValueOf(String name){
        if(data.containsKey(name)){
            return data.get(name);
        }else{
            return null;
        }
    }

    public String[] keys(){
        return data.keySet().toArray(new String[0]);
    }

}
