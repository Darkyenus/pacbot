package lego.bots.random;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 10:23
 */
public class RandomBot extends Bot<EnvironmentController> {

    private boolean continueRunning = true;

    @Override
    public synchronized void run() {
        try {
            this.wait();
        } catch (InterruptedException ignored) {}

        continueRunning = true;

        while(continueRunning){
            controller.moveByX((byte)-1);
            controller.moveByY((byte)-1);
            controller.moveByX((byte)1);
            controller.moveByY((byte)1);
        }

    }

    @Override
    public void onEvent(BotEvent event, Object param) {
        switch (event){
            case ESCAPE_PRESSED:
                continueRunning = false;
            case ENTER_PRESSED:
            case LEFT_PRESSED:
            case RIGHT_PRESSED:
                synchronized (this){
                    notifyAll(); //Should wake up the main thread.
                }
        }
    }
}
