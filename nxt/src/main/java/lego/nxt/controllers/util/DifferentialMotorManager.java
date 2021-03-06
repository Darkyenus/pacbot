
package lego.nxt.controllers.util;

import lego.nxt.util.MotorController;
import lejos.nxt.MotorPort;

/** Manages movement of two motors in a tank-like fashion.
 *
 * Created by Darkyen on 13.11.2014. */
public final class DifferentialMotorManager {

	private static final boolean FLIP_DIRECTION = HWParameters.FLIP_DIRECTION;

	public static final float HALF_PI = (float)(Math.PI / 2.0);
	public static final float PI = (float)Math.PI;

	public final MotorController leftMotor;
	public final MotorController rightMotor;

	public DifferentialMotorManager (MotorPort leftMotorPort, MotorPort rightMotorPort) {
		leftMotor = new MotorController(leftMotorPort);
		rightMotor = new MotorController(rightMotorPort);
		reset();
	}

	public static float MAX_SPEED () {
		return MotorController.getMaxSpeed() * HWParameters.SPEED;
	}

	public static final float MAX_ACCELERATION = 9000;
	public static final float SMOOTH_ACCELERATION = HWParameters.ACCELERATION;
	public static final float NO_DECELERATION = MotorController.DONT_STOP;

	public static final float wheelDiameterCM = HWParameters.WHEEL_DIAMETER;
	public static final float wheelCircumferenceCM = PI * wheelDiameterCM;
	public static final float wheelDistanceCM = HWParameters.WHEEL_DISTANCE;// Half distance between wheels

	public void turnRad (float angleRad, float speed, float acceleration, float deceleration, boolean hold) {
		move(-wheelDistanceCM * angleRad, wheelDistanceCM * angleRad, speed, acceleration, deceleration, hold);
	}

	/** @param leftCM cm to move forward with left wheel
	 * @param rightCM cm to move forward with right wheel
	 * @param speed the max speed of any wheel during the movement (will be scaled down for slower moving wheel)
	 * @param acceleration the acceleration with which the first part of movement will be performed
	 * @param deceleration the acceleration with which the second part of movement will be performed
	 * @param hold whether motors should float after movement */
	public void move (float leftCM, float rightCM, float speed, float acceleration, float deceleration, boolean hold) {
		moveAsync(leftCM, rightCM, speed, acceleration, deceleration, hold);
		completeAsync();
	}

	/** @param leftCM cm to move forward with left wheel
	 * @param rightCM cm to move forward with right wheel
	 * @param speed the max speed of any wheel during the movement (will be scaled down for slower moving wheel)
	 * @param acceleration the acceleration with which the first part of movement will be performed
	 * @param deceleration the acceleration with which the second part of movement will be performed
	 * @param hold whether motors should float after movement */
	public void moveAsync (float leftCM, float rightCM, float speed, float acceleration, float deceleration, boolean hold) {
		if (FLIP_DIRECTION) {
			leftCM = -leftCM;
			rightCM = -rightCM;
		}
		float rightSpeed;
		float leftSpeed;

		if (leftCM < rightCM) {
			rightSpeed = speed;
			leftSpeed = (leftCM / rightCM) * speed;
		} else {
			leftSpeed = speed;
			rightSpeed = (rightCM / leftCM) * speed;
		}

		float toMoveLeft = leftMotor.permanentSavedLocation + (leftCM / wheelCircumferenceCM) * 360f;
		float toMoveRight = rightMotor.permanentSavedLocation + (rightCM / wheelCircumferenceCM) * 360f;

		leftMotor.permanentSavedLocation = toMoveLeft;
		rightMotor.permanentSavedLocation = toMoveRight;

		leftMotor.newMove(Math.abs(leftSpeed), acceleration, deceleration, toMoveLeft, hold, false);
		rightMotor.newMove(Math.abs(rightSpeed), acceleration, deceleration, toMoveRight, hold, false);
	}

	public float asyncProgress () {
		if (asyncMoving()) {
			return (leftMotor.getProgress() + rightMotor.getProgress()) * 0.5f;
		} else {
			return 1f;
		}
	}

	public boolean asyncMoving () {
		return leftMotor.isMoving() || rightMotor.isMoving();
	}

	public void waitForAsyncProgress (float minimalProgress) {
		while (asyncProgress() < minimalProgress) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ignored) {
			}
		}
	}

	/** Blocks until async move is complete. */
	public void completeAsync () {
		leftMotor.waitComplete();
		rightMotor.waitComplete();
	}

	/** Will stop and reset both motors. */
	public void reset () {
		leftMotor.stop(true);
		rightMotor.stop(false);
		leftMotor.stop(false);

		leftMotor.resetTachoCount(true);
		rightMotor.resetTachoCount(true);
	}
}
