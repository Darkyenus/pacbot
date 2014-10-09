package lego.simulator;

import lego.simulator.simulationmodule.Brain;
import lego.simulator.simulationmodule.TrainingMap;

import java.util.ArrayList;

/**
 * Private property.
 * User: jIRKA
 * Date: 8.10.2014
 * Time: 17:54
 */
public class Storage {

    private static ArrayList<TrainingMap> loadedMaps = new ArrayList<TrainingMap>();

    public static void addMap(TrainingMap map){
        loadedMaps.add(map);
    }

    public static void clearMaps(){
        loadedMaps.clear();
    }

    public static TrainingMap[] getMaps(){
        return loadedMaps.toArray(new TrainingMap[loadedMaps.size()]);
    }



    private static ArrayList<Brain> preparedBrainz = new ArrayList<Brain>();

    public static void addBrain(Brain brain){
        preparedBrainz.add(brain);
    }

    public static void removeBrain(int index){
        if(preparedBrainz.size() > index ) preparedBrainz.remove(index);
    }

    public static void clearBrainz(){
        preparedBrainz.clear();
    }

    public static Brain[] getBrainz(){
        return preparedBrainz.toArray(new Brain[preparedBrainz.size()]);
    }


}
