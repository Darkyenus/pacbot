package lego.bots.weightnav;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.PositionStack;
import lego.util.Queue;

/**
 * Weight navigated bot. This computes weight of every field on map and navigates the shortest way to target (unvisited field with best weight)
 *
 * Private property.
 * User: jIRKA
 * Date: 23/10/14
 * Time: 10:23
 */
public class WeightNavBot extends Bot<EnvironmentController> {

    private static final int STACK_SIZE = 16;

    private boolean continueRunning = true;
    private final byte[][] distances = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    @Override
    public synchronized void run() {
        try {
            this.wait();
        } catch (InterruptedException ignored) {}

        final Queue<EnvironmentController.Direction> directions = new Queue<EnvironmentController.Direction>(STACK_SIZE);
        final Queue<Byte> distances = new Queue<Byte>(STACK_SIZE);

        final PositionStack route = new PositionStack(STACK_SIZE);

        while(continueRunning){
            calcDistances();

            route.clear();
            calcRoute(route);
            directions.clear();
            distances.clear();

            EnvironmentController.Direction actualDir = null;
            byte movingDist = 0;


            byte prevX = 0;
            byte prevY = 0;
            if(!route.isEmpty()) {
                prevX = route.peekX();
                prevY = route.peekY();
                route.pop();
            }

            while(!route.isEmpty()){
                byte nextX = route.peekX();
                byte nextY = route.peekY();
                route.pop();

                if(nextX == prevX && nextY == prevY + 1){
                    if(actualDir == EnvironmentController.Direction.DOWN){
                        movingDist ++;
                    }else{
                        if(movingDist > 0) {
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
                        }
                        actualDir = EnvironmentController.Direction.DOWN;
                        movingDist = 1;
                    }
                }
                if(nextX == prevX && nextY == prevY - 1){
                    if(actualDir == EnvironmentController.Direction.UP){
                        movingDist ++;
                    }else{
                        if(movingDist > 0) {
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
                        }
                        actualDir = EnvironmentController.Direction.UP;
                        movingDist = 1;
                    }
                }
                if(nextX == prevX - 1 && nextY == prevY){
                    if(actualDir == EnvironmentController.Direction.LEFT){
                        movingDist ++;
                    }else{
                        if(movingDist > 0) {
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
                        }
                        actualDir = EnvironmentController.Direction.LEFT;
                        movingDist = 1;
                    }
                }
                if(nextX == prevX + 1 && nextY == prevY){
                    if(actualDir == EnvironmentController.Direction.RIGHT){
                        movingDist ++;
                    }else{
                        if(movingDist > 0) {
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
                        }
                        actualDir = EnvironmentController.Direction.RIGHT;
                        movingDist = 1;
                    }
                }

                prevX = nextX;
                prevY = nextY;
            }
            if(movingDist > 0) {
                directions.pushNext(actualDir);
                distances.pushNext(movingDist);
            }

            while(!directions.isEmpty()){
                actualDir = directions.retreiveFirst();
                movingDist = distances.retreiveFirst();
                if(directions.isEmpty()){
                    byte itMovedBy = movingDist;
                    EnvironmentController.FieldStatus nextTile = controller.getField((byte)(controller.getX() + actualDir.x * itMovedBy), (byte)(controller.getY() + actualDir.y * itMovedBy));
                    while(nextTile != EnvironmentController.FieldStatus.FREE_VISITED && nextTile != EnvironmentController.FieldStatus.START && nextTile != EnvironmentController.FieldStatus.OBSTACLE){
                        itMovedBy ++;
                        nextTile = controller.getField((byte)(controller.getX() + actualDir.x * itMovedBy), (byte)(controller.getY() + actualDir.y * itMovedBy));
                    }
                    if(nextTile == EnvironmentController.FieldStatus.OBSTACLE){
                        controller.move(actualDir);
                    }else{
                        itMovedBy -= 1; //For some reason
                        if(actualDir == EnvironmentController.Direction.DOWN){
                            controller.moveByY(itMovedBy);
                        }else if(actualDir == EnvironmentController.Direction.UP){
                            controller.moveByY((byte)-itMovedBy);
                        }else if(actualDir == EnvironmentController.Direction.LEFT){
                            controller.moveByX((byte)-itMovedBy);
                        }else if(actualDir == EnvironmentController.Direction.RIGHT){
                            controller.moveByX(itMovedBy);
                        }
                    }
                }else{
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
        }
    }

    private static boolean cmpDistFromBorder( byte psX, byte psY, byte ps2X, byte ps2Y ) {
        /* //Original code before simplification
        if( Math.min( psX, EnvironmentController.mazeWidth - 1 - psX ) < Math.min( ps2X, EnvironmentController.mazeWidth - 1 - ps2X ) )
            return true;

        if( Math.min( psY, EnvironmentController.mazeHeight - 1 - psY ) < Math.min( ps2Y, EnvironmentController.mazeHeight - 1 - ps2Y ) )
            return true;

        return false;
        */
        return Math.min(psX, EnvironmentController.mazeWidth - 1 - psX) < Math.min(ps2X, EnvironmentController.mazeWidth - 1 - ps2X) || Math.min(psY, EnvironmentController.mazeHeight - 1 - psY) < Math.min(ps2Y, EnvironmentController.mazeHeight - 1 - ps2Y);

    }

    private void calcRoute(PositionStack outputRoute) {
        byte targetX = Byte.MIN_VALUE;
        byte targetY = Byte.MIN_VALUE;
        byte minDist = Byte.MAX_VALUE;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){

                if( (x != controller.getX() || y != controller.getY()) &&
                        ( controller.getField(x,y) == EnvironmentController.FieldStatus.UNKNOWN || controller.getField(x, y) == EnvironmentController.FieldStatus.FREE_UNVISITED ) &&
                        (( distances[x][y] < minDist) || (distances[x][y] == minDist && cmpDistFromBorder( x, y, targetX,targetY )))) {
                    minDist = distances[ x ][ y ];
                    targetX = x;
                    targetY = y;
                }
            }
        }

        if(targetX == Byte.MIN_VALUE && targetY == Byte.MIN_VALUE){
            continueRunning = false;
            return;
        }

        outputRoute.push(targetX, targetY);

        byte psX = targetX;
        byte psY = targetY;
        byte robotPosX = controller.getX();
        byte robotPosY = controller.getY();
        byte count = 0;
        while( psX != robotPosX || psY != robotPosY ) {
            minDist = Byte.MAX_VALUE;
            targetX = psX;
            targetY = psY;

            if( psX > 0 && distances[ psX - 1 ][ psY ] < minDist ) {
                minDist = distances[ psX - 1 ][ psY ];
                targetX = (byte) (psX - 1);
                targetY = psY;
            }

            if( psY > 0 && distances[ psX ][ psY - 1 ] < minDist ) {
                minDist = distances[ psX ][ psY - 1 ];
                targetX = psX;
                targetY = (byte)(psY - 1);
            }

            if( psX < EnvironmentController.mazeWidth - 1 && distances[ psX + 1 ][ psY ] < minDist ) {
                minDist = distances[psX + 1][psY];
                targetX = (byte) (psX + 1);
                targetY = psY;
            }

            if( psY < EnvironmentController.mazeHeight - 1 && distances[ psX ][ psY + 1 ] < minDist ) {
                targetX = psX;
                targetY = (byte)(psY + 1);
            }

            outputRoute.push(targetX, targetY);
            psX = targetX;
            psY = targetY;

            if( count ++ > 100 ) {
                controller.onError((byte)50);  // Cannot compute route, algo has stacked.
                break;
            }
        }
    }

