package lego.nxt.bootstrap;

import lego.api.Bot;
import lego.api.BotController;
import lego.bots.weightnav.WeightNavBot;
import lego.nxt.controllers.DifferentialEnvironmentRobotController;

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
    public static <C extends BotController> void main(final Bot<C> bot, final C controller){
        controller.initialize();
        bot.controller = controller;
        bot.run();
        controller.deinitialize();

        System.exit(0);
    }
}
