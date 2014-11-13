package lego.nxt.controllers;

import lego.api.controllers.CartesianController;
import lego.nxt.Driver;
import lego.nxt.util.TaskProcessor;
import lejos.nxt.Motor;

/**
 * Private property.
 * User: Darkyen
 * Date: 21/10/14
 * Time: 20:30
 *
 * @deprecated Uses old API
 */
@Deprecated
public class CartesianRobotController extends CartesianController {

    @Override
    protected void initialize() {
        Driver.initialize();
    }

    public static final float BLOCK_DISTANCE = 28.5f;
    public static final int DEFAULT_SPEED = 800;

    private boolean inVerticalMode = true;

    @Override
    public boolean isInVerticalMode(){
        return inVerticalMode;
    }

    @Override
    public void setInVerticalMode(boolean inVerticalMode){
        this.inVerticalMode = inVerticalMode;
    }

    private TaskProcessor.Task constructAxisToggle(final boolean targetInVertical){
        return new TaskProcessor.Task() {
            @Override
            protected void process() {
                Motor.A.setStallThreshold(30,250);//This should prevent mishaps TODO QQQ
                if(!targetInVertical && isInVerticalMode()) {
                    Motor.A.rotate(-450);
                    setInVerticalMode(false);
                }else if(targetInVertical && !isInVerticalMode()){
                    Motor.A.rotate(450);
                    setInVerticalMode(true);
                }
            }

            @Override
            public boolean isStationery() {
                return isInVerticalMode() == targetInVertical;
            }

            @Override
            public String toString() {
                return "T:Toggle " + targetInVertical;
            }
        };
    }

    @Override
    public void queueAddMoveForward() {
        TaskProcessor.appendTask(constructAxisToggle(true));
        TaskProcessor.appendTask(
                new TaskProcessor.Task() {
                    @Override
                    protected void process() {
                        Driver.MotorManager.move(BLOCK_DISTANCE, 0, DEFAULT_SPEED, Driver.MotorManager.SMOOTH_ACCELERATION, isNextStationery() ? Driver.MotorManager.SMOOTH_ACCELERATION : Driver.MotorManager.NO_DECELERATION, isNextStationery());
                    }

                    @Override
                    public boolean isStationery() {
                        return false;
                    }

                    @Override
                    public String toString() {
                        return "T:Straight " + BLOCK_DISTANCE;
                    }
                }
        );
    }

    @Override
    public void queueAddMoveBackward() {
        TaskProcessor.appendTask(constructAxisToggle(true));
        TaskProcessor.appendTask(
                new TaskProcessor.Task() {
                    @Override
                    protected void process() {
                        Driver.MotorManager.move(-BLOCK_DISTANCE, 0, DEFAULT_SPEED, Driver.MotorManager.SMOOTH_ACCELERATION, isNextStationery() ? Driver.MotorManager.SMOOTH_ACCELERATION : Driver.MotorManager.NO_DECELERATION, isNextStationery());
                    }

                    @Override
                    public boolean isStationery() {
                        return false;
                    }

                    @Override
                    public String toString() {
                        return "T:Straight " + -BLOCK_DISTANCE;
                    }
                }
        );
    }


    @Override
    public void queueAddMoveLeft() { //Relative movement * facing down = absolute move right
        TaskProcessor.appendTask(constructAxisToggle(false));
        TaskProcessor.appendTask(
                new TaskProcessor.Task() {
                    @Override
                    protected void process() {
                        Driver.MotorManager.move(-BLOCK_DISTANCE, 0, DEFAULT_SPEED, Driver.MotorManager.SMOOTH_ACCELERATION, isNextStationery() ? Driver.MotorManager.SMOOTH_ACCELERATION : Driver.MotorManager.NO_DECELERATION, isNextStationery());
                    }

                    @Override
                    public boolean isStationery() {
                        return false;
                    }

                    @Override
                    public String toString() {
                        return "T:Straight " + -BLOCK_DISTANCE;
                    }
                }
        );
    }

    @Override
    public void queueAddMoveRight() {
        TaskProcessor.appendTask(constructAxisToggle(false));
        TaskProcessor.appendTask(
                new TaskProcessor.Task() {
                    @Override
                    protected void process() {
                        Driver.MotorManager.move(BLOCK_DISTANCE, 0, DEFAULT_SPEED, Driver.MotorManager.SMOOTH_ACCELERATION, isNextStationery() ? Driver.MotorManager.SMOOTH_ACCELERATION : Driver.MotorManager.NO_DECELERATION, isNextStationery());
                    }

                    @Override
                    public boolean isStationery() {
                        return false;
                    }

                    @Override
                    public String toString() {
                        return "T:Straight " + BLOCK_DISTANCE;
                    }
                }
        );
    }


    @Override
    public boolean scanLeft() {
        //TODO need to enhance robot hardware
        return false;
    }

    @Override
    public boolean scanRight() {
        //TODO need to enhance robot hardware
        return false;
    }

    @Override
    public boolean scanFront() {
        //TODO need to enhance robot hardware
        return false;
    }

    @Override
    public boolean scanBack() {
        //TODO need to enhance robot hardware
        return false;
    }

    @Override
    public boolean isQueueRunning() {
        return !TaskProcessor.isIdle();
    }

    @Override
    public void waitUntilQueueIsEmpty() {
        TaskProcessor.waitUntilIdle();
    }
}
