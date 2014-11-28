package lego.bots.node;

import lego.api.controllers.EnvironmentController;

import java.util.ArrayList;

/**
 * Private property.
 * User: jIRKA
 * Date: 17.11.2014
 * Time: 21:42
 */
public final class Debug {

    public static void printEdge(byte[] edge){
        System.out.print("<-> ");
        for(Byte b:edge){
            byte wayX = (byte)((b >> 4) & 15);
            byte wayY = (byte)(b & 15);

            System.out.print("(" + wayX + ", " + wayY + ")");

        }
        System.out.println();
        System.out.println("+---------------------------+");
        for(int y = 0; y < EnvironmentController.mazeHeight; y ++){
            System.out.print("|");
            for(int x = 0; x < EnvironmentController.mazeWidth; x ++){
                byte render = 0;
                for(Byte b:edge){
                    byte wayX = (byte)((b >> 4) & 15);
                    byte wayY = (byte)(b & 15);

                    if(wayX == x && wayY == y){
                        render++;
                    }

                }
                if(render == 0){
                    System.out.print(" * ");
                }else{
                    System.out.print("["+render+"]");
                }
            }
            System.out.println("|");
        }
        System.out.println("+---------------------------+");
    }

    public static void printNodes(Node[][] nodes, ArrayList<Byte[]> edges){
        ArrayList<Node> nodeList = new ArrayList<Node>();

        System.out.println("Map:");
        System.out.println();

        for(int y = 0; y < nodes[0].length; y ++){
            for(int x = 0; x < nodes.length; x ++){
                if(nodes[x][y] == null){
                    System.out.print("[--]");
                }else{
                    nodeList.add(nodes[x][y]);
                    String s = Integer.toString(nodeList.size());
                    if(s.length() == 1){
                        s = "0"+s;
                    }
                    System.out.print("["+s+"]");
                }
            }
            System.out.println();
        }

        System.out.println();System.out.println();System.out.println();
        System.out.println("Nodes:");
        System.out.println();

        int nid = 1;

        for(Node n: nodeList.toArray(new Node[nodeList.size()])){

            String s = Integer.toString(nid);
            if(s.length() == 1){
                s = "0"+s;
            }

            System.out.println("--- Node "+s+" ---");

            System.out.println("x: " + n.x + ", y: " + n.y);
            System.out.println();

            System.out.println("horLeftLinkedX: "+n.horLeftLinkedX+", horLeftLinkedY: "+n.horLeftLinkedY);
            System.out.println("horLeftPrice: "+n.horLeftPrice);
            System.out.println("horLeftEdgeId: "+n.horLeftEdgeId);
            System.out.println();
            System.out.println("horRightLinkedX: "+n.horRightLinkedX+", horRightLinkedY: "+n.horRightLinkedY);
            System.out.println("horRightPrice: "+n.horRightPrice);
            System.out.println("horRightEdgeId: "+n.horRightEdgeId);
            System.out.println();
            System.out.println("verUpLinkedX: "+n.verUpLinkedX+", verUpLinkedY: "+n.verUpLinkedY);
            System.out.println("verUpPrice: "+n.verUpPrice);
            System.out.println("verUpEdgeId: "+n.verUpEdgeId);
            System.out.println();
            System.out.println("verDownLinkedX: "+n.verDownLinkedX+", verDownLinkedY: "+n.verDownLinkedY);
            System.out.println("verDownPrice: "+n.verDownPrice);
            System.out.println("verDownEdgeId: "+n.verDownEdgeId);
            System.out.println();


            System.out.println("--- End ---");
            System.out.println();
            nid ++;
        }

        System.out.println();System.out.println();
        System.out.println("Edges:");
        System.out.println();

        int eid = 0;

        for(byte[] way: edges.toArray(new byte[0][0])){

            String s = Integer.toString(eid);
            if(s.length() == 1){
                s = "0"+s;
            }

            System.out.println("--- Edge "+s+" ---");

            printEdge(way);

            System.out.println("--- End ---");
            System.out.println();
            eid ++;
        }

    }

    public static void printRectangle(GraphStruct.Rectangle r, EnvironmentController.FieldStatus[][] map){
        System.out.println("Rectangle (x: "+r.x+", y: "+r.y+", width: "+r.width+", height: "+r.height+")");
        System.out.println("+---------------------------+");
        for(int y = 0; y < 6; y++){
            System.out.print("|");
            for(int x = 0; x < 9; x++){
                if((r.x <= x && r.x + r.width > x) && (r.y - r.height < y && r.y >= y)){
                    System.out.print("###");
                }else{
                    if(map[y][x] == EnvironmentController.FieldStatus.FREE_UNVISITED){
                        System.out.print(" o ");
                    }else if(map[y][x] == EnvironmentController.FieldStatus.START){
                        System.out.print(" v ");
                    }else if(map[y][x] == EnvironmentController.FieldStatus.OBSTACLE){
                        System.out.print("[x]");
                    }
                }
            }
            System.out.println("|");
        }
        System.out.println("+---------------------------+");
    }

}
