package lego.simulator.simulationmodule;

import lego.robot.api.RobotEnvironment;
import lego.robot.api.RobotStrategy;
import lego.robot.api.constants.AbsoluteHeading;
import lego.simulator.BrainStatistic;
import lego.simulator.userinterface.Print;
import lego.simulator.userinterface.Render;
import lego.simulator.userinterface.UserInput;
import lego.util.TupleIntInt;

import java.util.LinkedHashMap;

/**
 * Private property.
 * User: jIRKA
 * Date: 8.10.2014
 * Time: 17:17
 */
public class Simulator {

    private int totalMovements = 0;
    private int totalTurns = 0;
    private int totalObstacleHinders = 0;
    private int totalSleptSteps = 0;

    private TrainingMap map;
    private RobotStrategy strategy;
    private SimulatorRobotInterface simInterface;

    private RobotEnvironment env = new RobotEnvironment();

    private AbsoluteHeading robotHeading;
    private TupleIntInt robotPos;

    public TupleIntInt getRobotPos(){
        return robotPos;
    }
    public AbsoluteHeading getRobotHeading(){
        return robotHeading;
    }

    public void setRobotPos(TupleIntInt robotPos){
        this.robotPos = robotPos;
    }
    public void setRobotHeading(AbsoluteHeading robotHeading){
        this.robotHeading = robotHeading;
    }

    private String[] lines;

    private boolean ffMode = false;

    public Simulator(TrainingMap map){
        this.map = map;
        this.simInterface = new SimulatorRobotInterface(map, this);
    }

    public void getReady(RobotStrategy strategy){
        map.reset();
        strategy.getRobotEnvironment().deploy(map.getStartPos());
        strategy.getRobotEnvironment().rotateTo(AbsoluteHeading.DOWN);
        robotHeading = AbsoluteHeading.DOWN;
        robotPos = map.getStartPos();
        this.strategy = strategy;
    }

    public void setFastForwardMode(boolean mode){
        ffMode = mode;
    }

    public void debugRender(String[] lines){
        this.lines = lines;
    }

    public void nextStepBlock(String robotBehaviourDescription){
        if(map.getBlocksRemainingToCollect() == 0){ //Done.

            strategy.stop();

            if (!ffMode) {
                Render.textAlongSideMap(map, robotPos, null, lines);
                Print.line("");
                Print.line("Robot has " + robotBehaviourDescription + " in last step.");
                Print.line("And this step was its last, because all pac-dots collected!");
                Print.line("");
                UserInput.waitForEnter(true);
                Print.line("");
            }
        }else {
            if (!ffMode) {
                Render.textAlongSideMap(map, robotPos, null, lines);
                Print.line("");
                Print.line("Robot has " + robotBehaviourDescription + " in last step.");
                Print.line("");
                boolean cont = UserInput.waitForEnterCancelable(true);
                Print.line("");
                if(!cont){
                    strategy.stop();
                    Print.warn("Robot movement has been terminated by user.\n");
                    Print.line("");
                }
            }
        }
        lines = null;
    }


    public BrainStatistic getStatistics(RobotStrategy robotStrategy){

        LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();

        boolean passed = true;
        int totalCollected = 0;
        int totalCollectible = 0;

        for(int x = 0; x < TrainingMap.mazeWidth; x ++){
            for(int y = 0; y < TrainingMap.mazeHeight; y ++){
                if(!map.getMaze()[x][y].isBlock && !map.getMaze()[x][y].isStart){
                    if(map.getMaze()[x][y].visitedTimes == 0){
                        passed = false;
                    }else {
                        totalCollectible++;
                        totalCollected += map.getMaze()[x][y].visitedTimes;
                    }
                }
            }
        }

        data.put("Brain name", robotStrategy.getStrategyDescriptor());
        data.put("Passed", passed?"Yes":"No");
        data.put("Efficiency",totalCollected==0?"NaN":(totalCollectible * 100 / totalCollected+"%"));
        data.put("No action count", Integer.toString(totalSleptSteps));
        data.put("Total movements", Integer.toString(totalMovements));
        data.put("Total turns", Integer.toString(totalTurns));
        data.put("Total ~ hinders", Integer.toString(totalObstacleHinders));

        return  new BrainStatistic(data);
    }

    public void logMovement(){
        totalMovements ++;
    }
    public void logTurn(){
        totalTurns ++;
    }
    public void logObstacleHinders(){
        totalObstacleHinders ++;
    }
    public void logDoNothing() {
        totalSleptSteps ++;
    }


    public SimulatorRobotInterface getSimulatorRobotInterface(){
        return this.simInterface;
    }


}
