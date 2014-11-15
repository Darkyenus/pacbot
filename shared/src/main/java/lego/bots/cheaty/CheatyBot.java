package lego.bots.cheaty;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;

/**
 * This bot is called cheaty, because it uses predefined map and it is cheat against the original rules. However after dozens of rule patches is this behaviour acceptable.
 *
 * Private property.
 * User: jIRKA
 * Date: 23/10/14
 * Time: 10:23
 */
public class CheatyBot extends Bot<EnvironmentController> {

    private boolean continueRunning = true;

    private static final EnvironmentController.FieldStatus FREE = EnvironmentController.FieldStatus.FREE_UNVISITED;
    private static final EnvironmentController.FieldStatus BLOCK = EnvironmentController.FieldStatus.OBSTACLE;
    private static final EnvironmentController.FieldStatus START = EnvironmentController.FieldStatus.START;

    //Default map from video
    private final EnvironmentController.FieldStatus[][] preparedMap = {
            {FREE,  FREE,  FREE, BLOCK,  FREE, BLOCK,  FREE,  FREE,  FREE},
            {FREE, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            {FREE,  FREE,  FREE, BLOCK, START, BLOCK,  FREE,  FREE,  FREE},
            {FREE, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            {FREE, BLOCK, BLOCK,  FREE, BLOCK,  FREE, BLOCK, BLOCK,  FREE},
            {FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE}
    };

    @Override
    public synchronized void run() {
        try {
            this.wait();
        } catch (InterruptedException ignored) {}

        preProcess();

        while(continueRunning){

        }
    }

    public void preProcess(){
        for(byte y = 0; y < preparedMap.length; y ++){
            for(byte x = 0; x < preparedMap[y].length; x++){
                controller.setField(x, y, preparedMap[y][x]);
            }
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
