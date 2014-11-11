package lego.bots.clever;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;

import java.util.LinkedList;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 10:23
 */
public class CleverBot extends Bot<EnvironmentController> {

    private boolean continueRunning = true;
    private byte[][] distances;

    @Override
    public synchronized void run() {
        continueRunning = true;
        try {
            this.wait();
        } catch (InterruptedException ignored) {}

        while(continueRunning){

            calcDistances();
            LinkedList<Pos> route = calcRoute();

            while(!route.isEmpty()){
                Pos next = route.getFirst();
                route.removeFirst();

                //TODO: actually move

            }


            controller.move(EnvironmentController.Direction.DOWN);
            //try { Thread.sleep(10000); } catch (InterruptedException e) {}
            controller.move(EnvironmentController.Direction.LEFT);
            //try { Thread.sleep(20000); } catch (InterruptedException e) {}
            controller.move(EnvironmentController.Direction.UP);
            //try { Thread.sleep(30000); } catch (InterruptedException e) {}
            controller.move(EnvironmentController.Direction.RIGHT);
            //try { Thread.sleep(40000); } catch (InterruptedException e) {}
            //controller.moveByX((byte) -1);
            //controller.moveByY((byte)-1);
            //controller.moveByX((byte)1);
            //controller.moveByY((byte)1);
        }


        System.out.println();
        for(byte y = 0; y < EnvironmentController.mazeHeight; y ++){
            for(byte x = 0; x < EnvironmentController.mazeWidth; x ++){
                EnvironmentController.FieldStatus stat = controller.getField(x,y);
                if(stat == EnvironmentController.FieldStatus.OBSTACLE){
                    System.out.print("[X]");
                }else if(stat == EnvironmentController.FieldStatus.START){
                    System.out.print("-S-");
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

    private LinkedList<Pos> calcRoute() {
        Pos target = new Pos(Byte.MIN_VALUE, Byte.MIN_VALUE);
        byte minDist = Byte.MAX_VALUE;

        for(byte x = 0; x < EnvironmentController.mazeWidth; x++){
            for(byte y = 0; y < EnvironmentController.mazeHeight; y++){

                if( x != controller.getX() && y != controller.getY() &&
                        ( controller.getField(x,y) == EnvironmentController.FieldStatus.UNKNOWN || controller.getField(x, y) == EnvironmentController.FieldStatus.FREE_UNVISITED ) &&
                        (( distances[x][y] < minDist) || (distances[x][y] == minDist && cmpDistFromBorder( new Pos(x, y), target )))) {
                    minDist = distances[ x ][ y ];
                    target = new Pos(x, y);
                }
            }
        }

        LinkedList<Pos> route = new LinkedList<Pos>();
        route.addFirst(target);

        Pos ps = target;
        Pos robotPos = new Pos(controller.getX(), controller.getY());
        byte count = 0;
        while( ps != robotPos ) {
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

            if( ps.x < EnvironmentController.mazeWidth && distances[ ps.x + 1 ][ ps.y ] < minDist ) {
                minDist = distances[ ps.x + 1 ][ ps.y ];
                target = new Pos((byte)(ps.x + 1), ps.y);
            }

            if( ps.y < EnvironmentController.mazeHeight && distances[ ps.x ][ ps.y + 1 ] < minDist ) {
                target = new Pos( ps.x, (byte)(ps.y + 1));
            }

            route.addFirst(target);
            ps = target;

            if( count ++ > 100 ) {
                System.out.println("E:PF");
                break;
            }
        }
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
        LinkedList<Pos> toCalc = new LinkedList<Pos>();
        toCalc.addFirst(new Pos(controller.getX(), controller.getY()));

        while( ! toCalc.isEmpty() ) {
            Pos ps = toCalc.getFirst();
            toCalc.removeFirst();

            if(controller.getField(ps.x, ps.y) != EnvironmentController.FieldStatus.OBSTACLE){
                byte psDistActual = distances[ ps.x ][ ps.y ];
                byte psDistNew = (byte) (psDistActual + ( controller.getField(ps.x, ps.y) == EnvironmentController.FieldStatus.FREE_VISITED  ? 3 : 1 ));
                if( ps.x > 0 && controller.getField((byte)(ps.x -1), ps.y) != EnvironmentController.FieldStatus.OBSTACLE && ( distances[ ps.x - 1 ][ ps.y ] > psDistNew ) ) {
                    distances[ps.x - 1][ps.y] = psDistNew;
                    toCalc.addFirst(new Pos((byte) (ps.x - 1), ps.y));
                }
                if( ps.x < EnvironmentController.mazeWidth && controller.getField((byte)(ps.x + 1), ps.y) != EnvironmentController.FieldStatus.OBSTACLE && ( distances[ ps.x + 1 ][ ps.y ] > psDistNew ) ) {
                    distances[ps.x + 1][ps.y] = psDistNew;
                    toCalc.addFirst(new Pos((byte) (ps.x + 1), ps.y));
                }
                if( ps.y > 0 && controller.getField(ps.x , (byte)(ps.y - 1)) != EnvironmentController.FieldStatus.OBSTACLE && controller.getField(ps.x, ps.y) != EnvironmentController.FieldStatus.START && ( distances[ ps.x ][ ps.y - 1 ] > psDistNew ) ) {
                    distances[ps.x][ps.y - 1] = psDistNew;
                    toCalc.addFirst(new Pos(ps.x, (byte)(ps.y - 1)));
                }
                if( ps.y < EnvironmentController.mazeHeight && controller.getField(ps.x , (byte)(ps.y + 1)) != EnvironmentController.FieldStatus.OBSTACLE && controller.getField(ps.x, (byte)(ps.y - 1)) != EnvironmentController.FieldStatus.START && ( distances[ ps.x ][ ps.y + 1 ] > psDistNew ) ) {
                    distances[ps.x][ps.y + 1] = psDistNew;
                    toCalc.addFirst(new Pos(ps.x, (byte)(ps.y + 1)));
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
