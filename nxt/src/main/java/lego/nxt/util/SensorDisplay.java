package lego.nxt.util;

import lejos.nxt.*;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 09:55
 */
public class SensorDisplay {
    public static void main(String[] args) throws Exception {
        Sound.beepSequenceUp();
        Sound.beepSequence();

        UltrasonicSensor uz = new UltrasonicSensor(SensorPort.S4);
        LightSensor light = new LightSensor(SensorPort.S3);
        while(Button.ENTER.isUp()){
            LCD.drawString("US4: "+uz.getDistance()+" "+uz.getUnits(),0,0);
            if(Button.LEFT.isDown()){
                light.setFloodlight(true);
            }else if(Button.RIGHT.isDown()){
                light.setFloodlight(false);
            }
            LCD.drawString("L3: "+light.readValue(),0,1);
            Thread.sleep(100);
        }
    }
}
