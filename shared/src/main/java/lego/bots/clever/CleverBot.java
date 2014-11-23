package lego.bots.clever;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;

/**
 * Private property.
 * User: jIRKA
 * Date: 23.11.2014
 * Time: 15:07
 */
public class CleverBot  extends Bot<EnvironmentController> {

    private boolean continueRunning = true;

    public void prepare(){

        //TODO code

    }


    @Override
    public synchronized void run() {
        try {
            this.wait();
        } catch (InterruptedException ignored) {}

        //TODO code

    }



    @Override
    public void onEvent(BotEvent event) {
        switch (event){
            case RUN_PREPARE:
                prepare();
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
