package lego.bots.test;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.Latch;

/**
 * Bot class created only for testing some subsystems of controllers. Use and edit as you wish
 *
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 10:23
 */
@SuppressWarnings("UnusedDeclaration")
public class TestBot extends Bot<EnvironmentController> {

    private boolean continueRunning = true;
    private final Latch startLatch = new Latch();

    @Override
    public synchronized void run() {
        startLatch.pass();
        while(continueRunning){
            //tightLoop((byte)2);
            thereAndBackAgain((byte)2);
        }
    }

    private void thereAndBackAgain(byte dist){
        controller.moveByY(dist);
        controller.moveByY((byte)-dist);
        controller.moveByX(dist);
        controller.moveByX((byte)-dist);
    }

    private void tightLoop(byte dist){
        controller.moveByY(dist);
        controller.moveByX(dist);
        controller.moveByY((byte)-dist);
        controller.moveByX((byte)-dist);
    }

    private void figureEight(byte dist){
        controller.moveByY(dist);
        controller.moveByX((byte)-dist);
        controller.moveByY((byte)-dist);
        controller.moveByX((byte) -dist);
        controller.moveByY(dist);
        controller.moveByX(dist);
        controller.moveByY((byte) -dist);
        controller.moveByX(dist);
    }

    @Override
    public void onEvent(BotEvent event) {
        switch (event){
            case RUN_ENDED:
                continueRunning = false;
                startLatch.open();
                break;
            case RUN_STARTED:
                continueRunning = true;
                startLatch.open();
                break;
        }
    }
}
