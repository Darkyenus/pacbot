
package lego.nxt.controllers;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.nxt.controllers.util.DifferentialController;
import lego.nxt.controllers.util.DifferentialMotorManager;
import lego.nxt.controllers.util.DifferentialSensors;
import lego.nxt.util.AbstractMoveTask;
import lego.nxt.util.MotorController;
import lego.nxt.util.TaskProcessor;
import lejos.nxt.*;
import lejos.util.Delay;

/** Hi
 *
 * Created by Darkyen on 13.11.2014. */
@SuppressWarnings({ "ConstantConditions"})
public final class DifferentialEnvironmentRobotController extends EnvironmentController implements DifferentialController {

	private static final DifferentialMotorManager motors = new DifferentialMotorManager(MotorPort.C, MotorPort.B);
	private static final MotorPort warningLight = MotorPort.A;
	private static final TouchSensor frontTouch = new TouchSensor(SensorPort.S1);
	private static final TouchSensor backTouch = new TouchSensor(SensorPort.S2);
	private static final DifferentialSensors sensors = null;//Sensors are off

	private String lastError = "";
	private byte warnings = 0;

	private byte lastMoved = 0;

	@Override
	public void initialize () {
		LCD.setAutoRefresh(false);
		TaskProcessor.initialize();

		Thread debugViewThread = new Thread("DV") {

			private boolean glows = false;

			private void doDrawing () {
				for (byte x = 0; x < mazeWidth; x++) {
					for (byte y = 0; y < mazeHeight; y++) {
						LCD.drawString(getFieldDisplay(x,y), x, y, getX() == x && getY() == y);
					}
				}
				LCD.drawString(lastError, mazeWidth + 1, 0);
				// LCD.drawString((short)(motors.asyncProgress()*100f)+"%   ",mazeWidth+1,1);
				if(sensors != null){
					sensors.readSensors();
					LCD.drawString("L:", 0, mazeHeight + 1);
					LCD.drawInt(sensors.displayLightReadings, 3, mazeHeight + 1);
					LCD.drawString("S:", 0, mazeHeight + 2);
					LCD.drawInt(sensors.displaySonicReadings, 3, mazeHeight + 2);
				}

				if (glows) {
					LCD.drawChar('U', LCD.DISPLAY_CHAR_WIDTH - 1, LCD.DISPLAY_CHAR_DEPTH - 1);
				} else {
					LCD.drawChar(' ', LCD.DISPLAY_CHAR_WIDTH - 1, LCD.DISPLAY_CHAR_DEPTH - 1);
				}

				LCD.asyncRefresh();
				glows = !glows;
				if (warnings > 0) {
					warningLight.controlMotor(glows ? 100 : 0, BasicMotorPort.FORWARD);
				} else {
					warningLight.controlMotor(0, BasicMotorPort.FORWARD);
				}
			}

			@SuppressWarnings({"StatementWithEmptyBody", "ConstantConditions"})
			@Override
			public void run () {
				setPriority(Thread.MAX_PRIORITY);
				long startPrepare = System.currentTimeMillis();
				Bot.active.onEvent(BotEvent.RUN_PREPARE);
				int msToInit = (int)(System.currentTimeMillis() - startPrepare);
				LCD.drawString(msToInit + "ms", 0, LCD.DISPLAY_CHAR_DEPTH - 1);
				setPriority(Thread.MIN_PRIORITY);

				MotorController.startWheelControl();
				doDrawing();

				System.gc();// While we still have time

				//Alert that we're ready to go
				warningLight.controlMotor(100,MotorPort.FORWARD);
				Sound.playTone(800,300,100);
				Delay.msDelay(300);
				Sound.playTone(800,500,100);
				Delay.msDelay(300);
				Sound.playTone(800,500,100);
				Delay.msDelay(300);
				Sound.playTone(1500,800,100);
				//ta-tada-daa!

				while (!frontTouch.isPressed()) {
					Delay.msDelay(50);
				}
				while (frontTouch.isPressed()) {
					Delay.msDelay(50);
				}
				Delay.msDelay(500);
				Bot.active.onEvent(BotEvent.RUN_STARTED);
				warningLight.controlMotor(30,BasicMotorPort.FORWARD);

				// noinspection InfiniteLoopStatement
				while (true) {
					if (Button.ESCAPE.isDown()) {
						Bot.active.onEvent(BotEvent.RUN_ENDED);
					}
					doDrawing();
					try {
						Thread.sleep(200);
					} catch (InterruptedException ignored) {
					}
				}
			}
		};
		debugViewThread.setDaemon(true);
		debugViewThread.setPriority(Thread.MIN_PRIORITY);
		debugViewThread.start();

		if(sensors != null){
			sensors.start();
		}
	}

