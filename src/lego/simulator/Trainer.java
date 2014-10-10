package lego.simulator;

import lego.robot.brain.Brain;
import lego.simulator.simulationmodule.TrainingMap;
import lego.util.Tuple3;

import java.util.ArrayList;

/**
 * Private property.
 * User: jIRKA
 * Date: 9.10.2014
 * Time: 18:56
 */
public class Trainer {

    private ArrayList<Tuple3<Brain, BrainStatistic, TrainingMap>> list = new ArrayList<Tuple3<Brain, BrainStatistic, TrainingMap>>();

    public void addExecutedBrain(Brain b, BrainStatistic stats, TrainingMap map){
        list.add(new Tuple3<Brain, BrainStatistic, TrainingMap>(b, stats, map));
    }

    public void allTrained(){
        //TODO somehow, fitness function, create new set of brains and run again
    }

}
