package lego.nxt.util;

import lejos.nxt.*;

/**
 * This is like standard old motor controller, but rewritten in clearer, more concise way.
 * Some things may be simpler, some perhaps harder, but generally, it should be easier to write FAST motor controllers.
 *
 * Principle:
 *
 * Keeps track of target tacho count. Main objective is to get to that tacho count.
 * Tacho counts are in absolute rotation degrees.
 * Then there is target speed. That is, speed that should be achieved during most of the movement.
 * Speed is in degrees per second.
 * Next, acceleration. This means, by how much degrees per second per second should velocity change to travel.
 *
 * There is also deceleration. This does work a bit like acceleration, but is a bit different.
 * It is possible to determine, from tacho readings, speed and stuff, when motor has to start to decelerate.
 * At that point, a new move will start, with same target count, zero target speed, and acceleration of previous value of deceleration.
 * Deceleration will be set to zero, read below.
 *
 * When deceleration is set to zero, it means, that you don't want to decelerate and stop at the end.
 * Motor will just keep going, but unregulated, keep that in mind. (Also unsynchronized.)
 *
 * When controller completes it's task and stops (signaled by zero deceleration), motor will keep its last speed values
 * and go into unregulated mode, that is no longer maintained by this controller.
 * Spawning new move task will resume regulation.
 *
 * Implementation:
 *
 * It locally keeps track of these things:
 * baseTacho        signed - ideal tacho it had at the end of last movement, starts at zero
 * targetTacho      signed - position it tries to achieve when riding regulated
 *
 * currentSpeed     unsigned - currently maintained desired speed. Gets closer to targetSpeed with time by the speed of
 *                             acceleration
 * targetSpeed      unsigned - speed it will try to achieve
 * acceleration     unsigned - rate of change of speed, up or down, that is not important
 * deceleration     unsigned - when time comes, and it is time to decelerate, new move will take this ones place, using
 *                             same targetTacho, zero target speed and this value for acceleration. See above.
 *                             Will be zero if no slowing down should happen.
 *
 * Whole system can be also synchronized to one other controller of same type.
 * That will somehow cause this motor to wait at the other one, should it lag behind in execution.
 * That is, it should keep intentionally the same error to this one.
 *
 * Private property.
 * User: Darkyen
 * Date: 14/11/14
 * Time: 23:42
 */
@SuppressWarnings("UnusedDeclaration")
public class LightMotorController {

    /** Tacho count read from hardware by Controller thread */
    int hardwareTacho;
    /** Tacho count at the start of active move (if any). Used to calculate progress */
    int baseTacho;
    /** Tacho count that this motor is trying to achieve. Changes over time from regulateMotor */
    int currentTacho;
    /** Tacho count at which this motor is ultimately trying to get */
    int targetTacho;

    /** Speed at which is motor travelling right now */
    int currentSpeed;
    /** Speed at which was motor travelling at the start of move */
    int baseSpeed;
    /** Speed were ultimately trying to accelerate to */
    int targetSpeed;

    /** Speed at which will current speed get closer to */
    int acceleration;
    /** Acceleration that will be used to slow down at the end. */
    int deceleration;
    /** Non moving motor with hold set to true will be actively trying to keep its position. */
    boolean hold;

    /** Outgoing value, motor will be set to this power on every Controller update. */
    int power;
    /** See power above. */
    int mode;
    /** Whether or not this motor should be actively regulated.
     * Non moving motor will be doing whatever it was doing when it stopped moving. Unless hold is true. */
    boolean moving = false;
    /** Port used for this motor */
    private MotorPort port;

    public LightMotorController(MotorPort port) {
        this.port = port;
        reset();
        cont.motors[cont.activeMotorsMinusOne+1] = this;
        cont.activeMotorsMinusOne += 1;
    }

    public void reset(){
        port.resetTachoCount();
        hardwareTacho = 0;
        baseTacho = hardwareTacho;
        targetTacho = hardwareTacho;
        currentSpeed = 0;
        baseSpeed = 0;
        targetSpeed = 0;
        acceleration = 0;
        deceleration = 0;
        moving = false;
        mode = MotorPort.FLOAT;
        power = 0;
    }

