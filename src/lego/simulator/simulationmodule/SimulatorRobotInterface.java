package lego.simulator.simulationmodule;

import lego.robot.api.RobotInterface;
import lego.robot.api.constants.AbsoluteHeading;
import lego.robot.api.constants.RelativeMovement;
import lego.util.TupleIntInt;
import lego.util.Util;

/**
 * Private property.
 * User: jIRKA
 * Date: 3.10.2014
 * Time: 17:25
 */
public class SimulatorRobotInterface extends RobotInterface {

    private TrainingMap map;
    private Simulator sim;



    public SimulatorRobotInterface(TrainingMap map, Simulator sim){
        super();
        this.map = map;
        this.sim = sim;
    }

    @Override
    public void queueAddMoveForward() {
        TupleIntInt newPos = Util.getTransformedPos(sim.getRobotPos(), sim.getRobotHeading(), RelativeMovement.FORWARD);
        if(Util.isWithinMapBounds(newPos)){
            if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
                if(sim.getRobotHeading() == AbsoluteHeading.UP){ //Moving from bottom to top, the only entrance of start
                    sim.setRobotPos(newPos);
                    sim.logMovement();
                }else{
                    sim.logObstacleHinders();
                }
            }else if(!map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
                if(map.getMaze()[sim.getRobotPos().getX()][sim.getRobotPos().getY()].isStart){
                    if(sim.getRobotHeading() != AbsoluteHeading.UP){
                        sim.setRobotPos(newPos);
                        if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                            map.markAnotherBlockAsCollected();
                        }
                        map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                        sim.logMovement();
                    }else{
                        sim.logObstacleHinders();
                    }
                }else {
                    sim.setRobotPos(newPos);
                    if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                        map.markAnotherBlockAsCollected();
                    }
                    map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                    sim.logMovement();
                }
            }else{
                sim.logObstacleHinders();
            }
        }else{
            sim.logObstacleHinders();
        }
        sim.nextStepBlock("moved forward");
    }

    @Override
    public void queueAddMoveLeft() {
        TupleIntInt newPos = Util.getTransformedPos(sim.getRobotPos(), sim.getRobotHeading(), RelativeMovement.LEFT);
        if(Util.isWithinMapBounds(newPos)){
            if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
                if(sim.getRobotHeading() == AbsoluteHeading.RIGHT){ //Moving from bottom to top, the only entrance of start
                    sim.setRobotPos(newPos);
                    sim.logMovement();
                }else{
                    sim.logObstacleHinders();
                }
            }else if(!map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
                if(map.getMaze()[sim.getRobotPos().getX()][sim.getRobotPos().getY()].isStart){
                    if(sim.getRobotHeading() != AbsoluteHeading.RIGHT){
                        sim.setRobotPos(newPos);
                        if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                            map.markAnotherBlockAsCollected();
                        }
                        map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                        sim.logMovement();
                    }else{
                        sim.logObstacleHinders();
                    }
                }else {
                    sim.setRobotPos(newPos);
                    if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                        map.markAnotherBlockAsCollected();
                    }
                    map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                    sim.logMovement();
                }
            }else{
                sim.logObstacleHinders();
            }
        }else{
            sim.logObstacleHinders();
        }
        sim.nextStepBlock("moved left");
    }

    @Override
    public void queueAddMoveRight() {
        TupleIntInt newPos = Util.getTransformedPos(sim.getRobotPos(), sim.getRobotHeading(), RelativeMovement.RIGHT);
        if(Util.isWithinMapBounds(newPos)){
            if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
                if(sim.getRobotHeading() == AbsoluteHeading.LEFT){ //Moving from bottom to top, the only entrance of start
                    sim.setRobotPos(newPos);
                    sim.logMovement();
                }else{
                    sim.logObstacleHinders();
                }
            }else if(!map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
                if(map.getMaze()[sim.getRobotPos().getX()][sim.getRobotPos().getY()].isStart){
                    if(sim.getRobotHeading() != AbsoluteHeading.LEFT){
                        sim.setRobotPos(newPos);
                        if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                            map.markAnotherBlockAsCollected();
                        }
                        map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                        sim.logMovement();
                    }else{
                        sim.logObstacleHinders();
                    }
                }else {
                    sim.setRobotPos(newPos);
                    if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                        map.markAnotherBlockAsCollected();
                    }
                    map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                    sim.logMovement();
                }
            }else{
                sim.logObstacleHinders();
            }
        }else{
            sim.logObstacleHinders();
        }
        sim.nextStepBlock("moved right");
    }

    @Override
    public void queueAddMoveBackward() {
        TupleIntInt newPos = Util.getTransformedPos(sim.getRobotPos(), sim.getRobotHeading(), RelativeMovement.BACKWARD);
        if(Util.isWithinMapBounds(newPos)){
            if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
                if(sim.getRobotHeading() == AbsoluteHeading.DOWN){ //Moving from bottom to top, the only entrance of start
                    sim.setRobotPos(newPos);
                    sim.logMovement();
                }else{
                    sim.logObstacleHinders();
                }
            }else if(!map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
                if(map.getMaze()[sim.getRobotPos().getX()][sim.getRobotPos().getY()].isStart){
                    if(sim.getRobotHeading() != AbsoluteHeading.DOWN){
                        sim.setRobotPos(newPos);
                        if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                            map.markAnotherBlockAsCollected();
                        }
                        map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                        sim.logMovement();
                    }else{
                        sim.logObstacleHinders();
                    }
                }else {
                    sim.setRobotPos(newPos);
                    if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                        map.markAnotherBlockAsCollected();
                    }
                    map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                    sim.logMovement();
                }
            }else{
                sim.logObstacleHinders();
            }
        }else{
            sim.logObstacleHinders();
        }
        sim.nextStepBlock("moved backward");
    }

    @Override
    public void queueAddTurnLeft() {
        switch(sim.getRobotHeading()) {
            case DOWN:
                sim.setRobotHeading(AbsoluteHeading.RIGHT);
                break;
            case RIGHT:
                sim.setRobotHeading(AbsoluteHeading.UP);
                break;
            case UP:
                sim.setRobotHeading(AbsoluteHeading.LEFT);
                break;
            case LEFT:
                sim.setRobotHeading(AbsoluteHeading.DOWN);
                break;
        }
        sim.logTurn();
        sim.nextStepBlock("turned left");
    }

    @Override
    public void queueAddTurnRight() {
        switch(sim.getRobotHeading()) {
            case DOWN:
                sim.setRobotHeading(AbsoluteHeading.LEFT);
                break;
            case LEFT:
                sim.setRobotHeading(AbsoluteHeading.UP);
                break;
            case UP:
                sim.setRobotHeading(AbsoluteHeading.RIGHT);
                break;
            case RIGHT:
                sim.setRobotHeading(AbsoluteHeading.DOWN);
                break;
        }
        sim.logTurn();
        sim.nextStepBlock("turned right");
    }



    @Override
    public boolean scanLeft() {
        TupleIntInt newPos = Util.getTransformedPos(sim.getRobotPos(), sim.getRobotHeading(), RelativeMovement.LEFT);
        if(!Util.isWithinMapBounds(newPos))
            return false;
        if(map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
            return false;
        }else if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
            return sim.getRobotHeading() == AbsoluteHeading.RIGHT;
        }else if(map.getMaze()[sim.getRobotPos().getX()][sim.getRobotPos().getY()].isStart){
            return sim.getRobotHeading() == AbsoluteHeading.LEFT;
        }else{
            return true;
        }
    }

    @Override
    public boolean scanRight() {
        TupleIntInt newPos = Util.getTransformedPos(sim.getRobotPos(), sim.getRobotHeading(), RelativeMovement.RIGHT);
        if(!Util.isWithinMapBounds(newPos))
            return false;
        if(map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
            return false;
        }else if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
            return sim.getRobotHeading() == AbsoluteHeading.LEFT;
        }else if(map.getMaze()[sim.getRobotPos().getX()][sim.getRobotPos().getY()].isStart){
            return sim.getRobotHeading() == AbsoluteHeading.RIGHT;
        }else{
            return true;
        }
    }

    @Override
    public boolean scanFront() {
        TupleIntInt newPos = Util.getTransformedPos(sim.getRobotPos(), sim.getRobotHeading(), RelativeMovement.FORWARD);
        if(!Util.isWithinMapBounds(newPos))
            return false;
        if(map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
            return false;
        }else if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
            return sim.getRobotHeading() == AbsoluteHeading.UP;
        }else if(map.getMaze()[sim.getRobotPos().getX()][sim.getRobotPos().getY()].isStart){
            return sim.getRobotHeading() == AbsoluteHeading.DOWN;
        }else{
            return true;
        }
    }

    @Override
    public boolean scanBack() {
        TupleIntInt newPos = Util.getTransformedPos(sim.getRobotPos(), sim.getRobotHeading(), RelativeMovement.BACKWARD);
        if(!Util.isWithinMapBounds(newPos))
            return false;
        if(map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
            return false;
        }else if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
            return sim.getRobotHeading() == AbsoluteHeading.DOWN;
        }else if(map.getMaze()[sim.getRobotPos().getX()][sim.getRobotPos().getY()].isStart){
            return sim.getRobotHeading() == AbsoluteHeading.UP;
        }else{
            return true;
        }
    }

    @Override
    public void debugRender(String[] lines) {
        sim.debugRender(lines);
    }


    //Little fraud...

    @Override
    public boolean isQueueRunning() {
        return false;
    }

    @Override
    public void waitUntilQueueIsEmpty() {

    }
}
