package lego.nxt.util;

import lego.api.controllers.EnvironmentController;

/**
* Created by a on 13.11.2014.
*/
public abstract class AbstractMoveTask extends TaskProcessor.Task implements EnvironmentController.MoveFieldsTask {

    @Override
    protected abstract void process();

    protected byte moved;
    protected volatile boolean done;

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public byte moved() {
        waitUntilDone();
        return moved;
    }

    @Override
    public void waitUntilDone() {
        if(done)return;
        synchronized (this) {
            while (!done){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    done = true;
                }
            }
        }
    }

    protected void doComplete(){
        if(done)return;
        synchronized (this){
            done = true;
            this.notify();
        }
    }
}
