package lego.bots.clever;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.util.Queue;
import lego.util.Stack;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 10:23
 */
public class CleverBot extends Bot<EnvironmentController> {

    private static final int STACK_SIZE = 16;

    private boolean continueRunning = true;
    private byte[][] distances;

    @Override
    public synchronized void run() {

        System.out.println("Clever bot has compiled again //started!");
        continueRunning = true;
        try {
            this.wait();
        } catch (InterruptedException ignored) {}

        while(continueRunning){

            calcDistances();

            System.out.println();
            for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
                for(byte x = 0; x < EnvironmentController.mazeWidth; x ++){
                    EnvironmentController.FieldStatus stat = controller.getField(x,y);
                    if(stat == EnvironmentController.FieldStatus.OBSTACLE){
                        System.out.print("[X]");
                    }else if(stat == EnvironmentController.FieldStatus.START){
                        System.out.print(" v ");
                    }else{
                        String val = Byte.toString(distances[x][y]);

                        //You haven't seen this:
                        if(val.length() == 1){
                            val = " "+val+" ";
                        }else if(val.length() == 2){
                            val = "0"+val;
                        }
                        //Ok, the following code can be seen

                        System.out.print(val);
                    }
                }
                System.out.println();
            }


            Stack<Pos> route = calcRoute();

            System.out.println("Route is empty: "+route.isEmpty());

            Queue<EnvironmentController.Direction> directions = new Queue<EnvironmentController.Direction>(STACK_SIZE);
            Queue<Integer> distances = new Queue<Integer>(STACK_SIZE);

            EnvironmentController.Direction actualDir = null;
            int movingDist = 0;

            Pos prev = route.pop();
            System.out.println(prev.x + ", " + prev.y);
            while(!route.isEmpty()){
                Pos next = route.pop();
                System.out.println(next.x + ", " + next.y);

                if(next.x == prev.x && next.y == prev.y + 1){
                    if(actualDir == EnvironmentController.Direction.DOWN){
                        movingDist ++;
                    }else{
                        if(movingDist > 0) {
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
                        }
                        actualDir = EnvironmentController.Direction.DOWN;
                        movingDist = 1;
                    }
                }
                if(next.x == prev.x && next.y == prev.y - 1){
                    if(actualDir == EnvironmentController.Direction.UP){
                        movingDist ++;
                    }else{
                        if(movingDist > 0) {
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
                        }
                        actualDir = EnvironmentController.Direction.UP;
                        movingDist = 1;
                    }
                }
                if(next.x == prev.x - 1 && next.y == prev.y){
                    if(actualDir == EnvironmentController.Direction.LEFT){
                        movingDist ++;
                    }else{
                        if(movingDist > 0) {
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
                        }
                        actualDir = EnvironmentController.Direction.LEFT;
                        movingDist = 1;
                    }
                }
                if(next.x == prev.x + 1 && next.y == prev.y){
                    if(actualDir == EnvironmentController.Direction.RIGHT){
                        movingDist ++;
                    }else{
                        if(movingDist > 0) {
                            directions.pushNext(actualDir);
                            distances.pushNext(movingDist);
                        }
                        actualDir = EnvironmentController.Direction.RIGHT;
                        movingDist = 1;
                    }
                }

                prev = next;
            }
            if(movingDist > 0) {
                directions.pushNext(actualDir);
                distances.pushNext(movingDist);
            }

            System.out.println("Movement generated");

            System.out.println("An it is empty: "+directions.isEmpty());

            while(!directions.isEmpty()){
                actualDir = directions.retreiveFirst();
                movingDist = distances.retreiveFirst();
                if(directions.isEmpty()){
                    System.out.println("Move "+actualDir);
                    controller.move(actualDir);
                }else{
                    System.out.println("Move "+actualDir+" by "+movingDist);
                    if(actualDir == EnvironmentController.Direction.DOWN){
                        controller.moveByY((byte)movingDist);
                    }else if(actualDir == EnvironmentController.Direction.UP){
                        controller.moveByY((byte)-movingDist);
                    }else if(actualDir == EnvironmentController.Direction.LEFT){
                        controller.moveByX((byte)-movingDist);
                    }else if(actualDir == EnvironmentController.Direction.RIGHT){
                        controller.moveByX((byte)movingDist);
                    }
                }
            }

            System.out.println("Move iteration done. Next step incoming.");
            try {
                Thread.sleep(1000);
            }catch (InterruptedException ignored){}
        }



    }

    private static class Pos{
        public byte x,y;
        public Pos(byte x, byte y){
            this.x = x;
            this.y = y;
        }
    }

    private static boolean cmpDistFromBorder( Pos ps, Pos ps2 ) {
        if( Math.min( ps.x, EnvironmentController.mazeWidth - 1 - ps.x ) < Math.min( ps2.x, EnvironmentController.mazeWidth - 1 - ps2.x ) )
            return true;

        if( Math.min( ps.y, EnvironmentController.mazeHeight - 1 - ps.y ) < Math.min( ps2.y, EnvironmentController.mazeHeight - 1 - ps2.y ) )
            return true;

        return false;
    }

    private Stack<Pos> calcRoute() {
        Pos target = new Pos(Byte.MIN_VALUE, Byte.MIN_VALUE);
        byte minDist = Byte.MAX_VALUE;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){

                if( (x != controller.getX() || y != controller.getY()) &&
                        ( controller.getField(x,y) == EnvironmentController.FieldStatus.UNKNOWN || controller.getField(x, y) == EnvironmentController.FieldStatus.FREE_UNVISITED ) &&
                        (( distances[x][y] < minDist) || (distances[x][y] == minDist && cmpDistFromBorder( new Pos(x, y), target )))) {
                    minDist = distances[ x ][ y ];
                    target = new Pos(x, y);
                }
            }
        }

        System.out.println("Robot pos: "+controller.getX()+", "+controller.getY());

        if(target.x == Byte.MIN_VALUE && target.y == Byte.MIN_VALUE)
            System.out.println("Route not found because all blocks have been visited");

        Stack<Pos> route = new Stack<Pos>(STACK_SIZE);
        route.push(target);

        System.out.println("Target: "+target.x+", "+target.y);

        Pos ps = target;
        Pos robotPos = new Pos(controller.getX(), controller.getY());
        byte count = 0;
        while( ps.x != robotPos.x || ps.y != robotPos.y ) {
            minDist = Byte.MAX_VALUE;
            target = ps;

            if( ps.x > 0 && distances[ ps.x - 1 ][ ps.y ] < minDist ) {
                minDist = distances[ ps.x - 1 ][ ps.y ];
                target = new Pos((byte)(ps.x - 1), ps.y );
            }

            if( ps.y > 0 && distances[ ps.x ][ ps.y - 1 ] < minDist ) {
                minDist = distances[ ps.x ][ ps.y - 1 ];
                target = new Pos(ps.x, (byte)(ps.y - 1));
            }

            if( ps.x < EnvironmentController.mazeWidth - 1 && distances[ ps.x + 1 ][ ps.y ] < minDist ) {
                minDist = distances[ ps.x + 1 ][ ps.y ];
                target = new Pos((byte)(ps.x + 1), ps.y);
            }

            if( ps.y < EnvironmentController.mazeHeight - 1 && distances[ ps.x ][ ps.y + 1 ] < minDist ) {
                target = new Pos( ps.x, (byte)(ps.y + 1));
                minDist = distances[ ps.x ][ ps.y + 1];
            }

            System.out.println("Nearest value: "+minDist);

            route.push(target);
            ps = target;

            if( count ++ > 100 ) {
                System.out.println("E:PF");
                break;
            }
        }
        route.push(robotPos);
        return route;
    }

    private void calcDistances() {
        distances = new byte[EnvironmentController.mazeWidth][EnvironmentController.mazeHeight];
        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
           for(byte y = 0; y < EnvironmentController.mazeHeight; y++){
               distances[x][y] = Byte.MAX_VALUE;
           }
        }

        distances[controller.getX()][controller.getY()] = 0;
        Stack<Pos> toCalc = new Stack<Pos>(STACK_SIZE);

        toCalc.push(new Pos(controller.getX(), controller.getY()));

        while( ! toCalc.isEmpty() ) {
            Pos ps = toCalc.pop();

            if(controller.getField(ps.x, ps.y) != EnvironmentController.FieldStatus.OBSTACLE){
                byte psDistActual = distances[ ps.x ][ ps.y ];
                byte psDistNew = (byte) (psDistActual + ( controller.getField(ps.x, ps.y) == EnvironmentController.FieldStatus.FREE_VISITED  ? 3 : 1 ));
                if( ps.x > 0 && controller.getField((byte)(ps.x -1), ps.y) != EnvironmentController.FieldStatus.OBSTACLE && ( distances[ ps.x - 1 ][ ps.y ] > psDistNew ) ) {
                    distances[ps.x - 1][ps.y] = psDistNew;
                    toCalc.push(new Pos((byte) (ps.x - 1), ps.y));
                }
                if( ps.x < EnvironmentController.mazeWidth && controller.getField((byte)(ps.x + 1), ps.y) != EnvironmentController.FieldStatus.OBSTACLE && ( distances[ ps.x + 1 ][ ps.y ] > psDistNew ) ) {
                    distances[ps.x + 1][ps.y] = psDistNew;
                    toCalc.push(new Pos((byte) (ps.x + 1), ps.y));
                }
                if( ps.y > 0 && controller.getField(ps.x , (byte)(ps.y - 1)) != EnvironmentController.FieldStatus.OBSTACLE && controller.getField(ps.x, ps.y) != EnvironmentController.FieldStatus.START && ( distances[ ps.x ][ ps.y - 1 ] > psDistNew ) ) {
                    distances[ps.x][ps.y - 1] = psDistNew;
                    toCalc.push(new Pos(ps.x, (byte)(ps.y - 1)));
                }
                if( ps.y < EnvironmentController.mazeHeight && controller.getField(ps.x , (byte)(ps.y + 1)) != EnvironmentController.FieldStatus.OBSTACLE && controller.getField(ps.x, (byte)(ps.y - 1)) != EnvironmentController.FieldStatus.START && ( distances[ ps.x ][ ps.y + 1 ] > psDistNew ) ) {
                    distances[ps.x][ps.y + 1] = psDistNew;
                    toCalc.push(new Pos(ps.x, (byte)(ps.y + 1)));
                }
            }
        }
    }


    @Override
    public void onEvent(BotEvent event, Object param) {
        switch (event){
            case ESCAPE_PRESSED:
                continueRunning = false;
            case ENTER_PRESSED:
            case LEFT_PRESSED:
            case RIGHT_PRESSED:
                synchronized (this){
                    notifyAll(); //Should wake up the main thread.
                }
        }
    }
}
