package lego.simulator.userinterface;

import lego.simulator.BrainStatistic;
import lego.simulator.simulationmodule.Brain;
import lego.simulator.simulationmodule.TrainingMap;
import lego.util.Constants;
import lego.util.TupleIntInt;
import lego.util.Util;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by jIRKA on 4.10.2014.
 */
public class Render {

    public static void trainingMap(TrainingMap map, TupleIntInt robotPos) {
        trainingMap(map, robotPos, null);
    }

    public static void trainingMap(TrainingMap map, TupleIntInt robotPos, String label){
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
                if(robotPos != null && robotPos.getX() == x && robotPos.getY() == y){
                    Print.color(Constants.RENDER_ROBOT, Constants.COLOR_MAZE_ROBOT);
                }else if(map.getMaze()[x][y].isStart){
                    Print.color(Constants.RENDER_START, Constants.COLOR_MAZE_START);
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

    public static void brain(Brain brain, String label){
        if(RenderPermissions.renderBrainz()) {
            if(label == null)
                label = "";

            HashMap<String, String> data = brain.getData();
            if(data == null){
                data = new HashMap<String, String>();
            }
            if(data.isEmpty()){
                data.put("No data","---");
            }

            int longestKey = 0;
            int longestValue = 0;
            String[] keysTmp = data.keySet().toArray(new String[data.size()]);
            String[] keys = new String[keysTmp.length + 2];
            System.arraycopy(keysTmp, 0, keys, 2, keysTmp.length);
            String[] values = new String[keys.length];

            keys[0] = "Brain type";
            values[0] = brain.getType();

            keys[1] = "";
            values[1] = "";

            for(int i = 0; i < keys.length; i++){
                if(i > 1){
                    values[i] = data.get(keys[i]);
                }
                if(longestKey < keys[i].length())
                    longestKey = keys[i].length();
                if(longestValue < values[i].length())
                    longestValue = values[i].length();
            }


            longestKey += 1;

            String header = "+- "+label+" ";

            header = header + Util.repeatNtimes("-", longestKey+longestValue+10-header.length());

            Print.color(header+"+\n", ConsoleColors.CYAN);
            Print.color("|   "+Util.repeatNtimes(" ", longestKey + longestValue + 3)+"   |", ConsoleColors.CYAN);

            for(int i = 0; i < keys.length; i++){
                if(!keys[i].isEmpty()) {
                    Print.color("\n|   " + keys[i] + ":" + Util.repeatNtimes(" ", longestKey - keys[i].length()) + "  " +
                            values[i] + Util.repeatNtimes(" ", longestValue - values[i].length()) + "   |", ConsoleColors.CYAN);
                }else{
                    Print.color("\n|   "+Util.repeatNtimes(" ", longestKey + longestValue + 3)+"   |", ConsoleColors.CYAN);
                }
            }
            Print.color("\n|   "+Util.repeatNtimes(" ", longestKey + longestValue + 3)+"   |", ConsoleColors.CYAN);
            Print.color("\n+"+Util.repeatNtimes("-", longestKey+longestValue+9)+"+\n", ConsoleColors.CYAN);

        }
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

    public static void statistics(BrainStatistic stats){
        if(RenderPermissions.renderStats()) {

            int longestKey = 0;
            int longestValue = 0;
            String[] keys = stats.keys();
            String[] values = new String[keys.length];

            for(int i = 0; i < keys.length; i++){
                values[i] = stats.getValueOf(keys[i]);
                if(longestKey < keys[i].length())
                    longestKey = keys[i].length();
                if(longestValue < values[i].length())
                    longestValue = values[i].length();
            }

            longestKey += 3;
            longestValue += 3;

            int titleLength = Math.max(40,4 + longestKey + 2 + longestValue);
            String header = "= Robot brain statistics output =";
            String footer = " End of Robot brain stats output ";
            String equalises = Util.repeatNtimes("=",(titleLength-header.length()+1) / 2);

            Print.color(equalises+header+equalises+"\n", ConsoleColors.CYAN);

            for(int i = 0; i < keys.length; i++){
                Print.color("\n    "+keys[i]+":"+Util.repeatNtimes(" ",longestKey-keys[i].length())+"  "+
                        values[i], ConsoleColors.CYAN);
            }

            Print.color("\n\n"+equalises+footer+equalises+"\n\n", ConsoleColors.CYAN);

        }
    }

}
