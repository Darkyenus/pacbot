package lego.bots.node;

import lego.api.controllers.EnvironmentController;

import java.util.ArrayList;

/**
 * Private property.
 * User: jIRKA
 * Date: 18.11.2014
 * Time: 9:46
 */
public class GraphStruct {

    private final byte PRICE_MOVE = 2;
    private final byte PRICE_TURN_AROUND = 1;
    private final byte PRICE_TURN = 3;

    public final ArrayList<Byte[]> edges = new ArrayList<Byte[]>();
    public Node[][] nodes = new Node[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];
    private EnvironmentController.FieldStatus[][] map;

    public void prepareNodes(EnvironmentController.FieldStatus[][] map){
        this.map = map;

        Node n = new Node();
        n.x = EnvironmentController.startX;
        n.y = EnvironmentController.startY;

        nodes[n.x][n.y] = n;

        getNextNodeStructure(n);

    }

    private void getNextNodeStructure(Node lastNode){
        byte workX = lastNode.x;
        byte workY = lastNode.y;

        byte prevX = -1;
        byte prevY = -1;
        byte prevXSto = workX;
        byte prevYSto = workY;

        byte lastDirectionLeaving = 0;
        ArrayList<Byte> lastEdge = new ArrayList<Byte>();

        boolean cont = true;

        while(cont) {
            byte accessibility = getAccessibility(workX, workY);

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

            if (workX == lastNode.x && workY == lastNode.y) {

                lastEdge.clear();
                lastEdge.add((byte) ((workX << 4) | workY));

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

                lastEdge.add((byte) ((workX << 4) | workY));

            } else if (accessibility != 0 && (accessibility & (accessibility - 1)) != 0) { //Kind of magic. More possible ways

                Node n = nodes[workX][workY];
                if (n == null) {
                    n = new Node();
                    n.x = workX;
                    n.y = workY;
                }

                Byte[] lastEdgeArr = lastEdge.toArray(new Byte[lastEdge.size()]);

                byte edgeId = (byte) edges.size();
                byte edgePrice = getEdgePrice(lastEdgeArr);

                if (prevX == workX && prevY == workY - 1 && n.verUpEdgeId == -1) { //Prev UP
                    edges.add(lastEdgeArr);
                    n.verUpEdgeId = edgeId;
                    n.verUpLinkedX = lastNode.x;
                    n.verUpLinkedY = lastNode.y;
                    n.verUpPrice = edgePrice;
                } else if (prevX == workX + 1 && prevY == workY  && n.horRightEdgeId == -1) { //Prev RIGHT
                    edges.add(lastEdgeArr);
                    n.horRightEdgeId = edgeId;
                    n.horRightLinkedX = lastNode.x;
                    n.horRightLinkedY = lastNode.y;
                    n.horRightPrice = edgePrice;
                } else if (prevX == workX && prevY == workY + 1 && n.verDownEdgeId == -1) { //Prev DOWN
                    edges.add(lastEdgeArr);
                    n.verDownEdgeId = edgeId;
                    n.verDownLinkedX = lastNode.x;
                    n.verDownLinkedY = lastNode.y;
                    n.verDownPrice = edgePrice;
                } else if (prevX == workX - 1 && prevY == workY && n.horLeftEdgeId == -1) { //Prev LEFT
                    edges.add(lastEdgeArr);
                    n.horLeftEdgeId = edgeId;
                    n.horLeftLinkedX = lastNode.x;
                    n.horLeftLinkedY = lastNode.y;
                    n.horLeftPrice = edgePrice;
                }

                if(edgeId == edges.size() - 1){
                    if (lastDirectionLeaving == 8) { //Up
                        lastNode.verUpEdgeId = edgeId;
                        lastNode.verUpLinkedX = workX;
                        lastNode.verUpLinkedY = workY;
                        lastNode.verUpPrice = edgePrice;
                    } else if (lastDirectionLeaving == 4) { //Right
                        lastNode.horRightEdgeId = edgeId;
                        lastNode.horRightLinkedX = workX;
                        lastNode.horRightLinkedY = workY;
                        lastNode.horRightPrice = edgePrice;
                    } else if (lastDirectionLeaving == 2) { //Down
                        lastNode.verDownEdgeId = edgeId;
                        lastNode.verDownLinkedX = workX;
                        lastNode.verDownLinkedY = workY;
                        lastNode.verDownPrice = edgePrice;
                    } else if (lastDirectionLeaving == 1) { //Left
                        lastNode.horLeftEdgeId = edgeId;
                        lastNode.horLeftLinkedX = workX;
                        lastNode.horLeftLinkedY = workY;
                        lastNode.horLeftPrice = edgePrice;
                    }
                    nodes[lastNode.x][lastNode.y] = lastNode;
                    nodes[workX][workY] = n;

                    getNextNodeStructure(n);
                }

                workX = lastNode.x;
                workY = lastNode.y;

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
                lastEdge.add((byte) ((workX << 4) | workY));
            } else if(accessibility == 0) { //Dead end

                Node n = nodes[workX][workY];
                if (n == null) {
                    n = new Node();
                    n.x = workX;
                    n.y = workY;
                }

                Byte[] lastEdgeArr = lastEdge.toArray(new Byte[lastEdge.size()]);

                byte edgeId = (byte) edges.size();
                byte edgePrice = getEdgePrice(lastEdgeArr);

                if (prevX == workX && prevY == workY - 1 && n.verUpEdgeId == -1) { //Prev UP
                    edges.add(lastEdgeArr);
                    n.verUpEdgeId = edgeId;
                    n.verUpLinkedX = lastNode.x;
                    n.verUpLinkedY = lastNode.y;
                    n.verUpPrice = edgePrice;
                } else if (prevX == workX + 1 && prevY == workY  && n.horRightEdgeId == -1) { //Prev RIGHT
                    edges.add(lastEdgeArr);
                    n.horRightEdgeId = edgeId;
                    n.horRightLinkedX = lastNode.x;
                    n.horRightLinkedY = lastNode.y;
                    n.horRightPrice = edgePrice;
                } else if (prevX == workX && prevY == workY + 1 && n.verDownEdgeId == -1) { //Prev DOWN
                    edges.add(lastEdgeArr);
                    n.verDownEdgeId = edgeId;
                    n.verDownLinkedX = lastNode.x;
                    n.verDownLinkedY = lastNode.y;
                    n.verDownPrice = edgePrice;
                } else if (prevX == workX - 1 && prevY == workY && n.horLeftEdgeId == -1) { //Prev LEFT
                    edges.add(lastEdgeArr);
                    n.horLeftEdgeId = edgeId;
                    n.horLeftLinkedX = lastNode.x;
                    n.horLeftLinkedY = lastNode.y;
                    n.horLeftPrice = edgePrice;
                }

                if(edgeId == edges.size() - 1){
                    if (lastDirectionLeaving == 8) { //Up
                        lastNode.verUpEdgeId = edgeId;
                        lastNode.verUpLinkedX = workX;
                        lastNode.verUpLinkedY = workY;
                        lastNode.verUpPrice = edgePrice;
                    } else if (lastDirectionLeaving == 4) { //Right
                        lastNode.horRightEdgeId = edgeId;
                        lastNode.horRightLinkedX = workX;
                        lastNode.horRightLinkedY = workY;
                        lastNode.horRightPrice = edgePrice;
                    } else if (lastDirectionLeaving == 2) { //Down
                        lastNode.verDownEdgeId = edgeId;
                        lastNode.verDownLinkedX = workX;
                        lastNode.verDownLinkedY = workY;
                        lastNode.verDownPrice = edgePrice;
                    } else if (lastDirectionLeaving == 1) { //Left
                        lastNode.horLeftEdgeId = edgeId;
                        lastNode.horLeftLinkedX = workX;
                        lastNode.horLeftLinkedY = workY;
                        lastNode.horLeftPrice = edgePrice;
                    }
                    nodes[lastNode.x][lastNode.y] = lastNode;
                    nodes[workX][workY] = n;
                }

                workX = lastNode.x;
                workY = lastNode.y;

            } else { //This should not happen, but who knows
                throw new Error("This should not happen. accessibility = "+Integer.toBinaryString((int)accessibility)+", workX = "+workX +", workY = "+workY);
            }

            prevX = prevXSto;
            prevY = prevYSto;
            prevXSto = workX;
            prevYSto = workY;

        }
    }

