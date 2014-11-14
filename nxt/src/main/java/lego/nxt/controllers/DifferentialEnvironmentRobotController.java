package lego.nxt.controllers;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.nxt.controllers.util.DifferentialMotorManager;
import lego.nxt.util.AbstractMoveTask;
import lego.nxt.util.TaskProcessor;
import lejos.nxt.*;
import lejos.util.Delay;

/**
 * Hi
 *
 * Created by Darkyen on 13.11.2014.
 */
@SuppressWarnings("UnusedDeclaration")
public final class DifferentialEnvironmentRobotController extends EnvironmentController {

    private static final DifferentialMotorManager motors = new DifferentialMotorManager(MotorPort.C,MotorPort.B);
    private static final MotorPort warningLight = MotorPort.A;
    private static final LightSensor leftLight = new LightSensor(SensorPort.S4);
    private static final UltrasonicSensor rightSonic = new UltrasonicSensor(SensorPort.S1);
    private static final TouchSensor frontTouch = new TouchSensor(SensorPort.S2);
    private static final TouchSensor backTouch = new TouchSensor(SensorPort.S3);

    private String lastError = "";
    private String sensorReadings = "";
    private byte warnings = 0;
    private boolean glows = false;

    // Sensor related
    private byte readingPointer = 0;
    private boolean highLight = true;
    private final byte READINGS = 8;
    private final int[] usReadings = new int[READINGS];
    private final int[] lowLightReadings = new int[READINGS];
    private final int[] highLightReadings = new int[READINGS];
    //

    @Override
    protected void initialize() {
        TaskProcessor.initialize();

        Thread debugViewThread = new Thread("DV"){
            @SuppressWarnings({"StatementWithEmptyBody", "ConstantConditions"})
            @Override
            public void run() {
                LCD.setAutoRefresh(false);
                while(!frontTouch.isPressed()){}
                while(frontTouch.isPressed()){}
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
                    LCD.drawString(lastError,mazeWidth+1,0);
                    LCD.drawString((short)(motors.asyncProgress()*100f)+"%   ",mazeWidth+1,1);
                    readSensors();
                    LCD.drawString(sensorReadings,0,mazeHeight+1);
                    if(glows){
                        LCD.drawChar('U',LCD.DISPLAY_CHAR_WIDTH-1,LCD.DISPLAY_CHAR_DEPTH-1);
                    }else{
                        LCD.drawChar(' ',LCD.DISPLAY_CHAR_WIDTH-1,LCD.DISPLAY_CHAR_DEPTH-1);
                    }

                    LCD.asyncRefresh();
                    glows = !glows;
                    if(warnings > 0){
                        warningLight.controlMotor(glows ? 100 : 0, BasicMotorPort.FORWARD);
                    }else{
                        warningLight.controlMotor(0,BasicMotorPort.FLOAT);
                    }
                    Delay.msDelay(200);
                }
            }
        };
        debugViewThread.setDaemon(true);
        debugViewThread.setPriority(Thread.MIN_PRIORITY);
        debugViewThread.start();

