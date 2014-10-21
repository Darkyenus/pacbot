package lego.simulator;

import lego.simulator.commands.Command;
import lego.simulator.commands.CommandManager;
import lego.simulator.userinterface.ConsoleColors;
import lego.simulator.userinterface.Print;
import lego.simulator.userinterface.UserInput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Private property.
 * User: jIRKA
 * Date: 30.9.2014
 * Time: 17:42
 */
public class Main {

    public static final String SOFT_NAME = "PacmanTrainings 2000+ - 'The rocket speed of Lite version'";


    private static boolean markQuit = false;

    public static void main(String[] args) {

        Print.color("Welcome to "+SOFT_NAME+"\n\n", ConsoleColors.CYAN);

        File autorun = new File("data/autorun.txt");
        if(autorun.canRead()){
            Print.info("Loading autorun...\n");
            try {
                BufferedReader br = new BufferedReader(new FileReader(autorun));
                String nextLine = br.readLine();
                while(nextLine != null){
                    if(!nextLine.isEmpty())
                        parseCommand(nextLine);
                    nextLine = br.readLine();
                }
                br.close();
                Print.success("Autorun executed\n");
            }catch (IOException e){
                Print.error("Some problem reading file (IOException).");
            }
        }

        while(!markQuit){

            Print.color("$ ", ConsoleColors.BRIGHT_BLUE);
            parseCommand(UserInput.getUserInputScanner().nextLine());

        }

        Print.line("");
        Print.info("Quitting...\n");
    }

    private static void parseCommand(String line){

        if(!line.isEmpty()) {
            if (line.contains(" ")) {
                String[] input = line.split("(?<!\\\\) ");
                for (int i = 0; i < input.length; i++) {
                    input[i] = input[i].replace("\\ ", " ");
                }
                String command = input[0];
                String[] arguments = new String[input.length - 1];
                System.arraycopy(input, 1, arguments, 0, arguments.length);

                Command cmd = CommandManager.getInstanceOf(command);
                if (cmd != null) {
                    cmd.execute(arguments);
                } else {
                    Print.error("Command " + command + " doesn't exists. Please check for typos or try 'help' command.\n");
                }
            } else {
                Command cmd = CommandManager.getInstanceOf(line);
                if (cmd != null) {
                    cmd.execute(new String[0]);
                } else {
                    Print.error("Command " + line + " doesn't exists. Please check for typos or try 'help' command.\n");
                }
            }
        }
    }


    /**
     * Mark application as it should quit. When all issued commands are executed completely applications quits.
     * Please note that this method should be call only, and only if everything is ready for quitting.
     * I.E. no threads are running (except main and daemon ones), all streams are closed and so on.
     */
    public static void quit(){
        markQuit = true;
    }

}
