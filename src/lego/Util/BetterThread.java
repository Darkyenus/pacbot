package lego.util;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 16:45
 */
public abstract class BetterThread extends Thread {

    protected boolean canRun = true;

    public void finish(){
        canRun = false;
    }

    protected void delay(long millis){
        Util.delay(millis);
    }

}
