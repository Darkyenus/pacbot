package lego.bots.cheaty;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.Queue;

/**
 * This bot is called cheaty, because it uses predefined map and it is cheat against the original rules. However after dozens of rule patches is this behaviour acceptable.
 *
 * Private property.
 * User: jIRKA
 * Date: 23/10/14
 * Time: 10:23
 */
public class CheatyBot extends Bot<EnvironmentController> {

    private static final int STACK_SIZE = 16;

    private boolean continueRunning = true;

    private static final EnvironmentController.FieldStatus FREE = EnvironmentController.FieldStatus.FREE_UNVISITED;
    private static final EnvironmentController.FieldStatus BLOCK = EnvironmentController.FieldStatus.OBSTACLE;
    private static final EnvironmentController.FieldStatus START = EnvironmentController.FieldStatus.START;

    //Default map from video
    private final EnvironmentController.FieldStatus[][] preparedMap = {
            {FREE,  FREE,  FREE, BLOCK,  FREE, BLOCK,  FREE,  FREE,  FREE},
            {FREE, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            {FREE,  FREE,  FREE, BLOCK, START, BLOCK,  FREE,  FREE,  FREE},
            {FREE, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            {FREE, BLOCK, BLOCK,  FREE, BLOCK,  FREE, BLOCK, BLOCK,  FREE},
            {FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE}
    };

    final Queue<EnvironmentController.Direction> directions = new Queue<EnvironmentController.Direction>(STACK_SIZE);
    final Queue<Byte> distances = new Queue<Byte>(STACK_SIZE);

    @Override
    public synchronized void run() {
        try {
            this.wait();
        } catch (InterruptedException ignored) {}

        directions.clear();
        distances.clear();

        prepare();

        EnvironmentController.Direction actualDir;
        byte movingDist = 0;

        /*
          This actually runs previously computed route.
          There should be some error handling done, like on unexpected collision: somehow recalculate path.
        */

        while(!directions.isEmpty() && continueRunning){
            actualDir = directions.retreiveFirst();
            movingDist = distances.retreiveFirst();

            EnvironmentController.FieldStatus nextTile = controller.getField((byte)(controller.getX() + actualDir.x * (movingDist + 1)), (byte)(controller.getY() + actualDir.y * (movingDist + 1)));
            if(nextTile == EnvironmentController.FieldStatus.OBSTACLE){
                controller.move(actualDir);
            }else {
                if (actualDir == EnvironmentController.Direction.DOWN) {
                    controller.moveByY(movingDist);
                } else if (actualDir == EnvironmentController.Direction.UP) {
                    controller.moveByY((byte) -movingDist);
                } else if (actualDir == EnvironmentController.Direction.LEFT) {
                    controller.moveByX((byte) -movingDist);
                } else if (actualDir == EnvironmentController.Direction.RIGHT) {
                    controller.moveByX(movingDist);
                }
            }
        }
    }

    public void prepare(){
        for(byte y = 0; y < preparedMap.length; y ++){
            for(byte x = 0; x < preparedMap[y].length; x++){
                controller.setField(x, y, preparedMap[y][x]);
            }
        }

        //TODO compute route

    }


    @Override
    public void onEvent(BotEvent event) {
        switch (event){
            case RUN_ENDED:
                continueRunning = false;
                synchronized (this){
                    notifyAll(); //Should wake up the main thread.
                }
                break;
            case RUN_STARTED:
                continueRunning = true;
                synchronized (this){
                    notifyAll(); //Should wake up the main thread.
                }
                break;
        }
    }
}
