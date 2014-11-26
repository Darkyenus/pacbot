package lego.nxt.bootstrap;

import lego.api.Bot;
import lego.api.BotController;
import lego.api.BotEvent;
import lego.bots.weightnav.WeightNavBot;
import lego.nxt.controllers.DifferentialEnvironmentRobotController;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 11:01
 */
public class MainBootstrap {
    public static void main(String[] args){
        main(
                //new CheatyBot()
                //new CleverBot()
                //new NodeBot()
                //new TestificateBot()
                new WeightNavBot()
                ,
                new DifferentialEnvironmentRobotController()
        );
    }

    /**
     * Call this method from non-abstract bootstrap with selected bot and controller.
     */
    public static <C extends BotController> void  main(final Bot<C> bot, final C controller){
        ButtonListener buttonListener = new ButtonListener() {
            @Override
            public void buttonPressed(Button button) {
                if(button == Button.ENTER){
                    bot.onEvent(BotEvent.ENTER_PRESSED);
                }else if(button == Button.ESCAPE){
                    bot.onEvent(BotEvent.ESCAPE_PRESSED);
                }else if(button == Button.LEFT){
                    bot.onEvent(BotEvent.LEFT_PRESSED);
                }else if(button == Button.RIGHT){
                    bot.onEvent(BotEvent.RIGHT_PRESSED);
                }
            }

            @Override
            public void buttonReleased(Button button) {}
        };
        Button.ENTER.addButtonListener(buttonListener);
        Button.ESCAPE.addButtonListener(buttonListener);
        Button.LEFT.addButtonListener(buttonListener);
        Button.RIGHT.addButtonListener(buttonListener);

        controller.initialize();
        bot.controller = controller;
        bot.run();
        controller.deinitialize();

        System.exit(0);
    }
}
