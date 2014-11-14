package lego.nxt.controllers.util;

import lego.nxt.util.MotorController;
import lejos.nxt.TachoMotorPort;

/**
 * Hi
 * Created by Darkyen on 13.11.2014.
 */
public class DifferentialMotorManager {

    private static final boolean FLIP_DIRECTION = false;

    public static final float HALF_PI = (float) (Math.PI / 2.0);
    public static final float PI = (float) Math.PI;
    public static final float TWO_PI = (float) (Math.PI * 2.0);

    public static final float GRAD_TO_RAD = TWO_PI / 400f;

    private final MotorController leftMotor;
    private final MotorController rightMotor;

    private int leftMotorTarget = 0;
    private int rightMotorTarget = 0;
    private int leftMotorJob = 0;
    private int rightMotorJob = 0;
    private int leftMotorMovingDirection = 0;
    private int rightMotorMovingDirection = 0;

    public DifferentialMotorManager(TachoMotorPort leftMotorPort,TachoMotorPort rightMotorPort) {
        leftMotor = new MotorController(leftMotorPort);
        rightMotor = new MotorController(rightMotorPort);
        leftMotor.setSynchronizedMotor(rightMotor);
        rightMotor.setSynchronizedMotor(leftMotor);
    }

    public static float MAX_SPEED() {
        return MotorController.getMaxSpeed()*0.825f;
    }

    public static final float MAX_ACCELERATION = 9000;
    public static final float SMOOTH_ACCELERATION = 1000;
    public static final float NO_DECELERATION = MAX_ACCELERATION;

    public static final float wheelDiameterCM = 8.4f;
    public static final float wheelCircumferenceCM = PI * wheelDiameterCM;
    public static final float wheelDistanceCM = 7.05f;//Half distance between wheels

    public void go(boolean leftForward, boolean rightForward, float leftSpeed, float rightSpeed, float acceleration) {
        if (FLIP_DIRECTION) {
            leftForward = !leftForward;
            rightForward = !rightForward;
        }
        leftMotor.setAcceleration(acceleration);
        rightMotor.setAcceleration(acceleration);
        leftMotor.setSpeed(leftSpeed);
        rightMotor.setSpeed(rightSpeed);
        if (leftForward) {
            leftMotor.forward();
        } else {
            leftMotor.backward();
        }
        if (rightForward) {
            rightMotor.forward();
        } else {
            rightMotor.backward();
        }
    }

    public void turnRad(float angleRad, float speed, float acceleration, float deceleration,boolean hold) {
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
    public void move(float leftCM, float rightCM, float speed, float acceleration, float deceleration, boolean hold) {
        if (FLIP_DIRECTION) {
            leftCM = -leftCM;
            rightCM = -rightCM;
        }

        if (leftCM < rightCM) {
            rightMotor.setSpeed(speed);
            leftMotor.setSpeed((leftCM / rightCM) * speed);
        } else {
            leftMotor.setSpeed(speed);
            rightMotor.setSpeed((rightCM / leftCM) * speed);
        }
        leftMotor.setAcceleration(acceleration);
        rightMotor.setAcceleration(acceleration);
        leftMotor.setDeceleration(deceleration);
        rightMotor.setDeceleration(deceleration);

        int toMoveLeft = (int) (leftCM / wheelCircumferenceCM * 360);
        int toMoveRight = (int) (rightCM / wheelCircumferenceCM * 360);

        leftMotor.rotate(toMoveLeft, hold, true);
        rightMotor.rotate(toMoveRight, hold, true);

        leftMotor.waitComplete();
        rightMotor.waitComplete();
    }

    /**
     * @param leftCM       cm to move forward with left wheel
     * @param rightCM      cm to move forward with right wheel
     * @param speed        the max speed of any wheel during the movement (will be scaled down for slower moving wheel)
     * @param acceleration the acceleration with which the first part of movement will be performed
     * @param deceleration the acceleration with which the second part of movement will be performed
     * @param hold         whether motors should float after movement
     */
    public void moveAsync(float leftCM, float rightCM, float speed, float acceleration, float deceleration, boolean hold) {
        if (FLIP_DIRECTION) {
            leftCM = -leftCM;
            rightCM = -rightCM;
        }

        if (leftCM < rightCM) {
            rightMotor.setSpeed(speed);
            leftMotor.setSpeed((leftCM / rightCM) * speed);
        } else {
            leftMotor.setSpeed(speed);
            rightMotor.setSpeed((rightCM / leftCM) * speed);
        }
        leftMotor.setAcceleration(acceleration);
        rightMotor.setAcceleration(acceleration);
        leftMotor.setDeceleration(deceleration);
        rightMotor.setDeceleration(deceleration);

        int toMoveLeft = (int) (leftCM / wheelCircumferenceCM * 360);
        int toMoveRight = (int) (rightCM / wheelCircumferenceCM * 360);

        leftMotorTarget = leftMotor.getTachoCount() + toMoveLeft;
        rightMotorTarget = leftMotor.getTachoCount() + toMoveRight;
        leftMotorJob = Math.abs(toMoveLeft);
        rightMotorJob = Math.abs(toMoveRight);
        leftMotorMovingDirection = toMoveLeft > 0 ? 1 : 0;
        rightMotorMovingDirection = toMoveRight > 0 ? 1 : 0;

        leftMotor.rotate(toMoveLeft, hold, true);
        rightMotor.rotate(toMoveRight, hold, true);
    }

    public float asyncProgress(){
        if(asyncMoving()){
            int remainingLeft = (leftMotorTarget - leftMotor.getTachoCount()) * leftMotorMovingDirection;
            int remainingRight = (leftMotorTarget - leftMotor.getTachoCount()) * rightMotorMovingDirection;

            float leftMotorProgress = 1 - (remainingLeft / leftMotorJob);
            float rightMotorProgress = 1 - (remainingRight / rightMotorJob);

            return leftMotorProgress < rightMotorProgress ? leftMotorProgress : rightMotorProgress;
        }else{
            return 1f;
        }
    }

    public boolean asyncMoving(){
        return leftMotor.isMoving() || rightMotor.isMoving();
    }

    /**
     * Blocks until async move is complete.
     */
    public void completeAsync(){
        leftMotor.waitComplete();
        rightMotor.waitComplete();
    }

    public void stop(float deceleration) {
        leftMotor.setAcceleration(deceleration);
        rightMotor.setAcceleration(deceleration);
        leftMotor.stop(true);
        rightMotor.stop(false);
        leftMotor.waitComplete();
    }

    public void relax() {
        leftMotor.flt(true);
        rightMotor.flt(true);
    }
}
