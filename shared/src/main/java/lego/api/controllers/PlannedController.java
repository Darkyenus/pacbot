package lego.api.controllers;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.util.BatchByteQueue;
import lego.api.controllers.EnvironmentController.Direction;

import java.util.Random;

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

    public abstract byte travelX(byte amount,Direction nextDirection);

    public abstract byte travelY(byte amount,Direction nextDirection);

    public final int pathSize(){
        return pathQueue.size();
    }

    private boolean onX(byte command){
        return (command & X_BIT) == X_BIT;//Check if first bit is present
    }
    private byte amount(byte command){
        //Set first bit to the value of second bit.
        //That is done by shifting whole 8bit value so many times, that first value is lost. Then it is shifted back.
        //Due to way how java handles signed shifts, given task is accomplished.
        return (byte) ((command << 25) >> 25);
    }
    public final void travelPath(){
        if(pathQueue.isEmpty())return;
        boolean onX;
        byte amount;
        {
            byte command = pathQueue.remove();
            onX = onX(command);
            amount = amount(command);
        }

        while(true){
            final byte nextCommand = pathQueue.isEmpty() ? 0 : pathQueue.remove();
            final boolean nextOnX = onX(nextCommand);
            final byte nextAmount = amount(nextCommand);

            byte actuallyTravelled;
            if(onX){
                actuallyTravelled = travelX(amount, Direction.toDirection(nextOnX, nextAmount));
            }else{
                actuallyTravelled = travelY(amount, Direction.toDirection(nextOnX, nextAmount));
            }
            if(actuallyTravelled != amount){
                Bot.active.onEvent(BotEvent.BOT_LOST);
            }
            onX = nextOnX;
            amount = nextAmount;
            if(amount == 0 && pathQueue.isEmpty())break;
        }
    }

    private boolean berserk = false;
    /**
     * This method cleans stacks and goes berserk. Also, blocks for ever. Make sure that travelPath is not running!
     */
    public final void takeoverAndGoRandom(){
        if(berserk)return;
        berserk = true;
        clearPath();
        Random random = new Random();
        //noinspection InfiniteLoopStatement
        while(true){
            try{
                travelX((byte) (random.nextInt(4)+1),Direction.UP);
                travelY((byte) (random.nextInt(4) + 1), Direction.LEFT);
                travelX((byte) -(random.nextInt(4)+1),Direction.DOWN);
                travelY((byte) -(random.nextInt(4)+1),Direction.RIGHT);
            }catch (Exception e){
                //I don't care
            }
        }
    }
}