    private byte getEdgePrice(Byte[] edge){
        byte price = 0;

        byte x = -1, y = -1;
        byte prevX = -1, prevY = -1;

        byte dir = -1;

        for(Byte b:edge){
            prevX = x;
            prevY = y;
            x = (byte)((b >> 4) & 15);
            y = (byte)(b & 15);

            if(prevY != -1 && (x != prevX || y != prevY)){
                if(prevX == x && prevY - 1 == y){ //Moved UP
                    if(dir == 8){
                        price += PRICE_MOVE;
                    }else if(dir == 2){
                        price += PRICE_TURN_AROUND + PRICE_MOVE;
                    }else if(dir != -1){
                        price += PRICE_TURN + PRICE_MOVE;
                    }else{
                        price += PRICE_MOVE;
                    }
                    dir = 8;
                } else if(prevX + 1 == x && prevY == y){ //Moved RIGHT
                    if(dir == 4){
                        price += PRICE_MOVE;
                    }else if(dir == 1){
                        price += PRICE_TURN_AROUND + PRICE_MOVE;
                    }else if(dir != -1){
                        price += PRICE_TURN + PRICE_MOVE;
                    }else{
                        price += PRICE_MOVE;
                    }
                    dir = 4;
                } else if(prevX == x && prevY + 1 == y){ //Moved DOWN
                    if(dir == 2){
                        price += PRICE_MOVE;
                    }else if(dir == 8){
                        price += PRICE_TURN_AROUND + PRICE_MOVE;
                    }else if(dir != -1){
                        price += PRICE_TURN + PRICE_MOVE;
                    }else{
                        price += PRICE_MOVE;
                    }
                    dir = 2;
                } else if(prevX - 1== x && prevY == y){ //Moved LEFT
                    if(dir == 1){
                        price += PRICE_MOVE;
                    }else if(dir == 4){
                        price += PRICE_TURN_AROUND + PRICE_MOVE;
                    }else if(dir != -1){
                        price += PRICE_TURN + PRICE_MOVE;
                    }else{
                        price += PRICE_MOVE;
                    }
                    dir = 1;
                }
            }
        }

        return price;
    }

