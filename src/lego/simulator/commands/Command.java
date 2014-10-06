package lego.simulator.commands;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 20:25
 */
public interface Command {

    public static final String messageNoArgs = "There are no arguments, therefore this line cannot explain anything.";
    public static final String messageTypos = "Please check for typos or look at the manual.";
    public static final String messageFlagsTitle = "Options for flag(s) are:";

    public abstract void execute(String[] args);

    public abstract String[] getManPage();

    public abstract String getName();

    public abstract String getShortDesc();

}
