package lego.nxt;

import lego.nxt.util.TaskProcessor;
import lego.robot.api.RobotInterface;
import lejos.nxt.Motor;

/**
 * Private property.
 * User: jIRKA
 * Date: 9.10.2014
 * Time: 17:34
 *
 * @deprecated This uses Driver class and is old api. Will be deleted soon, probably.
 */
@Deprecated
public class CartesianRobotInterface extends RobotInterface {

    public static final float BLOCK_DISTANCE = 28.5f;
    public static final int DEFAULT_SPEED = 800;

    private boolean inVerticalMode = true;

    public boolean isInVerticalMode(){
        return inVerticalMode;
    }

    public void setInVerticalMode(boolean inVerticalMode){
        this.inVerticalMode = inVerticalMode;
    }

    private TaskProcessor.Task constructAxisToggle(final boolean targetInVertical){
        return new TaskProcessor.Task() {
            @Override
            protected void process() {
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

    @Override
    public void queueAddTurnLeft() {
        throw new UnsupportedOperationException("Cartesian robot cannot turn");
    }

    @Override
    public void queueAddTurnRight() {
        throw new UnsupportedOperationException("Differential robot cannot turn");
    }
}
