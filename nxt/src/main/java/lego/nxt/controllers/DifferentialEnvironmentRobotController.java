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
    private static final LightSensor leftLight = new LightSensor(SensorPort.S3);
    private static final UltrasonicSensor rightSonic = new UltrasonicSensor(SensorPort.S4);
    private static final TouchSensor frontTouch = new TouchSensor(SensorPort.S1);
    private static final TouchSensor backTouch = new TouchSensor(SensorPort.S2);

    private String lastError = "";
    private int displayLightReadings = 0;
    private int displaySonicReadings = 0;
    private byte warnings = 0;
    private boolean glows = false;

    private byte lastMoved = 0;

    // Sensor related
    private boolean highLight = true;
    private final byte READINGS = 8;
    private byte sonicPointer = 0;
    private int sonicSum = 0;
    private final int[] sonicReadings = new int[READINGS];
    private byte lowLightPointer = 0;
    private int lowLightSum = 0;
    private final int[] lowLightReadings = new int[READINGS];
    private byte highLightPointer = 0;
    private int highLightSum = 0;
    private final int[] highLightReadings = new int[READINGS];

    private final byte LIGHT_THRESHOLD = 50;
    private final byte SONIC_BLOCK_SIZE = 28;
    //

    @Override
    protected void initialize() {
        TaskProcessor.initialize();

        Thread debugViewThread = new Thread("DV"){
            @SuppressWarnings({"StatementWithEmptyBody", "ConstantConditions"})
            @Override
            public void run() {
                LCD.setAutoRefresh(false);
                Sound.beep();
                while(!frontTouch.isPressed()){}
                Sound.beepSequenceUp();
                while(frontTouch.isPressed()){}
                Delay.msDelay(500);
                Sound.beepSequence();
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
                    LCD.drawString(motors.asyncProgress()+"%  ",mazeWidth+1,1);
                    //readSensors();
                    LCD.drawString("L:",0,mazeHeight+1);
                    LCD.drawInt(displayLightReadings,3,mazeHeight+1);
                    LCD.drawString("S:",0,mazeHeight+2);
                    LCD.drawInt(displaySonicReadings,3,mazeHeight+2);

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
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {}
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
                    int sonicDistance = rightSonic.getDistance();
                    if(sonicDistance != 255){
                        sonicSum -= sonicReadings[sonicPointer];
                        sonicReadings[sonicPointer] = sonicDistance;
                        sonicSum += sonicDistance;
                        sonicPointer++;
                        if(sonicPointer == READINGS){
                            sonicPointer = 0;
                        }
                    }
                    int lightReading = leftLight.getNormalizedLightValue();
                    if(highLight){
                        highLightSum = highLightSum - highLightReadings[highLightPointer] + lightReading;
                        highLightReadings[highLightPointer] = lightReading;
                        highLight = false;
                        highLightPointer++;
                        if(highLightPointer == READINGS){
                            highLightPointer = 0;
                        }
                    }else{
                        lowLightSum = lowLightSum - lowLightReadings[lowLightPointer] + lightReading;
                        lowLightReadings[lowLightPointer] = lightReading;
                        highLight = true;
                        lowLightPointer++;
                        if(lowLightPointer == READINGS){
                            lowLightPointer = 0;
                        }
                    }
                    leftLight.setFloodlight(highLight);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {}
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
            case ERROR_CAL_BLOCK_EXPECTED:
                lastError = "cX";
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
                if(lastMoved > 3 && getField((byte)(x - to.x),(byte)(y - to.y)) == FieldStatus.OBSTACLE){
                    calibrateBackward();
                }
                break;
            case 2:
                turnAround();
                if(getField((byte)(x - to.x),(byte)(y - to.y)) == FieldStatus.OBSTACLE){
                    calibrateBackward();
                }
                break;
            case 3:
                turnRight();
                if(lastMoved > 3 && getField((byte)(x - to.x),(byte)(y - to.y)) == FieldStatus.OBSTACLE){
                    calibrateBackward();
                }
                break;
            default:
                throw new Error();
        }
        warnings--;
        direction = to;
        lastMoved = 0;
        readSensors();
    }

    private void turnLeft(){
        motors.turnRad(DifferentialMotorManager.HALF_PI,DifferentialMotorManager.MAX_SPEED()/10,DifferentialMotorManager.SMOOTH_ACCELERATION,DifferentialMotorManager.SMOOTH_ACCELERATION,true);
    }

    private void turnRight(){
        motors.turnRad(-DifferentialMotorManager.HALF_PI,DifferentialMotorManager.MAX_SPEED()/10,DifferentialMotorManager.SMOOTH_ACCELERATION,DifferentialMotorManager.SMOOTH_ACCELERATION,true);
    }

    private void turnAround(){
        motors.turnRad(DifferentialMotorManager.PI,DifferentialMotorManager.MAX_SPEED()/10,DifferentialMotorManager.SMOOTH_ACCELERATION,DifferentialMotorManager.SMOOTH_ACCELERATION,true);
    }

    private void readSensors(){
        Direction left = direction.left;
        byte leftX = (byte) (x+left.x);
        byte leftY = (byte) (y+left.y);

        displayLightReadings = (highLightSum-lowLightSum) / READINGS;
        if(displayLightReadings > LIGHT_THRESHOLD){
        //    setField(leftX, leftY, FieldStatus.OBSTACLE);
        }else{
        //    if(getField(leftX, leftY) != FieldStatus.FREE_VISITED && getField(leftX, leftY) != FieldStatus.START)
        //        setField(leftX, leftY, FieldStatus.FREE_UNVISITED);
        }

        Direction right = direction.right;

        displaySonicReadings = sonicSum / READINGS;

        if(displaySonicReadings != 255) { //Invalid measurement, can mean either too far away or too close.

            for (int i = 1; displaySonicReadings - i * SONIC_BLOCK_SIZE > 0; i++) {
                byte rightX = (byte) (x + right.x * i);
                byte rightY = (byte) (y + right.y * i);
        //        if(getField(rightX, rightY) != FieldStatus.FREE_VISITED && getField(leftX, leftY) != FieldStatus.START)
        //            setField(rightX, rightY, FieldStatus.FREE_UNVISITED);
            }
        //    setField((byte) (x + right.x), (byte) (y + right.y), FieldStatus.OBSTACLE);
        }

        // sensors should work now.
    }

    private static final float BLOCK_DISTANCE = 28.5f;//28.5 cm
    private static final float BACKING_DISTANCE = BLOCK_DISTANCE / 10;

    private boolean driveForward(boolean accelerate,boolean decelerate){
        motors.moveAsync(BLOCK_DISTANCE,BLOCK_DISTANCE,DifferentialMotorManager.MAX_SPEED(),
                accelerate ? DifferentialMotorManager.SMOOTH_ACCELERATION : DifferentialMotorManager.MAX_ACCELERATION,
                decelerate ? DifferentialMotorManager.SMOOTH_ACCELERATION : DifferentialMotorManager.NO_DECELERATION, decelerate);
        while(motors.asyncProgress() < 950){
            if(frontTouch.isPressed() && motors.asyncProgress() > 700){
                warnings++;
                Delay.msDelay(400);
                motors.reset();
                motors.move(-BACKING_DISTANCE, -BACKING_DISTANCE, DifferentialMotorManager.MAX_SPEED() / 3,
                        DifferentialMotorManager.MAX_ACCELERATION, DifferentialMotorManager.NO_DECELERATION, true);
                warnings--;
                return false;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
        return true;
    }

    private void calibrateBackward(){
        Sound.beepSequence();
        warnings++;
        motors.moveAsync(-BLOCK_DISTANCE,-BLOCK_DISTANCE,DifferentialMotorManager.MAX_SPEED(),
                DifferentialMotorManager.SMOOTH_ACCELERATION,
                DifferentialMotorManager.SMOOTH_ACCELERATION,
                true);

        while(motors.asyncProgress() < 1){
            if(backTouch.isPressed()){

                Delay.msDelay(400);
                motors.reset();
                motors.move(BACKING_DISTANCE,BACKING_DISTANCE,DifferentialMotorManager.MAX_SPEED() / 8,
                        DifferentialMotorManager.MAX_ACCELERATION,DifferentialMotorManager.NO_DECELERATION,false);
                warnings--;
                return;
            }
        }
        onError(ERROR_CAL_BLOCK_EXPECTED);
        motors.moveAsync(BLOCK_DISTANCE,BLOCK_DISTANCE,DifferentialMotorManager.MAX_SPEED(),
                DifferentialMotorManager.SMOOTH_ACCELERATION,
                DifferentialMotorManager.NO_DECELERATION,
                true);
        while(motors.asyncProgress() < 0.95f){
            Delay.msDelay(50);
        }
        warnings--;
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
            lastMoved = moved;
            doComplete();
        }
    }
}
