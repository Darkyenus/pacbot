package lego.nxt;

import lego.robot.api.RobotInterface;
import lejos.nxt.Motor;

/**
 * Private property.
 * User: jIRKA
 * Date: 9.10.2014
 * Time: 17:34
 */
public class CartesianRobotInterface extends RobotInterface{

    public static final float BLOCK_DISTANCE = 28.5f;
    public static final int DEFAULT_SPEED = 800;

    private boolean inVerticalMode = true;

    public boolean isInVerticalMode(){
        return inVerticalMode;
    }

    public void setInVerticalMode(boolean inVerticalMode){
        this.inVerticalMode = inVerticalMode;
    }

    private Driver.TaskProcessor.Task constructAxisToggle(final boolean targetInVertical){
        return new Driver.TaskProcessor.Task() {
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
        Driver.TaskProcessor.appendTask(constructAxisToggle(true));
        Driver.TaskProcessor.appendTask(
                new Driver.TaskProcessor.Task() {
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
        Driver.TaskProcessor.appendTask(constructAxisToggle(true));
        Driver.TaskProcessor.appendTask(
                new Driver.TaskProcessor.Task() {
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
        Driver.TaskProcessor.appendTask(constructAxisToggle(false));
        Driver.TaskProcessor.appendTask(
                new Driver.TaskProcessor.Task() {
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
        Driver.TaskProcessor.appendTask(constructAxisToggle(false));
        Driver.TaskProcessor.appendTask(
                new Driver.TaskProcessor.Task() {
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
        return !Driver.TaskProcessor.isIdle();
    }

    @Override
    public void waitUntilQueueIsEmpty() {
        Driver.TaskProcessor.waitUntilIdle();
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