	@Override
	public void onError (byte error) {
		switch (error) {
		case ERROR_SET_DEFINITIVE:
			lastError = "D" + x + " " + y;
			break;
		case ERROR_SET_OUT_OF_BOUNDS:
			if (x < 0) {
				x = 0;
			} else if (x >= EnvironmentController.mazeWidth) {
				x = EnvironmentController.mazeWidth - 1;
			}
			if (y < 0) {
				y = 0;
			} else if (y >= EnvironmentController.mazeHeight) {
				y = EnvironmentController.mazeHeight - 1;
			}
			lastError = "OOB";
			break;
		case ERROR_CAL_BLOCK_EXPECTED:
			lastError = "cX";
			break;
		case WARNING_ALERT:
			Sound.beep();
			break;
		case WARNING_TOOK_TOO_LONG_TIME_TO_COMPUTE:
			lastError = "wttlttc";
		case SUCCESS_PATH_COMPUTED:
			for (byte i = 0; i < 50; i++) {
				Sound.playTone(400 + i * 15, 20);
				// Sound.playTone(1135 - i*15,10);
			}
		default:
			Sound.buzz();
			lastError = "!" + error;
		}
	}

	private Direction direction = Direction.DOWN;

	private boolean isInOppositeDirection (Direction to) {
		return to.x == -direction.x && to.y == -direction.y;
	}

	private boolean ensureDirection (Direction to, boolean stopAfterCalibrating) {
		if (to == direction) return false;
		boolean calibrated = false;
		// warnings++;
		switch ((direction.ordinal() - to.ordinal() + 4) % 4) {// It's a kind of magic
		case 1:
			turnLeft();
			if (lastMoved > 3 && isObstacle((byte)(x - to.x), (byte)(y - to.y))) {
				calibrateBackward(stopAfterCalibrating);
				calibrated = true;
			}
			break;
		case 2:
			turnAround();
			if (isObstacle((byte)(x - to.x), (byte)(y - to.y))) {
				calibrateBackward(stopAfterCalibrating);
				calibrated = true;
			}
			break;
		case 3:
			turnRight();
			if (lastMoved > 3 && isObstacle((byte)(x - to.x), (byte)(y - to.y))) {
				calibrateBackward(stopAfterCalibrating);
				calibrated = true;
			}
			break;
		}
		// warnings--;
		direction = to;
		lastMoved = 0;
		if(sensors != null)sensors.readSensors();
		return calibrated;
	}

	private static final int CALIBRATION_WAITING = 200;
	private static final float TURNING_BIAS = 0.010f;

	private float calculateBias () {
		return TURNING_BIAS * lastDirection;
	}

	private void turnLeft () {
		motors.reset();
		motors.turnRad(DifferentialMotorManager.HALF_PI + calculateBias(), DifferentialMotorManager.MAX_SPEED(),
			DifferentialMotorManager.SMOOTH_ACCELERATION, DifferentialMotorManager.SMOOTH_ACCELERATION, true);
	}

