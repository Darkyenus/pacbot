package lego.simulator.commands;

import lego.simulator.Main;
import lego.simulator.userinterface.ConsoleColors;
import lego.simulator.userinterface.Print;
import lego.util.Util;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 20:31
 */
public class Help implements Command {

    @Override
    public void execute(String[] args) {


        String title = "\n     Have no fear, the help is near!     \n";
        String headline = Util.repeatNTimes("=", title.length() - 2)+"\n\n";

        Print.color(title, ConsoleColors.CYAN);
        Print.color(headline, ConsoleColors.CYAN);

        Print.line("Welcome in help command of "+ Main.SOFT_NAME+"\n");
        Print.line("PacmanTrainings is software responsible for training the pacman algorithms.");
        Print.line("The main use is expected for ČVUT robosoutěž 2014, but it can be used freely it other non-commercial occasion.");
        Print.line("");
        Print.line("The architecture and some principles are written here, so be careful when reading.");
        Print.line("You have two separate thing. Training maps and Braiz. Let me start explaining maps.");
        Print.line("Maps are stored in so called 'map stack' which is actually list of all available,");
        Print.line("maps. You can extend or modify this list with commands below (see later).");
        Print.line("Every training, every attempt to successfully complete competition rules and");
        Print.line("have best score is called training (as well as footballers are training to be");
        Print.line("good, also algorithm (especially neural networks) have to train to be the best).");
        Print.line("Training consist in fast-forwarded simulation on all loaded maps with all");
        Print.line("available Brainz. Brainz are stored in stack (as well as maps, but in different one).");
        Print.line("You can 'prepare' new Brain. It is basically creating new brain (one of preprogrammed");
        Print.line("packages) and (only if you want) you can give your brain some arguments.");
        Print.line("");
        Print.line("Use commands to control this program. Input line is split by spaces and first");
        Print.line("word is treated as command and the rest as bunch of arguments (space separated)");
        Print.line("If you want to pass space as part of argument, use '\\ ' (backslash and space) instead.");
        Print.line("");
        Print.color("Here is list of detected commands:\n\n", ConsoleColors.CYAN);

        Command[] cmds = CommandManager.getAvailableCommands();

        for (Command cmd : cmds) {
            Print.line("    "+cmd.getName()+Util.repeatNTimes(" ", 20 - cmd.getName().length())+cmd.getShortDesc());
        }

        Print.color("\nAnd this is the end of help today. \nYou can invoke this message again by typing 'help' command (which you are familiar with)\n\n", ConsoleColors.BRIGHT_BLUE);

    }



    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getShortDesc() {
        return "Helps you.";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "help show useful and helpful information about this software,",
                "such as name of software, use licence and command list.",
                "Use help in combination with man in order to be informed about every possibility here.",
                "And remember, help is here anytime. If you don't have idea what or how to do something,",
                "type 'help' see the command you think you would need and check for detailed information",
                "about that command in manual page (see 'man man' for more info how to use manual).",
                "",
                "use: help",
                "",
                messageNoArgs
        };
    }
}
