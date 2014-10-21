package lego.simulator.userinterface;

import lego.robot.brain.Brain;
import lego.simulator.BrainStatistic;
import lego.simulator.simulationmodule.TrainingMap;
import lego.util.DebugRenderConstants;
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
        textAlongSideMap(map, robotPos, null, null);
    }

    public static void textAlongSideMap(TrainingMap map, TupleIntInt robotPos, String label, String[] text){
        if(RenderPermissions.renderTrainingMaps()) {
            if (label != null) {
                String str = label;
                if (str.length() == 0) {
                    str = "+---------------------------+";
                } else if (str.length() <= 28) {
                    str += Util.repeatNTimes("-", 28 - label.length());
                    str += "+";
                } else if (str.length() == 29) {
                    str += Util.repeatNTimes("-", 28 - label.length());
                }
                Print.color(str, DebugRenderConstants.COLOR_MAZE_BLOCK);
            } else {
                Print.color("+---------------------------+", DebugRenderConstants.COLOR_MAZE_BLOCK);
            }

            String margin = "          ";

            if(text != null && text.length != 0){
                Print.color(margin+"Brain thinks that...\n", ConsoleColors.CYAN);
            }else{
                Print.line("");
            }

            int lineIndex = -1;

            for (int y = 0; y < 6; y++) {
                Print.color("|", DebugRenderConstants.COLOR_MAZE_BLOCK);
                for (int x = 0; x < 9; x++) {
                    if (robotPos != null && robotPos.getX() == x && robotPos.getY() == y) {
                        Print.color(DebugRenderConstants.RENDER_ROBOT, DebugRenderConstants.COLOR_MAZE_ROBOT);
                    } else if (map.getMaze()[x][y].isStart) {
                        Print.color(DebugRenderConstants.RENDER_START, DebugRenderConstants.COLOR_MAZE_START);
                    } else if (map.getMaze()[x][y].isBlock) {
                        Print.color(DebugRenderConstants.RENDER_BLOCK, DebugRenderConstants.COLOR_MAZE_BLOCK);
                    } else if (map.getMaze()[x][y].visitedTimes == 0) {
                        Print.color(DebugRenderConstants.RENDER_PAC_DOT, DebugRenderConstants.COLOR_MAZE_PAC_DOT);
                    } else {
                        Print.color(DebugRenderConstants.RENDER_PAC_DOT_EATEN, DebugRenderConstants.COLOR_MAZE_PAC_DOT_EATEN);
                    }
                }
                Print.color("|", DebugRenderConstants.COLOR_MAZE_BLOCK);
                if(text != null && lineIndex != -1 && text.length > lineIndex){
                    Print.line(margin+text[lineIndex]);
                }else{
                    Print.line("");
                }
                lineIndex ++;
            }
            Print.color("+---------------------------+", DebugRenderConstants.COLOR_MAZE_BLOCK);
            if(text != null && text.length > lineIndex){
                Print.line(margin+text[lineIndex]);
            }else{
                Print.line("");
            }
            for(lineIndex++ ;text != null && lineIndex < text.length; lineIndex++){
                Print.line("                             "+margin+text[lineIndex]);
            }
        }
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

            header = header + Util.repeatNTimes("-", longestKey + longestValue + 10 - header.length());

            Print.color(header+"+\n", ConsoleColors.CYAN);
            Print.color("|   "+Util.repeatNTimes(" ", longestKey + longestValue + 3)+"   |", ConsoleColors.CYAN);

            for(int i = 0; i < keys.length; i++){
                if(!keys[i].isEmpty()) {
                    Print.color("\n|   " + keys[i] + ":" + Util.repeatNTimes(" ", longestKey - keys[i].length()) + "  " +
                            values[i] + Util.repeatNTimes(" ", longestValue - values[i].length()) + "   |", ConsoleColors.CYAN);
                }else{
                    Print.color("\n|   "+Util.repeatNTimes(" ", longestKey + longestValue + 3)+"   |", ConsoleColors.CYAN);
                }
            }
            Print.color("\n|   "+Util.repeatNTimes(" ", longestKey + longestValue + 3)+"   |", ConsoleColors.CYAN);
            Print.color("\n+"+Util.repeatNTimes("-", longestKey + longestValue + 9)+"+\n", ConsoleColors.CYAN);

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
                } catch (InterruptedException ignored){

                } catch (IOException ignored) {
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
            String equalises = Util.repeatNTimes("=", (titleLength - header.length() + 1) / 2);

            Print.color(equalises+header+equalises+"\n", ConsoleColors.CYAN);

            for(int i = 0; i < keys.length; i++){
                Print.color("\n    "+keys[i]+":"+Util.repeatNTimes(" ", longestKey - keys[i].length())+"  "+
                        values[i], ConsoleColors.CYAN);
            }

            Print.color("\n\n"+equalises+footer+equalises+"\n\n", ConsoleColors.CYAN);

        }
    }

}
