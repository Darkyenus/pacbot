package lego.api;

/**
 * Private property.
 * User: Darkyen56788
 * Date: 19/10/14
 * Time: 13:45
 */
public abstract class BotController {

    protected boolean isInitialized = false;

    public boolean isInitialized(){
        return isInitialized;
    }

    public void initializeController(){
        initialize();
        isInitialized = true;
    }

    public void deinitializeController(){
        deinitialize();
        isInitialized = false;
    }

    protected void initialize(){}
    protected void deinitialize(){}
    public void onError(byte errorid){}
}
