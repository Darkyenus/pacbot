package lego.bots.random;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lejos.nxt.Button;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 10:23
 */
public class RandomBot extends Bot<EnvironmentController> {



    @Override
    public void run() {

        Button.waitForAnyPress();

        while(Button.ENTER.isUp()){
            controller.moveByX((byte)-1);
            controller.moveByY((byte)-1);
            controller.moveByX((byte)1);
            controller.moveByY((byte)1);
        }

    }

    @Override
    public void onEvent(BotEvent event, Object param) {

    }
}
