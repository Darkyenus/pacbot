package lego.bots.cheaty;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.PositionStack;
import lego.util.Queue;

import java.util.ArrayList;

/**
 * This bot is called cheaty, because it uses predefined map and it is cheat against the original rules. However after dozens of rule patches is this behaviour acceptable.
 *
 * Private property.
 * User: jIRKA
 * Date: 23/10/14
 * Time: 10:23
 */
@Deprecated
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

    private final Queue<EnvironmentController.Direction> directions = new Queue<EnvironmentController.Direction>(STACK_SIZE);
    private final Queue<Byte> distances = new Queue<Byte>(STACK_SIZE);
    private final PositionStack route = new PositionStack(STACK_SIZE);

    private final byte[][] fieldValues = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    private final ArrayList<Rectangle> activeRectangles = new ArrayList<Rectangle>();


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

    private static class Rectangle{
        public byte x = -1;
        public byte y = -1;
        public byte width = -1;
        public byte height = -1;

        public Rectangle(byte x, byte y, byte width, byte height){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public void findAreas(){
        for(byte y = 0; y < preparedMap.length; y ++){
            byte startX = -1;
            for(byte x = 0; x < preparedMap[y].length; x ++){
                if(preparedMap[y][x] == EnvironmentController.FieldStatus.FREE_UNVISITED){
                    if(startX == -1){
                        startX = x;
                    }
                }else{
                    if(startX != -1 && x - startX > 1){
                        for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
                            if(r.y + 1 == y){ //Rec is relevant for us
                                byte end = (byte)(r.x + r.width);
                                byte start = (byte)(Math.max(r.x, startX));
                                byte width = (byte)Math.min(end - start, x - start);
                                byte height = (byte)(r.height + 1);
                                if(width > 1)
                                    activeRectangles.add(new Rectangle(start, y, width, height));
                            }
                        }
                        activeRectangles.add(new Rectangle(startX, y, (byte)(x - startX), (byte)1));
                        startX = -1;
                    }
                }
            }
            if(startX != -1 && preparedMap[y].length - startX > 1){
                for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
                    if(r.y + 1 == y){ //Rec is relevant for us
                        byte end = (byte)(r.x + r.width);
                        byte start = (byte)(Math.max(r.x, startX));
                        byte width = (byte)Math.min(end - start, preparedMap[y].length - start);
                        byte height = (byte)(r.height + 1);
                        if(width > 1)
                            activeRectangles.add(new Rectangle(start, y, width, height));
                    }
                }
                activeRectangles.add(new Rectangle(startX, y, (byte)(preparedMap[y].length - startX), (byte)1));
            }
        }

        for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
            if(r.height == 1){
                activeRectangles.remove(r);
            }
        }

        for(Rectangle r1: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
            for(Rectangle r2: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
                if(r1 != r2) {
                    if (r1.x == r2.x && r1.width == r2.width) {
                        if (r1.y - r1.height == r2.y - r2.height) {
                            if (r1.height < r2.height) {
                                activeRectangles.remove(r1);
                            } else {
                                activeRectangles.remove(r2);
                            }
                        }else if(r1.y == r2.y){
                            if (r1.height < r2.height) {
                                activeRectangles.remove(r1);
                            } else {
                                activeRectangles.remove(r2);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Areas found:");

        for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
            System.out.println("Rectangle (x: "+r.x+", y: "+r.y+", width: "+r.width+", height: "+r.height+")");
            System.out.println("+---------------------------+");
            for(int y = 0; y < 6; y++){
                System.out.print("|");
                for(int x = 0; x < 9; x++){
                    if((r.x <= x && r.x + r.width > x) && (r.y - r.height < y && r.y >= y)){
                        System.out.print("###");
                    }else{
                        if(preparedMap[y][x] == EnvironmentController.FieldStatus.FREE_UNVISITED){
                            System.out.print(" o ");
                        }else if(preparedMap[y][x] == EnvironmentController.FieldStatus.START){
                            System.out.print(" v ");
                        }else if(preparedMap[y][x] == EnvironmentController.FieldStatus.OBSTACLE){
                            System.out.print("[x]");
                        }
                    }
                }
                System.out.println("|");
            }
            System.out.println("+---------------------------+");

        }

    }

    public boolean isOnArea(byte x, byte y){
        for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
            if((r.x <= x && r.x + r.width > x) && (r.y - r.height < y && r.y >= y)){
                return true;
            }
        }
        return false;
    }
    public byte getAreaDirection(byte x, byte y){
        byte result = 0;
        for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
            if((r.x <= x && r.x + r.width > x) && (r.y - r.height < y && r.y >= y)){
                if(r.width > r.height){
                    result |= 2;
                }else if(r.width < r.height){
                    result |= 1;
                }else if(r.width == r.height){
                    result |= 3;
                }
            }
        }
        return result;
    }

    public byte[] collectInArea(byte simX, byte simY, EnvironmentController.Direction movedIn){
        byte areaDirection = getAreaDirection(simX, simY);
        boolean startHorizontally = false;

        if(areaDirection == 1){
            System.out.println(" vertically");
            startHorizontally = false;
        }else if(areaDirection == 2){
            System.out.println(" horizontally");
            startHorizontally = true;
        }else if(areaDirection == 3){
            System.out.println("randomly, but: " + (movedIn.x == 0 ? "vertically" : "horizontally"));
            if(movedIn.x == 0){
                startHorizontally = false;
            }else{
                startHorizontally = true;
            }
        }

        //TODO

        return new byte[] {simX, simY};
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
            byte possibleWays = possibleWays(simX, simY, false);
            if (possibleWays == 0) {//No way

                byte moreWays = possibleWays(simX, simY, true);
                if(moreWays == 0){ //Really no way
                    System.out.print("Returning to ");

                    byte targetX = crossings.peekX();
                    byte targetY = crossings.peekY();
                    crossings.pop();

                    calcDistances();
                    calcRoute(route, targetX, targetY);

                    simX = targetX;
                    simY = targetY;

                    System.out.println("x: "+simX+", y: "+simY);
                } else { //There is way - enter area
                    System.out.println("Entering area ");
                    EnvironmentController.Direction dir = null;
                    if ((moreWays & 8) == 8) {
                        dir = EnvironmentController.Direction.UP;
                        controller.moveByY((byte)-1);
                    }
                    if ((moreWays & 4) == 4) {
                        dir = EnvironmentController.Direction.RIGHT;
                        controller.moveByX((byte) 1);
                    }
                    if ((moreWays & 2) == 2) {
                        dir = EnvironmentController.Direction.DOWN;
                        controller.moveByY((byte) 1);
                    }
                    if ((moreWays & 1) == 1) {
                        dir = EnvironmentController.Direction.LEFT;
                        controller.moveByX((byte) -1);
                    }

                    simX += dir.x;
                    simY += dir.y;

                    controller.setField(simX, simY, EnvironmentController.FieldStatus.FREE_VISITED);
                    route.push(simX, simY);

                    byte[] newPos = collectInArea(simX, simY, dir);
                    simX = newPos[0];
                    simY = newPos[1];

                }

            } else if ((possibleWays & (possibleWays - 1)) == 0) { //Kind of magic. Only one possible way
                System.out.print("Only one way, chosen ");
                if ((possibleWays & 8) == 8) { //Possible way UP
                    simY -= 1;
                    System.out.println("UP");
                    controller.moveByY((byte)-1);
                }
                if ((possibleWays & 4) == 4) { //Possible way RIGHT
                    simX += 1;
                    System.out.println("RIGHT");
                    controller.moveByX((byte) 1);
                }
                if ((possibleWays & 2) == 2) { //Possible way DOWN
                    simY += 1;
                    System.out.println("DOWN");
                    controller.moveByY((byte) 1);
                }
                if ((possibleWays & 1) == 1) { //Possible way LEFT
                    simX -= 1;
                    System.out.println("LEFT");
                    controller.moveByX((byte) -1);
                }

                controller.setField(simX, simY, EnvironmentController.FieldStatus.FREE_VISITED);
                route.push(simX, simY);

            } else { //Multiple possible ways
                byte bestCount = Byte.MAX_VALUE;
                EnvironmentController.Direction bestDirection = null;

                if ((possibleWays & 8) == 8) { //Possible way UP
                    resetToIterate();
                    byte count = countDots(simX, (byte) (simY - 1), EnvironmentController.Direction.DOWN, simX, simY);
                    if (count != -1 && count < bestCount) {
                        bestCount = count;
                        bestDirection = EnvironmentController.Direction.UP;
                    }
                }
                if ((possibleWays & 4) == 4) { //Possible way RIGHT
                    resetToIterate();
                    byte count = countDots((byte) (simX + 1), simY, EnvironmentController.Direction.LEFT, simX, simY);
                    if (count != -1 &&  count < bestCount) {
                        bestCount = count;
                        bestDirection = EnvironmentController.Direction.RIGHT;
                    }
                }
                if ((possibleWays & 2) == 2) { //Possible way DOWN
                    resetToIterate();
                    byte count = countDots(simX, (byte) (simY + 1), EnvironmentController.Direction.UP, simX, simY);
                    if (count != -1 && count < bestCount) {
                        bestCount = count;
                        bestDirection = EnvironmentController.Direction.DOWN;
                    }
                }
                if ((possibleWays & 1) == 1) { //Possible way LEFT
                    resetToIterate();
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

                    System.out.println("Multiple ways, chosen "+bestDirection);


                    if(bestDirection.x == 0){
                        controller.moveByY(bestDirection.y);
                    }else{
                        controller.moveByX(bestDirection.x);
                    }
                    controller.setField(simX, simY, EnvironmentController.FieldStatus.FREE_VISITED);

                    route.push(simX, simY);

                }

            }
        }
    }

    private byte possibleWays(byte fromX, byte fromY, boolean includeAreas){
        byte result = 0;
        if(controller.getField(fromX, (byte)(fromY - 1)) == EnvironmentController.FieldStatus.FREE_UNVISITED && controller.getField(fromX, fromY) != EnvironmentController.FieldStatus.START && !(!includeAreas && isOnArea(fromX, (byte)(fromY - 1)))){
            result = (byte) (result | 8);
        }
        if(controller.getField((byte)(fromX + 1), fromY) == EnvironmentController.FieldStatus.FREE_UNVISITED && !(!includeAreas && isOnArea((byte)(fromX + 1), fromY))){
            result = (byte) (result | 4);
        }
        if(controller.getField(fromX, (byte)(fromY + 1)) == EnvironmentController.FieldStatus.FREE_UNVISITED && controller.getField(fromX, (byte)(fromY + 1)) != EnvironmentController.FieldStatus.START && !(!includeAreas && isOnArea(fromX, (byte)(fromY + 1)))){
            result = (byte) (result | 2);
        }
        if(controller.getField((byte)(fromX - 1), fromY) == EnvironmentController.FieldStatus.FREE_UNVISITED && !(!includeAreas && isOnArea((byte)(fromX - 1), fromY))){
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
                controller.onError(EnvironmentController.ERROR_STUCK_IN_LOOP);  // Cannot compute route, algo has stacked.
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
