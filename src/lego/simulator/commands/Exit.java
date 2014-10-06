package lego.simulator.commands;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 16:32
 */
public class Exit implements Command {

    @Override
    public void execute(String[] args) {
        new Quit().execute(args);
    }



    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getShortDesc() {
        return "Quits the application";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "This is alias for 'quit' command.",
                "See quit for more details.",
                "",
                "use: exit <arguments for quit>",
                "",
                "use is not explained here, therefore this line cannot explain anything.",
                "See manual of quit command for more details."
        };
    }
}
