package lego.training.commands;

import lego.training.TrainingsMain;
import lego.training.userinterface.ConsoleColors;
import lego.training.userinterface.Print;
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


        String title = "\nHave no fear, the help is near!\n";
        String headline = Util.repeatNtimes("=",title.length() - 2)+"\n\n";

        Print.color(title, ConsoleColors.BRIGHT_BLUE);
        Print.color(headline, ConsoleColors.BRIGHT_BLUE);

        Print.line("Welcome in help command of "+ TrainingsMain.SOFT_NAME+"\n");
        Print.line("PacmanTrainings is software responsible for training the pacman algorithms.");
        Print.line("The main use is expected for ČVUT robosoutěž 2014, but it can be used freely it other non-commercial occasion.");
        Print.line("");
        Print.line("Basic use of this program is as follows: command and space-separated arguments.");
        Print.line("If you want to pass space as part of argument, use '\\ ' (backslash and space) instead.");
        Print.line("");
        Print.color("Here is list of detected commands:\n\n", ConsoleColors.BRIGHT_BLUE);

        Command[] cmds = CommandManager.getAvailableCommands();

        for (Command cmd : cmds) {
            Print.line("    "+cmd.getName()+Util.repeatNtimes(" ",20-cmd.getName().length())+cmd.getShortDesc());
        }

        Print.color("\nAnd this is the end of help today. You can invoke this message again by typing 'help' command (which you are familiar with)\n\n", ConsoleColors.BRIGHT_BLUE);

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
