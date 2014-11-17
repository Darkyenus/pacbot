package lego.nxt.controllers;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.nxt.util.LightMotorController;
import lego.nxt.util.AbstractMoveTask;
import lego.nxt.util.TaskProcessor;
import lejos.nxt.*;
import lejos.util.Delay;

/**
 *
 * README:
 * When starting a program using this, the robot should be on X axis wheels, that means, on big wheels.
 *
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 12:11
 */
public class CartesianEnvironmentRobotController extends EnvironmentController {

    private static final int DEFAULT_SPEED = 800;
    private static final int BACKING_SPEED = 250;
    private static final LightMotorController xMotor = new LightMotorController(MotorPort.B);
    private static final LightMotorController yMotor = new LightMotorController(MotorPort.C);
    private static final LightMotorController axisMotor = new LightMotorController(MotorPort.A);

    private static final TouchSensor xTouch = new TouchSensor(SensorPort.S1);
    private static final TouchSensor yTouch = new TouchSensor(SensorPort.S2);
    private static final LightSensor detector = new LightSensor(SensorPort.S4);

    private final byte READINGS = 8;
    private byte lowLightPointer = 0;
    private int lowLightSum = 0;
    private final int[] lowLightReadings = new int[READINGS];
    private byte highLightPointer = 0;
    private int highLightSum = 0;
    private final int[] highLightReadings = new int[READINGS];

    private static final boolean defaultOnX = true;
    private static boolean onX = defaultOnX;

