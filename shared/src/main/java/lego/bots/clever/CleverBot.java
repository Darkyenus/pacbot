package lego.bots.clever;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.Latch;
import lego.util.PositionQueue;
import lego.util.PositionStack;
import lego.util.Queue;

/**
 * Private property.
 * User: jIRKA
 * Date: 23.11.2014
 * Time: 15:07
 */
public class CleverBot  extends Bot<EnvironmentController> {

    private final Latch startLatch = new Latch();

    private final byte MAX_LINE_LENGTH = 3;
    private static final int STACK_SIZE = 16;

    private final byte[][] maze = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];
    private final byte[][] distances = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];
    byte botX = EnvironmentController.startX;
    byte botY = EnvironmentController.startY + 1; //I don't want to handle special cases for start field again.
    EnvironmentController.Direction botDir = EnvironmentController.Direction.DOWN;
    private final PositionQueue outputRoute = new PositionQueue(STACK_SIZE);
    private final Queue<EnvironmentController.Direction> pDirections = new Queue<EnvironmentController.Direction>(STACK_SIZE);
    private final Queue<Byte> pDistances = new Queue<Byte>(STACK_SIZE);

    private void preprocess(){
        outputRoute.pushNext(botX, botY);

        byte start;
        //Sorry about this not cache optimal iteration, but i had to do it
        for(byte y = 0; y < EnvironmentController.mazeHeight; y++) {
            //start = -1;
            for (byte x = 0; x < EnvironmentController.mazeWidth; x++) {
                /*maze[x][y] = -1; //We don't intent to go there
                if (controller.getField(x, y) == EnvironmentController.FieldStatus.FREE_UNVISITED) {
                    if (x - start == MAX_LINE_LENGTH) {
                        maze[start + 1][y] = 0; //Go there
                        maze[start + 2][y] = 0;
                        maze[x][y] = 0;
                    } else if (x - start > MAX_LINE_LENGTH) {
                        maze[x][y] = 0;
                    }
                } else {
                    start = x;*/
                    maze[x][y] = -2; //Cannot go there
                //}
            }
        }
        /*for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            start = -1;
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                if(controller.getField(x,y) == EnvironmentController.FieldStatus.FREE_UNVISITED){
                    if(y - start == MAX_LINE_LENGTH){
                        maze[x][start + 1] = 0;
                        maze[x][start + 2] = 0;
                        maze[x][y] = 0;
                    }else if(y - start > MAX_LINE_LENGTH){
                        maze[x][y] = 0;
                    }
                }
            }
        }*/
    }

    public void prepare(){

        preprocess();

        moveNext();

        //TODO: postprocess();

        EnvironmentController.Direction actualDir = null;
        byte movingDist = 0;

        byte prevX = 0;
        byte prevY = 0;
        if(!outputRoute.isEmpty()) {
            prevX = outputRoute.retreiveFirstX();
            prevY = outputRoute.retreiveFirstY();
            outputRoute.moveReadHead();
        }

        //Preprocess path
        while(!outputRoute.isEmpty()){
            byte nextX = outputRoute.retreiveFirstX();
            byte nextY = outputRoute.retreiveFirstY();
            outputRoute.moveReadHead();

            if (nextX == prevX && nextY == prevY + 1) {
                if (actualDir == EnvironmentController.Direction.DOWN) {
                    movingDist++;
                } else {
                    if (movingDist > 0) {
                        pDirections.pushNext(actualDir);
                        pDistances.pushNext(movingDist);
                    }
                    actualDir = EnvironmentController.Direction.DOWN;
                    movingDist = 1;
                }
            }
            if (nextX == prevX && nextY == prevY - 1) {
                if (actualDir == EnvironmentController.Direction.UP) {
                    movingDist++;
                } else {
                    if (movingDist > 0) {
                        pDirections.pushNext(actualDir);
                        pDistances.pushNext(movingDist);
                    }
                    actualDir = EnvironmentController.Direction.UP;
                    movingDist = 1;
                }
            }
            if (nextX == prevX - 1 && nextY == prevY) {
                if (actualDir == EnvironmentController.Direction.LEFT) {
                    movingDist++;
                } else {
                    if (movingDist > 0) {
                        pDirections.pushNext(actualDir);
                        pDistances.pushNext(movingDist);
                    }
                    actualDir = EnvironmentController.Direction.LEFT;
                    movingDist = 1;
                }
            }
            if (nextX == prevX + 1 && nextY == prevY) {
                if (actualDir == EnvironmentController.Direction.RIGHT) {
                    movingDist++;
                } else {
                    if (movingDist > 0) {
                        pDirections.pushNext(actualDir);
                        pDistances.pushNext(movingDist);
                    }
                    actualDir = EnvironmentController.Direction.RIGHT;
                    movingDist = 1;
                }
            }

            prevX = nextX;
            prevY = nextY;
        }
        if(movingDist > 0) {
            pDirections.pushNext(actualDir);
            pDistances.pushNext(movingDist);
        }

        controller.onError(EnvironmentController.SUCCESS_PATH_COMPUTED);

    }

    private void checkForDeadEnds(){
        //TODO
    }

    private void moveNext(){

        if(botX > 0 && botY > 0 && botX < EnvironmentController.mazeWidth && botY < EnvironmentController.mazeHeight && (maze[botX + botDir.x][botY + botDir.y] == 0 || maze[botX + botDir.x][botY + botDir.y] == -1)){
            maze[botX][botY] ++;
            botX += botDir.x;
            botY += botDir.y;
            outputRoute.pushNext(botX, botY);
        }else{
            checkForDeadEnds();
            EnvironmentController.Direction leftDir = botDir.left;
            EnvironmentController.Direction rightDir = botDir.right;
            byte leftDist, rightDist;

            for(leftDist = 0; leftDist < 10; leftDist ++){
                if(maze[botX + leftDir.x * leftDist][botY + leftDir.y * leftDist] == 0){
                    leftDist ++;
                }else{
                    break;
                }
            }
            for(rightDist = 0; rightDist < 10; rightDist ++){
                if(maze[botX + rightDir.x * rightDist][botY + rightDir.y * rightDist] == 0){
                    rightDist ++;
                }else{
                    break;
                }
            }

            if(leftDist != 0 && leftDist < rightDist){
                for(byte i = 0; i < leftDist; i++){
                    botX += leftDir.x;
                    botY += leftDir.y;
                    maze[botX][botY] ++;
                    outputRoute.pushNext(botX, botY);
                }
                botDir = leftDir;
            }else if(rightDist != 0){
                for(byte i = 0; i < rightDist; i++){
                    botX += rightDir.x;
                    botY += rightDir.y;
                    maze[botX][botY] ++;
                    outputRoute.pushNext(botX, botY);
                }
                botDir = rightDir;
            }else if(leftDist == 0){
                calcDistances();
                calcRoute();
            }
        }
    }

    private final PositionStack toCalc = new PositionStack(STACK_SIZE); //Used in calcDistances function
    private byte lastDir = 0;
    private void calcDistances() {
        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                distances[x][y] = Byte.MAX_VALUE;
            }
        }

        distances[botX][botY] = 0;
        toCalc.clear();

        toCalc.push(botX, botY);

        while( !toCalc.isEmpty() ) {
            byte psX = toCalc.peekX();
            byte psY = toCalc.peekY();
            toCalc.pop();

            if(controller.getField(psX, psY) != EnvironmentController.FieldStatus.OBSTACLE && controller.getField(psX, psY) != EnvironmentController.FieldStatus.START){
                byte psDistActual = distances[ psX ][ psY ];
                byte psDistNew = (byte) (psDistActual + (maze[psX][psY] == 0  ? 1 : (maze[psX][psY] == -1 ? 3 : 5) ));
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
    private boolean calcRoute() {
        byte targetX = Byte.MIN_VALUE;
        byte targetY = Byte.MIN_VALUE;
        byte minDist = Byte.MAX_VALUE;
        byte dist;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                if((x != botX || y != botY) && maze[x][y] == 0){
                    dist = (byte)(distances[x][y] * 2);

                    if(botX == x && botY == y + 1 && lastDir == 8){ //Moved up
                        dist -= 1;
                    }else if(botX == x && botY == y - 1 && lastDir == 2){ //Moved down
                        dist -= 1;
                    }else if(botX == x + 1&& botY == y && lastDir == 1){ //Moved left
                        dist -= 1;
                    }else if(botX == x - 1 && botY == y && lastDir == 4){ //Moved right
                        dist -= 1;
                    }

                    if(dist < minDist){// || (dist == minDist && cmpDistFromBorder( x, y, targetX,targetY ))){

                        minDist = dist;
                        targetX = x;
                        targetY = y;

                    }
                }
            }
        }

        if(targetX == Byte.MIN_VALUE && targetY == Byte.MIN_VALUE){
            return true;
        }

        PositionStack tmp = new PositionStack(STACK_SIZE);
        tmp.push(targetX, targetY);

        controller.setField(targetX, targetY, EnvironmentController.FieldStatus.FREE_VISITED);

        byte psX = targetX;
        byte psY = targetY;
        byte robotPosX = botX;
        byte robotPosY = botY;
        botX = psX;
        botY = psY;
        byte count = 0;
        while( psX != robotPosX || psY != robotPosY ) {
            minDist = Byte.MAX_VALUE;
            targetX = psX;
            targetY = psY;

            if( psX > 0 && (distances[ psX - 1 ][ psY ] < minDist || (distances[ psX - 1 ][ psY ] <= minDist && lastDir == 1))) {
                minDist = distances[ psX - 1 ][ psY ];
                targetX = (byte) (psX - 1);
                targetY = psY;
                lastDir = 1;
            }

            if( psY > 0 && (distances[ psX ][ psY - 1 ] < minDist || (distances[ psX ][ psY - 1 ] <= minDist && lastDir == 2))) {
                minDist = distances[ psX ][ psY - 1 ];
                targetX = psX;
                targetY = (byte)(psY - 1);
                lastDir = 2;
            }

            if( psX < EnvironmentController.mazeWidth - 1 && (distances[ psX + 1 ][ psY ] < minDist || (distances[ psX + 1 ][ psY ] <= minDist && lastDir == 4))) {
                minDist = distances[psX + 1][psY];
                targetX = (byte) (psX + 1);
                targetY = psY;
                lastDir = 4;
            }

            if( psY < EnvironmentController.mazeHeight - 1 && (distances[ psX ][ psY + 1 ] < minDist || (distances[ psX ][ psY + 1 ] <= minDist && lastDir == 8))) {
                targetX = psX;
                targetY = (byte)(psY + 1);
                lastDir = 8;
            }

            if(controller.getField(targetX, targetY) != EnvironmentController.FieldStatus.START) {
                controller.setField(targetX, targetY, EnvironmentController.FieldStatus.FREE_VISITED);
            }
            tmp.push(targetX, targetY);

            psX = targetX;
            psY = targetY;

            if( count ++ > 100 ) {
                controller.onError(EnvironmentController.ERROR_STUCK_IN_LOOP);  // Cannot compute route, algo is stuck.
                break;
            }
        }
        tmp.pop(); //Here is the pos where I am
        while(!tmp.isEmpty()){
            outputRoute.pushNext(tmp.peekX(), tmp.peekY());
            maze[tmp.peekX()][tmp.peekY()] ++;
            tmp.pop();
        }

        return false;
    }

    @Override
    public synchronized void run() {
        startLatch.pass();

        EnvironmentController.Direction actualDir;
        byte movingDist;
        while(!pDirections.isEmpty()){
            actualDir = pDirections.retreiveFirst();
            movingDist = pDistances.retreiveFirst();

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



    @Override
    public void onEvent(BotEvent event) {
        switch (event){
            case RUN_PREPARE:
                prepare();
            case RUN_ENDED:
                startLatch.open();
                break;
            case RUN_STARTED:
                startLatch.open();
                break;
        }
    }
}
