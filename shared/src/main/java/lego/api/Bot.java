package lego.api;

/**
 * Private property.
 * User: Darkyen
 * Date: 19/10/14
 * Time: 13:41
 */
public abstract class Bot <C extends BotController> {

    /**
     * This should be written to only from Bootstrap and read only from controller() method.
     *
     * Tl;dr: DON'T TOUCH
     */
    C controllerInstance;

    /**
     * Provided controller of robot. Use this to interact with outer world.
     */
    protected final C controller(){
        return controllerInstance;
    }

    public abstract void run();

    public abstract void onEvent(BotEvent event,Object param);
}
