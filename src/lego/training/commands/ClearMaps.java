package lego.training.commands;

import lego.training.TrainingsMain;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 18:51
 */
public class ClearMaps implements Command{

    @Override
    public void execute(String[] args) {
        TrainingsMain.clearMaps();
    }

    @Override
    public String getName() {
        return "clearMaps";
    }

    @Override
    public String getShortDesc() {
        return "Clears map(s) from stack.";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "clearMaps removes map(s) from stack.",
                "This command might be somewhat useful, when you have prepared",
                "Different sets of maps you want to try robot on. Remove maps",
                "from stack and load another file. Or if you just want a bigger",
                "testing set and you know that robot has no problem with all currently",
                "loaded maps.",
                "",
                "use: clearMaps",
                "",
                messageNoArgs
        };
    }
}
