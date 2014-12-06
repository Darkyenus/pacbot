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

    private final BatchByteQueue pathQueue = new BatchByteQueue(96);

    public final void addXPath(byte x){
        pathQueue.add((byte) (x | X_BIT));
    }

    public final void addYPath(byte y){
        pathQueue.add((byte) (y & Y_ANTIBIT));
    }

    public final void clearPath(){
        pathQueue.clear();
    }

    public abstract byte travelX(byte amount);

    public abstract byte travelY(byte amount);

    public final void travelPath(){
        while(pathQueue.nonEmpty()){
            final byte command = pathQueue.remove();
            final boolean onX = (command & X_BIT) == X_BIT;
            final byte amount = (byte) (((byte)(command << 1)) >> 1);
            if(onX){
                travelX(amount);
            }else{
                travelY(amount);
            }
        }
    }
}
