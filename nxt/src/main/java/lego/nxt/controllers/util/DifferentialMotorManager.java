package lego.nxt.controllers.util;

import lego.nxt.util.LightMotorController;
import lejos.nxt.MotorPort;

/**
 * Hi
 * Created by Darkyen on 13.11.2014.
 */
public class DifferentialMotorManager {

    private static final boolean FLIP_DIRECTION = false;

    public static final float HALF_PI = (float) (Math.PI / 2.0);
    public static final float PI = (float) Math.PI;
    public static final float TWO_PI = (float) (Math.PI * 2.0);

    private final LightMotorController leftMotor;
    private final LightMotorController rightMotor;


    public DifferentialMotorManager(MotorPort leftMotorPort, MotorPort rightMotorPort) {
        leftMotor = new LightMotorController(leftMotorPort);
        rightMotor = new LightMotorController(rightMotorPort);
    }

    public static int MAX_SPEED() {
        return (int) (LightMotorController.getMaxSpeed()*0.820f);
    }

    public static final int MAX_ACCELERATION = 0;
    public static final int SMOOTH_ACCELERATION = 1000;
    public static final int NO_DECELERATION = 0;

    public static final float wheelDiameterCM = 8.2f;
    public static final float wheelCircumferenceCM = PI * wheelDiameterCM;
    public static final float wheelDistanceCM = 6.8f;//Half distance between wheels

    public void turnRad(float angleRad, int speed, int acceleration, int deceleration,boolean hold) {
        move(-wheelDistanceCM * angleRad, wheelDistanceCM * angleRad, speed, acceleration, deceleration, hold);
    }

    /**
     * @param leftCM       cm to move forward with left wheel
     * @param rightCM      cm to move forward with right wheel
     * @param speed        the max speed of any wheel during the movement (will be scaled down for slower moving wheel)
     * @param acceleration the acceleration with which the first part of movement will be performed
     * @param deceleration the acceleration with which the second part of movement will be performed
     * @param hold         whether motors should float after movement
     */
    public void move(float leftCM, float rightCM, int speed, int acceleration, int deceleration, boolean hold) {
        moveAsync(leftCM, rightCM, speed, acceleration, deceleration, hold);
        completeAsync();
    }

    /**
     * @param leftCM       cm to move forward with left wheel
     * @param rightCM      cm to move forward with right wheel
     * @param speed        the max speed of any wheel during the movement (will be scaled down for slower moving wheel)
     * @param acceleration the acceleration with which the first part of movement will be performed
     * @param deceleration the acceleration with which the second part of movement will be performed
     * @param hold         whether motors should float after movement
     */
    public void moveAsync(float leftCM, float rightCM, int speed, int acceleration, int deceleration, boolean hold) {
        if (FLIP_DIRECTION) {
            leftCM = -leftCM;
            rightCM = -rightCM;
        }

        float rightSpeed;
        float leftSpeed;
        if (leftCM < rightCM) {
            rightSpeed = speed;
            leftSpeed = Math.abs(leftCM / rightCM) * speed;
        } else {
            rightSpeed = Math.abs(rightCM / leftCM) * speed;
            leftSpeed = speed;
        }

        int toMoveLeft = (int) ((leftCM / wheelCircumferenceCM) * 360);
        int toMoveRight = (int) ((rightCM / wheelCircumferenceCM) * 360);

        leftMotor.moveBy(toMoveLeft, (int) (leftSpeed + 0.5f), acceleration, deceleration, hold);
        rightMotor.moveBy(toMoveRight, (int) (rightSpeed + 0.5f), acceleration, deceleration, hold);
    }

    /**
     * @return Progress in 0-1000 range
     */
    public int asyncProgress(){
        if(asyncMoving()){
            return (leftMotor.getProgress() + rightMotor.getProgress()) / 2;
        }else{
            return 1000;
        }
    }

    public boolean asyncMoving(){
        return leftMotor.isMoving() || rightMotor.isMoving();
    }

    /**
     * Blocks until async move is complete.
     */
    public void completeAsync(){
        leftMotor.waitForComplete();
        rightMotor.waitForComplete();
    }

    public void reset() {
        leftMotor.reset();
        rightMotor.reset();
    }
}
