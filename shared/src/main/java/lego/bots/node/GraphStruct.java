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

    private final ArrayList<Rectangle> activeRectangles = new ArrayList<Rectangle>();


    public void prepareNodes(EnvironmentController.FieldStatus[][] map){
        this.map = map;

        Node n = new Node();
        n.x = EnvironmentController.startX;
        n.y = EnvironmentController.startY;

        nodes[n.x][n.y] = n;

        findRectangles();

        getNextNodeStructure(n);

        optimizeForRectangles();

    }

    private void optimizeForRectangles(){

        boolean keepGlobal[][] = new boolean[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];

        for(Rectangle r:activeRectangles){
            boolean keep[][] = new boolean[r.width][r.height];
            for(byte x = r.x; x < r.x + r.width; x++){
                for(byte y = (byte)(r.y - r.height + 1); y <= r.y; y++){
                    Node n = nodes[x][y];

                    keep[x - r.x][y - (r.y - r.height + 1)] = false;

                    if(n != null) {

                        if (n.verUpEdgeId != -1) {
                            if (r.x > n.verUpLinkedX || r.x + r.width <= n.verUpLinkedX || r.y - r.height >= n.verUpLinkedY || r.y < n.verUpLinkedY) {
                                keep[x - r.x][y - (r.y - r.height + 1)] = true;
                            }
                        }
                        if (n.horRightEdgeId != -1) {
                            if (r.x > n.horRightLinkedX || r.x + r.width <= n.horRightLinkedX || r.y - r.height >= n.horRightLinkedY || r.y < n.horRightLinkedY) {
                                keep[x - r.x][y - (r.y - r.height + 1)] = true;
                            }
                        }
                        if (n.verDownEdgeId != -1) {
                            if (r.x > n.verDownLinkedX || r.x + r.width <= n.verDownLinkedX || r.y - r.height >= n.verDownLinkedY || r.y < n.verDownLinkedY) {
                                keep[x - r.x][y - (r.y - r.height + 1)] = true;
                            }
                        }
                        if (n.horLeftEdgeId != -1) {
                            if (r.x > n.horLeftLinkedX || r.x + r.width <= n.horLeftLinkedX || r.y - r.height >= n.horLeftLinkedY || r.y < n.horLeftLinkedY) {
                                keep[x - r.x][y - (r.y - r.height + 1)] = true;
                            }
                        }
                    }
                }
            }
            keep[0][0] = true; //Enough, it will mirror itself in all remaining corners

            for(byte x = 0; x < r.width; x++){
                keep[x][0] = keep[x][0] || keep[x][r.height - 1];
                keep[x][r.height - 1] = keep[x][0] || keep[x][r.height - 1];
            }
            for(byte y = 0; y < r.height; y++){
                keep[0][y] = keep[0][y] || keep[r.width - 1][y];
                keep[r.width - 1][y] = keep[0][y] || keep[r.width - 1][y];
            }

            for(byte x = 0; x < r.width; x ++){
                for(byte y = 0; y < r.height; y++){
                    keepGlobal[r.x + x][r.y - y] = keepGlobal[r.x + x][r.y - y] || keep[x][y];
                }
            }
        }
        for(Rectangle r:activeRectangles){
            for(byte y = (byte)(r.y - r.height + 1); y <= r.y; y++){
                Node first = nodes[r.x][y];
                byte specEdgeId = -1;
                if(first == null && (y == r.y - r.height + 1 || y == r.y)){
                    first = nodes[r.x + 1][y];
                    specEdgeId = first.horLeftEdgeId;
                    first = nodes[first.horLeftLinkedX][first.horLeftLinkedY];
                }
                Node n;
                for(byte x = r.x; x < r.x + r.width; x++){
                    n = nodes[x][y];
                    if(first == null && keepGlobal[x][y]){
                        first = n;
                    }else if(n != null && keepGlobal[x][y]){
                        if(first.horRightLinkedX != n.x || n.horLeftLinkedX != first.x) {
                            Byte[] edge;
                            if(specEdgeId != -1){
                                edge = NodeBot.mergeEdges(getStraightEdge((byte)(r.x + 1), y, x, y), edges.get(specEdgeId));
                            }else{
                                edge = getStraightEdge(first.x, y, x, y);
                            }
                            edges.add(edge);
                            byte price = getEdgePrice(edge);

                            first.horRightLinkedX = x;
                            first.horLeftLinkedY = y;
                            first.horRightEdgeId = (byte) (edges.size() - 1);
                            first.horRightPrice = price;
                            n.horLeftLinkedX = first.x;
                            n.horLeftLinkedY = first.y;
                            n.horLeftEdgeId = (byte) (edges.size() - 1);
                            n.horLeftPrice = price;
                        }
                        first = n;
                    }
                }
                n = nodes[r.x + r.width - 1][y];
                if(n == null && (y == r.y - r.height + 1 || y == r.y)){
                    n = nodes[r.x + r.width - 2][y];
                    specEdgeId = n.horRightEdgeId;
                    n = nodes[n.horRightLinkedX][n.horRightLinkedY];

                    Byte[] edge = NodeBot.mergeEdges(getStraightEdge((byte)(r.x + 1), y, (byte)(r.x + r.width - 2), y), edges.get(specEdgeId));
                    edges.add(edge);
                    byte price = getEdgePrice(edge);

                    first.horRightLinkedX = (byte)(r.x + r.width - 2);
                    first.horLeftLinkedY = y;
                    first.horRightEdgeId = (byte) (edges.size() - 1);
                    first.horRightPrice = price;
                    n.horLeftLinkedX = first.x;
                    n.horLeftLinkedY = first.y;
                    n.horLeftEdgeId = (byte) (edges.size() - 1);
                    n.horLeftPrice = price;
                }
            }
            for(byte x = r.x; x < r.x + r.width; x++){
                Node first = nodes[x][r.y - r.height + 1];
                byte specEdgeId = -1;
                if(first == null && (x == r.x || x == r.x + r.width)){
                    first = nodes[x][r.y - r.height + 2];
                    specEdgeId = first.horLeftEdgeId;
                    first = nodes[first.horLeftLinkedX][first.horLeftLinkedY];
                }
                Node n;
                for(byte y = (byte)(r.y - r.height + 1); y <= r.y; y++){
                    n = nodes[x][y];
                    if(first == null && keepGlobal[x][y]){
                        first = n;
                    }else if(n != null && keepGlobal[x][y]){
                        if(first.verDownLinkedY != n.y || n.verUpLinkedY != first.y) {
                            Byte[] edge;
                            if(specEdgeId != -1){
                                edge = NodeBot.mergeEdges(getStraightEdge(x, (byte)(r.y - r.height + 2), x, y), edges.get(specEdgeId));
                            }else{
                                edge = getStraightEdge(x, first.y, x, y);
                            }
                            edges.add(edge);
                            byte price = getEdgePrice(edge);

                            first.verDownLinkedX = x;
                            first.verDownEdgeId = (byte) (edges.size() - 1);
                            first.verDownPrice = price;
                            n.verUpLinkedX = first.x;
                            n.verUpEdgeId = (byte) (edges.size() - 1);
                            n.verUpPrice = price;
                        }
                        first = n;
                    }
                }
                n = nodes[x][r.y];
                if(n == null && (x == r.x || x == r.x + r.width)){
                    n = nodes[x][r.y - 1];
                    specEdgeId = n.horRightEdgeId;
                    n = nodes[n.horRightLinkedX][n.horRightLinkedY];

                    Byte[] edge = NodeBot.mergeEdges(getStraightEdge(x, (byte)(r.y - r.height + 2), x, (byte)(r.y - 1)), edges.get(specEdgeId));
                    edges.add(edge);
                    byte price = getEdgePrice(edge);

                    first.horRightLinkedX = x;
                    first.horLeftLinkedY = (byte)(r.y - 1);
                    first.horRightEdgeId = (byte) (edges.size() - 1);
                    first.horRightPrice = price;
                    n.horLeftLinkedX = first.x;
                    n.horLeftLinkedY = first.y;
                    n.horLeftEdgeId = (byte) (edges.size() - 1);
                    n.horLeftPrice = price;
                }
            }
        }
    }

    private Byte[] getStraightEdge(byte fromX, byte fromY, byte toX, byte toY){
        byte tmp = toX; //Well, I will used third var, even when we have learned how to do this without 3rd var
        if(fromX > toX){
            toX = fromX;
            fromX = tmp;
        }
        tmp = toY;
        if(fromY > toY){
            toY = fromY;
            fromY = tmp;
        }

        byte length = (byte)((toX - fromX) + (toY - fromY) + 1);
        Byte[] result = new Byte[length];

        if(fromX == toX){
            for (byte i = 0; i < length; i++) {
                result[i] = (byte)(fromX << 4 | (fromY + i));
            }
        }else if(fromY == toY){
            for (byte i = 0; i < length; i++) {
                result[i] = (byte)((fromX + i) << 4 | fromY);
            }
        }
        return result;
    }

    static class Rectangle{
        public byte x = -1;
        public byte y = -1;
        public byte width = -1;
        public byte height = -1;

        public Rectangle(byte x, byte y, byte width, byte height){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public void findRectangles(){
        for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
            byte startX = -1;
            for(byte x = 0; x < EnvironmentController.mazeWidth; x ++){
                if(map[y][x] == EnvironmentController.FieldStatus.FREE_UNVISITED){
                    if(startX == -1){
                        startX = x;
                    }
                }else{
                    if(startX != -1 && x - startX > 1){
                        for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
                            if(r.y + 1 == y){ //Rec is relevant for us
                                byte end = (byte)(r.x + r.width);
                                byte start = (byte)(Math.max(r.x, startX));
                                byte width = (byte)Math.min(end - start, x - start);
                                byte height = (byte)(r.height + 1);
                                if(width > 1)
                                    activeRectangles.add(new Rectangle(start, y, width, height));
                            }
                        }
                        activeRectangles.add(new Rectangle(startX, y, (byte)(x - startX), (byte)1));
                    }
                    startX = -1;
                }
            }
            if(startX != -1 && EnvironmentController.mazeWidth - startX > 1){
                for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
                    if(r.y + 1 == y){ //Rec is relevant for us
                        byte end = (byte)(r.x + r.width);
                        byte start = (byte)(Math.max(r.x, startX));
                        byte width = (byte)Math.min(end - start, EnvironmentController.mazeWidth - start);
                        byte height = (byte)(r.height + 1);
                        if(width > 1)
                            activeRectangles.add(new Rectangle(start, y, width, height));
                    }
                }
                activeRectangles.add(new Rectangle(startX, y, (byte)(EnvironmentController.mazeWidth - startX), (byte)1));
            }
        }

        for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
            if(r.height == 1){
                activeRectangles.remove(r);
            }
        }

        for(Rectangle r1: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
            for(Rectangle r2: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
                if(r1 != r2) {
                    if (r1.x == r2.x && r1.width == r2.width) {
                        if (r1.y - r1.height == r2.y - r2.height) {
                            if (r1.height < r2.height) {
                                activeRectangles.remove(r1);
                            } else {
                                activeRectangles.remove(r2);
                            }
                        }else if(r1.y == r2.y){
                            if (r1.height < r2.height) {
                                activeRectangles.remove(r1);
                            } else {
                                activeRectangles.remove(r2);
                            }
                        }
                    }
                }
            }
        }
    }

    public Rectangle onRectangle(byte x, byte y){
        for(Rectangle r: activeRectangles.toArray(new Rectangle[activeRectangles.size()])){
            if((r.x <= x && r.x + r.width > x) && (r.y - r.height < y && r.y >= y)){
                return r;
            }
        }
        return null;
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

            } else {
                //This should not happen, but who knows
                //TODO remove exception before race
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
        byte prevX, prevY;

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
