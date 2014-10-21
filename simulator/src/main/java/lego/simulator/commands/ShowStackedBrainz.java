package lego.simulator.commands;

import lego.simulator.Storage;
import lego.robot.brain.Brain;
import lego.simulator.userinterface.Print;
import lego.simulator.userinterface.Render;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 18:57
 */
public class ShowStackedBrainz implements Command{

    @Override
    public void execute(String[] args) {
        Brain[] brains = Storage.getBrainz();
        if(brains.length == 0){
            Print.warn("No brain(z) loaded in stack. Have a look at 'prepareBrain' command\n");
        }else{
            int index = 1;
            for (Brain brain : brains) {
                Render.brain(brain, "Brain#"+index);
                index ++;
            }
            Print.line("");
        }
    }

    @Override
    public String getName() {
        return "showStackedBrainz";
    }

    @Override
    public String getShortDesc() {
        return "Renders all brainz from stack";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "showStackedBrainz shows stacked brainz. (One wouldn't expect, would he?)",
                "Ok, now seriously. This command is especially useful when you want to see",
                "your robot's brain configuration. Great in debugging, you can preset some",
                "variables to brains and via this command you can see them.",
                "",
                "use: showStackedBrainz",
                "",
                messageNoArgs
        };
    }
}