    private final PositionStack toCalc = new PositionStack(STACK_SIZE); //Used in calcDistances function
    private void calcDistances() {
        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
           for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
               distances[x][y] = Byte.MAX_VALUE;
           }
        }

        distances[controller.getX()][controller.getY()] = 0;
        toCalc.clear();

        toCalc.push(controller.getX(), controller.getY());

        while( ! toCalc.isEmpty() ) {
            byte psX = toCalc.peekX();
            byte psY = toCalc.peekY();
            toCalc.pop();

            if(controller.getField(psX, psY) != EnvironmentController.FieldStatus.OBSTACLE){
                byte psDistActual = distances[ psX ][ psY ];
                byte psDistNew = (byte) (psDistActual + ( controller.getField(psX, psY) == EnvironmentController.FieldStatus.FREE_VISITED  ? 3 : 1 ));
                if( psX > 0 && controller.getField((byte)(psX -1), psY) != EnvironmentController.FieldStatus.OBSTACLE && ( distances[ psX - 1 ][ psY ] > psDistNew ) ) {
                    distances[psX - 1][psY] = psDistNew;
                    toCalc.push((byte) (psX - 1), psY);
                }
                if( psX < EnvironmentController.mazeWidth && controller.getField((byte)(psX + 1), psY) != EnvironmentController.FieldStatus.OBSTACLE && ( distances[ psX + 1 ][ psY ] > psDistNew ) ) {
                    distances[psX + 1][psY] = psDistNew;
                    toCalc.push((byte) (psX + 1), psY);
                }
                if( psY > 0 && controller.getField(psX , (byte)(psY - 1)) != EnvironmentController.FieldStatus.OBSTACLE && controller.getField(psX, psY) != EnvironmentController.FieldStatus.START  && ( distances[ psX ][ psY - 1 ] > psDistNew ) ) {
                    distances[psX][psY - 1] = psDistNew;
                    toCalc.push(psX, (byte) (psY - 1));
                }
                if( psY < EnvironmentController.mazeHeight && controller.getField(psX , (byte)(psY + 1)) != EnvironmentController.FieldStatus.OBSTACLE && controller.getField(psX, (byte)(psY + 1)) != EnvironmentController.FieldStatus.START && ( distances[ psX ][ psY + 1 ] > psDistNew ) ) {
                    distances[psX][psY + 1] = psDistNew;
                    toCalc.push(psX, (byte) (psY + 1));
                }
            }
        }
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