    @Override
    protected void initialize() {
        TaskProcessor.initialize();

        LCD.drawString("Press ENTER to", 2, 3);
        LCD.drawString("get ready (on Y)", 1, 4);
        Button.ENTER.waitForPressAndRelease();
        LCD.clear();
        getOnY();

        Thread debugViewThread = new Thread(){
            @Override
            public void run() {
                LCD.setAutoRefresh(false);
                while(!yTouch.isPressed()){}
                while(yTouch.isPressed()){}
                Delay.msDelay(500);
                Bot.active.onEvent(BotEvent.RUN_STARTED);

                //noinspection InfiniteLoopStatement
                while(true){
                    if(Button.ESCAPE.isDown()){
                        Bot.active.onEvent(BotEvent.ESCAPE_PRESSED);
                        Bot.active.onEvent(BotEvent.RUN_ENDED);
                    }
                    for (byte x = 0; x < mazeWidth; x++) {
                        for (byte y = 0; y < mazeHeight; y++) {
                            LCD.drawString(maze[x][y].displayChar, x, y, getX() == x && getY() == y);
                        }
                    }
                    LCD.drawString("H: "+TaskProcessor.getStackHead()+"   ",0,mazeHeight);
                    LCD.drawString(lastError,mazeWidth+1,0);
                    LCD.asyncRefresh();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {}
                }
            }
        };
        debugViewThread.setDaemon(true);
        debugViewThread.setPriority(Thread.MIN_PRIORITY);
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

    private String lastError = "";

    @Override
    public void onError(byte error) {
        switch (error){
            case ERROR_SET_DEFINITIVE:
                lastError = "D"+x+" "+y;
                break;
            case ERROR_SET_OUT_OF_BOUNDS:
                if(x < 0){
                    x = 0;
                }else if(x >= EnvironmentController.mazeWidth){
                    x = EnvironmentController.mazeWidth - 1;
                }
                if(y < 0){
                    y = 0;
                }else if(y >= EnvironmentController.mazeHeight){
                    y = EnvironmentController.mazeHeight - 1;
                }
                lastError = "OOB";
                break;
            default:
                lastError = "!"+error;
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

        detector.setFloodlight(true);
        Delay.msDelay(50); //Give chance to light to move to block and back. You know, light is very slow
        int highLightReading = detector.getNormalizedLightValue();
        Delay.msDelay(50);
        detector.setFloodlight(false);
        Delay.msDelay(50);
        int lowLightReading = detector.getNormalizedLightValue();
        Delay.msDelay(50);

        int delta = highLightReading - lowLightReading;
        boolean isBlock;

        if(delta > 50){
            isBlock = true;
        }else{
            isBlock = false;
        }

        LCD.drawString(delta+" l  ",0,mazeHeight+1);//Hijacking debug loop rendering

        //TODO Interpret read value and make decisions (write result)
        //Sound.beep(); //We can sing too. Beep.
    }

    private final int AXIS_CHANGE_DEGREES = 450;
    private final int AXIS_CHANGE_SPEED = 800;
    private final int AXIS_CHANGE_ACCELERATION = 9000;

    private void getOnX(){
        axisMotor.moveBy(AXIS_CHANGE_DEGREES, AXIS_CHANGE_SPEED, AXIS_CHANGE_ACCELERATION, AXIS_CHANGE_ACCELERATION, true);
        axisMotor.waitForComplete();
        CartesianEnvironmentRobotController.onX = true;
        doDetectorReading();
    }

    private void getOnY(){
        axisMotor.moveBy(-AXIS_CHANGE_DEGREES, AXIS_CHANGE_SPEED, AXIS_CHANGE_ACCELERATION, AXIS_CHANGE_ACCELERATION, true);
        axisMotor.waitForComplete();
        CartesianEnvironmentRobotController.onX = false;
        doDetectorReading();
    }

    /**
     * Contains everything needed to process a single moving task by given amount of fields on given axis.
     * It is a little bit messy because of supporting both axis.
     * This also updates robot x, y and maze fields, but only using data available - that means touch sensor in front.
     */
    private class MoveTask extends AbstractMoveTask {

        public static final int X_FIELD_DISTANCE = 382;
        public static final int Y_FIELD_DISTANCE = 585;

        public static final int X_ACCELERATION = 1000;
        public static final int Y_ACCELERATION = 1500;
        public static final int MAX_ACCELERATION = 6000;

        private final boolean onX;
        private final byte by;

        private MoveTask(boolean onX, byte by) {
            this.onX = onX;
            this.by = by;
        }

        private boolean moveByField(byte directionSign, boolean accelerate, boolean decelerate){
            LightMotorController motor = onX ? xMotor : yMotor;
            TouchSensor touch = onX ? xTouch : yTouch;
            boolean returningFromWall = false;
            final boolean nextStationery = isNextStationery();
            final int acceleration = nextStationery ? (onX ? X_ACCELERATION : Y_ACCELERATION) : MAX_ACCELERATION;

            final int tachoTarget = (onX ? X_FIELD_DISTANCE : Y_FIELD_DISTANCE)*directionSign;
            motor.moveBy(tachoTarget,DEFAULT_SPEED,accelerate ? acceleration : 0,decelerate ? acceleration : 0,!decelerate);

            while(motor.getProgress() < 960 && !returningFromWall){
                if(touch.isPressed() && motor.getProgress() < 500){
                    //collision
                    motor.moveBy(3000*directionSign, BACKING_SPEED,1500,0,false);
                    try {
                        Thread.sleep(1000); //Give motor thread a bit of breathing space
                    } catch (InterruptedException ignored) {}
                    motor.reset();

                    final int backingAcceleration = (onX ? X_ACCELERATION : Y_ACCELERATION) / 2;
                    motor.moveBy(((onX ? X_FIELD_DISTANCE : Y_FIELD_DISTANCE)* -directionSign)/4 ,BACKING_SPEED,backingAcceleration,backingAcceleration,true);
                    motor.waitForComplete();
                    returningFromWall = true;
                }else{
                    try {
                        Thread.sleep(50); //Give motor thread a bit of breathing space
                    } catch (InterruptedException ignored) {}
                }
            }/*
            motor.setSpeed(DEFAULT_SPEED);
            if(!returningFromWall){
                if(!decelerate){
                    if(directionSign > 0){
                        motor.forward();
                    }else{
                        motor.backward();
                    }
                }else{
                    motor.stop(true);
                }
            }*/
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
                if(moveByField(moveSignum,i == 0,i+1 == steps)){
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
}
