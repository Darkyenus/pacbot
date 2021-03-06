package lego.bots.node;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.api.controllers.EnvironmentController.Direction;
import lego.util.*;

import java.io.*;
import java.util.ArrayList;

/**
 * Private property.
 * User: jIRKA
 * Date: 17.11.2014
 * Time: 18:31
 */
public final class NodeBot extends Bot<EnvironmentController> {

    private static final byte MAX_ALLOWED_COMPLEXITY = 50;
    static final int STACK_SIZE = 32;
    private static final int ARTIFACTS_SEARCH_SIZE = 4;

    private final Latch startLatch = new Latch();
    private static final Object SAVE_ROUTE_LOCK = new Object();

    private final PositionBatchQueue route = new PositionBatchQueue(STACK_SIZE);

    private final GraphStruct graph = new GraphStruct();

    private void prepare(){
        graph.prepareNodes(controller);

        //Debug.printNodes(graph.nodes, graph.edges.getCopyAsArray());

        findBestWay();

        byte[] edgePath = graph.edges.get(bestPath[0]);
        byte i;
        for(i = 1; i < bestPathLength; i++){
            edgePath = mergeEdges(edgePath, graph.edges.get(bestPath[i]));
        }

        if(edgePath[0] != ((EnvironmentController.startX << 4) | (EnvironmentController.startY))) {
            for (i = (byte)(edgePath.length - 1); i >= 0; i--) {
                route.pushNext((byte) ((edgePath[i] >> 4) & 15), (byte) (edgePath[i] & 15));
            }
        }else{
            for (i = 0; i < edgePath.length; i++) {
                route.pushNext((byte) ((edgePath[i] >> 4) & 15), (byte) (edgePath[i] & 15));
            }
        }

        route.compress();

        for(i = 0; i < route.size(); i++){
            visited[route.getXAt(i)][route.getYAt(i)] ++;
        }

        ignoreSomeDots(true);

        price = route.computePrice(GraphStruct.PRICE_MOVE, GraphStruct.PRICE_TURN_AROUND, GraphStruct.PRICE_TURN);

        System.out.println("Price: "+price);

        ignoreSomeDots(false); //Returns if we have enough time to get through maze.

        collectIgnoredDots();

        synchronized (SAVE_ROUTE_LOCK) {// So multiple NodeBots can run in parallel and not mess with each others saving
            saveRoute(controller.getMapIndex());
        }

    }

    private void ignoreSomeDots(boolean searchForArtifacts){
        short localMaxPrice;
        int localMaxPriceIndex;
        PositionBatchQueue tmp =  new PositionBatchQueue(5);

        while(price > GraphStruct.PRICE_MAX_ALLOWED || searchForArtifacts){

            localMaxPrice = 0;
            localMaxPriceIndex = -1;

            for(int i = 2; i < route.size() - 1; i++){

                if(route.getXAt(i) == route.getXAt(i - 2) && route.getYAt(i) == route.getYAt(i - 2)){

                    if(searchForArtifacts) {
                        if (visited[route.getXAt(i - 1)][route.getYAt(i - 1)] > 1) {
                            localMaxPriceIndex = i;
                            localMaxPrice = Short.MAX_VALUE; //Almost infinity
                        }
                    } else {

                        tmp.clear();

                        tmp.pushNext(route.getXAt(i - 3), route.getYAt(i - 3));
                        tmp.pushNext(route.getXAt(i - 2), route.getYAt(i - 2));
                        tmp.pushNext(route.getXAt(i - 1), route.getYAt(i - 1));
                        tmp.pushNext(route.getXAt(i), route.getYAt(i));
                        tmp.pushNext(route.getXAt(i + 1), route.getYAt(i + 1));

                        short localPrice = tmp.computePrice(GraphStruct.PRICE_MOVE, GraphStruct.PRICE_TURN_AROUND, GraphStruct.PRICE_TURN);

                        tmp.clear();

                        tmp.pushNext(route.getXAt(i - 3), route.getYAt(i - 3));
                        tmp.pushNext(route.getXAt(i - 2), route.getYAt(i - 2));
                        tmp.pushNext(route.getXAt(i + 1), route.getYAt(i + 1));

                        localPrice -= tmp.computePrice(GraphStruct.PRICE_MOVE, GraphStruct.PRICE_TURN_AROUND, GraphStruct.PRICE_TURN);

                        if (localPrice > localMaxPrice) {
                            localMaxPrice = localPrice;
                            localMaxPriceIndex = i;
                        }
                    }
                }

            }

            short lastBlockPrice = - GraphStruct.PRICE_MOVE;
            tmp.clear();
            tmp.pushNext(route.getXAt(route.size() - 3), route.getYAt(route.size() - 3));
            tmp.pushNext(route.getXAt(route.size() - 2), route.getYAt(route.size() - 2));
            tmp.pushNext(route.getXAt(route.size() - 1), route.getYAt(route.size() - 1));
            lastBlockPrice += tmp.computePrice(GraphStruct.PRICE_MOVE, GraphStruct.PRICE_TURN_AROUND, GraphStruct.PRICE_TURN);

            if(localMaxPrice > lastBlockPrice){
                visited[route.getXAt(localMaxPriceIndex - 1)][route.getYAt(localMaxPriceIndex - 1)] --;
                visited[route.getXAt(localMaxPriceIndex)][route.getYAt(localMaxPriceIndex)] --;

                route.changeValue(localMaxPriceIndex - 1, route.getXAt(localMaxPriceIndex), route.getYAt(localMaxPriceIndex));
                price -= localMaxPrice;
            }else{
                return;
            }

            route.compress();
        }
    }

