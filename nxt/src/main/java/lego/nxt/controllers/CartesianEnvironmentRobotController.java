package lego.nxt.controllers;

import lego.api.controllers.EnvironmentController;
import lego.nxt.MotorController;
import lego.nxt.util.TaskProcessor;
import lejos.nxt.*;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 12:11
 */
public class CartesianEnvironmentRobotController extends EnvironmentController {

    private static final int DEFAULT_SPEED = 800;
    private final MotorController xMotor = new MotorController(MotorPort.B);
    private final MotorController yMotor = new MotorController(MotorPort.C); //TODO change to match bot
    {
        xMotor.setSpeed(DEFAULT_SPEED);
        yMotor.setSpeed(DEFAULT_SPEED);
    }
    private final MotorController axisMotor = new MotorController(MotorPort.A);
    {
        axisMotor.setSpeed(MotorController.getMaxSpeed());
        axisMotor.setStallThreshold(20,200);
    }

    private final TouchSensor xTouch = new TouchSensor(SensorPort.S2);
    private final TouchSensor yTouch = new TouchSensor(SensorPort.S1);
    private final UltrasonicSensor detector = new UltrasonicSensor(SensorPort.S3);

    private boolean onX = true;

    @Override
    protected void initialize() {
        TaskProcessor.initialize();

        Thread debugViewThread = new Thread(){
            @Override
            public void run() {

            }
        };
        debugViewThread.setDaemon(true);
        debugViewThread.setName("DebugView");
    }

    @Override
    public MoveFieldsTask moveByXAsync(byte x) {
        MoveTask result = new MoveTask(true,x);
        TaskProcessor.appendTask(result);
        return result;
    }

    @Override
    public MoveFieldsTask moveByYAsync(byte y) {
        MoveTask result = new MoveTask(false,y);
        TaskProcessor.appendTask(result);
        return result;
    }

    @Override
    public MoveFieldsTask moveAsync(Direction in) {
        switch (in){
            case UP:
                return moveByYAsync(Byte.MIN_VALUE);
            case DOWN:
                return moveByYAsync(Byte.MAX_VALUE);
            case LEFT:
                return moveByXAsync(Byte.MIN_VALUE);
            case RIGHT:
                return moveByYAsync(Byte.MAX_VALUE);
            default:
                //This will never return, above match is exhaustive. Unless in is null. Then you have bigger problems.
                throw new Error();
        }
    }

    private class MoveTask extends AbstractMoveTask {

        public static final float X_FIELD_DISTANCE = 370f;
        public static final float Y_FIELD_DISTANCE = 550f;

        private final boolean onX;
        private final byte by;

        private MoveTask(boolean onX, byte by) {
            this.onX = onX;
            this.by = by;
        }

        private void getOnX(){
            axisMotor.rotate(450,true,false);
            CartesianEnvironmentRobotController.this.onX = true;
        }

        private void getOnY(){
            axisMotor.rotate(-450,true,false);
            CartesianEnvironmentRobotController.this.onX = false;
        }

        private boolean moveByField(byte directionSign){
            MotorController motor = onX ? xMotor : yMotor;
            TouchSensor touch = onX ? xTouch : yTouch;
            int originalTachoCount = motor.getTachoCount();
            boolean returningFromWall = false;
            motor.rotate((onX ? X_FIELD_DISTANCE : Y_FIELD_DISTANCE)*directionSign,isNextStationery(),true);
            while(motor.isMoving() && !returningFromWall){
                if(touch.isPressed()){
                    //! collision
                    if(motor.getProgress() < 0.75f){
                        returningFromWall = true;
                        motor.rotateTo(originalTachoCount,isNextStationery(),false);
                    }
                }
            }
            return !returningFromWall;
        }

        @Override
        protected void process() {
            if(onX && !CartesianEnvironmentRobotController.this.onX){
                getOnX();
            }else if(!onX && CartesianEnvironmentRobotController.this.onX){
                getOnY();
            }
            if(by > 0){
                for (byte i = 0; i < by; i++) {
                    if(moveByField((byte)1)){
                        moved += 1;
                    }else {
                        break;
                    }
                }
                if(onX){
                    x += moved;
                }else{
                    y += moved;
                }
            }else{
                for (byte i = 0; i < -by; i++) {
                    if(moveByField((byte)-1)){
                        moved += 1;
                    }else{
                        break;
                    }
                }
                if(onX){
                    x -= moved;
                }else{
                    y -= moved;
                }
            }
            doComplete();
        }
    }

    private abstract class AbstractMoveTask extends TaskProcessor.Task implements MoveFieldsTask {

        @Override
        protected abstract void process();

        protected byte moved;
        protected volatile boolean done;

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public byte moved() {
            waitUntilDone();
            return moved;
        }

        @Override
        public void waitUntilDone() {
            if(done)return;
            synchronized (this) {
                while (!done){
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        done = true;
                    }
                }
            }
        }

        protected void doComplete(){
            if(done)return;
            synchronized (this){
                done = true;
                this.notify();
            }
        }
    }
}
