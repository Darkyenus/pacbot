package lego.bots.clever.algo;

import lego.api.controllers.EnvironmentController;
import lego.bots.clever.Algo;
import lego.util.PositionStack;

/**
 * Private property.
 * User: jIRKA
 * Date: 28.11.2014
 * Time: 21:21
 */
public class TriplePass extends Algo{

    private final short[][] distances = new short[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight]; //After about 4 days of no idea I noticed that this might overflow
    byte botX = EnvironmentController.startX;
    byte botY = EnvironmentController.startY + 1; //I don't want to handle special cases for start field again.
    EnvironmentController.Direction botDir = EnvironmentController.Direction.DOWN;

    //Kind of pooled vars; do not make final
    private byte calcRouteTargetX;
    private byte calcRouteTargetY;
    private short calcRouteDist;
    private byte calcRoutePsX;
    private byte calcRoutePsY;
    private byte calcRouteRobotPosX;
    private byte calcRouteRobotPosY;
    private short calcRouteMinDist;
    
    @Override
    public void run() {
        //Move by one downwards
        bestRoute.pushNext(botX, (byte)(botY - 1));
        bestRoute.pushNext(botX, botY);

        for (byte x = 0; x < EnvironmentController.mazeWidth; x++) {
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++) {
                if(controller.isFree(x,y)){
                    controller.setMetaNum(x, y, (byte)0);
                }else{
                    controller.setMetaNum(x, y, (byte)31); //Cannot go there
                }
            }
        }

        controller.setMetaNum(botX, botY, (byte)1);

        try {
            while(true) {
                moveNext(bestRoute.size() - 1, (byte)-1, (byte)-1);
            }
        }catch (Error e){
            if(!"Done".equals(e.getMessage())){
                throw e;
            }
        }
        //TODO: postprocess();


