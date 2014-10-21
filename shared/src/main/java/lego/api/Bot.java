package lego.api;

/**
 * Private property.
 * User: Darkyen
 * Date: 19/10/14
 * Time: 13:41
 */
public abstract class Bot <C extends BotController> {

    /**
     * Provided controller of robot. Use this to interact with outer world.
     */
    protected final C controller;

    protected Bot(C controller) {
        this.controller = controller;
    }


    public abstract void run();

    public abstract void onEvent(BotEvent event,Object param);
}
