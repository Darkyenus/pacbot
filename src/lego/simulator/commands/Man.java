package lego.simulator.commands;

import lego.simulator.userinterface.ConsoleColors;
import lego.simulator.userinterface.Print;
import lego.util.Util;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 20:31
 */
public class Man implements Command {

    @Override
    public void execute(String[] args) {
        if(args.length > 0){
            Command cmd = CommandManager.getInstanceOf(args[0]);

            if(cmd != null){
                String title = "            Manual for command: "+cmd.getName()+"           \n";
                String headline = Util.repeatNtimes("=",title.length())+"\n";

                Print.color(headline, ConsoleColors.CYAN);
                Print.color(title, ConsoleColors.CYAN);
                Print.color(headline, ConsoleColors.CYAN);
                Print.line("");

                String[] manPage = cmd.getManPage();

                for (String line : manPage) {
                    Print.line("    "+line);
                }

                Print.line("\n\n");
                Print.color("================ End of manual ================\n\n", ConsoleColors.CYAN);

            }else{
                Print.error("Command "+ args[0] + " doesn't exists. Please check for typos or try 'help' command.");
            }

        }else{
            Print.error("Wrong use. "+messageTypos+" (Type 'man man' [ENTER])");
        }
    }



    @Override
    public String getName() {
        return "man";
    }

    @Override
    public String getShortDesc() {
        return "Shows manual about other commands.";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "mans show manual about other commands.",
                "Use man in combination with help in order to be informed about every possibility here.",
                "And remember, man is here anytime. If you don't have idea what arguments",
                "you should give to some command, just see the man page of for that command.",
                "",
                "use: man <command>",
                "",
                "Where 'command' is name of command you would like to know about.",
        };
    }
}
