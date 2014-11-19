package lego.bots.node;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.ByteStack;
import lego.util.PositionStack;

import java.io.IOException;
import java.util.Arrays;

/**
 * Private property.
 * User: jIRKA
 * Date: 17.11.2014
 * Time: 18:31
 */
public class NodeBot extends Bot<EnvironmentController> {

    static final int STACK_SIZE = 16;

    private boolean continueRunning = true;

    private static final EnvironmentController.FieldStatus FREE = EnvironmentController.FieldStatus.FREE_UNVISITED;
    private static final EnvironmentController.FieldStatus BLOCK = EnvironmentController.FieldStatus.OBSTACLE;
    private static final EnvironmentController.FieldStatus START = EnvironmentController.FieldStatus.START;


    /*
    private final EnvironmentController.FieldStatus[][] preparedMap = {
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE},
            { FREE, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            { FREE, BLOCK,  FREE, BLOCK, START, BLOCK,  FREE, BLOCK,  FREE},
            { FREE, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            { FREE, BLOCK,  FREE, BLOCK, BLOCK, BLOCK,  FREE, BLOCK,  FREE},
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE}
    };
    */



    private final EnvironmentController.FieldStatus[][] preparedMap = {
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            {BLOCK, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE},
            { FREE,  FREE, BLOCK, BLOCK, START, BLOCK,  FREE,  FREE,  FREE},
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            { FREE,  FREE,  FREE, BLOCK, BLOCK, BLOCK,  FREE, BLOCK,  FREE},
            { FREE,  FREE,  FREE, BLOCK,  FREE,  FREE,  FREE, BLOCK,  FREE}
    };



    /*
    private final EnvironmentController.FieldStatus[][] preparedMap = {
            {BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            {BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            {BLOCK,  FREE, BLOCK, BLOCK, START, BLOCK,  FREE, BLOCK,  FREE},
            {BLOCK,  FREE, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE},
            {BLOCK,  FREE, BLOCK, BLOCK,  FREE, BLOCK, BLOCK, BLOCK,  FREE},
            {BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE}
    };
    */



    private final PositionStack route = new PositionStack(STACK_SIZE);

    private final GraphStruct graph = new GraphStruct();

    private void prepare(){
        System.out.println("Computation started");

        graph.prepareNodes(preparedMap);

        Debug.printNodes(graph.nodes, graph.edges);

        System.out.println("Graph prepared");

        long now = System.currentTimeMillis();

        findBestWay();

        System.out.println();
        System.out.println("Found solution in "+(((System.currentTimeMillis() - now) / 100) / 10f)+"s");

        System.out.println();

        System.out.println("==== BEST ====");

        System.out.println("Price: "+bestPrice);

        System.out.println(Arrays.toString(bestPath));

        for(Byte edgeId: bestPath){
            Debug.printEdge(graph.edges.get(edgeId));
            try{
                System.in.read();
            }catch (IOException ignored){

            }
        }

        System.out.println();

        System.out.println("Price: "+bestPrice);

        System.out.println();

        System.out.println("Computation ended, waiting for start signal");
    }


    Byte[] edgesUsed;
    Byte[] edgesPrice;

    ByteStack path = new ByteStack(STACK_SIZE);
    short price = 0;

    Byte[] bestPath = new Byte[0];
    short bestPrice = Short.MAX_VALUE;

    final byte[][] visited = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    final ByteStack directionStack = new ByteStack(STACK_SIZE);
    final PositionStack positionStack = new PositionStack(STACK_SIZE);

    public void findBestWay(){

        edgesUsed = new Byte[graph.edges.size()];
        edgesPrice = new Byte[graph.edges.size()];

        for(byte x = 0; x < EnvironmentController.mazeWidth; x ++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
                visited[x][y] = -1;

                Node n = graph.nodes[x][y];
                if(n != null) {
                    if (n.verUpEdgeId != -1) {
                        edgesPrice[n.verUpEdgeId] = n.verUpPrice;
                    }
                    if (n.horRightEdgeId != -1) {
                        edgesPrice[n.horRightEdgeId] = n.horRightPrice;
                    }
                    if (n.verDownEdgeId != -1) {
                        edgesPrice[n.verDownEdgeId] = n.verDownPrice;
                    }
                    if (n.horLeftEdgeId != -1) {
                        edgesPrice[n.horLeftEdgeId] = n.horLeftPrice;
                    }
                }
            }
        }

