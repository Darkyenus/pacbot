package lego.nxt.controllers.util;

/**
 * HW specific settings for differential bot.
 * Created on 4.12.2014.
 */
public class HWParameters {
    //0.50f;//0.95f; //Racing values for direct bot 0.95f
    public static final float SPEED = 0.95f;

    //900;//1300 - Racing;
    public static final int ACCELERATION = 1200;

    //13.8333333f;//8.3f;//For direct motor
    public static final float WHEEL_DIAMETER = 8.3f;

    // Half distance between wheels
    public static final float WHEEL_DISTANCE = 6.8f;

    // For direct bot false, for geared bot true
    public static final boolean FLIP_DIRECTION = true;//false;

    public static final boolean FLIP_TURN_DIRECTION = true;

    //0.010f;//Racing values
    public static final float TURNING_BIAS = 0.00f;

    //How long wait after button touch
    public static final int CALIBRATION_WAITING = 200;

    /**
     * Stop the wheel in one peek. Rotate motor slowly. Stop just before the wheel would start moving.
     * Write here absolute value of measured motor rotation in degrees.
     */
    public static final int GEARBOX_INACCURACY_MOTOR_DEG = 0;
}
