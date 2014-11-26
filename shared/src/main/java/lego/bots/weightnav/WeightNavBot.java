package lego.bots.weightnav;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.*;

/**
 * Weight navigated bot. This computes weight of every field on map and navigates the shortest way to target (unvisited field with best weight)
 *
 * Private property.
 * User: jIRKA
 * Date: 23/10/14
 * Time: 10:23
 */
public final class WeightNavBot extends Bot<EnvironmentController> {

    private static final int STACK_SIZE = 16;

    private final Latch startLatch = new Latch();
    private final byte[][] distances = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];
    private final byte[][] specialPriority = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    private byte botX = -1;
    private byte botY = -1;

    private final Queue<EnvironmentController.Direction> pDirections = new Queue<EnvironmentController.Direction>(STACK_SIZE);
    private final Queue<Byte> pDistances = new Queue<Byte>(STACK_SIZE);

    public void prepare(){

        pDirections.clear();
        pDistances.clear();

        //This may take a while
        boolean done = false;
        botX = EnvironmentController.startX;
        botY = EnvironmentController.startY;

        generateSpecialPriority();

        PositionQueue route = new PositionQueue(STACK_SIZE);

        while(!done) {
            calcDistances();
            done = calcRoute(route);
        }

        EnvironmentController.Direction actualDir = null;
        byte movingDist = 0;

        byte prevX = 0;
        byte prevY = 0;
        if(!route.isEmpty()) {
            prevX = route.retreiveFirstX();
            prevY = route.retreiveFirstY();
            route.moveReadHead();
        }

        //Preprocess path
        while(!route.isEmpty()){
            byte nextX = route.retreiveFirstX();
            byte nextY = route.retreiveFirstY();
            route.moveReadHead();

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


    private final Node[][] nodes = new Node[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];
    private final ByteArrayArrayList edges = new ByteArrayArrayList(STACK_SIZE);

    private byte getAccessibility(byte x, byte y){

        byte result = 0;

        if((controller.getField(x, (byte) (y - 1)) == EnvironmentController.FieldStatus.FREE_UNVISITED || controller.getField(x, (byte) (y - 1)) == EnvironmentController.FieldStatus.START) && controller.getField(x, y) != EnvironmentController.FieldStatus.START){
            result = (byte) (result | 8);
        }
        if(controller.getField((byte) (x + 1), y) == EnvironmentController.FieldStatus.FREE_UNVISITED){
            result = (byte) (result | 4);
        }
        if(controller.getField(x, (byte) (y + 1)) == EnvironmentController.FieldStatus.FREE_UNVISITED && controller.getField(x, (byte) (y + 1)) != EnvironmentController.FieldStatus.START){
            result = (byte) (result | 2);
        }
        if(controller.getField((byte) (x - 1), y) == EnvironmentController.FieldStatus.FREE_UNVISITED){
            result = (byte) (result | 1);
        }

        return result;
    }

    private void getNextNodeStructure(Node lastNode){
        byte workX = lastNode.x;
        byte workY = lastNode.y;

        byte prevX = -1;
        byte prevY = -1;
        byte prevXSto = workX;
        byte prevYSto = workY;

        byte lastDirectionLeaving = 0;
        ByteStack lastEdge = new ByteStack(STACK_SIZE);

        boolean cont = true;

        byte accessibility;
        boolean loop = false;

        while(cont) {
            accessibility = getAccessibility(workX, workY);

            /* We don't want to return back */
            if ((accessibility & 8) == 8) { //Up
                if (prevX == workX && prevY == workY - 1) {
                    accessibility = (byte)(accessibility & ~8);
                }
            }
            if ((accessibility & 4) == 4) { //Right
                if (prevX == workX + 1 && prevY == workY) {
                    accessibility = (byte)(accessibility & ~4);
                }
            }
            if ((accessibility & 2) == 2) { //Down
                if (prevX == workX && prevY == workY + 1) {
                    accessibility = (byte)(accessibility & ~2);
                }
            }
            if ((accessibility & 1) == 1) { //Left
                if (prevX == workX - 1 && prevY == workY) {
                    accessibility = (byte)(accessibility & ~1);
                }
            }

            if (workX == lastNode.x && workY == lastNode.y && !loop) {

                lastEdge.clear();
                lastEdge.push((byte) ((workX << 4) | workY));

                if(lastNode.verUpEdgeId == -1 && (accessibility & 8) == 8){
                    workY -= 1;
                    lastDirectionLeaving = 8;
                } else if(lastNode.horRightEdgeId == -1 && (accessibility & 4) == 4){
                    workX += 1;
                    lastDirectionLeaving = 4;
                } else if(lastNode.verDownEdgeId == -1 && (accessibility & 2) == 2){
                    workY += 1;
                    lastDirectionLeaving = 2;
                } else if(lastNode.horLeftEdgeId == -1 && (accessibility & 1) == 1){
                    workX -= 1;
                    lastDirectionLeaving = 1;
                } else {
                    cont = false;
                }

                lastEdge.push((byte) ((workX << 4) | workY));

            } else if (accessibility != 0 && (accessibility & (accessibility - 1)) != 0) { //Kind of magic. More possible ways

                Node n = nodes[workX][workY];
                if (n == null) {
                    n = new Node();
                    n.x = workX;
                    n.y = workY;
                }

                byte[] lastEdgeArr = lastEdge.getCopyAsArray();

                byte edgeId = edges.size();

                if (prevX == workX && prevY == workY - 1 && n.verUpEdgeId == -1) { //Prev UP
                    edges.add(lastEdgeArr);
                    n.verUpEdgeId = edgeId;
                    n.verUpLinkedX = lastNode.x;
                    n.verUpLinkedY = lastNode.y;
                } else if (prevX == workX + 1 && prevY == workY  && n.horRightEdgeId == -1) { //Prev RIGHT
                    edges.add(lastEdgeArr);
                    n.horRightEdgeId = edgeId;
                    n.horRightLinkedX = lastNode.x;
                    n.horRightLinkedY = lastNode.y;
                } else if (prevX == workX && prevY == workY + 1 && n.verDownEdgeId == -1) { //Prev DOWN
                    edges.add(lastEdgeArr);
                    n.verDownEdgeId = edgeId;
                    n.verDownLinkedX = lastNode.x;
                    n.verDownLinkedY = lastNode.y;
                } else if (prevX == workX - 1 && prevY == workY && n.horLeftEdgeId == -1) { //Prev LEFT
                    edges.add(lastEdgeArr);
                    n.horLeftEdgeId = edgeId;
                    n.horLeftLinkedX = lastNode.x;
                    n.horLeftLinkedY = lastNode.y;
                }

                if(edgeId == edges.size() - 1){
                    if (lastDirectionLeaving == 8) { //Up
                        lastNode.verUpEdgeId = edgeId;
                        lastNode.verUpLinkedX = workX;
                        lastNode.verUpLinkedY = workY;
                    } else if (lastDirectionLeaving == 4) { //Right
                        lastNode.horRightEdgeId = edgeId;
                        lastNode.horRightLinkedX = workX;
                        lastNode.horRightLinkedY = workY;
                    } else if (lastDirectionLeaving == 2) { //Down
                        lastNode.verDownEdgeId = edgeId;
                        lastNode.verDownLinkedX = workX;
                        lastNode.verDownLinkedY = workY;
                    } else if (lastDirectionLeaving == 1) { //Left
                        lastNode.horLeftEdgeId = edgeId;
                        lastNode.horLeftLinkedX = workX;
                        lastNode.horLeftLinkedY = workY;
                    }
                    nodes[lastNode.x][lastNode.y] = lastNode;
                    nodes[workX][workY] = n;

                    getNextNodeStructure(n);
                }

                workX = lastNode.x;
                workY = lastNode.y;
                loop = false;

            } else if (accessibility != 0) { //One and only one possible way to move
                if (accessibility == 8) { //Up
                    workY -= 1;
                } else if (accessibility == 4) { //Right
                    workX += 1;
                } else if (accessibility == 2) { //Down
                    workY += 1;
                } else if (accessibility == 1) { //Left
                    workX -= 1;
                }
                lastEdge.push((byte) ((workX << 4) | workY));
                loop = true;
            } else if(accessibility == 0) { //Dead end

                Node n = nodes[workX][workY];
                if (n == null) {
                    n = new Node();
                    n.x = workX;
                    n.y = workY;
                }

                byte[] lastEdgeArr = lastEdge.getCopyAsArray();

                byte edgeId = edges.size();

                if (prevX == workX && prevY == workY - 1 && n.verUpEdgeId == -1) { //Prev UP
                    edges.add(lastEdgeArr);
                    n.verUpEdgeId = edgeId;
                    n.verUpLinkedX = lastNode.x;
                    n.verUpLinkedY = lastNode.y;
                } else if (prevX == workX + 1 && prevY == workY  && n.horRightEdgeId == -1) { //Prev RIGHT
                    edges.add(lastEdgeArr);
                    n.horRightEdgeId = edgeId;
                    n.horRightLinkedX = lastNode.x;
                    n.horRightLinkedY = lastNode.y;
                } else if (prevX == workX && prevY == workY + 1 && n.verDownEdgeId == -1) { //Prev DOWN
                    edges.add(lastEdgeArr);
                    n.verDownEdgeId = edgeId;
                    n.verDownLinkedX = lastNode.x;
                    n.verDownLinkedY = lastNode.y;
                } else if (prevX == workX - 1 && prevY == workY && n.horLeftEdgeId == -1) { //Prev LEFT
                    edges.add(lastEdgeArr);
                    n.horLeftEdgeId = edgeId;
                    n.horLeftLinkedX = lastNode.x;
                    n.horLeftLinkedY = lastNode.y;
                }

                if(edgeId == edges.size() - 1){
                    if (lastDirectionLeaving == 8) { //Up
                        lastNode.verUpEdgeId = edgeId;
                        lastNode.verUpLinkedX = workX;
                        lastNode.verUpLinkedY = workY;
                    } else if (lastDirectionLeaving == 4) { //Right
                        lastNode.horRightEdgeId = edgeId;
                        lastNode.horRightLinkedX = workX;
                        lastNode.horRightLinkedY = workY;
                    } else if (lastDirectionLeaving == 2) { //Down
                        lastNode.verDownEdgeId = edgeId;
                        lastNode.verDownLinkedX = workX;
                        lastNode.verDownLinkedY = workY;
                    } else if (lastDirectionLeaving == 1) { //Left
                        lastNode.horLeftEdgeId = edgeId;
                        lastNode.horLeftLinkedX = workX;
                        lastNode.horLeftLinkedY = workY;
                    }
                    nodes[lastNode.x][lastNode.y] = lastNode;
                    nodes[workX][workY] = n;
                }

                workX = lastNode.x;
                workY = lastNode.y;

            }

            prevX = prevXSto;
            prevY = prevYSto;
            prevXSto = workX;
            prevYSto = workY;

        }
    }

    private void generateSpecialPriority(){

        Node n = new Node();
        n.x = EnvironmentController.startX;
        n.y = EnvironmentController.startY;

        nodes[n.x][n.y] = n;

        getNextNodeStructure(n);

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                n = nodes[x][y];
                if(n != null) {
                    if (n.verUpEdgeId != -1) {
                        resetToIterate();
                        if(countDots(n.x, (byte)(n.y - 1), EnvironmentController.Direction.DOWN, n.x, n.y) != -Byte.MAX_VALUE){
                            resetToIterate();
                            recursiveFill(n.x, (byte)(n.y - 1), EnvironmentController.Direction.DOWN);
                        }
                    }
                    if (n.horRightEdgeId != -1) {
                        resetToIterate();
                        if(countDots((byte)(n.x + 1), n.y, EnvironmentController.Direction.LEFT, n.x, n.y) != -Byte.MAX_VALUE){
                            resetToIterate();
                            recursiveFill((byte)(n.x + 1), n.y, EnvironmentController.Direction.LEFT);
                        }
                    }
                    if (n.verDownEdgeId != -1) {
                        resetToIterate();
                        if(countDots(n.x, (byte)(n.y + 1), EnvironmentController.Direction.UP, n.x, n.y) != -Byte.MAX_VALUE){
                            resetToIterate();
                            recursiveFill(n.x, (byte)(n.y + 1), EnvironmentController.Direction.UP);
                        }
                    }
                    if (n.horLeftEdgeId != -1) {
                        resetToIterate();
                        if(countDots((byte)(n.x - 1), n.y, EnvironmentController.Direction.RIGHT, n.x, n.y) != -Byte.MAX_VALUE){
                            resetToIterate();
                            recursiveFill((byte)(n.x - 1), n.y, EnvironmentController.Direction.RIGHT);
                        }
                    }
                }
            }
        }

    }


    byte lastDir = 0;
    private boolean calcRoute(PositionQueue outputRoute) {
        byte targetX = Byte.MIN_VALUE;
        byte targetY = Byte.MIN_VALUE;
        byte minDist = Byte.MAX_VALUE;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                if( (x != botX || y != botY) && ( controller.getField(x,y) == EnvironmentController.FieldStatus.UNKNOWN || controller.getField(x, y) == EnvironmentController.FieldStatus.FREE_UNVISITED )){
                    byte dist = (byte)(distances[x][y] * 3);
                    if(controller.getField((byte)(x + 1), y) == EnvironmentController.FieldStatus.OBSTACLE || controller.getField((byte)(x + 1), y) == EnvironmentController.FieldStatus.FREE_VISITED){
                        dist -= 1;
                    }else if(controller.getField((byte)(x - 1), y) == EnvironmentController.FieldStatus.OBSTACLE || controller.getField((byte)(x - 1), y) == EnvironmentController.FieldStatus.FREE_VISITED){
                        dist -= 1;
                    }else if(controller.getField(x, (byte)(y + 1)) == EnvironmentController.FieldStatus.OBSTACLE || controller.getField(x, (byte)(y + 1)) == EnvironmentController.FieldStatus.FREE_VISITED){
                        dist -= 1;
                    }else if(controller.getField( x, (byte)(y - 1)) == EnvironmentController.FieldStatus.OBSTACLE || controller.getField(x , (byte)(y - 1)) == EnvironmentController.FieldStatus.FREE_VISITED){
                        dist -= 1;
                    }

                    dist -= specialPriority[x][y];

                    if(botX == x && botY == y + 1 && lastDir == 8){ //Moved up
                        dist -= 2;
                    }else if(botX == x && botY == y - 1 && lastDir == 2){ //Moved down
                        dist -= 2;
                    }else if(botX == x + 1&& botY == y && lastDir == 1){ //Moved left
                        dist -= 2;
                    }else if(botX == x - 1 && botY == y && lastDir == 4){ //Moved right
                        dist -= 2;
                    }
                    if(botX == 7 && botY == 5 && x == 8 && y == 5){
                        dist -= 3;
                    }
                    if(botX == 8 && botY == 4 && x == 7 && y == 4){
                        dist -= 2;
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

        while(!tmp.isEmpty()){
            outputRoute.pushNext(tmp.peekX(), tmp.peekY());
            tmp.pop();
        }

        return false;
    }

    private final PositionStack toCalc = new PositionStack(STACK_SIZE); //Used in calcDistances function
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

    private boolean[][] toIterate = new boolean[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];


    private void resetToIterate(){
        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
                toIterate[x][y] = controller.getField(x, y) == EnvironmentController.FieldStatus.FREE_UNVISITED;
            }
        }
    }

    private void recursiveFill(byte x, byte y, EnvironmentController.Direction from){
        if(x >= 0 && y >= 0 && x < EnvironmentController.mazeWidth && y < EnvironmentController.mazeHeight && toIterate[x][y]){

            toIterate[x][y] = false;
            specialPriority[x][y] ++;

            if((x != EnvironmentController.startX || y + 1 != EnvironmentController.startY) && from != EnvironmentController.Direction.UP){
                recursiveFill(x, (byte)(y - 1), EnvironmentController.Direction.DOWN);
            }
            if(from != EnvironmentController.Direction.RIGHT){
                recursiveFill((byte) (x + 1), y, EnvironmentController.Direction.LEFT);
            }
            if((x != EnvironmentController.startX || y + 1 != EnvironmentController.startY) && from != EnvironmentController.Direction.DOWN){
                recursiveFill(x, (byte) (y + 1), EnvironmentController.Direction.UP);
            }
            if(from != EnvironmentController.Direction.LEFT){
                recursiveFill((byte) (x - 1), y, EnvironmentController.Direction.RIGHT);
            }
        }
    }

    private byte countDots(byte x, byte y, EnvironmentController.Direction from, byte masterStartX, byte masterStartY){

        if(x == masterStartX && y == masterStartY){
            return -Byte.MAX_VALUE;
        }

        if(x == EnvironmentController.startX && y == EnvironmentController.startY)
            return -Byte.MAX_VALUE;

        byte result = 0;

        if(x >= 0 && y >= 0 && x < EnvironmentController.mazeWidth && y < EnvironmentController.mazeHeight && toIterate[x][y]){

            result = 1;
            toIterate[x][y] = false;

            if(from != EnvironmentController.Direction.UP){
                byte val = countDots(x, (byte)(y - 1), EnvironmentController.Direction.DOWN, masterStartX, masterStartY);
                if(val == -Byte.MAX_VALUE)
                    return -Byte.MAX_VALUE;
                result += val;
            }
            if(from != EnvironmentController.Direction.RIGHT){
                byte val = countDots((byte)(x + 1), y, EnvironmentController.Direction.LEFT, masterStartX, masterStartY);
                if(val == -Byte.MAX_VALUE)
                    return -Byte.MAX_VALUE;
                result += val;
            }
            if(from != EnvironmentController.Direction.DOWN){
                byte val = countDots(x, (byte)(y + 1), EnvironmentController.Direction.UP, masterStartX, masterStartY);
                if(val == -Byte.MAX_VALUE)
                    return -Byte.MAX_VALUE;
                result += val;
            }
            if(from != EnvironmentController.Direction.LEFT){
                byte val = countDots((byte)(x - 1), y, EnvironmentController.Direction.RIGHT, masterStartX, masterStartY);
                if(val == -Byte.MAX_VALUE)
                    return -Byte.MAX_VALUE;
                result += val;
            }
        }


        return result;
    }

    @Override
    public void onEvent(BotEvent event) {
        switch (event){
            case RUN_PREPARE:
                prepare();
                break;
            case RUN_ENDED:
                startLatch.open();
                break;
            case RUN_STARTED:
                startLatch.open();
                break;
        }
    }
}
