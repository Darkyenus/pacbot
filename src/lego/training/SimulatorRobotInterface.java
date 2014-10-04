package lego.training;

import lego.robot.api.RobotStrategy;
import lego.util.Util;
import lego.robot.api.RobotInterface;
import lego.robot.api.constants.AbsoluteHeading;
import lego.robot.api.constants.RelativeMovement;

/**
 * Created by jIRKA on 3.10.2014.
 */
public class SimulatorRobotInterface extends RobotInterface {

    private TrainingMap map;
    private RobotStrategy strategy;

    private boolean ffMode = false;

    public SimulatorRobotInterface(TrainingMap map){
        super(map.getRobotEnvironment());
        this.map = map;
    }

    public void getReady(RobotStrategy strategy){
        getRobotEnvironment().deploy(map.getStartPos());
        getRobotEnvironment().rotateTo(AbsoluteHeading.DOWN);
        map.robotHeading = AbsoluteHeading.DOWN;
        map.robotPos = map.getStartPos();
        this.strategy = strategy;
    }

    public void setFastForwardMode(boolean mode){
        ffMode = mode;
    }

    public void nextStepBlock(String robotBehaviourDescription){
        if(map.getBlocksRemainingToCollect() == 0){ //Done.

            strategy.stop();

            if (!ffMode) {
                TrainingMain.renderMap(map, true);
                System.out.println();
                System.out.println("Robot has " + robotBehaviourDescription + " in last step.");
                System.out.println("And this step was its last, because all pac-dots are collected!");
                System.out.println();
                TrainingMain.waitForEnter(true);
                System.out.println();
            }
        }else {
            if (!ffMode) {
                TrainingMain.renderMap(map, true);
                System.out.println();
                System.out.println("Robot has " + robotBehaviourDescription + " in last step.");
                System.out.println();
                TrainingMain.waitForEnter(true);
                System.out.println();
            }
        }
    }

