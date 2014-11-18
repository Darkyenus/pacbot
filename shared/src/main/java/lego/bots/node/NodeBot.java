package lego.bots.node;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.PositionStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Private property.
 * User: jIRKA
 * Date: 17.11.2014
 * Time: 18:31
 */
public class NodeBot extends Bot<EnvironmentController> {

    private static final int STACK_SIZE = 16;
    private static final int MAX_ALLOWED_MOVES_ON_EDGE = 2;

    private boolean continueRunning = true;

    private static final EnvironmentController.FieldStatus FREE = EnvironmentController.FieldStatus.FREE_UNVISITED;
    private static final EnvironmentController.FieldStatus BLOCK = EnvironmentController.FieldStatus.OBSTACLE;
    private static final EnvironmentController.FieldStatus START = EnvironmentController.FieldStatus.START;




    private final EnvironmentController.FieldStatus[][] preparedMap = {
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE},
            { FREE, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            { FREE, BLOCK,  FREE, BLOCK, START, BLOCK,  FREE, BLOCK,  FREE},
            { FREE, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            { FREE, BLOCK,  FREE, BLOCK, BLOCK, BLOCK,  FREE, BLOCK,  FREE},
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE}
    };



    /*
    private final EnvironmentController.FieldStatus[][] preparedMap = {
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            {BLOCK, BLOCK,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE},
            { FREE,  FREE, BLOCK, BLOCK, START, BLOCK,  FREE,  FREE,  FREE},
            { FREE,  FREE,  FREE,  FREE,  FREE,  FREE,  FREE, BLOCK,  FREE},
            { FREE,  FREE,  FREE, BLOCK, BLOCK, BLOCK,  FREE, BLOCK,  FREE},
            { FREE,  FREE,  FREE, BLOCK,  FREE,  FREE,  FREE, BLOCK,  FREE}
    };
    */


    private final PositionStack route = new PositionStack(STACK_SIZE);

    private final GraphStruct graph = new GraphStruct();

    private void prepare(){
        System.out.println("Computation started");

        graph.prepareNodes(preparedMap);

        Debug.printNodes(graph.nodes, graph.edges);

        System.out.println("Graph prepared");

        findBestWay();

        System.out.println("Computation ended, waiting for start signal");
    }


    Byte[] edgesUsed;
    Byte[] edgesPrice;

    ArrayList<Byte> path;
    short price = 0;

    Byte[] bestPath;
    short bestPrice = Short.MAX_VALUE;

    byte[][] visited;

    public void findBestWay(){
        edgesUsed = new Byte[graph.edges.size()];
        edgesPrice = new Byte[graph.edges.size()];
        path = new ArrayList<Byte>();
        bestPath = new Byte[0];
        price = 0;
        visited = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

        for(int x = 0; x < EnvironmentController.mazeWidth; x ++){
            for(int y = 0; y < EnvironmentController.mazeHeight; y ++){
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

        for(int i = 0; i < graph.edges.size(); i ++){
            Byte[] edge = graph.edges.get(i);
            for(Byte e:edge){
                byte x = (byte)((e >> 4) & 15);
                byte y = (byte)(e & 15);
                visited[x][y] = 0;
            }
        }

        for(int i = 0; i < edgesUsed.length; i++){
            edgesUsed[i] = 0;
        }
        path.clear();


        Node startNode = graph.nodes[EnvironmentController.startX][EnvironmentController.startY];

        decideOnNode(startNode);

        checkCompletedMap();

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


    }

    private boolean checkCompletedMap(){
        boolean complete = true;

        for(int x = 0; x < EnvironmentController.mazeWidth; x++){
            for(int y = 0; y < EnvironmentController.mazeHeight; y++){
                if(visited[x][y] == 0){
                    complete = false;
                    x = EnvironmentController.mazeWidth;
                    y = EnvironmentController.mazeHeight;
                }
            }
        }

        if(complete){

            if(price < bestPrice){
                bestPath = path.toArray(new Byte[path.size()]);
                bestPrice = price;
            }

            revertLast();
        }

        return complete;
    }

    private void revertLast(){
        if (!path.isEmpty()) {
            byte edgeId = path.get(path.size() - 1);

            edgesUsed[edgeId] = (byte) (Math.max(0, edgesUsed[edgeId] - 1));
            price -= edgesPrice[edgeId];
            path.remove(path.size() - 1);

            for(Byte e:graph.edges.get(edgeId)){
                byte x = (byte)((e >> 4) & 15);
                byte y = (byte)(e & 15);
                visited[x][y] = (byte)(visited[x][y] - 1);
            }
        }
    }

    private void logMovement(Byte edgeId){
        for(Byte e:graph.edges.get(edgeId)){
            byte x = (byte)((e >> 4) & 15);
            byte y = (byte)(e & 15);
            visited[x][y] = (byte)(visited[x][y] + 1);
        }
    }

    private void decideOnNode(Node n){

        if (price >= bestPrice) {
            revertLast();
            return;
        }

        if (checkCompletedMap())
            return;

        byte lookingFor = 0;

        for(; lookingFor < MAX_ALLOWED_MOVES_ON_EDGE; lookingFor ++){

            if(n.verUpEdgeId != -1){
                if(edgesUsed[n.verUpEdgeId] == lookingFor) {
                    path.add(n.verUpEdgeId);
                    edgesUsed[n.verUpEdgeId] = (byte) (edgesUsed[n.verUpEdgeId] + 1);
                    logMovement(n.verUpEdgeId);
                    price += edgesPrice[n.verUpEdgeId];
                    decideOnNode(graph.nodes[n.verUpLinkedX][n.verUpLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(n.horRightEdgeId != -1){
                if(edgesUsed[n.horRightEdgeId] == lookingFor) {
                    path.add(n.horRightEdgeId);
                    edgesUsed[n.horRightEdgeId] = (byte) (edgesUsed[n.horRightEdgeId] + 1);
                    logMovement(n.horRightEdgeId);
                    price += edgesPrice[n.horRightEdgeId];
                    decideOnNode(graph.nodes[n.horRightLinkedX][n.horRightLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(n.verDownEdgeId != -1){
                if(edgesUsed[n.verDownEdgeId] == lookingFor) {
                    path.add(n.verDownEdgeId);
                    edgesUsed[n.verDownEdgeId] = (byte) (edgesUsed[n.verDownEdgeId] + 1);
                    logMovement(n.verDownEdgeId);
                    price += edgesPrice[n.verDownEdgeId];
                    decideOnNode(graph.nodes[n.verDownLinkedX][n.verDownLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

            if(n.horLeftEdgeId != -1){
                if(edgesUsed[n.horLeftEdgeId] == lookingFor) {
                    path.add(n.horLeftEdgeId);
                    edgesUsed[n.horLeftEdgeId] = (byte) (edgesUsed[n.horLeftEdgeId] + 1);
                    logMovement(n.horLeftEdgeId);
                    price += edgesPrice[n.horLeftEdgeId];
                    decideOnNode(graph.nodes[n.horLeftLinkedX][n.horLeftLinkedY]);
                }
            }

            if (price >= bestPrice) {
                revertLast();
                return;
            }

        }

        revertLast();
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
