package lego.api;

/**
 * Private property.
 * User: Darkyen
 * Date: 21/10/14
 * Time: 20:22
 */
public abstract class AbstractBootstrap {

    /**
     * Call this method from non-abstract bootstrap with selected bot and controller.
     */
    public static <C extends BotController> void  main(Bot<C> bot,C controller){
        controller.initialize();
        bot.controller = controller;
        bot.run();
        controller.deinitialize();
    }
}
