package lego.training.userinterface;

import lego.training.TrainingMap;
import lego.util.Constants;

import java.io.IOException;

/**
 * Created by jIRKA on 4.10.2014.
 */
public class Render {

    public static void trainingMap(TrainingMap map, boolean renderRobot){
        Print.color("+---------------------------+\n", Constants.COLOR_MAZE_BLOCK);
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

        for (String aMessage : message) {
            int char_ = 0;
            for (; char_ < aMessage.length() && !skipAnimation; char_++) {
                Print.color(aMessage.substring(char_, char_ + 1), color);
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
                Print.color(aMessage.substring(char_), color);
            }
            Print.line("");
        }

    }

}