    private byte getAccessibility(byte x, byte y){

        byte result = 0;

        if((getField(x, (byte) (y - 1)) == EnvironmentController.FieldStatus.FREE_UNVISITED || getField(x, (byte) (y - 1)) == EnvironmentController.FieldStatus.START)&& getField(x, y) != EnvironmentController.FieldStatus.START){
            result = (byte) (result | 8);
        }
        if(getField((byte) (x + 1), y) == EnvironmentController.FieldStatus.FREE_UNVISITED){
            result = (byte) (result | 4);
        }
        if(getField(x, (byte) (y + 1)) == EnvironmentController.FieldStatus.FREE_UNVISITED && getField(x, (byte)(y + 1)) != EnvironmentController.FieldStatus.START){
            result = (byte) (result | 2);
        }
        if(getField((byte) (x - 1), y) == EnvironmentController.FieldStatus.FREE_UNVISITED){
            result = (byte) (result | 1);
        }

        return result;
    }

    private EnvironmentController.FieldStatus getField(byte x, byte y){
        if(x < 0 || y < 0 || x >= EnvironmentController.mazeWidth || y >= EnvironmentController.mazeHeight){
            return EnvironmentController.FieldStatus.OBSTACLE;
        }else{
            return map[y][x];
        }
    }

}