    @Override
    public void moveRobotForward() {
        TupleIntInt newPos = Util.getTransformedPos(map.robotPos, map.robotHeading, RelativeMovement.FORWARD);
        if(Util.isWithinMapBounds(newPos)){
            if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
                if(map.robotHeading == AbsoluteHeading.UP){ //Moving from bottom to top, the only entrance of start
                    map.robotPos = newPos;
                    map.logMovement();
                }else{
                    map.logObstacleHinders();
                }
            }else if(!map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
                if(map.getMaze()[map.robotPos.getX()][map.robotPos.getY()].isStart){
                    if(map.robotHeading != AbsoluteHeading.UP){
                        map.robotPos = newPos;
                        map.logMovement();
                    }else{
                        map.logObstacleHinders();
                    }
                }else {
                    map.robotPos = newPos;
                    if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                        map.markAnotherBlockAsCollected();
                    }
                    map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                    map.logMovement();
                }
            }else{
                map.logObstacleHinders();
            }
        }else{
            map.logObstacleHinders();
        }
        nextStepBlock("moved forward");
    }

    @Override
    public void moveRobotLeft() {
        TupleIntInt newPos = Util.getTransformedPos(map.robotPos, map.robotHeading, RelativeMovement.LEFT);
        if(Util.isWithinMapBounds(newPos)){
            if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
                if(map.robotHeading == AbsoluteHeading.RIGHT){ //Moving from bottom to top, the only entrance of start
                    map.robotPos = newPos;
                    map.logMovement();
                }else{
                    map.logObstacleHinders();
                }
            }else if(!map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
                if(map.getMaze()[map.robotPos.getX()][map.robotPos.getY()].isStart){
                    if(map.robotHeading != AbsoluteHeading.RIGHT){
                        map.robotPos = newPos;
                        map.logMovement();
                    }else{
                        map.logObstacleHinders();
                    }
                }else {
                    map.robotPos = newPos;
                    if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                        map.markAnotherBlockAsCollected();
                    }
                    map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                    map.logMovement();
                }
            }else{
                map.logObstacleHinders();
            }
        }else{
            map.logObstacleHinders();
        }
        nextStepBlock("moved left");
    }

    @Override
    public void moveRobotRight() {
        TupleIntInt newPos = Util.getTransformedPos(map.robotPos, map.robotHeading, RelativeMovement.RIGHT);
        if(Util.isWithinMapBounds(newPos)){
            if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
                if(map.robotHeading == AbsoluteHeading.LEFT){ //Moving from bottom to top, the only entrance of start
                    map.robotPos = newPos;
                    map.logMovement();
                }else{
                    map.logObstacleHinders();
                }
            }else if(!map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
                if(map.getMaze()[map.robotPos.getX()][map.robotPos.getY()].isStart){
                    if(map.robotHeading != AbsoluteHeading.LEFT){
                        map.robotPos = newPos;
                        map.logMovement();
                    }else{
                        map.logObstacleHinders();
                    }
                }else {
                    map.robotPos = newPos;
                    if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                        map.markAnotherBlockAsCollected();
                    }
                    map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                    map.logMovement();
                }
            }else{
                map.logObstacleHinders();
            }
        }else{
            map.logObstacleHinders();
        }
        nextStepBlock("moved right");
    }

    @Override
    public void moveRobotBackward() {
        TupleIntInt newPos = Util.getTransformedPos(map.robotPos, map.robotHeading, RelativeMovement.BACKWARD);
        if(Util.isWithinMapBounds(newPos)){
            if(map.getMaze()[newPos.getX()][newPos.getY()].isStart){
                if(map.robotHeading == AbsoluteHeading.DOWN){ //Moving from bottom to top, the only entrance of start
                    map.robotPos = newPos;
                    map.logMovement();
                }else{
                    map.logObstacleHinders();
                }
            }else if(!map.getMaze()[newPos.getX()][newPos.getY()].isBlock){
                if(map.getMaze()[map.robotPos.getX()][map.robotPos.getY()].isStart){
                    if(map.robotHeading != AbsoluteHeading.DOWN){
                        map.robotPos = newPos;
                        map.logMovement();
                    }else{
                        map.logObstacleHinders();
                    }
                }else {
                    map.robotPos = newPos;
                    if(map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes == 0){
                        map.markAnotherBlockAsCollected();
                    }
                    map.getMaze()[newPos.getX()][newPos.getY()].visitedTimes++;
                    map.logMovement();
                }
            }else{
                map.logObstacleHinders();
            }
        }else{
            map.logObstacleHinders();
        }
        nextStepBlock("moved backward");
    }

    @Override
    public void rotateRobotLeft() {
        switch(map.robotHeading) {
            case DOWN:
                map.robotHeading = AbsoluteHeading.RIGHT;
                break;
            case RIGHT:
                map.robotHeading = AbsoluteHeading.UP;
                break;
            case UP:
                map.robotHeading = AbsoluteHeading.LEFT;
                break;
            case LEFT:
                map.robotHeading = AbsoluteHeading.DOWN;
                break;
        }
        map.logTurn();
        nextStepBlock("turned left");
    }

    @Override
    public void rotateRobotRight() {
        switch(map.robotHeading) {
            case DOWN:
                map.robotHeading = AbsoluteHeading.LEFT;
                break;
            case LEFT:
                map.robotHeading = AbsoluteHeading.UP;
                break;
            case UP:
                map.robotHeading = AbsoluteHeading.RIGHT;
                break;
            case RIGHT:
                map.robotHeading = AbsoluteHeading.DOWN;
                break;
        }
        map.logTurn();
        nextStepBlock("turned right");
    }



    @Override
    public boolean scanLeft() {
        return false;
    }

    @Override
    public boolean scanRight() {
        return false;
    }

    @Override
    public boolean scanFront() {
        return false;
    }

    @Override
    public boolean scanBack() {
        return false;
    }
}
