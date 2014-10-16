package lego.robot.brain.clever;

import lego.simulator.userinterface.ConsoleColors;
import lego.util.Constants;
import lego.util.TupleIntInt;

/**
 * Private property.
 * User: jIRKA
 * Date: 10.10.2014
 * Time: 16:30
 */
class CleverUtil {

    public static void generateDebugInfo(MappedMap map, CleverMain cm){

        String[] debug = new String[8];
        debug[0] = new String(Constants.COLOR_MAZE_BLOCK.getCode())+"+---------------------------+"+new String(ConsoleColors.DEFAULT.getCode());
        debug[7] = new String(Constants.COLOR_MAZE_BLOCK.getCode())+"+---------------------------+"+new String(ConsoleColors.DEFAULT.getCode());

        for(int y = 0; y < 6; y++){
            StringBuilder ln = new StringBuilder();
            ln.append(new String(Constants.COLOR_MAZE_BLOCK.getCode()));
            ln.append("|");
            for(int x = 0; x < 9; x++){
                BlockType type = map.getBlockType(new TupleIntInt(x,y));
                if(cm.getRobotEnvironment().getPos().getX() == x && cm.getRobotEnvironment().getPos().getY() == y){
                    ln.append(new String(Constants.COLOR_MAZE_ROBOT.getCode()));
                    ln.append("(-)");
                }else {
                    switch (type) {
                        case BLOCK: ln.append(new String(Constants.COLOR_MAZE_BLOCK.getCode())); ln.append("[x]"); break;
                        case DEAD_END: ln.append(new String(ConsoleColors.RED.getCode())); ln.append("!!!"); break;
                        case PAC_DOT: ln.append(new String(Constants.COLOR_MAZE_PAC_DOT.getCode())); ln.append(" # "); break;
                        case START: ln.append(new String(Constants.COLOR_MAZE_START.getCode())); ln.append(" v "); break;
                        case UNKNOWN: ln.append(new String(ConsoleColors.BLUE.getCode())); ln.append("???"); break;
                        case VISITED: ln.append("   "); break;
                        case PLANNED:
                            ln.append(new String(ConsoleColors.CYAN.getCode()));
                            boolean hor = false;
                            boolean ver = false;
                            if(map.getBlockType(new TupleIntInt(x-1,y)) == BlockType.PLANNED){
                                ln.append("-");
                                hor = true;
                            }else{
                                ln.append(" ");
                            }
                            if(map.getBlockType(new TupleIntInt(x+1,y)) == BlockType.PLANNED){
                                hor = true;
                            }
                            if(map.getBlockType(new TupleIntInt(x,y-1)) == BlockType.PLANNED){
                                ver = true;
                            }
                            if(map.getBlockType(new TupleIntInt(x,y+1)) == BlockType.PLANNED){
                                ver = true;
                            }

                            if(hor && !ver){
                                ln.append("-");
                            }else if(!hor && ver){
                                ln.append("|");
                            }else{
                                ln.append("+");
                            }

                            if(map.getBlockType(new TupleIntInt(x+1,y)) == BlockType.PLANNED){
                                ln.append("-");
                            }else{
                                ln.append(" ");
                            }

                            break;
                    }
                }
            }
            ln.append(new String(Constants.COLOR_MAZE_BLOCK.getCode()));
            ln.append("|");
            debug[1+y] = ln.toString();
        }

        cm.getRobotInterface().debugRender(debug);
    }

    public static void generateDebugErrorMessage(String message, CleverMain cm){
        cm.getRobotInterface().debugRender(new String[]{"Error:",message});
    }

    public static void logScan(MappedMap map, TupleIntInt frontPos, boolean frontObstacle, TupleIntInt leftPos, boolean leftObstacle, TupleIntInt rightPos, boolean rightObstacle, TupleIntInt actualPos){

        if(map.getBlockType(frontPos) == BlockType.UNKNOWN){
            map.mark(frontPos, frontObstacle? BlockType.BLOCK: BlockType.PAC_DOT);
        }
        if(map.getBlockType(leftPos) == BlockType.UNKNOWN){
            map.mark(leftPos, leftObstacle? BlockType.BLOCK: BlockType.PAC_DOT);
        }
        if(map.getBlockType(rightPos) == BlockType.UNKNOWN){
            map.mark(rightPos, rightObstacle? BlockType.BLOCK: BlockType.PAC_DOT);
        }
        if(map.getBlockType(actualPos) == BlockType.PAC_DOT || map.getBlockType(actualPos) == BlockType.PLANNED){
            map.mark(actualPos, BlockType.VISITED);
        }

    }

}
