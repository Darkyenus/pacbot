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
        byte dist = 2;
        while(continueRunning){
            controller.moveByY(dist);
            controller.moveByX((byte)-dist);
            controller.moveByY((byte)-dist);
            controller.moveByX((byte) -dist);
            controller.moveByY(dist);
            controller.moveByX(dist);
            controller.moveByY((byte) -dist);
            controller.moveByX(dist);
        }

    }

    @Override
    public void onEvent(BotEvent event) {
        switch (event){
            case RUN_ENDED:
                continueRunning = false;
                synchronized (this){
                    notifyAll(); //Should wake up the main thread.
                }
                break;
            case RUN_STARTED:
                continueRunning = true;
                synchronized (this){
                    notifyAll(); //Should wake up the main thread.
                }
                break;
        }
    }
}