        for(byte i = 0; i < graph.edges.size(); i ++){
            Byte[] edge = graph.edges.get(i);
            for(Byte e:edge){
                byte x = (byte)((e >> 4) & 15);
                byte y = (byte)(e & 15);
                visited[x][y] = 0;
            }
        }

        for(byte i = 0; i < edgesUsed.length; i++){
            edgesUsed[i] = 0;
        }

        Node startNode = graph.nodes[EnvironmentController.startX][EnvironmentController.startY];

        decideOnNode(startNode);

        checkCompletedMap((byte)-1, (byte)-1);

    }

    private boolean checkCompletedMap(byte posX, byte posY){
        boolean complete = true;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                if(visited[x][y] == 0 && !(posX == x && posY == y)){
                    complete = false;
                    x = EnvironmentController.mazeWidth;
                    y = EnvironmentController.mazeHeight;
                }
            }
        }

        if(complete){

            if(price < bestPrice){
                bestPath = path.getCopyAsArray();
                bestPrice = price;
                System.out.println("Have something (" + bestPrice + ")");
                System.out.println("Path: "+Arrays.toString(bestPath));
                System.out.println();
            }

            revertLast();
        }

        return complete;
    }

    private void revertLast(){
        if (!path.isEmpty()) {
            byte edgeId = path.pop();

            edgesUsed[edgeId] = (byte) (edgesUsed[edgeId] - 1);
            price -= edgesPrice[edgeId];

            byte lastDir =  directionStack.pop();
            price -= nodeTurnPrice(lastDir);
            directionStack.pop();

            Byte[] edge = graph.edges.get(edgeId);
            for(int i = 1; i < edge.length - 1; i++){ //Affect everything except first and last
                byte x = (byte)((edge[i] >> 4) & 15);
                byte y = (byte)(edge[i] & 15);
                visited[x][y] = (byte)(visited[x][y] - 1);
            }

            byte junctionNodeX = positionStack.peekX();
            byte junctionNodeY = positionStack.peekY();
            positionStack.pop();


            byte visitedVal = visited[junctionNodeX][junctionNodeY];
            visitedVal = (byte)((visitedVal & 15) | ((((visitedVal >> 4) & 15) - 1) << 4));
            if(((visitedVal >> 4) & 15) == 0){
                visitedVal = 0;
            }
            visited[junctionNodeX][junctionNodeY] = visitedVal;


            /*if(junctionNodeX == 2 && junctionNodeY == 0 && visited[junctionNodeX][junctionNodeY] == 0){
                System.out.println("Edge: "+edgeId);
                System.out.println("Post visited: "+visited[junctionNodeX][junctionNodeY]);
            }*/

        }
    }

    private void logMovement(Byte edgeId, byte fromX, byte fromY, byte directionMoved){
        path.push(edgeId);
        edgesUsed[edgeId] = (byte) (edgesUsed[edgeId] + 1);

        Byte[] edge = graph.edges.get(edgeId);
        for(int i = 1; i < edge.length - 1; i++){ //Affect everything except first and last
            byte x = (byte)((edge[i] >> 4) & 15);
            byte y = (byte)(edge[i] & 15);
            visited[x][y] = (byte)(visited[x][y] + 1);
        }

        positionStack.push(fromX, fromY);

        byte directionCompressed = 0;
        if(directionMoved != 0) {
            while ((directionMoved >> directionCompressed) != 1) {
                directionCompressed++;
            }
        }
        byte visitedVal = visited[fromX][fromY];
        visitedVal = (byte)((((visitedVal >> 4) & 15) + 1) << 4);
        visitedVal = (byte)((visitedVal & 240) | 4 | directionCompressed);
        visited[fromX][fromY] = visitedVal;

        price += edgesPrice[edgeId];
        price += nodeTurnPrice(directionMoved);
    }

    private byte nodeTurnPrice(byte dir){

        byte lastDir = directionStack.isEmpty() ? 0 : directionStack.peek();
        if(lastDir == 0){
            directionStack.push(dir);
            return 0;
        }
        byte result = 0;
        if(lastDir == 8){
            if(dir == 2){
                result =  GraphStruct.PRICE_TURN_AROUND;
            }else if ( dir != 8){
                result =  GraphStruct.PRICE_TURN;
            }
        }else if(lastDir == 4){
            if(dir == 1){
                result =  GraphStruct.PRICE_TURN_AROUND;
            }else if(dir != 4){
                result =  GraphStruct.PRICE_TURN;
            }
        }else if(lastDir == 2){
            if(dir == 8){
                result =  GraphStruct.PRICE_TURN_AROUND;
            }else if ( dir != 2){
                result =  GraphStruct.PRICE_TURN;
            }
        }else if(lastDir == 1){
            if(dir == 4){
                result =  GraphStruct.PRICE_TURN_AROUND;
            }else if(dir != 1){
                result =  GraphStruct.PRICE_TURN;
            }
        }

        directionStack.push(dir);
        return result;
    }

    private void decideOnNode(Node n){

        if (price >= bestPrice) {
            revertLast();
            return;
        }

        if (checkCompletedMap(n.x, n.y)) {
            return;
        }

        byte hintUp = 0, hintRight = 0, hintDown = 0, hintLeft = 0;
        byte lastDir = directionStack.isEmpty() ? 0 : directionStack.peek();

        //System.out.println("a");

        if(n.verUpEdgeId != -1){
            resetToIterate();
            byte val = countDots(n.x, (byte)(n.y - 1), EnvironmentController.Direction.DOWN, n.x, n.y);
            //System.out.println("count up: "+val);
            if(val != -Byte.MAX_VALUE){ //cyclic
                if(val >= 0){
                    hintUp = Byte.MAX_VALUE; //We don't want to go there, cuz there is nothing or we have been there already
                }else{ //collected partially or even not touched yet
                    if(lastDir == 2){
                        if((visited[n.x][n.y] & 7) == 3+4){
                            revertLast();
                            return;
                        }
                    }
                    hintUp = (byte) -val;
                }
            }
        }
        if(n.horRightEdgeId != -1){
            resetToIterate();
            byte val = countDots((byte)(n.x + 1), n.y, EnvironmentController.Direction.LEFT, n.x, n.y);
            //System.out.println("count right: "+val);
            if(val != -Byte.MAX_VALUE){ //cyclic
                if(val >= 0){
                    hintRight = Byte.MAX_VALUE; //We don't want to go there, cuz there is nothing or we have been there already
                }else{ //collected partially or even not touched yet
                    if(lastDir == 1){
                        if((visited[n.x][n.y] & 7) == 2+4) {
                            revertLast();
                            return;
                        }
                    }
                    hintRight = (byte) -val;
                }
            }
        }
        if(n.verDownEdgeId != -1){
            resetToIterate();
            byte val = countDots(n.x, (byte)(n.y + 1), EnvironmentController.Direction.UP, n.x, n.y);
            //System.out.println("count down: "+val);
            if(val != -Byte.MAX_VALUE){ //cyclic
                if(val >= 0){
                    hintDown = Byte.MAX_VALUE; //We don't want to go there, cuz there is nothing or we have been there already
                }else{ //collected partially or even not touched yet
                    if(lastDir == 8){
                        if((visited[n.x][n.y] & 7) == 1+4){
                            revertLast();
                            return;
                        }
                    }
                    hintDown = (byte) -val;
                }
            }
        }
        if(n.horLeftEdgeId != -1){
            resetToIterate();
            byte val = countDots((byte)(n.x - 1), n.y, EnvironmentController.Direction.RIGHT, n.x, n.y);
            //System.out.println("count left: "+val);
            if(val != -Byte.MAX_VALUE){ //cyclic
                if(val >= 0){
                    hintLeft = Byte.MAX_VALUE; //We don't want to go there, cuz there is nothing or we have been there already
                }else{ //collected partially or even not touched yet
                    if(lastDir == 4){
                        if((visited[n.x][n.y] & 7) == 0+4){
                            revertLast();
                            return;
                        }
                    }
                    hintLeft = (byte) -val;
                }
            }
        }

        //System.out.println("b");

        byte hint = 0;
        byte hintValue = Byte.MAX_VALUE;
        if(hintUp != Byte.MAX_VALUE && hintUp != 0 && (!directionStack.isEmpty() && directionStack.peek() != 2)){
            hintValue = hintUp;
            hint = 8;
        }
        if(hintRight != Byte.MAX_VALUE && hintRight != 0 && (!directionStack.isEmpty() && directionStack.peek() != 1)){
            if(hintRight < hintValue) {
                hintValue = hintRight;
                hint = 4;
            }
        }
        if(hintDown != Byte.MAX_VALUE && hintDown != 0 && (!directionStack.isEmpty() && directionStack.peek() != 8)){
            if(hintDown < hintValue) {
                hintValue = hintDown;
                hint = 2;
            }
        }
        if(hintLeft != Byte.MAX_VALUE && hintLeft != 0 && (!directionStack.isEmpty() && directionStack.peek() != 4)){
            if(hintLeft < hintValue) {
                hint = 1;
            }
        }


        /*
        if(hint != 0){
            System.out.println("hint: "+hint+" @ x: "+n.x+", y: "+n.y);
        }
        */
/*        if(hintUp == Byte.MAX_VALUE){
            System.out.println("restriction up  @ x: "+n.x+", y: "+n.y);
        }
        if(hintDown == Byte.MAX_VALUE){
            System.out.println("restriction down  @ x: "+n.x+", y: "+n.y);
        }
        if(hintLeft == Byte.MAX_VALUE){
            System.out.println("restriction left  @ x: "+n.x+", y: "+n.y);
        }
        if(hintRight == Byte.MAX_VALUE){
            System.out.println("restriction right  @ x: "+n.x+", y: "+n.y);
        }
*/
/*
        System.out.println("x: "+n.x+", y: "+n.y);
        System.out.println("hint: "+hint);
        System.out.println("up: "+hintUp);
        System.out.println("right: "+hintRight);
        System.out.println("down: "+hintDown);
        System.out.println("left: "+hintLeft);
        System.out.println();

        try {
            System.in.read();
        } catch (IOException e) {

        }
*/

        for(byte lookingFor = -1; lookingFor < 2; lookingFor ++){

            if(n.verUpEdgeId != -1){
                if(lookingFor == -1 && hint == 8){
                    logMovement(n.verUpEdgeId, n.x, n.y, (byte)8);
                    decideOnNode(graph.nodes[n.verUpLinkedX][n.verUpLinkedY]);

                }else if(edgesUsed[n.verUpEdgeId] == lookingFor && hintUp != Byte.MAX_VALUE) {
                    logMovement(n.verUpEdgeId, n.x, n.y, (byte)8);
                    decideOnNode(graph.nodes[n.verUpLinkedX][n.verUpLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(n.horRightEdgeId != -1){
                if(lookingFor == -1 && hint == 4) {
                    logMovement(n.horRightEdgeId, n.x, n.y, (byte) 4);
                    decideOnNode(graph.nodes[n.horRightLinkedX][n.horRightLinkedY]);
                }else if(edgesUsed[n.horRightEdgeId] == lookingFor && hintRight != Byte.MAX_VALUE) {
                    logMovement(n.horRightEdgeId, n.x, n.y, (byte) 4);
                    decideOnNode(graph.nodes[n.horRightLinkedX][n.horRightLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(n.verDownEdgeId != -1){
                if(lookingFor == -1 && hint == 2) {
                    logMovement(n.verDownEdgeId, n.x, n.y, (byte) 2);
                    decideOnNode(graph.nodes[n.verDownLinkedX][n.verDownLinkedY]);

                }else if(edgesUsed[n.verDownEdgeId] == lookingFor && hintDown != Byte.MAX_VALUE) {
                    logMovement(n.verDownEdgeId, n.x, n.y, (byte) 2);
                    decideOnNode(graph.nodes[n.verDownLinkedX][n.verDownLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(n.horLeftEdgeId != -1){
                if(lookingFor == -1 && hint == 1) {
                    logMovement(n.horLeftEdgeId, n.x, n.y, (byte) 1);
                    decideOnNode(graph.nodes[n.horLeftLinkedX][n.horLeftLinkedY]);
                }else if(edgesUsed[n.horLeftEdgeId] == lookingFor && hintLeft != Byte.MAX_VALUE) {
                    logMovement(n.horLeftEdgeId, n.x, n.y, (byte) 1);
                    decideOnNode(graph.nodes[n.horLeftLinkedX][n.horLeftLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(hint != 0){
                revertLast();
                return;
            }

        }

        revertLast();
    }


    private boolean[][] toIterate = new boolean[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    private void resetToIterate(){
        for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
            for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
                toIterate[x][y] = preparedMap[y][x] == FREE;
            }
        }
    }

    private byte countDots(byte x, byte y, EnvironmentController.Direction from, byte masterStartX, byte masterStartY){

        if(x == masterStartX && y == masterStartY){
            return -Byte.MAX_VALUE;
        }


        byte result = 0;

        if(x >= 0 && y >= 0 && x < EnvironmentController.mazeWidth && y < EnvironmentController.mazeHeight && toIterate[x][y]){

            boolean notCollected = visited[x][y] == 0;
            result = (byte)(notCollected ? -1 : 1);
            toIterate[x][y] = false;

            if((x != EnvironmentController.startX || y + 1 != EnvironmentController.startY) && from != EnvironmentController.Direction.UP){
                byte val = countDots(x, (byte)(y - 1), EnvironmentController.Direction.DOWN, masterStartX, masterStartY);
                if(val == -Byte.MAX_VALUE)
                    return -Byte.MAX_VALUE;
                if(result < 0){
                    result -= Math.abs(val);
                }else {
                    result += Math.abs(val);
                }
                if(val < 0 || notCollected){
                    if(result > 0)
                        result *= -1;
                }
            }
            if(from != EnvironmentController.Direction.RIGHT){
                byte val = countDots((byte)(x + 1), y, EnvironmentController.Direction.LEFT, masterStartX, masterStartY);
                if(val == -Byte.MAX_VALUE)
                    return -Byte.MAX_VALUE;

                if(result < 0){
                    result -= Math.abs(val);
                }else {
                    result += Math.abs(val);
                }
                if(val < 0 || notCollected){
                    if(result > 0)
                        result *= -1;
                }
            }
            if((x != EnvironmentController.startX || y + 1 != EnvironmentController.startY) && from != EnvironmentController.Direction.DOWN){
                byte val = countDots(x, (byte)(y + 1), EnvironmentController.Direction.UP, masterStartX, masterStartY);
                if(val == -Byte.MAX_VALUE)
                    return -Byte.MAX_VALUE;
                if(result < 0){
                    result -= Math.abs(val);
                }else {
                    result += Math.abs(val);
                }
                if(val < 0 || notCollected){
                    if(result > 0)
                        result *= -1;
                }
            }
            if(from != EnvironmentController.Direction.LEFT){
                byte val = countDots((byte)(x - 1), y, EnvironmentController.Direction.RIGHT, masterStartX, masterStartY);
                if(val == -Byte.MAX_VALUE)
                    return -Byte.MAX_VALUE;
                if(result < 0){
                    result -= Math.abs(val);
                }else {
                    result += Math.abs(val);
                }
                if(val < 0 || notCollected){
                    if(result > 0)
                        result *= -1;
                }
            }
        }


        return result;
    }

    public static Byte[] mergeEdges(Byte[] edge1, Byte[] edge2){
        byte edge1start = edge1[0];
        byte edge1end = edge1[edge1.length - 1];
        byte edge2start = edge2[0];
        byte edge2end = edge2[edge2.length - 1];
        Byte[] result = new Byte[edge1.length + edge2.length - 1];

        if(edge1start == edge2start){
            for (byte i = 0; i < edge1.length; i++){
                result[i] = edge1[edge1.length - 1 - i];
            }
            for (byte i = (byte) (edge1.length - 1); i < edge2.length + edge1.length - 1; i++){
                result[i] = edge2[i - edge1.length + 1];
            }
        }else if(edge1end == edge2start){
            for (byte i = 0; i < edge1.length; i++){
                result[i] = edge1[i];
            }
            for (byte i = (byte) (edge1.length - 1); i < edge2.length + edge1.length - 1; i++){
                result[i] = edge2[i - edge1.length + 1];
            }
        }else if(edge1start == edge2end){
            for (byte i = 0; i < edge1.length; i++){
                result[i] = edge1[edge1.length - 1 - i];
            }
            for (byte i = (byte) (edge1.length - 1); i < edge2.length + edge1.length - 1; i++){
                result[i] = edge2[edge2.length + edge1.length - 2 - i];
            }
        }else if(edge1end == edge2end){
            for (byte i = 0; i < edge1.length; i++){
                result[i] = edge1[i];
            }
            for (byte i = (byte) (edge1.length - 1); i < edge2.length + edge1.length - 1; i++){
                result[i] = edge2[edge2.length + edge1.length - 2 - i];
            }
        }

        return result;
    }

    @Override
    public synchronized void run() {
        for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
            for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
                controller.setField(x, y, preparedMap[y][x]);
            }
        }
        prepare();

        if(true)
            return;

        try {
            this.wait();
        } catch (InterruptedException ignored) {}

        System.out.println("Start signal received, moving.");

        if(true)
            return;

        Movement.move(route, controller);

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
