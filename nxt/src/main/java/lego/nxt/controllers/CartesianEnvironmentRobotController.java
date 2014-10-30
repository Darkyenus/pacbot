package lego.nxt.controllers;

import lego.api.controllers.EnvironmentController;
import lego.nxt.MotorController;
import lego.nxt.util.TaskProcessor;
import lejos.nxt.*;

/**
 *
 * README:
 * When starting a program using this, the robot should be on X axis wheels, that means, on big wheels.
 *
 * TODO It will be better to start at Y
 *
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 12:11
 */
public class CartesianEnvironmentRobotController extends EnvironmentController {

    private static final int DEFAULT_SPEED = 800;
    private static final int BACKING_SPEED = 400;
    private static final MotorController xMotor = new MotorController(MotorPort.B);
    private static final MotorController yMotor = new MotorController(MotorPort.C);
    static {
        xMotor.setSpeed(DEFAULT_SPEED);
        yMotor.setSpeed(DEFAULT_SPEED);
    }
    private static final MotorController axisMotor = new MotorController(MotorPort.A);
    static {
        axisMotor.setSpeed(MotorController.getMaxSpeed());
        axisMotor.setStallThreshold(20,200);
    }

    private static final TouchSensor xTouch = new TouchSensor(SensorPort.S1);
    private static final TouchSensor yTouch = new TouchSensor(SensorPort.S2);
    private static final UltrasonicSensor detector = new UltrasonicSensor(SensorPort.S4);

    private static final boolean defaultOnX = true;
    private static boolean onX = defaultOnX;

    @Override
    protected void initialize() {
        TaskProcessor.initialize();

        Thread debugViewThread = new Thread(){
            @Override
            public void run() {
                LCD.setAutoRefresh(false);
                //noinspection InfiniteLoopStatement
                while(true){
                    for (byte x = 0; x < mazeWidth; x++) {
                        for (byte y = 0; y < mazeHeight; y++) {
                            if(getX() == x && getY() == y){
                                LCD.drawString(Character.toString(maze[x][y].displayChar), x, y, true);
                            } else {
                                LCD.drawChar(maze[x][y].displayChar, x, y);
                            }
                        }
                    }
                    LCD.drawString("H: "+TaskProcessor.getStackHead()+"   ",0,mazeHeight);
                    //LCD.drawString("X: "+xMotor.getProgress(),mazeWidth+1,0);
                    //LCD.drawString("Y: "+yMotor.getProgress(),mazeWidth+1,1);
                    LCD.asyncRefresh();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {}
                }
            }
        };
        debugViewThread.setDaemon(true);
        debugViewThread.setName("DebugView");
        debugViewThread.start();
    }

    @SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"}) //defaultOnX can be changed
    @Override
    protected void deinitialize() {
        if(onX != defaultOnX){
            if(onX){
                getOnY();
            }else{
                getOnX();
            }
        }
    }

