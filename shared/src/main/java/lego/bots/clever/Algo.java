package lego.bots.clever;

import lego.api.controllers.EnvironmentController;
import lego.util.PositionBatchQueue;

/**
 * Private property.
 * User: jIRKA
 * Date: 28.11.2014
 * Time: 21:15
 */
public abstract class Algo implements Runnable {

    public static final byte STACK_SIZE = 16;

    public static final byte PRICE_MOVE = 1;
    public static final byte PRICE_TURN = 1;
    public static final byte PRICE_TURN_AROUND = 2;

    protected EnvironmentController controller;

    protected final PositionBatchQueue bestRoute = new PositionBatchQueue(STACK_SIZE);
    protected byte bestScoredPoints = -1;
    protected short bestRoutePrice = Short.MAX_VALUE;

    public PositionBatchQueue getBestRoute(){
        return bestRoute;
    }

    public byte getBestScoredPoints(){
        return bestScoredPoints;
    }

    public short getBestRoutePrice(){
        return bestRoutePrice;
    }

    protected short computePrice(PositionBatchQueue route){
        short price = 0; //Because of some not really efficient algos

        EnvironmentController.Direction actualDir = null;

        byte prevX = 0;
        byte prevY = 0;
        byte nextX, nextY = 0;

        if(!route.isEmpty()) {
            prevX = route.retreiveFirstX();
            prevY = route.retreiveFirstY();
        }

        for(byte i = 1; i < route.size(); i++){
            nextX = route.getXAt(i);
            nextY = route.getYAt(i);

            if (nextX == prevX && nextY == prevY + 1) {
                if (actualDir == EnvironmentController.Direction.DOWN) {
                    price += PRICE_MOVE;
                } else if(actualDir == EnvironmentController.Direction.UP) {
                    price += PRICE_TURN_AROUND;
                    price += PRICE_MOVE;
                } else {
                    price += PRICE_TURN;
                    price += PRICE_MOVE;
                }
                actualDir = EnvironmentController.Direction.DOWN;
            }
            if (nextX == prevX && nextY == prevY - 1) {
                if (actualDir == EnvironmentController.Direction.UP) {
                    price += PRICE_MOVE;
                } else if(actualDir == EnvironmentController.Direction.DOWN) {
                    price += PRICE_TURN_AROUND;
                    price += PRICE_MOVE;
                } else {
                    price += PRICE_TURN;
                    price += PRICE_MOVE;
                }
                actualDir = EnvironmentController.Direction.UP;
            }
            if (nextX == prevX - 1 && nextY == prevY) {
                if (actualDir == EnvironmentController.Direction.LEFT) {
                    price += PRICE_MOVE;
                } else if(actualDir == EnvironmentController.Direction.RIGHT) {
                    price += PRICE_TURN_AROUND;
                    price += PRICE_MOVE;
                } else {
                    price += PRICE_TURN;
                    price += PRICE_MOVE;
                }
                actualDir = EnvironmentController.Direction.LEFT;
            }
            if (nextX == prevX + 1 && nextY == prevY) {
                if (actualDir == EnvironmentController.Direction.RIGHT) {
                    price += PRICE_MOVE;
                } else if(actualDir == EnvironmentController.Direction.LEFT) {
                    price += PRICE_TURN_AROUND;
                    price += PRICE_MOVE;
                } else {
                    price += PRICE_TURN;
                    price += PRICE_MOVE;
                }
                actualDir = EnvironmentController.Direction.RIGHT;
            }

            prevX = nextX;
            prevY = nextY;
        }

        return price;
    }

}
