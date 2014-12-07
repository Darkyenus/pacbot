package lego.api.controllers;

import lego.util.BatchByteQueue;

/**
 * Max supported x/y amount is 63. Undefined results for numbers any bigger.
 * Min is -64.
 *
 * Private property.
 * User: Darkyen
 * Date: 02/12/14
 * Time: 17:26
 */
public abstract class PlannedController extends MapAwareController {

    private static final byte X_BIT = (byte) 0x80;//1000_0000
    private static final byte Y_ANTIBIT = ~X_BIT; //0111_1111

    /** Path queue stores path sequentially encoded in byte values.
     * First bit (X_BIT, most significant) is reserved. If 1, then movement is on X axis, if 0, on y axis.
     * Rest is twos complement encoded signed amount of steps in that direction. */
    private final BatchByteQueue pathQueue = new BatchByteQueue(96);

    public final void addXPath(byte x){
        pathQueue.add((byte) (x | X_BIT));//Sets first bit to 1
    }

    public final void addYPath(byte y){
        pathQueue.add((byte) (y & Y_ANTIBIT));//Sets first bit to 0
    }

    public final void clearPath(){
        pathQueue.clear();
    }

    public abstract byte travelX(byte amount);

    public abstract byte travelY(byte amount);

    public final void travelPath(){
        while(pathQueue.nonEmpty()){
            final byte command = pathQueue.remove();
            final boolean onX = (command & X_BIT) == X_BIT;//Check if first bit is present
            //Set first bit to the value of second bit.
            //That is done by shifting whole 8bit value so many times, that first value is lost. Then it is shifted back.
            //Due to way how java handles signed shifts, given task is accomplished.
            final byte amount = (byte) ((command << 25) >> 25);
            if(onX){
                travelX(amount);
            }else{
                travelY(amount);
            }
        }
    }
}
