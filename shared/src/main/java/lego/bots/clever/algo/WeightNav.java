package lego.bots.clever.algo;

import lego.api.controllers.EnvironmentController;
import lego.bots.clever.Algo;
import lego.util.PositionQueue;
import lego.util.PositionStack;

/**
 * Weight navigated bot. This computes weight of every field on map and navigates the shortest way to target (unvisited field with best weight)
 *
 * Private property.
 * User: jIRKA
 * Date: 23/10/14
 * Time: 10:23
 */
public final class WeightNav extends Algo {

    private final byte[][] distances = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    private byte botX = -1;
    private byte botY = -1;

    private final Node[][] nodes = new Node[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

    //Kind of pooled vars; do not make final
    private byte calcRouteTargetX;
    private byte calcRouteTargetY;
    private short calcRouteDist;
    private byte calcRoutePsX;
    private byte calcRoutePsY;
    private byte calcRouteRobotPosX;
    private byte calcRouteRobotPosY;
    private short calcRouteMinDist;
    private byte calcRouteLastDir;


    @Override
    public void run(){

        boolean done = false;
        botX = EnvironmentController.startX;
        botY = EnvironmentController.startY;

        generateSpecialPriority();

        while(!done) {
            calcDistances();
            done = calcRoute(bestRoute);
        }

        bestRoutePrice = computePrice(bestRoute);
        bestScoredPoints = 40;

    }

    private byte getAccessibility(byte x, byte y){

        byte result = 0;

        if((controller.isFreeUnvisited(x, (byte) (y - 1)) || controller.isStart(x, (byte) (y - 1))) && !controller.isStart(x, y)){
            result = (byte) (result | 8);
        }
        if(controller.isFreeUnvisited((byte) (x + 1), y)){
            result = (byte) (result | 4);
        }
        if(controller.isFreeUnvisited(x, (byte) (y + 1)) && !controller.isStart(x, (byte) (y + 1))){
            result = (byte) (result | 2);
        }
        if(controller.isFreeUnvisited((byte) (x - 1), y)){
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

        boolean cont = true;

        byte accessibility;
        boolean loop = false;
        boolean changed;

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

            } else if (accessibility != 0 && (accessibility & (accessibility - 1)) != 0) { //Kind of magic. More possible ways

                Node n = nodes[workX][workY];
                if (n == null) {
                    n = new Node();
                    n.x = workX;
                    n.y = workY;
                }

                changed = false;
                if (prevX == workX && prevY == workY - 1 && n.verUpEdgeId == -1) { //Prev UP
                    n.verUpEdgeId = 1;
                    n.verUpLinkedX = lastNode.x;
                    n.verUpLinkedY = lastNode.y;
                    changed = true;
                } else if (prevX == workX + 1 && prevY == workY  && n.horRightEdgeId == -1) { //Prev RIGHT
                    n.horRightEdgeId = 1;
                    n.horRightLinkedX = lastNode.x;
                    n.horRightLinkedY = lastNode.y;
                    changed = true;
                } else if (prevX == workX && prevY == workY + 1 && n.verDownEdgeId == -1) { //Prev DOWN
                    n.verDownEdgeId = 1;
                    n.verDownLinkedX = lastNode.x;
                    n.verDownLinkedY = lastNode.y;
                    changed = true;
                } else if (prevX == workX - 1 && prevY == workY && n.horLeftEdgeId == -1) { //Prev LEFT
                    n.horLeftEdgeId = 1;
                    n.horLeftLinkedX = lastNode.x;
                    n.horLeftLinkedY = lastNode.y;
                    changed = true;
                }

                if(changed){
                    if (lastDirectionLeaving == 8) { //Up
                        lastNode.verUpEdgeId = 1;
                        lastNode.verUpLinkedX = workX;
                        lastNode.verUpLinkedY = workY;
                    } else if (lastDirectionLeaving == 4) { //Right
                        lastNode.horRightEdgeId = 1;
                        lastNode.horRightLinkedX = workX;
                        lastNode.horRightLinkedY = workY;
                    } else if (lastDirectionLeaving == 2) { //Down
                        lastNode.verDownEdgeId = 1;
                        lastNode.verDownLinkedX = workX;
                        lastNode.verDownLinkedY = workY;
                    } else if (lastDirectionLeaving == 1) { //Left
                        lastNode.horLeftEdgeId = 1;
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
                loop = true;
            } else if(accessibility == 0) { //Dead end

                Node n = nodes[workX][workY];
                if (n == null) {
                    n = new Node();
                    n.x = workX;
                    n.y = workY;
                }

                changed = false;
                if (prevX == workX && prevY == workY - 1 && n.verUpEdgeId == -1) { //Prev UP
                    n.verUpEdgeId = 1;
                    n.verUpLinkedX = lastNode.x;
                    n.verUpLinkedY = lastNode.y;
                    changed = true;
                } else if (prevX == workX + 1 && prevY == workY  && n.horRightEdgeId == -1) { //Prev RIGHT
                    n.horRightEdgeId = 1;
                    n.horRightLinkedX = lastNode.x;
                    n.horRightLinkedY = lastNode.y;
                    changed = true;
                } else if (prevX == workX && prevY == workY + 1 && n.verDownEdgeId == -1) { //Prev DOWN
                    n.verDownEdgeId = 1;
                    n.verDownLinkedX = lastNode.x;
                    n.verDownLinkedY = lastNode.y;
                    changed = true;
                } else if (prevX == workX - 1 && prevY == workY && n.horLeftEdgeId == -1) { //Prev LEFT
                    n.horLeftEdgeId = 1;
                    n.horLeftLinkedX = lastNode.x;
                    n.horLeftLinkedY = lastNode.y;
                    changed = true;
                }

                if(changed){
                    if (lastDirectionLeaving == 8) { //Up
                        lastNode.verUpEdgeId = 1;
                        lastNode.verUpLinkedX = workX;
                        lastNode.verUpLinkedY = workY;
                    } else if (lastDirectionLeaving == 4) { //Right
                        lastNode.horRightEdgeId = 1;
                        lastNode.horRightLinkedX = workX;
                        lastNode.horRightLinkedY = workY;
                    } else if (lastDirectionLeaving == 2) { //Down
                        lastNode.verDownEdgeId = 1;
                        lastNode.verDownLinkedX = workX;
                        lastNode.verDownLinkedY = workY;
                    } else if (lastDirectionLeaving == 1) { //Left
                        lastNode.horLeftEdgeId = 1;
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


    private boolean calcRoute(PositionQueue outputRoute) {
        calcRouteTargetX = Byte.MIN_VALUE;
        calcRouteTargetY = Byte.MIN_VALUE;
        calcRouteMinDist = Byte.MAX_VALUE;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                if( (x != botX || y != botY) && controller.isFreeUnvisited(x,y)){
                    calcRouteDist = (short)(distances[x][y] * 3);
                    if(controller.isObstacle((byte)(x + 1), y) || controller.isFreeUnvisited((byte)(x + 1), y)){
                        calcRouteDist -= 1;
                    }else if(controller.isObstacle((byte)(x - 1), y) || controller.isFreeVisited((byte)(x - 1), y)){
                        calcRouteDist -= 1;
                    }else if(controller.isObstacle(x, (byte)(y + 1)) || controller.isFreeVisited(x, (byte)(y + 1))){
                        calcRouteDist -= 1;
                    }else if(controller.isObstacle( x, (byte)(y - 1)) || controller.isFreeVisited(x , (byte)(y - 1))){
                        calcRouteDist -= 1;
                    }

                    calcRouteDist -= controller.getMetaNum(x, y);

                    if(botX == x && botY == y + 1 && calcRouteLastDir == 8){ //Moved up
                        calcRouteDist -= 2;
                    }else if(botX == x && botY == y - 1 && calcRouteLastDir == 2){ //Moved down
                        calcRouteDist -= 2;
                    }else if(botX == x + 1&& botY == y && calcRouteLastDir == 1){ //Moved left
                        calcRouteDist -= 2;
                    }else if(botX == x - 1 && botY == y && calcRouteLastDir == 4){ //Moved right
                        calcRouteDist -= 2;
                    }

                    if(calcRouteDist < calcRouteMinDist){// || (calcRouteDist == calcRouteMinDist && cmpDistFromBorder( x, y, calcRouteTargetX,calcRouteTargetY ))){

                        calcRouteMinDist = calcRouteDist;
                        calcRouteTargetX = x;
                        calcRouteTargetY = y;

                    }
                }
            }
        }

        if(calcRouteTargetX == Byte.MIN_VALUE && calcRouteTargetY == Byte.MIN_VALUE){
            return true;
        }

        PositionStack tmp = new PositionStack(STACK_SIZE);
        tmp.push(calcRouteTargetX, calcRouteTargetY);

        controller.setField(calcRouteTargetX, calcRouteTargetY, EnvironmentController.FREE_VISITED);

        calcRoutePsX = calcRouteTargetX;
        calcRoutePsY = calcRouteTargetY;
        calcRouteRobotPosX = botX;
        calcRouteRobotPosY = botY;
        botX = calcRouteTargetX;
        botY = calcRouteTargetY;

        byte count = 0;
        while( calcRoutePsX != calcRouteRobotPosX || calcRoutePsY != calcRouteRobotPosY ) {
            calcRouteMinDist = Byte.MAX_VALUE;
            calcRouteTargetX = calcRoutePsX;
            calcRouteTargetY = calcRoutePsY;

            if( calcRoutePsX > 0 && (distances[ calcRoutePsX - 1 ][ calcRoutePsY ] < calcRouteMinDist || (distances[ calcRoutePsX - 1 ][ calcRoutePsY ] <= calcRouteMinDist && calcRouteLastDir == 1))) {
                calcRouteMinDist = distances[ calcRoutePsX - 1 ][ calcRoutePsY ];
                calcRouteTargetX = (byte) (calcRoutePsX - 1);
                calcRouteTargetY = calcRoutePsY;
                calcRouteLastDir = 1;
            }

            if( calcRoutePsY > 0 && (distances[ calcRoutePsX ][ calcRoutePsY - 1 ] < calcRouteMinDist || (distances[ calcRoutePsX ][ calcRoutePsY - 1 ] <= calcRouteMinDist && calcRouteLastDir == 2))) {
                calcRouteMinDist = distances[ calcRoutePsX ][ calcRoutePsY - 1 ];
                calcRouteTargetX = calcRoutePsX;
                calcRouteTargetY = (byte)(calcRoutePsY - 1);
                calcRouteLastDir = 2;
            }

            if( calcRoutePsX < EnvironmentController.mazeWidth - 1 && (distances[ calcRoutePsX + 1 ][ calcRoutePsY ] < calcRouteMinDist || (distances[ calcRoutePsX + 1 ][ calcRoutePsY ] <= calcRouteMinDist && calcRouteLastDir == 4))) {
                calcRouteMinDist = distances[calcRoutePsX + 1][calcRoutePsY];
                calcRouteTargetX = (byte) (calcRoutePsX + 1);
                calcRouteTargetY = calcRoutePsY;
                calcRouteLastDir = 4;
            }

            if( calcRoutePsY < EnvironmentController.mazeHeight - 1 && (distances[ calcRoutePsX ][ calcRoutePsY + 1 ] < calcRouteMinDist || (distances[ calcRoutePsX ][ calcRoutePsY + 1 ] <= calcRouteMinDist && calcRouteLastDir == 8))) {
                calcRouteTargetX = calcRoutePsX;
                calcRouteTargetY = (byte)(calcRoutePsY + 1);
                calcRouteLastDir = 8;
            }

            if(!controller.isStart(calcRouteTargetX, calcRouteTargetY)) {
                controller.setField(calcRouteTargetX, calcRouteTargetY, EnvironmentController.FREE_VISITED);
            }
            tmp.push(calcRouteTargetX, calcRouteTargetY);

            calcRoutePsX = calcRouteTargetX;
            calcRoutePsY = calcRouteTargetY;

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
    private byte psDistActual;
    private byte psDistNew;

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
            calcRoutePsX = toCalc.peekX();
            calcRoutePsY = toCalc.peekY();
            toCalc.pop();

            if(!controller.isObstacle(calcRoutePsX, calcRoutePsY)){
                psDistActual = distances[ calcRoutePsX ][ calcRoutePsY ];
                psDistNew = (byte) (psDistActual + ( controller.isFreeVisited(calcRoutePsX, calcRoutePsY) ? 3 : 1 ));
                if( calcRoutePsX > 0 && !controller.isObstacle((byte)(calcRoutePsX -1), calcRoutePsY) && ( distances[ calcRoutePsX - 1 ][ calcRoutePsY ] > psDistNew ) ) {
                    distances[calcRoutePsX - 1][calcRoutePsY] = psDistNew;
                    toCalc.push((byte) (calcRoutePsX - 1), calcRoutePsY);
                }
                if( calcRoutePsX < EnvironmentController.mazeWidth - 1 && !controller.isObstacle((byte)(calcRoutePsX + 1), calcRoutePsY) && ( distances[ calcRoutePsX + 1 ][ calcRoutePsY ] > psDistNew ) ) {
                    distances[calcRoutePsX + 1][calcRoutePsY] = psDistNew;
                    toCalc.push((byte) (calcRoutePsX + 1), calcRoutePsY);
                }
                if( calcRoutePsY > 0 && !controller.isObstacle(calcRoutePsX , (byte)(calcRoutePsY - 1)) && !controller.isStart(calcRoutePsX, calcRoutePsY)  && ( distances[ calcRoutePsX ][ calcRoutePsY - 1 ] > psDistNew ) ) {
                    distances[calcRoutePsX][calcRoutePsY - 1] = psDistNew;
                    toCalc.push(calcRoutePsX, (byte) (calcRoutePsY - 1));
                }
                if( calcRoutePsY < EnvironmentController.mazeHeight - 1 && !controller.isObstacle(calcRoutePsX , (byte)(calcRoutePsY + 1)) && !controller.isStart(calcRoutePsX, (byte)(calcRoutePsY + 1)) && ( distances[ calcRoutePsX ][ calcRoutePsY + 1 ] > psDistNew ) ) {
                    distances[calcRoutePsX][calcRoutePsY + 1] = psDistNew;
                    toCalc.push(calcRoutePsX, (byte) (calcRoutePsY + 1));
                }
            }
        }
    }


    private void resetToIterate(){
        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
                if(controller.isFreeUnvisited(x, y)){
                    controller.setMetaBit(x, y);
                }else{
                    controller.unsetMetaBit(x, y);
                }
            }
        }
    }

    private void recursiveFill(byte x, byte y, EnvironmentController.Direction from){
        if(x >= 0 && y >= 0 && x < EnvironmentController.mazeWidth && y < EnvironmentController.mazeHeight && controller.getMetaBit(x, y)){

            controller.unsetMetaBit(x, y);
            controller.setMetaNum(x, y, (byte)(controller.getMetaNum(x, y) + 1));

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

        if(x >= 0 && y >= 0 && x < EnvironmentController.mazeWidth && y < EnvironmentController.mazeHeight && controller.getMetaBit(x, y)){

            result = 1;
            controller.unsetMetaBit(x, y);

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
}