	private void turnRight () {
		motors.reset();
		motors.turnRad(-DifferentialMotorManager.HALF_PI - calculateBias(), DifferentialMotorManager.MAX_SPEED(),
			DifferentialMotorManager.SMOOTH_ACCELERATION, DifferentialMotorManager.SMOOTH_ACCELERATION, true);
	}

	private void turnAround () {
		motors.reset();
		motors.turnRad(DifferentialMotorManager.PI + calculateBias(), DifferentialMotorManager.MAX_SPEED(),
			DifferentialMotorManager.SMOOTH_ACCELERATION, DifferentialMotorManager.SMOOTH_ACCELERATION, true);
	}

	private static final float BLOCK_DISTANCE = 28.75f;// 28.5 cm
	private static final float BACKING_DISTANCE = 6.0f;// 5.5

	private byte lastDirection = 1;

	private boolean driveForward (boolean accelerate, boolean decelerate) {
		lastDirection = 1;
		motors.moveAsync(BLOCK_DISTANCE, BLOCK_DISTANCE, DifferentialMotorManager.MAX_SPEED(),
			accelerate ? DifferentialMotorManager.SMOOTH_ACCELERATION : DifferentialMotorManager.MAX_ACCELERATION,
			decelerate ? DifferentialMotorManager.SMOOTH_ACCELERATION : DifferentialMotorManager.NO_DECELERATION, true);
		while (motors.asyncProgress() < 0.95f) {
			if (frontTouch.isPressed()) {// && motors.asyncProgress() > 0.7f //TODO OOOOO?
				// warnings++;

				Delay.msDelay(CALIBRATION_WAITING);
				motors.reset();
				motors.move(-BACKING_DISTANCE, -BACKING_DISTANCE, DifferentialMotorManager.MAX_SPEED() / 4,
					DifferentialMotorManager.SMOOTH_ACCELERATION, DifferentialMotorManager.SMOOTH_ACCELERATION, false);
				// warnings--;
				return false;
			}
			try {
				Thread.sleep(40);
			} catch (InterruptedException ignored) {
			}
		}
		return true;
	}

	private boolean driveBackward (boolean accelerate, boolean decelerate) {
		lastDirection = -1;
		motors.moveAsync(-BLOCK_DISTANCE, -BLOCK_DISTANCE, DifferentialMotorManager.MAX_SPEED(),
			accelerate ? DifferentialMotorManager.SMOOTH_ACCELERATION : DifferentialMotorManager.MAX_ACCELERATION,
			decelerate ? DifferentialMotorManager.SMOOTH_ACCELERATION : DifferentialMotorManager.NO_DECELERATION, true);
		warnings++;
		while (motors.asyncProgress() < 0.95) {
			// Sound.playTone(500+(int)(motors.asyncProgress() * 500),100);//TODO QQQ
			if (backTouch.isPressed()) {// && motors.asyncProgress() > 0.7f //TODO

				Delay.msDelay(CALIBRATION_WAITING);
				motors.reset();
				motors.move(BACKING_DISTANCE, BACKING_DISTANCE, DifferentialMotorManager.MAX_SPEED() / 4,
					DifferentialMotorManager.SMOOTH_ACCELERATION, DifferentialMotorManager.SMOOTH_ACCELERATION, false);
				warnings--;
				return false;
			}
			try {
				Thread.sleep(40);
			} catch (InterruptedException ignored) {
			}
		}
		warnings--;
		return true;
	}

