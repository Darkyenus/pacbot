package lego.robot.brain.random;

import lego.robot.api.AbstractRobotInterface;
import lego.robot.api.RobotStrategy;

/**
 * Private property.
 * User: jIRKA
 * Date: 3.10.2014
 * Time: 20:30
 */
public class RandomMain extends RobotStrategy {

    public RandomMain(AbstractRobotInterface ari){
        super(ari,"'Random' - Fully random");
    }

    private boolean run = true;

    @Override
    public void run() {
        run = true;
        boolean banForward = false;
        while(run){

            boolean left = robotInterface.scanLeft();
            boolean right = robotInterface.scanRight();
            boolean front = robotInterface.scanFront() && !banForward;

            banForward = false;

            if(!front && !left && !right){
                robotInterface.queueMoveBackward();
                banForward = true;
            }else if(front && !left && !right){
                robotInterface.queueMoveForward();
            }else if(!front && left && !right){
                robotInterface.queueTurnLeft();
                robotInterface.queueMoveForward();
            }else if(!front && !left && right){
                robotInterface.queueTurnRight();
                robotInterface.queueMoveForward();
            }else if(!front){
                if(Math.random() > 0.5f){
                    robotInterface.queueTurnLeft();
                }else{
                    robotInterface.queueTurnRight();
                }
                robotInterface.queueMoveForward();
            }else{
                if(left && right){
                    if(Math.random() < 0.333f){
                        robotInterface.queueTurnLeft();
                    }else if(Math.random() < 0.5f){
                        robotInterface.queueTurnRight();
                    }
                    robotInterface.queueMoveForward();
                }else if(left){
                    if(Math.random() < 0.5f){
                        robotInterface.queueTurnLeft();
                    }
                    robotInterface.queueMoveForward();
                }else{
                    if(Math.random() < 0.5f){
                        robotInterface.queueTurnRight();
                    }
                    robotInterface.queueMoveForward();
                }
            }

        }
    }

    @Override
    public void stop() {
        this.run = false;
    }
}
