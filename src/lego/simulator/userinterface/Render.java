package lego.simulator.userinterface;

import lego.simulator.TrainingMap;
import lego.simulator.TrainingStatistics;
import lego.util.Constants;
import lego.util.Util;

import java.io.IOException;

/**
 * Created by jIRKA on 4.10.2014.
 */
public class Render {

    public static void trainingMap(TrainingMap map, boolean renderRobot) {
        trainingMap(map, renderRobot, null);
    }

    public static void trainingMap(TrainingMap map, boolean renderRobot, String label){
        if(!RenderPermissions.renderTrainingMaps())
            return;
        if(label != null){
            String str = label;
            if(str.length() == 0){
                str = "+---------------------------+";
            }else if(str.length() <= 28){
                str += Util.repeatNtimes("-", 28 - label.length());
                str += "+";
            }else if(str.length() == 29){
                str += Util.repeatNtimes("-", 28 - label.length());
            }
            str += "\n";
            Print.color(str, Constants.COLOR_MAZE_BLOCK);
        }else {
            Print.color("+---------------------------+\n", Constants.COLOR_MAZE_BLOCK);
        }
        for(int y = 0; y < 6; y ++) {
            Print.color("|", Constants.COLOR_MAZE_BLOCK);
            for (int x = 0; x < 9; x ++) {
                if(map.getMaze()[x][y].isStart){
                    Print.color(Constants.RENDER_START, Constants.COLOR_MAZE_START);
                }else if(renderRobot && map.getRobotPos() != null && map.getRobotPos().getX() == x && map.getRobotPos().getY() == y){
                    Print.color(Constants.RENDER_ROBOT, Constants.COLOR_MAZE_ROBOT);
                }else if(map.getMaze()[x][y].isBlock){
                    Print.color(Constants.RENDER_BLOCK, Constants.COLOR_MAZE_BLOCK);
                }else if(map.getMaze()[x][y].visitedTimes == 0){
                    Print.color(Constants.RENDER_PAC_DOT, Constants.COLOR_MAZE_PAC_DOT);
                }else{
                    Print.color(Constants.RENDER_PAC_DOT_EATEN, Constants.COLOR_MAZE_PAC_DOT_EATEN);
                }
            }
            Print.color("|\n", Constants.COLOR_MAZE_BLOCK);
        }
        Print.color("+---------------------------+\n", Constants.COLOR_MAZE_BLOCK);
    }

    public static void messageBlock(String[] message, boolean instant, ConsoleColors color){

        if(color == null)
            color = ConsoleColors.DEFAULT;

        boolean skipAnimation = instant;

        for (String msg : message) {
            int char_ = 0;
            for (; char_ < msg.length() && !skipAnimation; char_++) {
                Print.color(msg.substring(char_, char_ + 1), color);
                try {
                    Thread.sleep(190);
                    if (System.in.available() > 0) {
                        System.in.read(new byte[System.in.available()]);
                        skipAnimation = true;
                    }
                } catch (InterruptedException | IOException ignored) {
                }
            }
            if (skipAnimation) {
                Print.color(msg.substring(char_), color);
            }
            Print.line("");
        }

    }

    public static void statistics(TrainingStatistics stats){
        if(RenderPermissions.renderStats()) {

            Print.color("======== Robot strategy statistics output ========\n", ConsoleColors.CYAN);
            Print.color("\n    Strategy descriptor:    " + stats.getStrategyDescriptor(), ConsoleColors.CYAN);
            Print.color("\n    Passed:                 " + (stats.hasPassed() ? "Yes" : "No"), ConsoleColors.CYAN);
            Print.color("\n    Efficiency:             " + stats.getEfficiency() + "%", ConsoleColors.CYAN);
            Print.color("\n    Total movements:        " + stats.getTotalMovements(), ConsoleColors.CYAN);
            Print.color("\n    Total turns:            " + stats.getTotalTurns(), ConsoleColors.CYAN);
            Print.color("\n    Total obstacle hinders: " + stats.getTotalObstacleHinders(), ConsoleColors.CYAN);
            Print.color("\n\n======= End of Robot strategy stats output =======\n\n", ConsoleColors.CYAN);

        }
    }

}