    ArrayList<byte[]> originalFileContent = new ArrayList<byte[]>();

    private void loadEverything(){
        FileInputStream input = null;
        File mapsFile = new File("routes");
        try {
            input = new FileInputStream(mapsFile);
            int mapName = input.read();
            while (mapName != -1) {

                int skip = 0;
                skip += (input.read() - '0') * 1000;
                skip += (input.read() - '0') * 100;
                skip += (input.read() - '0') * 10;
                skip += (input.read() - '0');

                byte[] data = new byte[skip + 5];
                data[0] = (byte)mapName;
                data[1] = (byte)((skip / 1000) + '0');
                data[2] = (byte)(((skip / 100) % 10) + '0');
                data[3] = (byte)(((skip / 10) % 10) + '0');
                data[4] = (byte)((skip % 10) + '0');

                int next = input.read();
                int i = 5;
                while(next != '\n' && next != -1){
                    data[i++] = (byte)next;
                    next = input.read();
                }

                originalFileContent.add(data);

                mapName = input.read();

            }
        } catch (FileNotFoundException e) {
            controller.onError(EnvironmentController.ERROR_LOADING_POINTER_FILE_MISSING);
        } catch (IOException e) {
            controller.onError(EnvironmentController.ERROR_LOADING_MAP_CORRUPTED);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private void saveRoute(int name){
        if(name != -1){
            loadEverything();
            FileOutputStream output = null;
            File routesFile = new File("routes");

            try {
                output = new FileOutputStream(routesFile);

                output.write(name);
                int size = route.size();
                output.write((size*2 / 1000) + '0');
                output.write(((size*2 / 100) % 10) + '0');
                output.write(((size*2 / 10) % 10) + '0');
                output.write((size*2 % 10) + '0');

                for (int i = 0; i < size; i++) {

                    output.write('0' + route.getXAt(i));
                    output.write('0' + route.getYAt(i));

                }
                output.write('\n');

                //noinspection ForLoopReplaceableByForEach
                for(int j = 0; j < originalFileContent.size(); j ++){
                    if(originalFileContent.get(j)[0] != name) {
                        byte[] data = originalFileContent.get(j); //Saving CPU time
                        //noinspection ForLoopReplaceableByForEach
                        for (int i = 0; i < data.length; i++) {
                            output.write(data[i]);
                        }
                        output.write('\n');
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (Throwable ignored) {
                    }
                }
            }

        }
    }


    byte[] edgesUsed;
    byte[] edgesPrice;

    final ByteStack path = new ByteStack(STACK_SIZE);
    short price = 0;

    private byte[] bestPath = new byte[STACK_SIZE];
    private int bestPathLength = 0;
    private short bestPrice = Short.MAX_VALUE;

    final byte[][] visited = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    /** 4 most significant bits contain direction to which has bot went
     *  4 least significant bits contain direction from which bot arrived */
    final ByteStack directionStack = new ByteStack(STACK_SIZE);
    final PositionStack positionStack = new PositionStack(STACK_SIZE);

    /** Called once from prepare() */
    public void findBestWay(){

        edgesUsed = new byte[graph.edges.size()];
        edgesPrice = new byte[graph.edges.size()];

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
            byte[] edge = graph.edges.get(i);
            for(byte e:edge){
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

    }

    private boolean checkCompletedMap(byte posX, byte posY){
        for (int x = EnvironmentController.mazeWidth - 1; x >= 0; x--) {
            for (int y = EnvironmentController.mazeHeight - 1; y >= 0; y--) {
                if(visited[x][y] == 0 && !(posX == x && posY == y)){
                    return false;
                }
            }
        }
        if(price < bestPrice){
            if(path.size() > bestPath.length){
                int newSize = bestPath.length << 1;
                while(newSize < path.size()){
                    newSize <<= 1;
                }
                bestPath = new byte[newSize];
            }
            path.getCopyAsArray(bestPath);
            bestPathLength = path.size();

            bestPrice = price;
            controller.onError(EnvironmentController.WARNING_ALERT);
            //System.out.println("Found one ("+price+")");
        }
        revertLast();
        return true;
    }

    private void revertLast(){
        if (!path.isEmpty()) {
            byte edgeId = path.pop();

            edgesUsed[edgeId] = (byte) (edgesUsed[edgeId] - 1);
            price -= edgesPrice[edgeId];

            byte lastDir =  directionStack.pop();
            price -= nodeTurnPrice((byte)(lastDir & 15), (byte)0);
            directionStack.pop();

            byte[] edge = graph.edges.get(edgeId);
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
        }
    }

    private void logMovement(byte edgeId, byte fromX, byte fromY,byte endX, byte endY, byte directionMoved){
        path.push(edgeId);
        edgesUsed[edgeId] = (byte) (edgesUsed[edgeId] + 1);

        byte[] edge = graph.edges.get(edgeId);
        for(int i = 1; i < edge.length - 1; i++){ //Affect everything except first and last
            byte x = (byte)((edge[i] >> 4) & 15);
            byte y = (byte)(edge[i] & 15);
            visited[x][y] = (byte)(visited[x][y] + 1);
        }

        positionStack.push(fromX, fromY);

        byte directionCompressed = 0;
        if(directionMoved != 0) {
            while ((directionMoved >> directionCompressed++) != 1);
        }
        byte visitedVal = visited[fromX][fromY];
        visitedVal = (byte)((((visitedVal >> 4) & 15) + 1) << 4);
        visitedVal = (byte)((visitedVal & 240) | 4 | directionCompressed);
        visited[fromX][fromY] = visitedVal;

        byte arrivedFrom = 0;
        Node n = graph.nodes[endX][endY];
        if(n.verUpEdgeId != -1 && n.verUpLinkedX == fromX && n.verUpLinkedY == fromY){
            arrivedFrom = 8;
        }else if(n.horRightEdgeId != -1 && n.horRightLinkedX == fromX && n.horRightLinkedY == fromY){
            arrivedFrom = 4;
        }else if(n.verDownEdgeId != -1 && n.verDownLinkedX == fromX && n.verDownLinkedY == fromY){
            arrivedFrom = 2;
        }else if(n.horLeftEdgeId != -1 && n.verDownLinkedX == fromX && n.verDownLinkedY == fromY){
            arrivedFrom = 1;
        }

        price += edgesPrice[edgeId];
        price += nodeTurnPrice(directionMoved, arrivedFrom);
    }

    private byte nodeTurnPrice(byte dir, byte arrivedFrom){
        byte lastDir = (byte)(directionStack.isEmpty() ? 0 : (directionStack.peek() & 0xF));

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

        directionStack.push((byte)(dir | (arrivedFrom << 4)));
        return result;
    }

    /** Called once from findBestWay with start node and then recursively from itself
     *
     * */
    private void decideOnNode(final Node n){

        if (price >= bestPrice) {
            revertLast();
            return;
        }

        if(directionStack.size() > MAX_ALLOWED_COMPLEXITY){
            revertLast();
            return;
        }

        if (checkCompletedMap(n.x, n.y)) {
            return;
        }

        byte hintUp = 0, hintRight = 0, hintDown = 0, hintLeft = 0;
        byte returnedFrom = (byte)(directionStack.isEmpty() ? 0 : (((directionStack.peek() >> 4) & 0xF)));//Return 4 most significant bits or zero


        if(n.verUpEdgeId != -1){
            resetToIterate();
            byte val = countDots(Direction.DOWN, n.x, n.y);
            //System.out.println("count up: "+val);
            if(val != -Byte.MAX_VALUE){ //cyclic
                if(val >= 0){
                    hintUp = Byte.MAX_VALUE; //We don't want to go there, cuz there is nothing or we have been there already
                }else{ //collected partially or even not touched yet
                    if(returnedFrom == 8){
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
            byte val = countDots(Direction.LEFT, n.x, n.y);
            //System.out.println("count right: "+val);
            if(val != -Byte.MAX_VALUE){ //cyclic
                if(val >= 0){
                    hintRight = Byte.MAX_VALUE; //We don't want to go there, cuz there is nothing or we have been there already
                }else{ //collected partially or even not touched yet
                    if(returnedFrom == 4){
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
            byte val = countDots(Direction.UP, n.x, n.y);
            //System.out.println("count down: "+val);
            if(val != -Byte.MAX_VALUE){ //cyclic
                if(val >= 0){
                    hintDown = Byte.MAX_VALUE; //We don't want to go there, cuz there is nothing or we have been there already
                }else{ //collected partially or even not touched yet
                    if(returnedFrom == 2){
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
            byte val = countDots(Direction.RIGHT, n.x, n.y);
            //System.out.println("count left: "+val);
            if(val != -Byte.MAX_VALUE){ //cyclic
                if(val >= 0){
                    hintLeft = Byte.MAX_VALUE; //We don't want to go there, cuz there is nothing or we have been there already
                }else{ //collected partially or even not touched yet
                    if(returnedFrom == 1){
                        if((visited[n.x][n.y] & 7) == 4){
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
        if(hintUp != Byte.MAX_VALUE && hintUp != 0 && returnedFrom != 8){
            hintValue = hintUp;
            hint = 8;
        }
        if(hintRight != Byte.MAX_VALUE && hintRight != 0 && returnedFrom != 4){
            if(hintRight < hintValue) {
                hintValue = hintRight;
                hint = 4;
            }
        }
        if(hintDown != Byte.MAX_VALUE && hintDown != 0 && returnedFrom != 2){
            if(hintDown < hintValue) {
                hintValue = hintDown;
                hint = 2;
            }
        }
        if(hintLeft != Byte.MAX_VALUE && hintLeft != 0 && returnedFrom != 1){
            if(hintLeft < hintValue) {
                hint = 1;
            }
        }


        for(byte lookingFor = -1; lookingFor < 2; lookingFor ++){

            if(n.verUpEdgeId != -1){
                if(lookingFor == -1 && hint == 8){
                    logMovement(n.verUpEdgeId, n.x, n.y, n.verUpLinkedX, n.verUpLinkedY, (byte)8);
                    decideOnNode(graph.nodes[n.verUpLinkedX][n.verUpLinkedY]);
                }else if(edgesUsed[n.verUpEdgeId] == lookingFor && hintUp != Byte.MAX_VALUE) {
                    logMovement(n.verUpEdgeId, n.x, n.y, n.verUpLinkedX, n.verUpLinkedY, (byte)8);
                    decideOnNode(graph.nodes[n.verUpLinkedX][n.verUpLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(n.horRightEdgeId != -1){
                if(lookingFor == -1 && hint == 4) {
                    logMovement(n.horRightEdgeId, n.x, n.y, n.horRightLinkedX, n.horRightLinkedY, (byte) 4);
                    decideOnNode(graph.nodes[n.horRightLinkedX][n.horRightLinkedY]);
                }else if(edgesUsed[n.horRightEdgeId] == lookingFor && hintRight != Byte.MAX_VALUE) {
                    logMovement(n.horRightEdgeId, n.x, n.y, n.horRightLinkedX, n.horRightLinkedY, (byte) 4);
                    decideOnNode(graph.nodes[n.horRightLinkedX][n.horRightLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(n.verDownEdgeId != -1){
                if(lookingFor == -1 && hint == 2) {
                    logMovement(n.verDownEdgeId, n.x, n.y, n.verDownLinkedX, n.verDownLinkedY, (byte) 2);
                    decideOnNode(graph.nodes[n.verDownLinkedX][n.verDownLinkedY]);
                }else if(edgesUsed[n.verDownEdgeId] == lookingFor && hintDown != Byte.MAX_VALUE) {
                    logMovement(n.verDownEdgeId, n.x, n.y, n.verDownLinkedX, n.verDownLinkedY, (byte) 2);
                    decideOnNode(graph.nodes[n.verDownLinkedX][n.verDownLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(n.horLeftEdgeId != -1){
                if(lookingFor == -1 && hint == 1) {
                    logMovement(n.horLeftEdgeId, n.x, n.y, n.horLeftLinkedX, n.horLeftLinkedY, (byte) 1);
                    decideOnNode(graph.nodes[n.horLeftLinkedX][n.horLeftLinkedY]);
                }else if(edgesUsed[n.horLeftEdgeId] == lookingFor && hintLeft != Byte.MAX_VALUE) {
                    logMovement(n.horLeftEdgeId, n.x, n.y, n.horLeftLinkedX, n.horLeftLinkedY, (byte) 1);
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

    public void collectIgnoredDots(){
        botX = route.getXAt(route.size() - 1);
        botY = route.getYAt(route.size() - 1);
        calcDistances();
        while(!calcRoute(route)){
            calcDistances();
        }
    }

    private final byte[][] distances = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];
    private byte botX = -1;
    private byte botY = -1;
    byte lastDir = 0;
    private boolean calcRoute(PositionBatchQueue outputRoute) {
        byte targetX = Byte.MIN_VALUE;
        byte targetY = Byte.MIN_VALUE;
        byte minDist = Byte.MAX_VALUE;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                if( (x != botX || y != botY) && visited[x][y] == 0){
                    byte dist = distances[x][y];

                    if(dist < minDist){// || (dist == minDist && cmpDistFromBorder( x, y, targetX,targetY ))){

                        minDist = dist;
                        targetX = x;
                        targetY = y;

                    }
                }
            }
        }

        if(targetX == Byte.MIN_VALUE){
            return true;
        }

        PositionStack tmp = new PositionStack(STACK_SIZE);
        tmp.push(targetX, targetY);

        controller.setField(targetX, targetY, EnvironmentController.FREE_VISITED);

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

            if(!controller.isStart(targetX, targetY)) {
                controller.setField(targetX, targetY, EnvironmentController.FREE_VISITED);
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
            visited[tmp.peekX()][tmp.peekY()] ++;
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

            if(!controller.isObstacle(psX, psY)){
                byte psDistActual = distances[ psX ][ psY ];
                byte psDistNew = (byte) (psDistActual + ( controller.isFreeVisited(psX, psY)  ? 3 : 1 ));
                if( psX > 0 && !controller.isObstacle((byte)(psX -1), psY) && ( distances[ psX - 1 ][ psY ] > psDistNew ) ) {
                    distances[psX - 1][psY] = psDistNew;
                    toCalc.push((byte) (psX - 1), psY);
                }
                if( psX < EnvironmentController.mazeWidth && !controller.isObstacle((byte)(psX + 1), psY) && ( distances[ psX + 1 ][ psY ] > psDistNew ) ) {
                    distances[psX + 1][psY] = psDistNew;
                    toCalc.push((byte) (psX + 1), psY);
                }
                if( psY > 0 && !controller.isObstacle(psX , (byte)(psY - 1)) && !controller.isStart(psX, psY)  && ( distances[ psX ][ psY - 1 ] > psDistNew ) ) {
                    distances[psX][psY - 1] = psDistNew;
                    toCalc.push(psX, (byte) (psY - 1));
                }
                if( psY < EnvironmentController.mazeHeight && !controller.isObstacle(psX , (byte)(psY + 1)) && !controller.isStart(psX, (byte)(psY + 1)) && ( distances[ psX ][ psY + 1 ] > psDistNew ) ) {
                    distances[psX][psY + 1] = psDistNew;
                    toCalc.push(psX, (byte) (psY + 1));
                }
            }
        }
    }

    private void resetToIterate(){
        final byte[][] maze = controller.getMindMaze();
        final byte FREE_BIT = (byte) 	0x80;//EnvironmentController.FREE_BIT;
        final byte META_BIT = 0x20;
        final byte META_ANTIBIT = ~META_BIT;

        for (int x = EnvironmentController.mazeWidth - 1; x >= 0; x--) {
            for (int y = EnvironmentController.mazeHeight - 1; y >= 0; y--) {
                if((maze[x][y] & FREE_BIT) == FREE_BIT){
                    maze[x][y] |= META_BIT;
                }else{
                    maze[x][y] &= META_ANTIBIT;
                }
            }
        }
    }


    private final PositionDirectionCache dotCache = new PositionDirectionCache(STACK_SIZE);

    private byte countDots(final Direction from, final int startX, final int startY){
        if(dotCache.contains((byte) startX, (byte) startY, from)){
            return -Byte.MAX_VALUE;
        }
        byte res = countDotsRec(startX - from.x, startY - from.y, from, startX, startY);
        if(res == -Byte.MAX_VALUE){
            dotCache.add((byte) startX, (byte) startY, from);
        }
        return res;
    }

    /** Called from decideOnNode and from itself recursively
     * -Byte.MAX_VALUE = cyclic
     * zero and positive = dont go here ever
     * negative = go here, now
     *
     * */
    private byte countDotsRec(final int x, final int y, final Direction from, final int masterStartX, final int masterStartY){

        if(x == masterStartX && y == masterStartY){
            return -Byte.MAX_VALUE;
        }
        if (x < 0 || y < 0 || x >= EnvironmentController.mazeWidth || y >= EnvironmentController.mazeHeight || !controller.getMetaBitUnsafe(x, y)) {
            return 0;
        }

        byte result;
        boolean notCollected = visited[x][y] == 0;
        result = (byte)(notCollected ? -1 : 1);
        controller.unsetMetaBitUnsafe(x, y);

        if((x != EnvironmentController.startX || y + 1 != EnvironmentController.startY) && from != Direction.UP){
            byte val = countDotsRec(x, (y - 1), Direction.DOWN, masterStartX, masterStartY);
            if(val == -Byte.MAX_VALUE) {
                return -Byte.MAX_VALUE;
            }
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
        if(from != Direction.RIGHT){
            byte val = countDotsRec((x + 1), y, Direction.LEFT, masterStartX, masterStartY);
            if(val == -Byte.MAX_VALUE) {
                return -Byte.MAX_VALUE;
            }

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
        if((x != EnvironmentController.startX || y + 1 != EnvironmentController.startY) && from != Direction.DOWN){
            byte val = countDotsRec(x, (y + 1), Direction.UP, masterStartX, masterStartY);
            if(val == -Byte.MAX_VALUE) {
                return -Byte.MAX_VALUE;
            }
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
        if(from != Direction.LEFT){
            byte val = countDotsRec((x - 1), y, Direction.RIGHT, masterStartX, masterStartY);
            if(val == -Byte.MAX_VALUE) {
                return -Byte.MAX_VALUE;
            }
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

        return result;
    }

    public static byte[] mergeEdges(byte[] edge1, byte[] edge2){
        byte edge1start = edge1[0];
        byte edge1end = edge1[edge1.length - 1];
        byte edge2start = edge2[0];
        byte edge2end = edge2[edge2.length - 1];
        byte[] result = new byte[edge1.length + edge2.length - 1];

        if(edge1start == edge2start){
            for (byte i = 0; i < edge1.length; i++){
                result[i] = edge1[edge1.length - 1 - i];
            }
            System.arraycopy(edge2, (byte) (edge1.length - 1) - edge1.length + 1, result, (byte) (edge1.length - 1), edge2.length + edge1.length - 1 - (byte) (edge1.length - 1));
            /*for (byte i = (byte) (edge1.length - 1); i < edge2.length + edge1.length - 1; i++){
                result[i] = edge2[i - edge1.length + 1];
            }*/
        }else if(edge1end == edge2start){
            System.arraycopy(edge1, 0, result, 0, edge1.length);
            /*for (byte i = 0; i < edge1.length; i++){
                result[i] = edge1[i];
            }*/
            System.arraycopy(edge2, (byte) (edge1.length - 1) - edge1.length + 1, result, (byte) (edge1.length - 1), edge2.length + edge1.length - 1 - (byte) (edge1.length - 1));
            /*for (byte i = (byte) (edge1.length - 1); i < edge2.length + edge1.length - 1; i++){
                result[i] = edge2[i - edge1.length + 1];
            }*/
        }else if(edge1start == edge2end){
            for (byte i = 0; i < edge1.length; i++){
                result[i] = edge1[edge1.length - 1 - i];
            }
            for (byte i = (byte) (edge1.length - 1); i < edge2.length + edge1.length - 1; i++){
                result[i] = edge2[edge2.length + edge1.length - 2 - i];
            }
        }else if(edge1end == edge2end){
            System.arraycopy(edge1, 0, result, 0, edge1.length);
            /*for (byte i = 0; i < edge1.length; i++){
                result[i] = edge1[i];
            }*/
            for (byte i = (byte) (edge1.length - 1); i < edge2.length + edge1.length - 1; i++){
                result[i] = edge2[edge2.length + edge1.length - 2 - i];
            }
        }

        return result;
    }

    @Override
    public synchronized void run() {
        startLatch.pass();

        Movement.move(route, controller);
    }


    @Override
    public void onEvent(BotEvent event) {
        switch (event){
            case RUN_ENDED:
                startLatch.open();
                break;
            case RUN_STARTED:
                startLatch.open();
                break;
            case RUN_PREPARE:
                prepare();
                break;
        }
    }
}
