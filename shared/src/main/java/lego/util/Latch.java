package lego.util;

/**
 *
 * A simple, light boolean version of CountDownLatch.
 * Starts closed by default. When closed, all pass() calls will block.
 * When open() is called, every blocked call of pass() is let through.
 *
 * Private property.
 * User: Darkyen
 * Date: 26/11/14
 * Time: 16:43
 */
public final class Latch {
    private boolean open;
    private final Object LOCK = new Object();

    /**
     * Create with custom default state.
     * @param open state
     */
    public Latch(boolean open) {
        this.open = open;
    }

    /**
     * Create initially closed Latch.
     */
    public Latch() {
        this.open = false;
    }

    /**
     * Will open this latch for passing through.
     */
    public void open(){
        synchronized (LOCK){
            open = true;
            LOCK.notifyAll();
        }
    }

    /**
     * Will close the latch again.
     * Does not trigger anything, just for reset purposes.
     */
    public void close(){
        synchronized (LOCK){
            open = false;
        }
    }

    /**
     * Will try to go through the Latch.
     * If latch is open, will return almost immediately.
     * (The call is always synchronized, so don't call in performance critical situations.)
     * If the latch is closed, will block until some other thread calls open() and will then return.
     */
    public void pass(){
        synchronized (LOCK){
            while(!open){
                try {
                    LOCK.wait(1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
