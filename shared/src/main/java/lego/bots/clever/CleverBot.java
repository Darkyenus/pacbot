package lego.bots.clever;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.bots.clever.algo.TriplePass;
import lego.bots.clever.algo.WeightNav;
import lego.util.Latch;
import lego.util.PositionQueue;
import lego.util.Queue;

/**
 * Private property.
 * User: jIRKA
 * Date: 23.11.2014
 * Time: 15:07
 */
public final class CleverBot  extends Bot<EnvironmentController> {

    private final Latch startLatch = new Latch();

    private final Queue<EnvironmentController.Direction> pDirections = new Queue<EnvironmentController.Direction>(Algo.STACK_SIZE);
    private final Queue<Byte> pDistances = new Queue<Byte>(Algo.STACK_SIZE);

    private PositionQueue bestRoute = null;
    private short bestUnitPrice = Short.MAX_VALUE;
    private byte bestScoredPoints;
    private String bestName; //TODO remove name stuff because of NXT performance

    public void prepare(){
        Runtime.getRuntime().gc();

        //Here goes every single algo that has been created. Ever.

        useAlgo(new TriplePass(), "Triple pass");
        useAlgo(new WeightNav(), "Weight navigation");

        //

        if(bestRoute != null){
            System.out.println("\nFound best route using \""+bestName+"\" algo.\nIt collects "+bestScoredPoints+"/40 points @ price of "+bestUnitPrice+"/100 points\n");
            preprocessRoute();
        }else{
            System.out.println("That's weird. None of algos returned any suitable route.");
        }
    }

    private void useAlgo(Algo a, String name){
        a.controller = controller;
        a.run();
        short unitPrice = (short)(a.getBestRoutePrice() * 100 / a.getBestScoredPoints());
        if(unitPrice < bestUnitPrice && a.getBestRoute() != null){
            bestRoute = a.getBestRoute();
            bestUnitPrice = unitPrice;
            bestScoredPoints = a.getBestScoredPoints();
            bestName = name;
        }
        for(byte x = 0; x < EnvironmentController.mazeWidth; x ++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
                if(controller.isFreeVisited(x, y))
                    controller.setField(x, y, EnvironmentController.FREE_UNVISITED);
                controller.unsetMetaBit(x, y);
                controller.setMetaNum(x, y, (byte) 0);
            }
        }
        controller.setField(EnvironmentController.startX, EnvironmentController.startY, EnvironmentController.START);

        Runtime.getRuntime().gc();
    }


    private void preprocessRoute(){
        EnvironmentController.Direction actualDir = null;
        byte movingDist = 0;

        byte prevX = 0;
        byte prevY = 0;
        byte nextX, nextY;

        if(!bestRoute.isEmpty()) {
            prevX = bestRoute.retreiveFirstX();
            prevY = bestRoute.retreiveFirstY();
            bestRoute.moveReadHead();
        }

        //Preprocess path
        while(!bestRoute.isEmpty()){
            nextX = bestRoute.retreiveFirstX();
            nextY = bestRoute.retreiveFirstY();
            bestRoute.moveReadHead();

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
