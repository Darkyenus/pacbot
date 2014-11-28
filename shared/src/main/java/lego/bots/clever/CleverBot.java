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
public final class CleverBot  extends Bot<EnvironmentController> {

    private final Latch startLatch = new Latch();

    private final byte MAX_LINE_LENGTH = 3;
    private static final int STACK_SIZE = 16;

    private final byte[][] maze = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];
    private final short[][] distances = new short[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight]; //After about 4 days of no idea I noticed that this might overflow
    byte botX = EnvironmentController.startX;
    byte botY = EnvironmentController.startY + 1; //I don't want to handle special cases for start field again.
    EnvironmentController.Direction botDir = EnvironmentController.Direction.DOWN;
    private final PositionQueue outputRoute = new PositionQueue(STACK_SIZE);
    private final Queue<EnvironmentController.Direction> pDirections = new Queue<EnvironmentController.Direction>(STACK_SIZE);
    private final Queue<Byte> pDistances = new Queue<Byte>(STACK_SIZE);

    private void preprocess(){
        //Move by one downwards
        outputRoute.pushNext(botX, (byte)(botY - 1));
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
                    if(controller.getField(x, y) == EnvironmentController.FieldStatus.FREE_UNVISITED){
                        maze[x][y] = 0;
                    }else{
                        maze[x][y] = -2; //Cannot go there
                    }
                //}
            }
        }
        maze[botX][botY] = 1;

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

        try {
            while(true) {
                moveNext(outputRoute.size() - 1, (byte)-1, (byte)-1);
            }
        }catch (Error e){
            if(!"Done".equals(e.getMessage())){
                throw e;
            }
        }
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

        byte x, y;
        byte res;
        byte lastBotX = botX, lastBotY = botY;
        EnvironmentController.Direction lastBotDir = botDir;
        int j = 0;


        for(int i = 1; i < outputRoute.size() - 1; i ++){
            x = outputRoute.getXAt(i);
            y = outputRoute.getYAt(i);

            if(y < EnvironmentController.mazeHeight - 1 && maze[x][y + 1] == 0){
                resetToIterate();
                res = countDots(x, (byte) (y + 1), EnvironmentController.Direction.UP, x, y);
                if (res > 0) {
                    botX = x;
                    botY = y;
                    botDir = EnvironmentController.Direction.DOWN;
                    moveNext(i, x, y);
                    break;
                }
            }
            if(y > 0 && maze[x][y - 1] == 0){
                resetToIterate();
                res = countDots(x, (byte) (y - 1), EnvironmentController.Direction.DOWN, x, y);
                if (res > 0) {
                    botX = x;
                    botY = y;
                    botDir = EnvironmentController.Direction.UP;
                    moveNext(i, x, y);
                    break;
                }

            }
            if(x < EnvironmentController.mazeWidth - 1 && maze[x + 1][y] == 0){
                resetToIterate();
                res = countDots((byte)(x + 1), y, EnvironmentController.Direction.LEFT, x, y);
                if (res > 0) {
                    botX = x;
                    botY = y;
                    botDir = EnvironmentController.Direction.RIGHT;
                    moveNext(i, x, y);
                    break;
                }
            }
            if(x > 0 && maze[x - 1][y] == 0){
                resetToIterate();
                res = countDots((byte)(x - 1), y, EnvironmentController.Direction.RIGHT, x, y);
                if (res > 0) {
                    botX = x;
                    botY = y;
                    botDir = EnvironmentController.Direction.LEFT;
                    moveNext(i, x, y);
                    break;
                }
            }
        }
        botX = lastBotX;
        botY = lastBotY;
        botDir = lastBotDir;
        moveNext(outputRoute.size() - 1, (byte)-1, (byte)-1);
    }

    private byte moveNext(int index, byte returnX, byte returnY){
        byte res = 0;
        if(botX >= 0 && botY >= 0 && botX < EnvironmentController.mazeWidth && botY < EnvironmentController.mazeHeight && botX + botDir.x >= 0 && botY + botDir.y >= 0 && botX + botDir.x < EnvironmentController.mazeWidth && botY + botDir.y < EnvironmentController.mazeHeight && maze[botX + botDir.x][botY + botDir.y] == 0){
            for(byte i = 1; i < 10; i ++){
                if(botX + botDir.x >= 0 && botY + botDir.y >= 0 && botX + botDir.x < EnvironmentController.mazeWidth && botY + botDir.y < EnvironmentController.mazeHeight && maze[botX + botDir.x][botY + botDir.y] == 0){
                    botX += botDir.x;
                    botY += botDir.y;
                    maze[botX][botY] ++;
                    outputRoute.insertAfter(index + i - 1, botX, botY);
                    res ++;
                }else{
                    break;
                }
            }
            if(returnX == -1) {
                checkForDeadEnds();
            }else{
                calcDistances();
                res = (byte)(calcRoute(index + res, returnX, returnY) - index);
            }
        }else if(botX >= 0 && botY >= 0 && botX < EnvironmentController.mazeWidth && botY < EnvironmentController.mazeHeight){
            EnvironmentController.Direction leftDir = botDir.left;
            EnvironmentController.Direction rightDir = botDir.right;
            byte leftDist, rightDist;

            for(leftDist = 1; leftDist < 10; leftDist ++){
                if((botX + leftDir.x * leftDist < 0 || botY + leftDir.y * leftDist < 0 || botX + leftDir.x * leftDist >= EnvironmentController.mazeWidth || botY + leftDir.y * leftDist >= EnvironmentController.mazeHeight) || (botX + leftDir.x * leftDist >= 0 && botY + leftDir.y * leftDist >= 0 && botX + leftDir.x * leftDist < EnvironmentController.mazeWidth && botY + leftDir.y * leftDist < EnvironmentController.mazeHeight && maze[botX + leftDir.x * leftDist][botY + leftDir.y * leftDist] != 0)) {
                    leftDist --;
                    break;
                }
            }
            for(rightDist = 1; rightDist < 10; rightDist ++) {
                if ((botX + rightDir.x * rightDist < 0 || botY + rightDir.y * rightDist < 0 || botX + rightDir.x * rightDist >= EnvironmentController.mazeWidth || botY + rightDir.y * rightDist >= EnvironmentController.mazeHeight) || (botX + rightDir.x * rightDist >= 0 && botY + rightDir.y * rightDist >= 0 && botX + rightDir.x * rightDist < EnvironmentController.mazeWidth && botY + rightDir.y * rightDist < EnvironmentController.mazeHeight && maze[botX + rightDir.x * rightDist][botY + rightDir.y * rightDist] != 0)){
                    rightDist --;
                    break;
                }
            }

            if(leftDist > 0 && ((rightDist > 0 && leftDist < rightDist) || rightDist <= 0)){
                for(byte i = 0; i < leftDist; i++){
                    botX += leftDir.x;
                    botY += leftDir.y;
                    maze[botX][botY] ++;
                    outputRoute.insertAfter(index + i, botX, botY);
                }
                botDir = leftDir;
                res = leftDist;
            }else if(rightDist > 0){
                for(byte i = 0; i < rightDist; i++){
                    botX += rightDir.x;
                    botY += rightDir.y;
                    maze[botX][botY] ++;
                    outputRoute.insertAfter(index + i, botX, botY);
                }
                botDir = rightDir;
                res = rightDist;
            }else if(leftDist <= 0){
                calcDistances();
                if(returnX == -1) {
                    res = calcRoute(index);
                }else{
                    res = calcRoute(index, returnX, returnY);
                }
            }
            if(returnX == -1) {
                checkForDeadEnds();
            }
        }
        return (byte)(index + res);
    }

    private final PositionStack toCalc = new PositionStack(STACK_SIZE); //Used in calcDistances function
    private void calcDistances() {
        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                distances[x][y] = Short.MAX_VALUE;
            }
        }

        distances[botX][botY] = 0;
        toCalc.clear();

        toCalc.push(botX, botY);

        while( !toCalc.isEmpty() ) {
            byte psX = toCalc.peekX();
            byte psY = toCalc.peekY();
            toCalc.pop();

            if(maze[psX][psY] != -2){
                short psDistNew = (short) (distances[ psX ][ psY ] + (maze[psX][psY] == 0  ? 1 : (maze[psX][psY] == -1 ? 3 : 5) ));
                if( psX > 0 && maze[psX - 1][psY] != -2 && ( distances[ psX - 1 ][ psY ] > psDistNew ) ) {
                    distances[psX - 1][psY] = psDistNew;
                    toCalc.push((byte) (psX - 1), psY);
                }
                if( psX < EnvironmentController.mazeWidth - 1 && maze[psX + 1][psY] != -2 && ( distances[ psX + 1 ][ psY ] > psDistNew ) ) {
                    distances[psX + 1][psY] = psDistNew;
                    toCalc.push((byte) (psX + 1), psY);
                }
                if( psY > 0 && maze[psX][psY - 1] != -2  && ( distances[ psX ][ psY - 1 ] > psDistNew ) ) {
                    distances[psX][psY - 1] = psDistNew;
                    toCalc.push(psX, (byte) (psY - 1));
                }
                if( psY < EnvironmentController.mazeHeight - 1&& maze[psX][psY + 1] != -2 && ( distances[ psX ][ psY + 1 ] > psDistNew ) ) {
                    distances[psX][psY + 1] = psDistNew;
                    toCalc.push(psX, (byte) (psY + 1));
                }
            }
        }
        for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
            for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
                System.out.print("   "+distances[x][y]+"   ");
            }
        }

    }
    private byte calcRoute(int index, byte targetX, byte targetY){
        PositionStack tmp = new PositionStack(STACK_SIZE);
        tmp.push(targetX, targetY);

        byte psX = targetX;
        byte psY = targetY;
        byte robotPosX = botX;
        byte robotPosY = botY;
        botX = psX;
        botY = psY; //We will end up here
        short minDist;

        EnvironmentController.Direction lastDir = botDir;

        byte count = 0;
        while( psX != robotPosX || psY != robotPosY ) {
            minDist = Byte.MAX_VALUE;
            targetX = psX;
            targetY = psY;

            if( psX > 0 && (distances[ psX - 1 ][ psY ] < minDist || (distances[ psX - 1 ][ psY ] <= minDist && lastDir == EnvironmentController.Direction.LEFT))) {
                minDist = distances[ psX - 1 ][ psY ];
                targetX = (byte) (psX - 1);
                targetY = psY;
                lastDir = EnvironmentController.Direction.LEFT;
            }

            if( psY > 0 && (distances[ psX ][ psY - 1 ] < minDist || (distances[ psX ][ psY - 1 ] <= minDist && lastDir == EnvironmentController.Direction.DOWN))) {
                minDist = distances[ psX ][ psY - 1 ];
                targetX = psX;
                targetY = (byte)(psY - 1);
                lastDir = EnvironmentController.Direction.DOWN;
            }

            if( psX < EnvironmentController.mazeWidth - 1 && (distances[ psX + 1 ][ psY ] < minDist || (distances[ psX + 1 ][ psY ] <= minDist && lastDir == EnvironmentController.Direction.RIGHT))) {
                minDist = distances[psX + 1][psY];
                targetX = (byte) (psX + 1);
                targetY = psY;
                lastDir = EnvironmentController.Direction.RIGHT;
            }

            if( psY < EnvironmentController.mazeHeight - 1 && (distances[ psX ][ psY + 1 ] < minDist || (distances[ psX ][ psY + 1 ] <= minDist && lastDir == EnvironmentController.Direction.UP))) {
                targetX = psX;
                targetY = (byte)(psY + 1);
                lastDir = EnvironmentController.Direction.UP;
            }

            tmp.push(targetX, targetY);

            if(psX == botX && psY == botY){
                botDir = lastDir;
            }

            psX = targetX;
            psY = targetY;

            if( count ++ > 100 ) {
                controller.onError(EnvironmentController.ERROR_STUCK_IN_LOOP);  // Cannot compute route, algo is stuck.
                break;
            }
        }
        tmp.pop(); //Here is the pos where I am
        byte i = 0;
        while(!tmp.isEmpty()){
            //outputRoute.pushNext(tmp.peekX(), tmp.peekY());
            outputRoute.insertAfter(index + i, tmp.peekX(), tmp.peekY());
            maze[tmp.peekX()][tmp.peekY()] ++;
            tmp.pop();
            i++;
        }
        return (byte)(index + i);
    }
    private byte calcRoute(int index) {
        byte targetX = Byte.MIN_VALUE;
        byte targetY = Byte.MIN_VALUE;
        byte minDist = Byte.MAX_VALUE;
        byte dist;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                if((x != botX || y != botY) && maze[x][y] == 0){
                    dist = (byte)(distances[x][y] * 2);

                    if(botX == x && botY == y + 1 && botDir == EnvironmentController.Direction.UP){
                        dist -= 1;
                    }else if(botX == x && botY == y - 1 && botDir == EnvironmentController.Direction.DOWN){
                        dist -= 1;
                    }else if(botX == x + 1&& botY == y && botDir == EnvironmentController.Direction.LEFT){
                        dist -= 1;
                    }else if(botX == x - 1 && botY == y && botDir == EnvironmentController.Direction.RIGHT){
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
        if(targetX == Byte.MIN_VALUE || targetX == Byte.MIN_VALUE){
            throw new Error("Done");
        }

        return calcRoute(index, targetX, targetY);
    }

    private boolean[][] toIterate = new boolean[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    private void resetToIterate(){
        for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
            for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
                toIterate[x][y] = maze[x][y] == 0;
            }
        }
    }

    private byte countDots(byte x, byte y, EnvironmentController.Direction from, byte masterStartX, byte masterStartY){

        if(x == masterStartX && y == masterStartY){
            return Byte.MIN_VALUE;
        }
        if(x == botX && y == botY){
            return Byte.MIN_VALUE;
        }

        byte result = 0;

        if(x >= 0 && y >= 0 && x < EnvironmentController.mazeWidth && y < EnvironmentController.mazeHeight && toIterate[x][y]){

            result = 1;
            toIterate[x][y] = false;

            if((x != EnvironmentController.startX || y + 1 != EnvironmentController.startY) && from != EnvironmentController.Direction.UP){
                byte val = countDots(x, (byte)(y - 1), EnvironmentController.Direction.DOWN, masterStartX, masterStartY);
                if(val == Byte.MIN_VALUE)
                    return Byte.MIN_VALUE;
                result += val;
            }
            if(from != EnvironmentController.Direction.RIGHT){
                byte val = countDots((byte)(x + 1), y, EnvironmentController.Direction.LEFT, masterStartX, masterStartY);
                if(val == Byte.MIN_VALUE)
                    return Byte.MIN_VALUE;
                result += val;
            }
            if((x != EnvironmentController.startX || y + 1 != EnvironmentController.startY) && from != EnvironmentController.Direction.DOWN){
                byte val = countDots(x, (byte)(y + 1), EnvironmentController.Direction.UP, masterStartX, masterStartY);
                if(val == Byte.MIN_VALUE)
                    return Byte.MIN_VALUE;
                result += val;
            }
            if(from != EnvironmentController.Direction.LEFT){
                byte val = countDots((byte)(x - 1), y, EnvironmentController.Direction.RIGHT, masterStartX, masterStartY);
                if(val == Byte.MIN_VALUE)
                    return Byte.MIN_VALUE;
                result += val;
            }
        }

        return result;
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
