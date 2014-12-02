package lego.bots.node;

import lego.api.controllers.EnvironmentController;
import lego.util.PositionBatchQueue;
import lego.util.BatchQueue;

/**
 * Private property.
 * User: jIRKA
 * Date: 18.11.2014
 * Time: 10:19
 */
public final class Movement {

    public static final byte STACK_SIZE = 16;

    public static void move(PositionBatchQueue route, EnvironmentController controller){
        BatchQueue<EnvironmentController.Direction> directions = new BatchQueue<EnvironmentController.Direction>(STACK_SIZE);
        BatchQueue<Byte> distances = new BatchQueue<Byte>(STACK_SIZE);

        EnvironmentController.Direction actualDir = null;
        byte movingDist = 0;

        if(route.isEmpty())
            return;

        byte prevX = route.retreiveFirstX();
        byte prevY = route.retreiveFirstY();
        route.moveReadHead();

        while(!route.isEmpty()){
            byte nextX = route.retreiveFirstX();
            byte nextY = route.retreiveFirstY();
            route.moveReadHead();

            if(nextX != prevX || nextY != prevY) {

                if (nextX == prevX && nextY == prevY + 1) {
                    if (actualDir == EnvironmentController.Direction.DOWN) {
                        movingDist++;
                    } else {
                        if (movingDist > 0) {
                            directions.add(actualDir);
                            distances.add(movingDist);
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
                            directions.add(actualDir);
                            distances.add(movingDist);
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
                            directions.add(actualDir);
                            distances.add(movingDist);
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
                            directions.add(actualDir);
                            distances.add(movingDist);
                        }
                        actualDir = EnvironmentController.Direction.RIGHT;
                        movingDist = 1;
                    }
                }
            }

            prevX = nextX;
            prevY = nextY;
        }
        if(movingDist > 0) {
            directions.add(actualDir);
            distances.add(movingDist);
        }

        while(!directions.isEmpty()){
            actualDir = directions.remove();
            movingDist = distances.remove();

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