        bestRoutePrice = computePrice(bestRoute);
        bestScoredPoints = 40; //TODO output from postprocess
    }

    private void checkForDeadEnds(){

        byte x, y;
        byte res;
        byte lastBotX = botX, lastBotY = botY;
        EnvironmentController.Direction lastBotDir = botDir;

        for(int i = 1; i < bestRoute.size() - 1; i ++){
            x = bestRoute.getXAt(i);
            y = bestRoute.getYAt(i);

            if(y < EnvironmentController.mazeHeight - 1 && controller.getMetaNum(x,(byte)(y + 1)) == 0){
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
            if(y > 0 && controller.getMetaNum(x,(byte)(y - 1)) == 0){
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
            if(x < EnvironmentController.mazeWidth - 1 && controller.getMetaNum((byte)(x + 1), y) == 0){
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
            if(x > 0 && controller.getMetaNum((byte)(x - 1), y) == 0){
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
        moveNext(bestRoute.size() - 1, (byte)-1, (byte)-1);
    }

    private byte moveNext(int index, byte returnX, byte returnY){
        byte res = 0;
        if(botX >= 0 && botY >= 0 && botX < EnvironmentController.mazeWidth && botY < EnvironmentController.mazeHeight && botX + botDir.x >= 0 && botY + botDir.y >= 0 && botX + botDir.x < EnvironmentController.mazeWidth && botY + botDir.y < EnvironmentController.mazeHeight && controller.getMetaNum((byte)(botX + botDir.x), (byte)(botY + botDir.y)) == 0){
            for(byte i = 1; i < 10; i ++){
                if(botX + botDir.x >= 0 && botY + botDir.y >= 0 && botX + botDir.x < EnvironmentController.mazeWidth && botY + botDir.y < EnvironmentController.mazeHeight && controller.getMetaNum((byte)(botX + botDir.x), (byte)(botY + botDir.y)) == 0){
                    botX += botDir.x;
                    botY += botDir.y;
                    controller.setMetaNum(botX, botY, (byte)(controller.getMetaNum(botX, botY) + 1));
                    bestRoute.insertAfter(index + i - 1, botX, botY);
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
                if((botX + leftDir.x * leftDist < 0 || botY + leftDir.y * leftDist < 0 || botX + leftDir.x * leftDist >= EnvironmentController.mazeWidth || botY + leftDir.y * leftDist >= EnvironmentController.mazeHeight) ||
                        (botX + leftDir.x * leftDist >= 0 && botY + leftDir.y * leftDist >= 0 && botX + leftDir.x * leftDist < EnvironmentController.mazeWidth && botY + leftDir.y * leftDist < EnvironmentController.mazeHeight &&
                                controller.getMetaNum((byte)(botX + leftDir.x * leftDist), (byte)(botY + leftDir.y * leftDist)) != 0)) {
                    leftDist --;
                    break;
                }
            }
            for(rightDist = 1; rightDist < 10; rightDist ++) {
                if ((botX + rightDir.x * rightDist < 0 || botY + rightDir.y * rightDist < 0 || botX + rightDir.x * rightDist >= EnvironmentController.mazeWidth || botY + rightDir.y * rightDist >= EnvironmentController.mazeHeight) ||
                        (botX + rightDir.x * rightDist >= 0 && botY + rightDir.y * rightDist >= 0 && botX + rightDir.x * rightDist < EnvironmentController.mazeWidth && botY + rightDir.y * rightDist < EnvironmentController.mazeHeight &&
                                controller.getMetaNum((byte)(botX + rightDir.x * rightDist), (byte)(botY + rightDir.y * rightDist)) != 0)){
                    rightDist --;
                    break;
                }
            }

            if(leftDist > 0 && ((rightDist > 0 && leftDist < rightDist) || rightDist <= 0)){
                for(byte i = 0; i < leftDist; i++){
                    botX += leftDir.x;
                    botY += leftDir.y;
                    controller.setMetaNum(botX, botY, (byte)(controller.getMetaNum(botX, botY) + 1));
                    bestRoute.insertAfter(index + i, botX, botY);
                }
                botDir = leftDir;
                res = leftDist;
            }else if(rightDist > 0){
                for(byte i = 0; i < rightDist; i++){
                    botX += rightDir.x;
                    botY += rightDir.y;
                    controller.setMetaNum(botX, botY, (byte)(controller.getMetaNum(botX, botY) + 1));
                    bestRoute.insertAfter(index + i, botX, botY);
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

            if(controller.getMetaNum(psX, psY) != 31){
                short psDistNew = (short) (distances[ psX ][ psY ] + (controller.getMetaNum(psX, psY) == 0 ? 1 : 5));
                if( psX > 0 && controller.getMetaNum((byte)(psX - 1), psY) != 31 && ( distances[ psX - 1 ][ psY ] > psDistNew ) ) {
                    distances[psX - 1][psY] = psDistNew;
                    toCalc.push((byte) (psX - 1), psY);
                }
                if( psX < EnvironmentController.mazeWidth - 1 && controller.getMetaNum((byte)(psX + 1), psY) != 31 && ( distances[ psX + 1 ][ psY ] > psDistNew ) ) {
                    distances[psX + 1][psY] = psDistNew;
                    toCalc.push((byte) (psX + 1), psY);
                }
                if( psY > 0 && controller.getMetaNum(psX, (byte)(psY - 1)) != 31  && ( distances[ psX ][ psY - 1 ] > psDistNew ) ) {
                    distances[psX][psY - 1] = psDistNew;
                    toCalc.push(psX, (byte) (psY - 1));
                }
                if( psY < EnvironmentController.mazeHeight - 1 && controller.getMetaNum(psX, (byte)(psY + 1)) != 31 && ( distances[ psX ][ psY + 1 ] > psDistNew ) ) {
                    distances[psX][psY + 1] = psDistNew;
                    toCalc.push(psX, (byte) (psY + 1));
                }
            }
        }
    }

    private byte calcRoute(int index, byte targetX, byte targetY){
        PositionStack tmp = new PositionStack(STACK_SIZE);
        tmp.push(targetX, targetY);

        calcRoutePsX = targetX;
        calcRoutePsY = targetY;
        botX = targetX; //We will end up here
        botY = targetY;
        calcRouteRobotPosX = botX;
        calcRouteRobotPosY = botY;

        EnvironmentController.Direction lastDir = botDir;

        byte count = 0;
        while( calcRoutePsX != calcRouteRobotPosX || calcRoutePsY != calcRouteRobotPosY ) {
            calcRouteMinDist = Byte.MAX_VALUE;
            targetX = calcRoutePsX;
            targetY = calcRoutePsY;

            if( calcRoutePsX > 0 && (distances[ calcRoutePsX - 1 ][ calcRoutePsY ] < calcRouteMinDist || (distances[ calcRoutePsX - 1 ][ calcRoutePsY ] <= calcRouteMinDist && lastDir == EnvironmentController.Direction.LEFT))) {
                calcRouteMinDist = distances[ calcRoutePsX - 1 ][ calcRoutePsY ];
                targetX = (byte) (calcRoutePsX - 1);
                targetY = calcRoutePsY;
                lastDir = EnvironmentController.Direction.LEFT;
            }

            if( calcRoutePsY > 0 && (distances[ calcRoutePsX ][ calcRoutePsY - 1 ] < calcRouteMinDist || (distances[ calcRoutePsX ][ calcRoutePsY - 1 ] <= calcRouteMinDist && lastDir == EnvironmentController.Direction.DOWN))) {
                calcRouteMinDist = distances[ calcRoutePsX ][ calcRoutePsY - 1 ];
                targetX = calcRoutePsX;
                targetY = (byte)(calcRoutePsY - 1);
                lastDir = EnvironmentController.Direction.DOWN;
            }

            if( calcRoutePsX < EnvironmentController.mazeWidth - 1 && (distances[ calcRoutePsX + 1 ][ calcRoutePsY ] < calcRouteMinDist || (distances[ calcRoutePsX + 1 ][ calcRoutePsY ] <= calcRouteMinDist && lastDir == EnvironmentController.Direction.RIGHT))) {
                calcRouteMinDist = distances[calcRoutePsX + 1][calcRoutePsY];
                targetX = (byte) (calcRoutePsX + 1);
                targetY = calcRoutePsY;
                lastDir = EnvironmentController.Direction.RIGHT;
            }

            if( calcRoutePsY < EnvironmentController.mazeHeight - 1 && (distances[ calcRoutePsX ][ calcRoutePsY + 1 ] < calcRouteMinDist || (distances[ calcRoutePsX ][ calcRoutePsY + 1 ] <= calcRouteMinDist && lastDir == EnvironmentController.Direction.UP))) {
                targetX = calcRoutePsX;
                targetY = (byte)(calcRoutePsY + 1);
                lastDir = EnvironmentController.Direction.UP;
            }

            tmp.push(targetX, targetY);

            if(calcRoutePsX == botX && calcRoutePsY == botY){
                botDir = lastDir;
            }

            calcRoutePsX = targetX;
            calcRoutePsY = targetY;

            if( count ++ > 100 ) {
                controller.onError(EnvironmentController.ERROR_STUCK_IN_LOOP);  // Cannot compute route, algo is stuck.
                break;
            }
        }
        tmp.pop(); //Here is the pos where I am
        byte i = 0;
        while(!tmp.isEmpty()){
            bestRoute.insertAfter(index + i, tmp.peekX(), tmp.peekY());
            controller.setMetaNum(tmp.peekX(), tmp.peekY(), (byte)(controller.getMetaNum(tmp.peekX(), tmp.peekY()) + 1));
            tmp.pop();
            i++;
        }
        return (byte)(index + i);
    }

    private byte calcRoute(int index) {

        calcRouteTargetX = Byte.MIN_VALUE;
        calcRouteTargetY = Byte.MIN_VALUE;
        calcRouteMinDist = Byte.MAX_VALUE;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                if((x != botX || y != botY) && controller.getMetaNum(x, y) == 0){
                    calcRouteDist = (short)(distances[x][y] * 2);

                    if(botX == x && botY == y + 1 && botDir == EnvironmentController.Direction.UP){
                        calcRouteDist -= 1;
                    }else if(botX == x && botY == y - 1 && botDir == EnvironmentController.Direction.DOWN){
                        calcRouteDist -= 1;
                    }else if(botX == x + 1&& botY == y && botDir == EnvironmentController.Direction.LEFT){
                        calcRouteDist -= 1;
                    }else if(botX == x - 1 && botY == y && botDir == EnvironmentController.Direction.RIGHT){
                        calcRouteDist -= 1;
                    }

                    if(calcRouteDist < calcRouteMinDist){// || (calcRouteDist == minDist && cmpDistFromBorder( x, y, calcRouteTargetX,calcRouteTargetY ))){

                        calcRouteMinDist = calcRouteDist;
                        calcRouteTargetX = x;
                        calcRouteTargetY = y;

                    }
                }
            }
        }
        if(calcRouteTargetX == Byte.MIN_VALUE || calcRouteTargetX == Byte.MIN_VALUE){
            throw new Error("Done");
        }

        return calcRoute(index, calcRouteTargetX, calcRouteTargetY);
    }

    private void resetToIterate(){
        for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
            for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
                if(controller.getMetaNum(x, y) == 0){
                    controller.setMetaBit(x, y);
                }else{
                    controller.unsetMetaBit(x, y);
                }
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

        if(x >= 0 && y >= 0 && x < EnvironmentController.mazeWidth && y < EnvironmentController.mazeHeight && controller.getMetaBit(x,y)){

            result = 1;
            controller.unsetMetaBit(x, y);

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
    
}
