package lego.nxt.controllers.util;

/**
 * Created by admin on 4.12.2014.
 */
public class HWParameters {

    public static final float SPEED = 0.50f;//* 0.95f; //Racing values

    public static final int ACCELERATION = 900;//1300;

    public static final float WHEEL_DIAMETER = 13.8333333f;//8.3f;//For direct motor

    public static final float WHEEL_DISTANCE = 6.8f;// Half distance between wheels

    public static final boolean FLIP_DIRECTION = true;//false;//For direct bot

    public static final float TURNING_BIAS = 0.00f;//0.010f;//Racing values

    public static final int CALIBRATION_WAITING = 200;

    /**
     * Stop the wheel in one peek. Rotate motor slowly. Stop just before the wheel would start moving.
     * Write here absolute value of measured motor rotation in degrees.
     */
    public static final int GEARBOX_INACCURACY_MOTOR_DEG = 0;

}