    /**
     * Returns progress of the movement (if any movement in progress, otherwise result is 1000).
     * From 0 to 1000;
     */
    public int getProgress(){
        //how far from baseTacho to targetTacho currentTacho is
        if(moving){
            int fullPath = targetTacho - baseTacho;
            int travelledPath = currentTacho - baseTacho;
            return (travelledPath * 1000) / fullPath;
        }else{
            return 1000;
        }
    }

    public boolean isMoving(){
        return moving;
    }

    public void waitForComplete(){
        if(moving){
            synchronized (this){
                while(moving){
                    try {
                        wait(1000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    public int getHardwareTacho(){
        return hardwareTacho;
    }

    public synchronized void moveBy(int degrees, int speed, int acceleration, int deceleration, boolean hold){
        moveTo(currentTacho+degrees,speed,acceleration,deceleration,hold);
    }

    /**
     * Will start a new move, travelling by target speed, getting to the target speed by acceleration,
     * and decelerating at the end.
     *
     * @param target tacho count in degrees
     * @param speed (unsigned) of the move
     * @param acceleration can be zero for immediate speed assumption
     * @param deceleration can be zero to not slow down at the end
     * @param hold when true, will stop at the end, false will float. Both will happen only when target speed is zero.
     *             This happens at the end of decelerating or manually.
     */
    public synchronized void moveTo(int target, int speed, int acceleration, int deceleration, boolean hold){
        targetTacho = target;
        currentTacho = hardwareTacho;//This should account for changes that happened while not moving.
        baseTacho = currentTacho;
        direction = targetTacho < currentTacho ? (byte)-1 : (byte)1;
        targetSpeed = speed;
        this.acceleration = acceleration;
        time = 0;
        if(acceleration == 0){
            currentSpeed = speed;
            accelerationTime = 0;
            accelerationDistance = 0;
        }else{
            /*
            t * a = v
            t = v / a
            s = (1/2) * a * t^2
            s = v * t
             */
            accelerationTime = (Math.abs(targetSpeed - currentSpeed) * 1000) / acceleration;
            float accelerationTime2 = accelerationTime / 1000f;
            accelerationTime2 = accelerationTime2 * accelerationTime2;
            accelerationDistance = (int)(acceleration * accelerationTime2 + 0.5f /* rounding */) >>> 1;
            // >>> 1 is like /2 which is like * 0.5f
        }
        this.deceleration = deceleration;
        if (deceleration == 0) {
            decelerationDistance = 0;
            if(targetSpeed == 0){
                finishOrDecelerationTime = accelerationTime;
            }else{
                int totalDistance = (targetTacho - baseTacho)*direction;
                int timeSpentTravelling = ((totalDistance - accelerationDistance))*1000 / targetSpeed;
                finishOrDecelerationTime = accelerationTime + timeSpentTravelling;
            }
            //Deceleration time is now essentially end time.
        } else {
            //time to accelerate + time to travel + time to decelerate = total time
            int totalDistance = (targetTacho - baseTacho)*direction;
            int distanceTravelledDuringAcceleration = accelerationDistance;
            float decelerationTime2 = (float)targetSpeed / (float)deceleration;
            decelerationTime2 = decelerationTime2 * decelerationTime2;
            decelerationDistance = (int)(deceleration * decelerationTime2 + 0.5f /* rounding */) >>> 1;

            int timeSpentTravelling = ((totalDistance - distanceTravelledDuringAcceleration - decelerationDistance))*1000 / targetSpeed;

            finishOrDecelerationTime = accelerationTime + timeSpentTravelling;
        }
        baseSpeed = currentSpeed;
        speedDirection = targetSpeed < currentSpeed ? (byte)-1 : (byte)1;
        this.hold = hold;
        moving = true;
        if(accelerationTime > finishOrDecelerationTime){
            Sound.buzz();
            Sound.beepSequence();
            Sound.beepSequenceUp();
            Sound.beep();
            Sound.buzz();
            LCD.drawString(Math.abs(target-hardwareTacho)+","+speed+","+acceleration+","+deceleration+","+hold+")",0,6);
        }
    }

    /** Direction signum of move */
    byte direction;
    /** Direction of acceleration */
    byte speedDirection;
    /** Time of move. Is being reset at the start of move. In ms. */
    int time;
    /** Time for how long we'll be accelerating. Used for calculation optimization. In ms. */
    int accelerationTime;
    /** Tacho distance, which will be travelled to accelerate. */
    int accelerationDistance;
    /** Time at which we'll start to decelerate */
    int finishOrDecelerationTime;
    /** Tacho distance, which will be travelled to decelerate. */
    int decelerationDistance;

    /**
     *
     * @param delta in ms
     */
    private synchronized void regulateMotor(int delta){
        if(!moving){
            if(hold){
                calcPower(currentTacho - hardwareTacho,HOLD_P,HOLD_I,HOLD_D);
            }
            return;
        }
        time += delta;
        if(time < accelerationTime){
            //Accelerating
            currentSpeed = baseSpeed + (time * acceleration * speedDirection) / 1000;
            currentTacho = baseTacho + (baseSpeed*time/1000 + (acceleration * time * time * speedDirection)/2000000)*direction;
        }else if(time >= finishOrDecelerationTime){
            //Decelerating
            if(deceleration == 0){
                //End move
                currentTacho = targetTacho;
                if(hold){
                    mode = MotorPort.STOP;
                }
                currentSpeed = targetSpeed;
                if(currentSpeed == 0){
                    power = 0;
                }
                moving = false;
                notifyAll();
                return;
            }else{
                currentSpeed = targetSpeed;
                currentTacho = targetTacho - decelerationDistance*direction;
                //Decelerate
                int decelerationTime = time - this.finishOrDecelerationTime;
                moveTo(targetTacho,0,deceleration,0,hold);
                time = decelerationTime;
                //regulateMotor(0);
                return;
            }
        }else{
            //Moving normally
            currentSpeed = targetSpeed;
            currentTacho = baseTacho + accelerationDistance*direction + (currentSpeed * (time - accelerationTime) * direction) / 1000;
        }

        calcPower(currentTacho - hardwareTacho,MOVE_P,MOVE_I,MOVE_D);
    }

    // PID constants for move and for hold
    /*private static final float MOVE_P = 13f;//6f; //Immediate
    private static final float MOVE_I = 0.00001f;//0.04f; //Cumulative
    private static final float MOVE_D = 44f;//22f; //Predictive

    private static final float HOLD_P = 18f;
    private static final float HOLD_I = 0.001f;
    private static final float HOLD_D = 70f;*/
    private static final float MOVE_P = 6f;
    private static final float MOVE_I = 0.04f;
    private static final float MOVE_D = 22f;

    private static final float HOLD_P = 2f;
    private static final float HOLD_I = 0.04f;
    private static final float HOLD_D = 8f;

    private float basePower = 0; //used to calculate power, but only in PID control
    private float err1 = 0; // used in smoothing
    private float err2 = 0; // used in smoothing

    private boolean errorPositive = true;
    private float cumulativeError = 0;
    float error = 0;
    /**
     * Calculates and sets motor power and mode based on error.
     * Calculates power from error using double smoothing and PID like control
     *
     * @param error that occurred
     */
    /*private void calcPower(float error, float P, float I, float D) {

        // use smoothing to reduce the noise in frequent tacho count readings
        // New values
        err1 = 0.375f * err1 + 0.625f * error;  // fast smoothing
        err2 = 0.75f * err2 + 0.25f * error; // slow smoothing

        //float newPower = basePower + P * err1 + D * (err1 - err2);
        float newPower = basePower + P * error + D * (err1 - err2);
        this.error = error;
        //basePower = basePower + I * (newPower - basePower);

        if(errorPositive){
            if(error < 0){
                cumulativeError = 0;
                errorPositive = false;
            } else {
                cumulativeError += error;
            }
        }else{
            if(error > 0){
                cumulativeError = 0;
                errorPositive = true;
            } else {
                cumulativeError += error;
            }
        }

        basePower = basePower + I * cumulativeError;

        if (basePower > MotorPort.MAX_POWER)
            basePower = MotorPort.MAX_POWER;
        else if (basePower < -MotorPort.MAX_POWER)
            basePower = -MotorPort.MAX_POWER;
        power = (newPower > MotorPort.MAX_POWER ? MotorPort.MAX_POWER : newPower < -MotorPort.MAX_POWER ? -MotorPort.MAX_POWER : Math.round(newPower));

        mode = (power == 0 ? MotorPort.STOP : MotorPort.FORWARD);
    }*/
    private void calcPower(float error, float P, float I, float D) {
        // use smoothing to reduce the noise in frequent tacho count readings
        // New values
        err1 = 0.375f * err1 + 0.625f * error;  // fast smoothing
        err2 = 0.75f * err2 + 0.25f * error; // slow smoothing

        float newPower = basePower + P * err1 + D * (err1 - err2);
        basePower = basePower + I * (newPower - basePower);

        if (basePower > TachoMotorPort.MAX_POWER)
            basePower = TachoMotorPort.MAX_POWER;
        else if (basePower < -TachoMotorPort.MAX_POWER)
            basePower = -TachoMotorPort.MAX_POWER;
        power = (newPower > TachoMotorPort.MAX_POWER ? TachoMotorPort.MAX_POWER : newPower < -TachoMotorPort.MAX_POWER ? -TachoMotorPort.MAX_POWER : Math.round(newPower));

        mode = (power == 0 ? TachoMotorPort.STOP : TachoMotorPort.FORWARD);
    }

    /**
     * Returns max normally sustainable speed with current battery charge and moderate load.
     * Believed to be current voltage in volts * 100.
     *
     * @return max sustainable speed
     */
    public static float getMaxSpeed() {
        // It is generally assumed, that the maximum accurate speed of Motor is 100 degree/second * Voltage
        return Battery.getVoltage() * 100.0f;
    }

    //----------------------------------------- Controller -------------------------------------------------------
    protected static final Controller cont = new Controller();

    static {
        // Start the single controller thread
        cont.setPriority(Thread.MAX_PRIORITY);
        cont.setDaemon(true);
        cont.start();
        // Add shutdown handler to stop the motors
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cont.shutdown();
            }
        });
    }

    /**
     * This class provides a single thread that drives all of the motor regulation
     * process. Only active motors will be regulated. To try and keep motors
     * as closely synchronized as possible tacho counts for all motors are gathered
     * as close as possible to the same time. Similarly new power levels for each
     * motor are also set at the same time.
     */
    protected static class Controller extends Thread {
        static final int UPDATE_PERIOD = 4;//4
        private final LightMotorController[] motors = new LightMotorController[3];
        private int activeMotorsMinusOne = -1;
        boolean running = false;

        synchronized void shutdown() {
            // Shutdown all of the motors and prevent them from running
            running = false;
            for (LightMotorController m : motors) {
                if (m != null)
                    m.port.controlMotor(0, TachoMotorPort.FLOAT);
            }
        }

        @Override
        public void run() {
            running = true;
            long now = System.currentTimeMillis();
            while (running) {
                long delta;
                synchronized (this) {
                    delta = System.currentTimeMillis() - now;
                    final LightMotorController[] motors = this.motors;
                    now += delta;
                    //Highly optimized code below. Couldn't resist.
                    int i;
                    //Iterating backwards is slightly faster. Very slightly. Maybe.
                    LightMotorController m;
                    final int activeMotorsMinusOne = this.activeMotorsMinusOne;
                    for (i = activeMotorsMinusOne; i >= 0; i--) {
                        m = motors[i];
                        m.hardwareTacho = m.port.getTachoCount();
                    }
                    for (i = activeMotorsMinusOne; i >= 0; i--) {
                        motors[i].regulateMotor((int)delta);
                    }
                    for (i = activeMotorsMinusOne; i >= 0; i--) {
                        m = motors[i];
                        m.port.controlMotor(m.power, m.mode);
                    }
                }
                try {
                    Thread.sleep(now + UPDATE_PERIOD - System.currentTimeMillis());
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
