package lego.nxt.controllers.util;

import lego.api.controllers.EnvironmentController;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * Private property.
 * User: Darkyen
 * Date: 02/12/14
 * Time: 18:37
 */
public class DifferentialSensors extends Thread {

    private static final LightSensor leftLight = new LightSensor(SensorPort.S3);
    private static final UltrasonicSensor rightSonic = new UltrasonicSensor(SensorPort.S4);

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

    public int displayLightReadings = 0;
    public int displaySonicReadings = 0;

    private final DifferentialController controller;

    public DifferentialSensors(DifferentialController controller) {
        setDaemon(true);
        setPriority(Thread.NORM_PRIORITY);
        this.controller = controller;
    }

    public void readSensors () {
        EnvironmentController.Direction left = controller.getHeadingDirection().left;
        byte leftX = (byte)(controller.getX() + left.x);
        byte leftY = (byte)(controller.getY() + left.y);

        displayLightReadings = (highLightSum - lowLightSum) / READINGS;
        if (displayLightReadings > LIGHT_THRESHOLD) {
            // setField(leftX, leftY, FieldStatus.OBSTACLE);
        } else {
            // if(getField(leftX, leftY) != FieldStatus.FREE_VISITED && getField(leftX, leftY) != FieldStatus.START)
            // setField(leftX, leftY, FieldStatus.FREE_UNVISITED);
        }

        EnvironmentController.Direction right = controller.getHeadingDirection().right;

        displaySonicReadings = sonicSum / READINGS;

        if (displaySonicReadings != 255) { // Invalid measurement, can mean either too far away or too close.

            for (int i = 1; displaySonicReadings - i * SONIC_BLOCK_SIZE > 0; i++) {
                byte rightX = (byte)(controller.getX() + right.x * i);
                byte rightY = (byte)(controller.getY() + right.y * i);
                // if(getField(rightX, rightY) != FieldStatus.FREE_VISITED && getField(leftX, leftY) != FieldStatus.START)
                // setField(rightX, rightY, FieldStatus.FREE_UNVISITED);
            }
            // setField((byte) (x + right.x), (byte) (y + right.y), FieldStatus.OBSTACLE);
        }
    }

    @Override
    public void run () {
        // noinspection InfiniteLoopStatement
        while (true) {
            int sonicDistance = rightSonic.getDistance();
            if (sonicDistance != 255) {
                sonicSum -= sonicReadings[sonicPointer];
                sonicReadings[sonicPointer] = sonicDistance;
                sonicSum += sonicDistance;
                sonicPointer++;
                if (sonicPointer == READINGS) {
                    sonicPointer = 0;
                }
            }
            int lightReading = leftLight.getNormalizedLightValue();
            if (highLight) {
                highLightSum = highLightSum - highLightReadings[highLightPointer] + lightReading;
                highLightReadings[highLightPointer] = lightReading;
                highLight = false;
                highLightPointer++;
                if (highLightPointer == READINGS) {
                    highLightPointer = 0;
                }
            } else {
                lowLightSum = lowLightSum - lowLightReadings[lowLightPointer] + lightReading;
                lowLightReadings[lowLightPointer] = lightReading;
                highLight = true;
                lowLightPointer++;
                if (lowLightPointer == READINGS) {
                    lowLightPointer = 0;
                }
            }
            leftLight.setFloodlight(highLight);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