        Thread sensorReadingThread = new Thread("SR"){
            @Override
            public void run() {
                while(true){
                    usReadings[readingPointer] = rightSonic.getDistance();
                    if(highLight){
                        highLightReadings[readingPointer] = leftLight.getNormalizedLightValue();
                        highLight = false;
                    }else{
                        lowLightReadings[readingPointer] = leftLight.getNormalizedLightValue();
                        highLight = true;
                    }
                    leftLight.setFloodlight(highLight);
                    readingPointer += 1;
                    if(readingPointer == READINGS){
                        readingPointer = 0;
                    }
                    Delay.msDelay(100);
                }
            }
        };
        sensorReadingThread.setDaemon(true);
        sensorReadingThread.setPriority(Thread.NORM_PRIORITY);
        sensorReadingThread.start();
    }

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

    private Direction direction = Direction.DOWN;

    private void ensureDirection(Direction to){
        if(to == direction)return;
        warnings++;
        switch((direction.ordinal() - to.ordinal() + 4) % 4){//It's a kind of magic
            case 1:
                turnLeft();
                break;
            case 2:
                turnAround();
                break;
            case 3:
                turnRight();
                break;
            default:
                throw new Error();
        }
        warnings--;
        direction = to;
        readSensors();
    }

    private void turnLeft(){
        motors.turnRad(DifferentialMotorManager.HALF_PI,DifferentialMotorManager.MAX_SPEED(),DifferentialMotorManager.SMOOTH_ACCELERATION,DifferentialMotorManager.SMOOTH_ACCELERATION,true);
    }

    private void turnRight(){
        motors.turnRad(-DifferentialMotorManager.HALF_PI,DifferentialMotorManager.MAX_SPEED(),DifferentialMotorManager.SMOOTH_ACCELERATION,DifferentialMotorManager.SMOOTH_ACCELERATION,true);
    }

    private void turnAround(){
        motors.turnRad(DifferentialMotorManager.PI,DifferentialMotorManager.MAX_SPEED(),DifferentialMotorManager.SMOOTH_ACCELERATION,DifferentialMotorManager.SMOOTH_ACCELERATION,true);
    }

    private void readSensors(){
        int sonicSum = 0;
        int sonicCount = 0;
        int lowLightSum = 0;
        int highLightSum = 0;
        for (int i = 0; i < READINGS; i++) {
            int sonic = usReadings[i];
            if(sonic != 255){
                sonicSum += sonic;
                sonicCount++;
            }
            lowLightSum += lowLightReadings[i];
            highLightSum += highLightReadings[i];
        }
        int sonicReading = -1;
        if(sonicCount != 0){
            sonicReading = sonicSum / sonicCount;
        }
        int lightReading = (highLightSum-lowLightSum) / READINGS;

        //TODO Interpret and use the data
        sensorReadings = "U:"+sonicReading+" L:"+lightReading;
    }

    private static final float BLOCK_DISTANCE = 28.5f;
    private static final float BACKING_DISTANCE = BLOCK_DISTANCE*0.25f;

    private boolean driveForward(boolean accelerate,boolean decelerate){
        motors.moveAsync(BLOCK_DISTANCE,BLOCK_DISTANCE,DifferentialMotorManager.MAX_SPEED(),
                accelerate ? DifferentialMotorManager.SMOOTH_ACCELERATION : DifferentialMotorManager.MAX_ACCELERATION,
                decelerate ? DifferentialMotorManager.SMOOTH_ACCELERATION : DifferentialMotorManager.NO_DECELERATION, true);
        while(motors.asyncProgress() < 0.93){
            if(frontTouch.isPressed()){//&& motors.asyncProgress() > 0.7f //TODO
                warnings++;
                Delay.msDelay(200);
                motors.relax();
                motors.move(-BACKING_DISTANCE,-BACKING_DISTANCE,DifferentialMotorManager.MAX_SPEED(),DifferentialMotorManager.SMOOTH_ACCELERATION,DifferentialMotorManager.SMOOTH_ACCELERATION,false);
                warnings--;
                return false;
            }
        }
        return true;
    }

    @Override
    public MoveFieldsTask moveByXAsync(byte x) {
        MoveTask result;
        if(x < 0){
            result = new MoveTask(Direction.LEFT, (byte) -x);
        }else{
            result = new MoveTask(Direction.RIGHT,x);
        }
        TaskProcessor.appendTask(result);
        return result;
    }

    @Override
    public MoveFieldsTask moveByYAsync(byte y) {
        MoveTask result;
        if(y < 0){
            result = new MoveTask(Direction.UP, (byte) -y);
        }else{
            result = new MoveTask(Direction.DOWN,y);
        }
        TaskProcessor.appendTask(result);
        return result;
    }

    private class MoveTask extends AbstractMoveTask {
        private Direction direction;
        private byte amount;

        public MoveTask(Direction direction, byte amount) {
            this.direction = direction;
            this.amount = amount;
        }

        @Override
        protected void process() {
            ensureDirection(direction);
            while(moved < amount){
                if(driveForward(moved == 0,moved == amount - 1)){
                    moved += 1;
                    x += direction.x;
                    y += direction.y;
                    setField(x,y,FieldStatus.FREE_VISITED);
                    readSensors();
                }else{
                    setField((byte)(x+direction.x),(byte)(y+direction.y),FieldStatus.OBSTACLE);
                    break;
                }
            }
            doComplete();
        }
    }
}
