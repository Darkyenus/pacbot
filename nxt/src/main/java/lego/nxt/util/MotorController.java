
package lego.nxt.util;

import lejos.nxt.Battery;
import lejos.nxt.MotorPort;
import lejos.nxt.TachoMotorPort;
import lejos.util.Delay;

/** Used to control single motor. Better than the default one. (Controller) But I don't remember how.
 *
 * Private property. User: Darkyen Date: 11/12/13 Time: 07:20 */
@SuppressWarnings("UnusedDeclaration")
public final class MotorController {

	public static final float DONT_STOP = Float.POSITIVE_INFINITY;
	protected static final int NO_LIMIT = Integer.MAX_VALUE;
	protected MotorPort tachoPort;
	protected boolean stalled = false;
	protected float speed = 360;
	protected float acceleration = 6000;
	protected float deceleration = 6000;
	protected int stallLimit = 50;
	protected int stallTime = 1000;
	protected static final Controller cont = new Controller();

	/** This HAS TO BE CALLED BEFORE ISSUING ANY COMMANDS, WILL CRASH OTHERWISE */
	public static void startWheelControl () {
		// Start the single controller thread
		cont.setPriority(Thread.MAX_PRIORITY);
		cont.setDaemon(true);
		cont.start();
		// Add shutdown handler to stop the motors
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run () {
				cont.shutdown();
			}
		});
	}

	/** Use this constructor to assign a variable of type motor connected to a particular port.
	 *
	 * @param port to which this motor is connected */
	public MotorController (MotorPort port) {
		tachoPort = port;
		port.setPWMMode(TachoMotorPort.PWM_BRAKE);
		cont.motors[port.getId()] = this;
	}

	/** Removes this motor from the motor regulation system. After this call the motor will be in float mode and will have stopped.
	 * Note calling any of the high level move operations (forward, rotate etc.), will automatically enable regulation.
	 *
	 * @return true iff regulation has been suspended. */
	public synchronized boolean suspendRegulation (boolean waitForSuspension) {
		// Putting the motor into float mode disables regulation. note
		// that we wait for the operation to complete.
		newMove(0, acceleration, deceleration, NO_LIMIT, false, true);
		// Now wait for the motor to become inactive
		if (waitForSuspension) {
			while (cont.activeMotors[tachoPort.getId()])
				Delay.msDelay(1);
			return true;
		} else
			return !cont.activeMotors[tachoPort.getId()];
	}

	/** @return the current tachometer count.
	 * @see lejos.robotics.RegulatedMotor#getTachoCount() */
	public int getTachoCount () {
		return tachoPort.getTachoCount();
	}

	/** Returns the current position that the motor regulator is trying to maintain. Normally this will be the actual position of
	 * the motor and will be the same as the value returned by getTachoCount(). However in some circumstances (motors that are in
	 * the process of stalling, or motors that have been forced out of position), the two values may differ. Note this value is not
	 * valid if regulation has been terminated.
	 *
	 * @return the current position calculated by the regulator. */
	public float getPosition () {
		return currentTCount;
	}

	/** @see lejos.nxt.BasicMotor#forward() */
	public synchronized void forward () {
		newMove(speed, acceleration, deceleration, +NO_LIMIT, true, false);
	}

	/** @see lejos.nxt.BasicMotor#backward() */
	public synchronized void backward () {
		newMove(speed, acceleration, deceleration, -NO_LIMIT, true, false);
	}

	/** Set the motor into float mode. This will stop the motor without braking and the position of the motor will not be
	 * maintained.
	 *
	 * @param immediateReturn If true do not wait for the motor to actually stop */
	public synchronized void flt (boolean immediateReturn) {
		newMove(0, acceleration, deceleration, NO_LIMIT, false, !immediateReturn);
	}

	/** Causes motor to stop, pretty much instantaneously. In other words, the motor doesn't just stop; it will resist any further
	 * motion. Cancels any rotate() orders in progress
	 *
	 * @param immediateReturn if true do not wait for the motor to actually stop */
	public synchronized void stop (boolean immediateReturn) {
		newMove(0, acceleration, deceleration, NO_LIMIT, true, !immediateReturn);
	}

	/** This method returns <b>true </b> if the motor is attempting to rotate. The return value may not correspond to the actual
	 * motor movement.<br>
	 * For example, If the motor is stalled, isMoving() will return <b> true. </b><br>
	 * After flt() is called, this method will return <b>false</b> even though the motor axle may continue to rotate by inertia. If
	 * the motor is stalled, isMoving() will return <b> true. </b> . A stall can be detected by calling {@link #isStalled()};
	 *
	 * @return true iff the motor is attempting to rotate.<br> */
	public boolean isMoving () {
		return moving;
	}

	/** Wait until the current movement operation is complete (this can include the motor stalling). */
	public synchronized void waitComplete () {
		while (moving) {
			try {
				wait();
			} catch (InterruptedException ignored) {
			}
		}
	}

	public synchronized void rotateTo (float limitAngle, boolean hold, boolean immediateReturn) {
		newMove(speed, acceleration, deceleration, limitAngle, hold, !immediateReturn);
	}

	/** Sets desired motor speed , in degrees per second; The maximum reliably sustainable velocity is 100 x battery voltage under
	 * moderate load, such as a direct drive robot on the level.
	 *
	 * @param speed value in degrees/sec */
	public synchronized void setSpeed (float speed) {
		this.speed = Math.abs(speed);
		adjustSpeed(this.speed);
	}

	/** sets the acceleration rate of this motor in degrees/sec/sec <br>
	 * The default value is 6000; Smaller values will make speeding up at the end of a rotate() task, smoother.
	 *
	 * @param acceleration to set */
	public synchronized void setAcceleration (float acceleration) {
		this.acceleration = Math.abs(acceleration);
		adjustAcceleration(this.acceleration);
	}

	/** Returns acceleration in degrees/second/second
	 *
	 * @return the value of acceleration */
	public float getAcceleration () {
		return acceleration;
	}

	/** Sets the deceleration rate of this motor in degrees/sec/sec <br>
	 * The default value is 6000; Smaller values will make stopping at the end of a rotate() task, smoother.
	 *
	 * @param deceleration to set */
	public synchronized void setDeceleration (float deceleration) {
		this.deceleration = Math.abs(deceleration);
		adjustAcceleration(this.deceleration);
	}

	/** Returns deceleration in degrees/second/second
	 *
	 * @return the value of deceleration */
	public float getDeceleration () {
		return deceleration;
	}

	/** Reset the tachometer associated with this motor. Note calling this method will cause any current move operation to be
	 * halted. */
	public synchronized void resetTachoCount (boolean waitForCompleteHalt) {
		// Make sure we are stopped!
		newMove(0, acceleration, deceleration, NO_LIMIT, true, waitForCompleteHalt);
		tachoPort.resetTachoCount();
		resetRelativeTachoCount();
	}

	/** Rotate by the request number of degrees.
	 *
	 * @param angle number of degrees to rotate relative to the current position
	 * @param immediateReturn if true do not wait for the move to complete */
	public synchronized void rotate (float angle, boolean hold, boolean immediateReturn) {
		rotateTo(currentTCount + angle, hold, immediateReturn);
	}

	/** Return the current target speed.
	 *
	 * @return the current target speed. */
	public float getSpeed () {
		return speed;
	}

	/** Return true if the motor is currently stalled.
	 *
	 * @return true if the motor is stalled, else false */
	public boolean isStalled () {
		return stalled;
	}

	/** Set the parameters for detecting a stalled motor. A motor will be recognised as stalled if the movement error (the amount
	 * the motor lags the regulated position) is greater than error for a period longer than time.
	 *
	 * @param error The error threshold
	 * @param time The time that the error threshold needs to be exceeded for. */
	public synchronized void setStallThreshold (int error, int time) {
		this.stallLimit = error;
		this.stallTime = time / Controller.UPDATE_PERIOD;
	}

	/** Return the current velocity.
	 *
	 * @return current velocity in degrees/s */
	public float getCurrentSpeed () {
		return currentVelocity;
	}

	/** Inner class to regulate velocity; also stop motor at desired rotation angle. This class uses a very simple movement model
	 * based on simple linear acceleration. This model is used to generate ideal target positions which are then used to generate
	 * error terms between the actual and target position this error term is then used to drive a PID style motor controller to
	 * regulate the power supplied to the motor.
	 * <p/>
	 * If new command are issued while a move is in progress, the new command is blended with the current one to provide smooth
	 * movement.
	 * <p/>
	 * If the requested speed is not possible then the controller will simply drop move cycles until the motor catches up with the
	 * ideal position. If too many consecutive dropped moves are required then the motor is viewed to have stalled and the move is
	 * terminated.
	 * <p/>
	 * Once the motor stops, the final position is held using the same PID control mechanism (with slightly different parameters),
	 * as that used for movement. */

	// PID constants for move and for hold
	private static final float MOVE_P = 6f;
	private static final float MOVE_I = 0.04f;
	private static final float MOVE_D = 22f;

	private static final float HOLD_P = 2f;
	private static final float HOLD_I = 0.04f;
	private static final float HOLD_D = 8f;

	private float basePower = 0; // used to calculate power, but only in PID control
	private float err1 = 0; // used in smoothing
	private float err2 = 0; // used in smoothing
	private float currentVelocity = 0;
	/** Is currentVelocity at the start of sub move */
	private float baseVelocity = 0;
	/** This is currentTCount at the start of sub move. */
	private float baseTCount = 0;
	/** Position in which motor should ideally be. Real position is cached in tachoTCount */
	private float currentTCount = 0;
	private float currentAcceleration = 0;
	private float currentDeceleration = 0;
	private float currentTargetVelocity = 0;

	/** Can be used by controllers to set some consistent location, that isn't broken by various calls. Only resetRelativeTachoCount
	 * resets this value to current tacho count. */
	public float permanentSavedLocation = 0;

	/** Absolute degrees to which is motor trying to get. */
	private float currentLimit = NO_LIMIT;
	private boolean currentHold = true;
	private float accelerationTCount = 0;
	private long baseTime = 0;
	private long now = 0;
	private long accelerationTime = 0;
	private boolean moving = false;
	/** Contains raw, Controller updated value of motor's tacho count. */
	private int tachoTCount;
	/** Outgoing value, motor will be set to this power on every Controller update. */
	private int power;
	/** See power above. */
	private int mode;

	private int stallCount = 0;

	// ----------------------------------------------- Pending
	private boolean pending = false;
	private boolean checkLimit = false;
	private float pendingSpeed = 0;
	private float pendingAcceleration = 0;
	private float pendingDeceleration = 0;
	private float pendingLimit = 0;
	private boolean pendingHold = true;

	/** SHOULD Return value 0 to 1. Probably does. */
	public float getProgress () {
		if (moving) {
			if(currentLimit == baseTCount)return 1f;
			return ((currentTCount - baseTCount) / (currentLimit - baseTCount));
		} else {
			return 1f;
		}
	}

	/** Reset the tachometer readings */
	public synchronized void resetRelativeTachoCount () {
		permanentSavedLocation = currentTCount = tachoTCount = tachoPort.getTachoCount();
		now = System.currentTimeMillis();
	}

	/** Helper method. Start a sub move operation. A sub move consists of acceleration/deceleration to a set velocity and then
	 * holding that velocity up to an optional limit point. If a limit point is set this method will be called again to initiate a
	 * controlled deceleration to that point
	 *
	 * @param speed to reach
	 * @param acceleration with which accelerate
	 * @param limit how far to go
	 * @param hold at the end ? */
	private synchronized void startSubMove (float speed, float acceleration, float deceleration, float limit, boolean hold) {
		checkLimit = Math.abs(limit) != NO_LIMIT;
		baseTime = now;
		currentTargetVelocity = (limit - currentTCount >= 0 ? speed : -speed);
		currentAcceleration = currentTargetVelocity - currentVelocity >= 0 ? Math.abs(acceleration) : -Math.abs(acceleration);
		currentDeceleration = (currentTargetVelocity >= 0 ? Math.abs(deceleration) : -Math.abs(deceleration));
		accelerationTime = Math.round(((currentTargetVelocity - currentVelocity) / currentAcceleration) * 1000);
		accelerationTCount = (currentVelocity + currentTargetVelocity) * accelerationTime / (2 * 1000);
		baseTCount = currentTCount;
		baseVelocity = currentVelocity;
		currentHold = hold;
		currentLimit = limit;
		moving = currentTargetVelocity != 0 || baseVelocity != 0;
	}

	/** Helper method, if move is currently active wait for it to be completed */
	private void waitStop () {
		if (moving) try {
			wait();
		} catch (Exception ignored) {
		}
	}

	/** Initiate a new move and optionally wait for it to complete. If some other move is currently executing then ensure that this
	 * move is terminated correctly and then start the new move operation.
	 *
	 * @param speed speed
	 * @param acceleration with which accelerate
	 * @param deceleration speed of slowing down. May be infinite, then never slow down and never stop.
	 * @param limit of how far to go
	 * @param hold at the end ? This has no limit when deceleration is NO_DECELERATION (infinite)
	 * @param waitComplete should block? */
	@SuppressWarnings("SpellCheckingInspection")
	public synchronized void newMove (float speed, float acceleration, float deceleration, float limit, boolean hold,
		boolean waitComplete) {
		if (!cont.activeMotors[tachoPort.getId()]) {
			resetRelativeTachoCount();
			cont.activeMotors[tachoPort.getId()] = true;
		}
		// ditch any existing pending command
		pending = false;
		// Stop moves always happen now
		if (speed == 0)
			startSubMove(0, acceleration, deceleration, NO_LIMIT, hold);
		else if (!moving) {
			// not moving so we start a new move
			startSubMove(speed, acceleration, deceleration, limit, hold);
			this.stalled = false;
		} else {
			// we already have a move in progress can we modify it to match
			// the new request? We must ensure that the new move is in the
			// same direction and that any stop will not exceed the current
			// acceleration request.
			float moveLen = limit - currentTCount;
			float acc = (currentVelocity * currentVelocity) / (2 * (moveLen));
			if (moveLen * currentVelocity >= 0 && Math.abs(acc) <= acceleration)
				startSubMove(speed, acceleration, deceleration, limit, hold);
			else {
				// Save the requested move
				pendingSpeed = speed;
				pendingAcceleration = acceleration;
				pendingDeceleration = deceleration;
				pendingLimit = limit;
				pendingHold = hold;
				pending = true;
				// stop the current move
				startSubMove(0, acceleration, deceleration, NO_LIMIT, true);
			}
		}
		if (waitComplete) waitStop();
	}

	/** The target speed has been changed. Reflect this change in the regulator.
	 *
	 * @param newSpeed new target speed. */
	private synchronized void adjustSpeed (float newSpeed) {
		if (currentTargetVelocity != 0) {
			startSubMove(newSpeed, currentAcceleration, currentDeceleration, currentLimit, currentHold);
		}
		if (pending) this.pendingSpeed = newSpeed;
	}

	/** The target acceleration has been changed. Updated the regulator.
	 *
	 * @param newAcceleration to set */
	private synchronized void adjustAcceleration (float newAcceleration) {
		if (currentTargetVelocity != 0) {
			startSubMove(Math.abs(currentTargetVelocity), newAcceleration, currentDeceleration, currentLimit, currentHold);
		}
		if (pending) this.pendingAcceleration = newAcceleration;
	}

	/** The target deceleration has been changed. Updated the regulator.
	 *
	 * @param newDeceleration to set */
	private synchronized void adjustDeceleration (float newDeceleration) {
		if (currentTargetVelocity != 0) {
			startSubMove(Math.abs(currentTargetVelocity), currentAcceleration, newDeceleration, currentLimit, currentHold);
		}
		if (pending) this.pendingDeceleration = newDeceleration;
	}

	/** The move has completed either by the motor stopping or by it stalling
	 *
	 * @param stalled whether is stalled */
	private synchronized void endMove (boolean stalled) {
		moving = pending;
		this.stalled = stalled;
		if (stalled) {
			// stalled try and maintain current position
			resetRelativeTachoCount();
			currentVelocity = 0;
			stallCount = 0;
			startSubMove(0, 0, 0, NO_LIMIT, currentHold);
		}
		// if we have a new move, go start it
		if (pending) {
			pending = false;
			startSubMove(pendingSpeed, pendingAcceleration, pendingDeceleration, pendingLimit, pendingHold);
			this.stalled = false;
		}
		notifyAll();
	}

	/** Monitors time and tachoCount to regulate velocity and stop motor rotation at limit angle. To be called only from Controller
	 * thread. */
	private synchronized void regulateMotor (long delta) {
		float error;
		now += delta;
		long elapsed = now - baseTime;
		if (moving) {
			if (elapsed < accelerationTime) {
				// We are still accelerating, calculate new position
				currentVelocity = baseVelocity + currentAcceleration * elapsed / (1000);
				currentTCount = baseTCount + (baseVelocity + currentVelocity) * elapsed / (2 * 1000);
				error = currentTCount - tachoTCount;
			} else {
				// no longer accelerating, calculate new position
				currentVelocity = currentTargetVelocity;
				currentTCount = baseTCount + accelerationTCount + currentVelocity * (elapsed - accelerationTime) / 1000;
				error = currentTCount - tachoTCount;
				// Check to see if the move is complete
				if (currentTargetVelocity == 0
					&& (pending || (Math.abs(error) < 2 && elapsed > accelerationTime + 100) || elapsed > accelerationTime + 500)) {
					endMove(false);
				}
			}
			// check for stall
			if (Math.abs(error) > stallLimit) {
				baseTime += delta;
				if (stallCount++ > stallTime) endMove(true);
			} else {
				stallCount /= 2;
			}
			calcPower(error, MOVE_P, MOVE_I, MOVE_D);
			// If we have a move limit, check for time to start the deceleration stage
			if (checkLimit) {
				float proximity = (currentLimit - currentTCount);
				if (!Float.isInfinite(currentDeceleration)) {
					float dec = (currentVelocity * currentVelocity) / (2 * (currentLimit - currentTCount));
					if (currentDeceleration / dec < 1.0) {
						startSubMove(0, dec, dec, NO_LIMIT, currentHold);
					}
				}// else We never stop. Deceleration is infinite.
			}
		} else if (currentHold) {
			// not moving, hold position
			error = currentTCount - tachoTCount;
			calcPower(error, HOLD_P, HOLD_I, HOLD_D);
		} else if (mode != TachoMotorPort.FLOAT && power != 0) {
			// Allow the motor to move freely
			currentTCount = tachoTCount;
			power = 0;
			mode = TachoMotorPort.FLOAT;
			cont.activeMotors[tachoPort.getId()] = false;
		}
	}

	/** helper method for velocity regulation. calculates power from error using double smoothing and PID like control
	 *
	 * @param error that occurred */
	private void calcPower (float error, float P, float I, float D) {
		// use smoothing to reduce the noise in frequent tacho count readings
		// New values
		err1 = 0.375f * err1 + 0.625f * error; // fast smoothing
		err2 = 0.75f * err2 + 0.25f * error; // slow smoothing

		float newPower = basePower + P * err1 + D * (err1 - err2);
		basePower = basePower + I * (newPower - basePower);

		if (basePower > TachoMotorPort.MAX_POWER)
			basePower = TachoMotorPort.MAX_POWER;
		else if (basePower < -TachoMotorPort.MAX_POWER) basePower = -TachoMotorPort.MAX_POWER;
		power = (newPower > TachoMotorPort.MAX_POWER ? TachoMotorPort.MAX_POWER
			: newPower < -TachoMotorPort.MAX_POWER ? -TachoMotorPort.MAX_POWER : Math.round(newPower));

		mode = (power == 0 ? TachoMotorPort.STOP : TachoMotorPort.FORWARD);
	}

	/** This class provides a single thread that drives all of the motor regulation process. Only active motors will be regulated.
	 * To try and keep motors as closely synchronized as possible tacho counts for all motors are gathered as close as possible to
	 * the same time. Similarly new power levels for each motor are also set at the same time. */
	protected static class Controller extends Thread {
		static final int UPDATE_PERIOD = 3;// 4
		private final MotorController[] motors = new MotorController[3];
		/** Active motor is being updated regularly. */
		private final boolean[] activeMotors = new boolean[motors.length];
		boolean running = false;

		synchronized void shutdown () {
			// Shutdown all of the motors and prevent them from running
			running = false;
			for (MotorController m : motors) {
				if (m != null) m.tachoPort.controlMotor(0, TachoMotorPort.FLOAT);
			}
		}

		@Override
		public void run () {
			running = true;
			long now = System.currentTimeMillis();
			while (running) {
				long delta;
				synchronized (this) {
					delta = System.currentTimeMillis() - now;
					MotorController[] motors = this.motors;
					now += delta;
					// Highly optimized code below. Couldn't resist.
					int i;
					final int motorsLengthMinusOne = motors.length - 1; // Iterating backwards is slightly faster. Very slightly.
// Maybe.
					MotorController m;
					for (i = motorsLengthMinusOne; i >= 0; i--) {
						if (activeMotors[i]) {
							m = motors[i];
							m.tachoTCount = m.tachoPort.getTachoCount();
						}
					}
					for (i = motorsLengthMinusOne; i >= 0; i--) {
						if (activeMotors[i]) {
							m = motors[i];
							m.regulateMotor(delta);
						}
					}
					for (i = motorsLengthMinusOne; i >= 0; i--) {
						if (activeMotors[i]) {
							m = motors[i];
							m.tachoPort.controlMotor(m.power, m.mode);
						}
					}
				}
				Delay.msDelay(now + UPDATE_PERIOD - System.currentTimeMillis());
			} // end keep going loop
		}
	}

	/** Returns max normally sustainable speed with current battery charge and moderate load. Believed to be current voltage in
	 * volts * 100.
	 *
	 * @return max sustainable speed */
	public static float getMaxSpeed () {
		// It is generally assumed, that the maximum accurate speed of Motor is 100 degree/second * Voltage
		return Battery.getVoltage() * 100.0f;
	}
}
