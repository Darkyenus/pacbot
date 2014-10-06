package lego.simulator.commands;

import lego.simulator.SimulatorMain;
import lego.simulator.TrainingMap;
import lego.simulator.userinterface.Print;
import lego.simulator.userinterface.Render;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 18:57
 */
public class ShowStackedMaps implements Command{

    @Override
    public void execute(String[] args) {
        TrainingMap[] maps = SimulatorMain.getMaps();
        if(maps.length == 0){
            Print.warn("No map(s) loaded in stack. Have a look at 'loadMaps' command or 'generateMaps' command.\n");
        }else{
            int index = 1;
            for (TrainingMap map : maps) {
                Render.trainingMap(map, false, "Map#"+index+" ");
                index ++;
            }
            Print.line("");
        }
    }

    @Override
    public String getName() {
        return "showStackedMaps";
    }

    @Override
    public String getShortDesc() {
        return "Renders all maps from stack";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "showStackedMaps show stacked maps. (One wouldn't expect, would he?)",
                "Ok, now seriously. This command is especially useful when you want to see",
                "where exactly is your robot training. Or you have generated 15 maps and",
                "you want to know if there aren't known issues, and the AI of robot is not ready to them, yet",
                "",
                "use: showStackedMaps",
                "",
                messageNoArgs
        };
    }
}
