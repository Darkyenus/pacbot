package lego.simulator.commands;

import lego.simulator.Storage;
import lego.simulator.userinterface.Print;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 18:51
 */
public class ClearBrainz implements Command{

    @Override
    public void execute(String[] args) {
        Storage.clearBrainz();
        Print.success("All brainz from stack cleared.");
        Print.line("");
    }

    @Override
    public String getName() {
        return "clearBrainz";
    }

    @Override
    public String getShortDesc() {
        return "Clears brain(z) from stack.";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "clearBrainz removes brain(z) from stack.",
                "This command might be somewhat useful, when you have prepared",
                "different sets of brainz you want to try. Or you just want",
                "to play with different brain and you don't want the actual one",
                "to bother you.",
                "",
                "use: clearBrainz",
                "",
                messageNoArgs
        };
    }
}
