package lego.simulator.commands;

import lego.simulator.Storage;
import lego.simulator.simulationmodule.Brain;
import lego.simulator.userinterface.Print;

import java.util.HashMap;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 18:19
 */
public class PrepareBrain implements Command{

    @Override
    public void execute(String[] args) {
        if(args.length != 0){

            if("-ls".equalsIgnoreCase(args[0])){
                if(Brain.supportedTypes.length != 0) {
                    for (String s : Brain.supportedTypes) {
                        Print.info("Supported brain type: " + s);
                    }
                }else{
                    Print.warn("No brain types detected. That may be unpleasant for you.");
                }
            }else{
                String bName = args[0];

                boolean supported = false;
                for (String s : Brain.supportedTypes) {
                    if(s.equalsIgnoreCase(bName)){
                        supported = true;
                    }
                }

                if(supported) {

                    HashMap<String, String> data = new HashMap<String, String>();

                    String key = "";

                    for (int argIndex = 1; argIndex < args.length; argIndex++) {
                        if (key.isEmpty()) {
                            key = args[argIndex];
                        } else {
                            data.put(key, args[argIndex]);
                            key = "";
                        }
                    }

                    if (key.isEmpty()) { //Complete key-value pairs

                        Brain b = new Brain(bName);
                        b.setData(data);

                        Storage.addBrain(b);

                        Print.success("New brain prepared and added to stack.");

                    } else {
                        Print.error("Incorrect use. " + messageTypos);
                    }
                }else{
                    Print.error("That brain type is not supported. " + messageTypos);
                }
            }
        }else{
            Print.error("Incorrect use. " + messageTypos);
        }
        Print.line("");
    }

    @Override
    public String getName() {
        return "prepareBrain";
    }

    @Override
    public String getShortDesc() {
        return "Prepares brain and adds to stack.";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "prepareBrain prepares brain and puts it into stack.",
                "Brainz are necessary in order to be used. For this reason I've added this",
                "command. It creates new Brain of some Brain type (you type your desired",
                "type) and possibly some arguments, which will be used only in this new",
                "brain.",
                "",
                "use: prepareBrain <type> [<key> <value>]...",
                "",
                "Where 'type' is desired type of brain (your algorithm) and key value",
                "pairs are data passed to brain. You can use as many pairs as you want,",
                "but remember that always have to write both of them (key + value pair).",
                "Fast check: If number of all arguments is odd (first argument + even number)",
                "everything is good.",
                "",
                "Or, you can use it like this: prepareBrain -ls",
                "Which lists all detected, supported and available brain types."

        };
    }
}