	private void calibrateBackward (boolean stopAfterCalibrating) {
		warnings++;
		float speed = DifferentialMotorManager.MAX_SPEED() * 0.8f;
		motors.moveAsync(-BLOCK_DISTANCE, -BLOCK_DISTANCE, speed, DifferentialMotorManager.SMOOTH_ACCELERATION,
			DifferentialMotorManager.SMOOTH_ACCELERATION, true);

		while (motors.asyncMoving()) {
			if (backTouch.isPressed()) {
				Delay.msDelay(CALIBRATION_WAITING);
				motors.reset();
				if (stopAfterCalibrating) {
					motors.move(BACKING_DISTANCE, BACKING_DISTANCE, speed, DifferentialMotorManager.SMOOTH_ACCELERATION,
						DifferentialMotorManager.SMOOTH_ACCELERATION, true);
				} else {
					motors.moveAsync(BACKING_DISTANCE, BACKING_DISTANCE, DifferentialMotorManager.MAX_SPEED(),
						DifferentialMotorManager.SMOOTH_ACCELERATION, DifferentialMotorManager.NO_DECELERATION, true);
					motors.waitForAsyncProgress(0.95f);
				}
				warnings--;
				return;
			}
		}
		onError(ERROR_CAL_BLOCK_EXPECTED);
		motors.reset();
		if (stopAfterCalibrating) {
			motors.moveAsync(BLOCK_DISTANCE, BLOCK_DISTANCE, speed, DifferentialMotorManager.SMOOTH_ACCELERATION,
				DifferentialMotorManager.SMOOTH_ACCELERATION, true);
		} else {
			// We shall continue forward after returning to center
			motors.moveAsync(BLOCK_DISTANCE, BLOCK_DISTANCE, DifferentialMotorManager.MAX_SPEED(),
				DifferentialMotorManager.SMOOTH_ACCELERATION, DifferentialMotorManager.NO_DECELERATION, true);
		}
		motors.waitForAsyncProgress(0.95f);
		warnings--;
	}

	@Override
	public MoveFieldsTask moveByXAsync (byte x) {
		MoveTask result;
		if (x < 0) {
			result = new MoveTask(Direction.LEFT, (byte)-x);
		} else {
			result = new MoveTask(Direction.RIGHT, x);
		}
		TaskProcessor.appendTask(result);
		return result;
	}

	@Override
	public MoveFieldsTask moveByYAsync (byte y) {
		MoveTask result;
		if (y < 0) {
			result = new MoveTask(Direction.UP, (byte)-y);
		} else {
			result = new MoveTask(Direction.DOWN, y);
		}
		TaskProcessor.appendTask(result);
		return result;
	}

	@Override
	public Direction getHeadingDirection() {
		return direction;
	}

	private final class MoveTask extends AbstractMoveTask {
		private Direction direction;
		private byte amount;

		public MoveTask (Direction direction, byte amount) {
			this.direction = direction;
			this.amount = amount;
		}

		@Override
		protected void process () {
			byte finalX = (byte)(x + (direction.x) * (amount + 1));
			byte finalY = (byte)(y + (direction.y) * (amount + 1));
			if (isObstacle(finalX, finalY)) {
				amount++; // Calibrate forward if going into wall
			}
			processForward();
			lastMoved = moved;
			doComplete();
		}

		private void processForward () {
			// Dont stop if were moving after this. Should never be true, but just in case
			boolean stopAfterCalibrating = amount == 0;
			boolean calibrated = ensureDirection(direction, stopAfterCalibrating);
			if (!calibrated && amount >= 3 && isObstacle((byte)(x - direction.x), (byte)(y - direction.y))) {
				calibrateBackward(stopAfterCalibrating);
			}
			while (moved < amount) {
				if (driveForward(moved == 0, moved == amount - 1)) {
					moved += 1;
					x += direction.x;
					y += direction.y;
					setField(x, y, FREE_VISITED);
					if(sensors != null)sensors.readSensors();
				} else {
					setField((byte)(x + direction.x), (byte)(y + direction.y), OBSTACLE);
					break;
				}
			}
		}

		private void processBackward () {
			// TODO Maybe some calibrating?
			while (moved < amount) {
				if (driveBackward(moved == 0, moved == amount - 1)) {
					moved += 1;
					x -= direction.x;
					y -= direction.y;
					setField(x, y, FREE_VISITED);
					if(sensors != null)sensors.readSensors();
				} else {
					setField((byte)(x - direction.x), (byte)(y - direction.y), OBSTACLE);
					break;
				}
			}
		}
	}
}
