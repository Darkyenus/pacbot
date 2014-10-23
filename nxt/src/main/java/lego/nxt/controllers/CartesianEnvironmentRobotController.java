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
    private final MotorController xMotor = new MotorController(MotorPort.B);
    private final MotorController yMotor = new MotorController(MotorPort.C);
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
                    LCD.drawString("H: "+TaskProcessor.getStackHead(),0,mazeHeight);
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

    /**
     * Contains everything needed to process a single moving task by given amount of fields on given axis.
     * It is a little bit messy because of supporting both axis.
     * This also updates robot x, y and maze fields, but only using data available - that means touch sensor in front.
     */
    private class MoveTask extends AbstractMoveTask {

        public static final float X_FIELD_DISTANCE = 370f;
        public static final float Y_FIELD_DISTANCE = 550f;

        private final boolean onX;
        private final byte by;

        private MoveTask(boolean onX, byte by) {
            this.onX = onX;
            this.by = by;
        }

        /**
         * Will do reading from mounted ultrasonic detector and save it into maze map.
         */
        private void doDetectorReading(){
            int distance = detector.getDistance();
            LCD.drawString(distance+" cm  ",0,mazeHeight+1);//Hijacking debug loop rendering

            //TODO Interpret read value and make decisions (write result)
            Sound.beep(); //We can sing too. Beep.
        }

        private final int AXIS_CHANGE_DEGREES = 450;

        private void getOnX(){
            axisMotor.rotate(AXIS_CHANGE_DEGREES,true,false);
            CartesianEnvironmentRobotController.this.onX = true;
            doDetectorReading();
        }

        private void getOnY(){
            axisMotor.rotate(-AXIS_CHANGE_DEGREES,true,false);
            CartesianEnvironmentRobotController.this.onX = false;
            doDetectorReading();
        }

        private boolean moveByField(byte directionSign){
            MotorController motor = onX ? xMotor : yMotor;
            TouchSensor touch = onX ? xTouch : yTouch;
            int originalTachoCount = motor.getTachoCount();
            boolean returningFromWall = false;
            motor.rotate((onX ? X_FIELD_DISTANCE : Y_FIELD_DISTANCE)*directionSign,isNextStationery(),true);
            while(motor.isMoving() && !returningFromWall){
                if(touch.isPressed()){
                    //collision
                    if(motor.getProgress() < 0.75f){
                        returningFromWall = true;
                        motor.rotateTo(originalTachoCount,isNextStationery(),false);
                    }
                }
            }
            doDetectorReading();
            return !returningFromWall;
        }

        @Override
        protected void process() {
            if(onX && !CartesianEnvironmentRobotController.this.onX){
                getOnX();
            }else if(!onX && CartesianEnvironmentRobotController.this.onX){
                getOnY();
            }

            byte moveSignum = by >= 0 ? (byte)1 : (byte)-1;
            byte oneDeltaX = onX ? moveSignum : 0;
            byte oneDeltaY = !onX ? moveSignum : 0;
            byte steps = (byte) (by * moveSignum); //Absolute value of 'by'. The beauty of maths.

            for (byte i = 0; i < steps; i++) {
                if(moveByField(moveSignum)){
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
            return onX != CartesianEnvironmentRobotController.this.onX;
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
