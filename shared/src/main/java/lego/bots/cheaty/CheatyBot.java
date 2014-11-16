package lego.bots.cheaty;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.PositionStack;
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


    private final EnvironmentController.FieldStatus[][] preparedMap = {
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            {BLOCK, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE},
            { FREE,  FREE, BLOCK, BLOCK, START, BLOCK,  FREE,  FREE,  FREE},
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            { FREE,  FREE,  FREE, BLOCK, BLOCK, BLOCK,  FREE, BLOCK,  FREE},
            { FREE,  FREE,  FREE, BLOCK,  FREE,  FREE,  FREE, BLOCK,  FREE}
    };

    final Queue<EnvironmentController.Direction> directions = new Queue<EnvironmentController.Direction>(STACK_SIZE);
    final Queue<Byte> distances = new Queue<Byte>(STACK_SIZE);
    final PositionStack route = new PositionStack(STACK_SIZE);

    private final byte[][] fieldValues = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    private final byte[][] areaIDs = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    @Override
    public synchronized void run() {
        try {
            this.wait();
        } catch (InterruptedException ignored) {}

        directions.clear();
        distances.clear();
        route.clear();

        prepare();

        EnvironmentController.Direction actualDir = null;
        byte movingDist = 0;

        /*
          This actually runs previously computed route.
          There should be some error handling done, like on unexpected collision: somehow recalculate path.
        */

        byte prevX = route.peekX();
        byte prevY = route.peekY();
        route.pop();

        if(true)
            return;

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

    public void findAreas(){
        for(byte x = 0; x < areaIDs.length; x ++){
            for(byte y = 0; y < areaIDs[x].length; y++){
                areaIDs[x][y] = 0; //0 means no area
            }
        }
        byte highestID = 0;
        for(byte x = 0; x < areaIDs.length - 1; x ++){
            for(byte y = 0; y < areaIDs[x].length - 1; y++){
                byte id = areaIDs[x][y];
                if(preparedMap[y][x] != EnvironmentController.FieldStatus.OBSTACLE){
                    if((preparedMap[y][x + 1] != EnvironmentController.FieldStatus.OBSTACLE && (areaIDs[x + 1][y] == id || areaIDs[x + 1][y] == 0)) &&
                            (preparedMap[y + 1][x] != EnvironmentController.FieldStatus.OBSTACLE && (areaIDs[x][y + 1] == id || areaIDs[x][y + 1] == 0)) &&
                            (preparedMap[y + 1][x + 1] != EnvironmentController.FieldStatus.OBSTACLE && (areaIDs[x + 1][y + 1] == id || areaIDs[x + 1][y + 1] == 0))){

                        if(id == 0){
                            id = highestID ++;
                        }
                        areaIDs[x][y] = id;
                        areaIDs[x + 1][y] = id;
                        areaIDs[x][y + 1] = id;
                        areaIDs[x + 1][y + 1] = id;
                    }else{
                        //TODO
                    }
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

        findAreas();

        byte simX = EnvironmentController.startX;
        byte simY = EnvironmentController.startY;

        final PositionStack crossings = new PositionStack(STACK_SIZE);
        crossings.push(simX, simY);

        while(!crossings.isEmpty()){
            byte possibleWays = possibleWays(simX, simY);
            if (possibleWays == 0) {//No way

                System.out.println("Returning to:");

                byte targetX = crossings.peekX();
                byte targetY = crossings.peekY();
                crossings.pop();

                calcDistances();
                calcRoute(route, targetX, targetY);

                simX = targetX;
                simY = targetY;

                System.out.println("X: "+simX+", Y: "+simY);

            } else if ((possibleWays & (possibleWays - 1)) == 0) { //Kind of magic. Only one possible way
                System.out.println("Only one way, chosen:");
                if ((possibleWays & 8) == 8) { //Possible way UP
                    simY -= 1;
                    controller.moveByY((byte)-1);
                }
                if ((possibleWays & 4) == 4) { //Possible way RIGHT
                    simX += 1;
                    controller.moveByX((byte) 1);
                }
                if ((possibleWays & 2) == 2) { //Possible way DOWN
                    simY += 1;
                    controller.moveByY((byte) 1);
                }
                if ((possibleWays & 1) == 1) { //Possible way LEFT
                    simX -= 1;
                    controller.moveByX((byte) -1);
                }

                controller.setField(simX, simY, EnvironmentController.FieldStatus.FREE_VISITED);
                //route.push(simX, simY);

            } else { //Multiple possible ways
                byte bestCount = Byte.MAX_VALUE;
                EnvironmentController.Direction bestDirection = null;
                resetToIterate();

                if ((possibleWays & 8) == 8) { //Possible way UP
                    byte count = countDots(simX, (byte) (simY - 1), EnvironmentController.Direction.DOWN, simX, simY);
                    if (count != -1 && count < bestCount) {
                        bestCount = count;
                        bestDirection = EnvironmentController.Direction.UP;
                    }
                }
                if ((possibleWays & 4) == 4) { //Possible way RIGHT
                    byte count = countDots((byte) (simX + 1), simY, EnvironmentController.Direction.LEFT, simX, simY);
                    if (count != -1 &&  count < bestCount) {
                        bestCount = count;
                        bestDirection = EnvironmentController.Direction.RIGHT;
                    }
                }
                if ((possibleWays & 2) == 2) { //Possible way DOWN
                    byte count = countDots(simX, (byte) (simY + 1), EnvironmentController.Direction.UP, simX, simY);
                    if (count != -1 && count < bestCount) {
                        bestCount = count;
                        bestDirection = EnvironmentController.Direction.DOWN;
                    }
                }
                if ((possibleWays & 1) == 1) { //Possible way LEFT
                    byte count = countDots((byte) (simX - 1), simY, EnvironmentController.Direction.RIGHT, simX, simY);
                    if (count != -1 && count < bestCount) {
                        bestDirection = EnvironmentController.Direction.LEFT;
                    }
                }

                if(bestCount == Byte.MAX_VALUE){
                    System.out.println("TODO - cyclic stuff, terminating");
                    return;
                }else{
                    crossings.push(simX, simY);

                    simX += bestDirection.x;
                    simY += bestDirection.y;

                    System.out.println("Multiple ways, chosen:");


                    if(bestDirection.x == 0){
                        controller.moveByY(bestDirection.y);
                    }else{
                        controller.moveByX(bestDirection.x);
                    }
                    //controller.setField(simX, simY, EnvironmentController.FieldStatus.FREE_VISITED);

                    route.push(simX, simY);

                }

            }
        }
    }

    private byte possibleWays(byte fromX, byte fromY){
        byte result = 0;
        if(controller.getField(fromX, (byte)(fromY - 1)) == EnvironmentController.FieldStatus.FREE_UNVISITED){
            result = (byte) (result | 8);
        }
        if(controller.getField((byte)(fromX + 1), fromY) == EnvironmentController.FieldStatus.FREE_UNVISITED){
            result = (byte) (result | 4);
        }
        if(controller.getField(fromX, (byte)(fromY + 1)) == EnvironmentController.FieldStatus.FREE_UNVISITED){
            result = (byte) (result | 2);
        }
        if(controller.getField((byte)(fromX - 1), fromY) == EnvironmentController.FieldStatus.FREE_UNVISITED){
            result = (byte) (result | 1);
        }

        return result;
    }

    private boolean[][] toIterate = new boolean[preparedMap[0].length][preparedMap.length];

    private void resetToIterate(){
        for(byte y = 0; y < preparedMap.length; y ++){
            for(byte x = 0; x < preparedMap[y].length; x++){
                toIterate[x][y] = preparedMap[y][x] == FREE;
            }
        }
    }

    private byte countDots(byte x, byte y, EnvironmentController.Direction from, byte masterStartX, byte masterStartY){

        if(x == masterStartX && y == masterStartY){
            return -1;
        }

        byte result = 0;

        if(x > 0 && y > 0 && x < toIterate.length && y < toIterate[x].length && toIterate[x][y]){
            result ++;
            toIterate[x][y] = false;

            if(controller.getField(x, y) != EnvironmentController.FieldStatus.START && from != EnvironmentController.Direction.UP){
                byte val = countDots(x, (byte)(y - 1), EnvironmentController.Direction.DOWN, masterStartX, masterStartY);
                if(val == -1)
                    return -1;
                result += val;
            }
            if(from != EnvironmentController.Direction.RIGHT){
                byte val = countDots((byte)(x + 1), y, EnvironmentController.Direction.LEFT, masterStartX, masterStartY);
                if(val == -1)
                    return -1;
                result += val;
            }
            if(controller.getField(x, (byte)(y + 1)) != EnvironmentController.FieldStatus.START && from != EnvironmentController.Direction.DOWN){
                byte val = countDots(x, (byte)(y + 1), EnvironmentController.Direction.UP, masterStartX, masterStartY);
                if(val == -1)
                    return -1;
                result += val;
            }
            if(from != EnvironmentController.Direction.LEFT){
                byte val = countDots((byte)(x - 1), y, EnvironmentController.Direction.RIGHT, masterStartX, masterStartY);
                if(val == -1)
                    return -1;
                result += val;
            }
        }


        return result;
    }

    private void calcRoute(PositionStack outputRoute, byte targetX, byte targetY) {
        byte minDist;

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

            if( psX > 0 && fieldValues[ psX - 1 ][ psY ] < minDist ) {
                minDist = fieldValues[ psX - 1 ][ psY ];
                targetX = (byte) (psX - 1);
                targetY = psY;
            }

            if( psY > 0 && fieldValues[ psX ][ psY - 1 ] < minDist ) {
                minDist = fieldValues[ psX ][ psY - 1 ];
                targetX = psX;
                targetY = (byte)(psY - 1);
            }

            if( psX < EnvironmentController.mazeWidth - 1 && fieldValues[ psX + 1 ][ psY ] < minDist ) {
                minDist = fieldValues[psX + 1][psY];
                targetX = (byte) (psX + 1);
                targetY = psY;
            }

            if( psY < EnvironmentController.mazeHeight - 1 && fieldValues[ psX ][ psY + 1 ] < minDist ) {
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
                fieldValues[x][y] = Byte.MAX_VALUE;
            }
        }

        fieldValues[controller.getX()][controller.getY()] = 0;
        toCalc.clear();

        toCalc.push(controller.getX(), controller.getY());

        while( ! toCalc.isEmpty() ) {
            byte psX = toCalc.peekX();
            byte psY = toCalc.peekY();
            toCalc.pop();

            if(controller.getField(psX, psY) != EnvironmentController.FieldStatus.OBSTACLE){
                byte psDistActual = fieldValues[ psX ][ psY ];
                byte psDistNew = (byte) (psDistActual + ( controller.getField(psX, psY) == EnvironmentController.FieldStatus.FREE_VISITED  ? 3 : 1 ));
                if( psX > 0 && controller.getField((byte)(psX -1), psY) != EnvironmentController.FieldStatus.OBSTACLE && ( fieldValues[ psX - 1 ][ psY ] > psDistNew ) ) {
                    fieldValues[psX - 1][psY] = psDistNew;
                    toCalc.push((byte) (psX - 1), psY);
                }
                if( psX < EnvironmentController.mazeWidth && controller.getField((byte)(psX + 1), psY) != EnvironmentController.FieldStatus.OBSTACLE && ( fieldValues[ psX + 1 ][ psY ] > psDistNew ) ) {
                    fieldValues[psX + 1][psY] = psDistNew;
                    toCalc.push((byte) (psX + 1), psY);
                }
                if( psY > 0 && controller.getField(psX , (byte)(psY - 1)) != EnvironmentController.FieldStatus.OBSTACLE && controller.getField(psX, psY) != EnvironmentController.FieldStatus.START  && ( fieldValues[ psX ][ psY - 1 ] > psDistNew ) ) {
                    fieldValues[psX][psY - 1] = psDistNew;
                    toCalc.push(psX, (byte) (psY - 1));
                }
                if( psY < EnvironmentController.mazeHeight && controller.getField(psX , (byte)(psY + 1)) != EnvironmentController.FieldStatus.OBSTACLE && controller.getField(psX, (byte)(psY + 1)) != EnvironmentController.FieldStatus.START && ( fieldValues[ psX ][ psY + 1 ] > psDistNew ) ) {
                    fieldValues[psX][psY + 1] = psDistNew;
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
