package lego.bots.load;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.api.controllers.PlannedController;
import lego.util.Latch;
import lego.util.PositionBatchQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Private property.
 * User: jIRKA
 * Date: 2.12.2014
 * Time: 16:44
 */
public class LoadBot extends Bot<PlannedController> {

    private static final int STACK_SIZE = 16;

    private final Latch startLatch = new Latch();

    private final PositionBatchQueue route = new PositionBatchQueue(STACK_SIZE);

    private void prepare(){
        if(loadRoute())
            preprocessRoute();
        else
            controller.onError(EnvironmentController.WARNING_ALERT);
    }

    private void preprocessRoute(){
        EnvironmentController.Direction actualDir = null;
        byte movingDist = 0;

        byte prevX = 0;
        byte prevY = 0;
        byte nextX, nextY;

        if(!route.isEmpty()) {
            prevX = route.retreiveFirstX();
            prevY = route.retreiveFirstY();
            route.moveReadHead();
        }

        //Preprocess path
        while(!route.isEmpty()){
            nextX = route.retreiveFirstX();
            nextY = route.retreiveFirstY();
            route.moveReadHead();

            if (nextX == prevX && nextY == prevY + 1) {
                if (actualDir == EnvironmentController.Direction.DOWN) {
                    movingDist++;
                } else {
                    if (movingDist > 0) {
                        if (actualDir == EnvironmentController.Direction.UP) {
                            controller.addYPath((byte) -movingDist);
                        } else if (actualDir == EnvironmentController.Direction.LEFT) {
                            controller.addXPath((byte) -movingDist);
                        } else {
                            controller.addXPath(movingDist);
                        }
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
                        if (actualDir == EnvironmentController.Direction.DOWN) {
                            controller.addYPath(movingDist);
                        } else if (actualDir == EnvironmentController.Direction.LEFT) {
                            controller.addXPath((byte) -movingDist);
                        } else {
                            controller.addXPath(movingDist);
                        }
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
                        if (actualDir == EnvironmentController.Direction.DOWN) {
                            controller.addYPath(movingDist);
                        } else if (actualDir == EnvironmentController.Direction.UP) {
                            controller.addYPath((byte) -movingDist);
                        } else {
                            controller.addXPath(movingDist);
                        }
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
                        if (actualDir == EnvironmentController.Direction.DOWN) {
                            controller.addYPath(movingDist);
                        } else if (actualDir == EnvironmentController.Direction.UP) {
                            controller.addYPath((byte) -movingDist);
                        } else {
                            controller.addXPath((byte) -movingDist);
                        }
                    }
                    actualDir = EnvironmentController.Direction.RIGHT;
                    movingDist = 1;
                }
            }

            prevX = nextX;
            prevY = nextY;
        }
        if(movingDist > 0) {
            if (actualDir == EnvironmentController.Direction.DOWN) {
                controller.addYPath(movingDist);
            } else if (actualDir == EnvironmentController.Direction.UP) {
                controller.addYPath((byte) -movingDist);
            } else if (actualDir == EnvironmentController.Direction.LEFT) {
                controller.addXPath((byte) -movingDist);
            } else {
                controller.addXPath(movingDist);
            }
        }

        controller.onError(EnvironmentController.SUCCESS_PATH_COMPUTED);
    }

    private boolean loadSavedRoute(int mapId){
        FileInputStream input = null;
        File mapsFile = new File("routes");
        try {
            input = new FileInputStream(mapsFile);
            while (true) {
                int mapName = input.read();
                if (mapName == mapId) {
                    input.skip(4);
                    int next = input.read();
                    while(next != '\n'){
                        byte x = (byte)(next - '0');
                        byte y = (byte)(input.read() - '0');

                        route.pushNext(x, y);

                        next = input.read();
                    }
                    return true;
                }else{
                    int skip = 1;
                    skip += (input.read() - '0') * 1000;
                    skip += (input.read() - '0') * 100;
                    skip += (input.read() - '0') * 10;
                    skip += (input.read() - '0');
                    if(input.skip(skip) == -1){
                        controller.onError(EnvironmentController.ERROR_LOADING_MAP_CORRUPTED);
                        return false;
                    }
                }
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
        return false;
    }

    private boolean loadRoute () {
        FileInputStream input = null;
        File mapsFile = new File("mappointer");
        try {
            input = new FileInputStream(mapsFile);
            int mapName = input.read();
            if (mapName == -1) {
                controller.onError(EnvironmentController.ERROR_LOADING_POINTER_FILE_CORRUPTED);
            } else {
                return loadSavedRoute(mapName);
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
        return false;
    }

    @Override
    public synchronized void run () {
        startLatch.pass();
        controller.travelPath();
    }

    @Override
    public void onEvent (BotEvent event) {
        switch (event) {
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
