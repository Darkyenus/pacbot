package lego.bots.node;

import lego.api.controllers.EnvironmentController;
import lego.util.PositionStack;
import lego.util.Queue;

/**
 * Private property.
 * User: jIRKA
 * Date: 18.11.2014
 * Time: 10:19
 */
public class Movement {

    public static final byte STACK_SIZE = 16;

    public static void move(PositionStack route, EnvironmentController controller){
        Queue<EnvironmentController.Direction> directions = new Queue<EnvironmentController.Direction>(STACK_SIZE);
        Queue<Byte> distances = new Queue<Byte>(STACK_SIZE);

        EnvironmentController.Direction actualDir = null;
        byte movingDist = 0;

        if(route.isEmpty())
            return;

        byte prevX = route.peekX();
        byte prevY = route.peekY();
        route.pop();

        while(!route.isEmpty()){
            byte nextX = route.peekX();
            byte nextY = route.peekY();
            route.pop();

            if(nextX != prevX || nextY != prevY) {

                if (nextX == prevX && nextY == prevY + 1) {
                    if (actualDir == EnvironmentController.Direction.DOWN) {
                        movingDist++;
                    } else {
                        if (movingDist > 0) {
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
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
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
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
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
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
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
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
            directions.pushNext(actualDir);
            distances.pushNext(movingDist);
        }

        while(!directions.isEmpty()){
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

}
