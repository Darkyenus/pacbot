package lego.simulator.userinterface;

import lego.robot.brain.Brain;
import lego.simulator.BrainStatistic;
import lego.simulator.simulationmodule.TrainingMap;
import lego.util.DebugRenderConstants;
import lego.util.TupleIntInt;
import lego.util.Util;

import java.util.HashMap;

/**
 * Created by jIRKA on 4.10.2014.
 */
public class Render {

    public static void trainingMap(TrainingMap map, TupleIntInt robotPos, String label){
        textAlongSideMap(map, robotPos, label, null);
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
                Print.text(str);
            } else {
                Print.text("+---------------------------+");
            }

            String margin = "          ";

            if(text != null && text.length != 0){
                Print.text(margin+"Brain thinks that...\n");
            }else{
                Print.line("");
            }

            int lineIndex = -1;

            for (int y = 0; y < 6; y++) {
                Print.text("|");
                for (int x = 0; x < 9; x++) {
                    if (robotPos != null && robotPos.getX() == x && robotPos.getY() == y) {
                        Print.text(DebugRenderConstants.RENDER_ROBOT);
                    } else if (map.getMaze()[x][y].isStart) {
                        Print.text(DebugRenderConstants.RENDER_START);
                    } else if (map.getMaze()[x][y].isBlock) {
                        Print.text(DebugRenderConstants.RENDER_BLOCK);
                    } else if (map.getMaze()[x][y].visitedTimes == 0) {
                        Print.text(DebugRenderConstants.RENDER_PAC_DOT);
                    } else {
                        Print.text(DebugRenderConstants.RENDER_PAC_DOT_EATEN);
                    }
                }
                Print.text("|");
                if(text != null && lineIndex != -1 && text.length > lineIndex){
                    Print.line(margin+text[lineIndex]);
                }else{
                    Print.line("");
                }
                lineIndex ++;
            }
            Print.text("+---------------------------+");
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

            Print.text(header+"+\n");
            Print.text("|   "+Util.repeatNTimes(" ", longestKey + longestValue + 3)+"   |");

            for(int i = 0; i < keys.length; i++){
                if(!keys[i].isEmpty()) {
                    Print.text("\n|   " + keys[i] + ":" + Util.repeatNTimes(" ", longestKey - keys[i].length()) + "  " +
                            values[i] + Util.repeatNTimes(" ", longestValue - values[i].length()) + "   |");
                }else{
                    Print.text("\n|   "+Util.repeatNTimes(" ", longestKey + longestValue + 3)+"   |");
                }
            }
            Print.text("\n|   "+Util.repeatNTimes(" ", longestKey + longestValue + 3)+"   |");
            Print.text("\n+"+Util.repeatNTimes("-", longestKey + longestValue + 9)+"+\n");

        }
    }

    public static void messageBlock(String[] block){
        for(String message:block){
            Print.line(message);
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

            Print.text(equalises+header+equalises+"\n");

            for(int i = 0; i < keys.length; i++){
                Print.text("\n    "+keys[i]+":"+Util.repeatNTimes(" ", longestKey - keys[i].length())+"  "+
                        values[i]);
            }

            Print.text("\n\n"+equalises+footer+equalises+"\n\n");

        }
    }

}