    @Override
    protected void onError(byte error) {
        switch (error){
            case ERROR_SET_DEFINITIVE:
                Sound.beepSequence();
                Sound.buzz();
                //QQQ TODO
                throw new Error();
                //break;
            case ERROR_SET_OUT_OF_BOUNDS:
                Sound.beepSequenceUp();
                Sound.buzz();
                throw new Error();
                //break; //QQQ
        }
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

    /**
     * Will do reading from mounted ultrasonic detector and save it into maze map.
     */
    private void doDetectorReading(){
        int distance = detector.getDistance();
        LCD.drawString(distance+" cm  ",0,mazeHeight+1);//Hijacking debug loop rendering

        //TODO Interpret read value and make decisions (write result)
        //Sound.beep(); //We can sing too. Beep.
    }

    private final int AXIS_CHANGE_DEGREES = 450;

    private void getOnX(){
        axisMotor.rotate(AXIS_CHANGE_DEGREES,true,false);
        CartesianEnvironmentRobotController.onX = true;
        doDetectorReading();
    }

    private void getOnY(){
        axisMotor.rotate(-AXIS_CHANGE_DEGREES,true,false);
        CartesianEnvironmentRobotController.onX = false;
        doDetectorReading();
    }

    /**
     * Contains everything needed to process a single moving task by given amount of fields on given axis.
     * It is a little bit messy because of supporting both axis.
     * This also updates robot x, y and maze fields, but only using data available - that means touch sensor in front.
     */
    private class MoveTask extends AbstractMoveTask {

        public static final float X_FIELD_DISTANCE = 380f;
        public static final float Y_FIELD_DISTANCE = 560f;

        public static final float X_ACCELERATION = 1000;
        public static final float Y_ACCELERATION = 1500;
        public static final float MAX_ACCELERATION = 6000;

        private final boolean onX;
        private final byte by;

        private MoveTask(boolean onX, byte by) {
            this.onX = onX;
            this.by = by;
        }

        private boolean moveByField(byte directionSign, boolean accelerate, boolean decelerate){
            MotorController motor = onX ? xMotor : yMotor;
            TouchSensor touch = onX ? xTouch : yTouch;
            boolean returningFromWall = false;
            final boolean nextStationery = isNextStationery();
            final float acceleration = nextStationery ? (onX ? X_ACCELERATION : Y_ACCELERATION) : MAX_ACCELERATION;
            final float decidedAcceleration = accelerate ? acceleration : MAX_ACCELERATION;
            final float decidedDeceleration = decelerate ? acceleration : MAX_ACCELERATION;

            final float tachoTarget = motor.getTachoCount() + (onX ? X_FIELD_DISTANCE : Y_FIELD_DISTANCE)*directionSign;
            motor.newMove(motor.getSpeed(),decidedAcceleration,decidedDeceleration,tachoTarget,!decelerate,false);

            while(Math.abs(motor.getPosition() - tachoTarget) > 10 && !returningFromWall){
                if(touch.isPressed()){
                    Sound.beepSequence();
                    Sound.beepSequenceUp();
                    //collision
                    returningFromWall = true;
                    final float backingAcceleration = (onX ? X_ACCELERATION : Y_ACCELERATION);
                    motor.newMove(BACKING_SPEED,backingAcceleration,backingAcceleration,motor.getPosition() + ((onX ? X_FIELD_DISTANCE : Y_FIELD_DISTANCE)*-directionSign)*0.25f,false,true);
                }else{
                    try {
                        Thread.sleep(100); //Give motor thread a bit of breathing space
                    } catch (InterruptedException ignored) {}
                }
            }
            doDetectorReading();
            return !returningFromWall;
        }

        @Override
        protected void process() {
            if(onX && !CartesianEnvironmentRobotController.onX){
                getOnX();
            }else if(!onX && CartesianEnvironmentRobotController.onX){
                getOnY();
            }

            byte moveSignum = by >= 0 ? (byte)1 : (byte)-1;
            byte oneDeltaX = onX ? moveSignum : 0;
            byte oneDeltaY = !onX ? moveSignum : 0;
            byte steps = (byte) (by * moveSignum); //Absolute value of 'by'. The beauty of maths.

            for (byte i = 0; i < steps; i++) {
                if(moveByField(moveSignum,i == 0,i+1 == steps && isNextStationery())){
                    moved += 1;
                    x += oneDeltaX;
                    y += oneDeltaY;
                    setField(x,y,FieldStatus.FREE_VISITED);
                } else {
                    setField((byte)(x+oneDeltaX),(byte)(y+oneDeltaY),FieldStatus.OBSTACLE);
                    break;
                }
            }
            doComplete();
        }

        @Override
        public boolean isStationery() {
            //Stationery if on different axis, because we'll need to stop
            return onX != CartesianEnvironmentRobotController.onX;
        }

        @Override
        public String toString() {
            return "MT "+(onX ? "x" : "y")+" "+by;
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