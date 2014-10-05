package lego.training;

import lego.training.commands.Command;
import lego.training.commands.CommandManager;
import lego.training.userinterface.ConsoleColors;
import lego.training.userinterface.Print;
import lego.training.userinterface.UserInput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Private property.
 * User: jIRKA
 * Date: 30.9.2014
 * Time: 17:42
 */
public class TrainingsMain {

    public static final String SOFT_NAME = "PacmanTrainings 2000+ - 'The rocket speed of Lite version'";
    private static ArrayList<TrainingMap> loadedMaps = new ArrayList<>();
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
                    parseCommand(nextLine);
                    nextLine = br.readLine();
                }

            }catch (IOException e){
                Print.error("Some problem reading file (IOException).");
            }
        }

        while(!markQuit){

            parseCommand(UserInput.getUserInputScanner().nextLine());

        }

        Print.line("");
        Print.info("Quitting...");
    }

    private static void parseCommand(String line){
        if (line.contains(" ")) {
            String[] input = line.split("(?<!\\\\) ");
            for(int i = 0; i < input.length; i++){
                input[i] = input[i].replace("\\ "," ");
            }
            String command = input[0];
            String[] arguments = new String[input.length - 1];
            System.arraycopy(input, 1, arguments, 0, arguments.length);

            Command cmd = CommandManager.getInstanceOf(command);
            if (cmd != null) {
                cmd.execute(arguments);
            } else {
                Print.error("Command " + command + " doesn't exists. Please check for typos or try 'help' command.");
            }
        } else {
            Command cmd = CommandManager.getInstanceOf(line);
            if (cmd != null) {
                cmd.execute(new String[0]);
            } else {
                Print.error("Command " + line + " doesn't exists. Please check for typos or try 'help' command.");
            }
        }
    }


    public static void addMap(TrainingMap map){
        loadedMaps.add(map);
    }

    public static void clearMaps(){
        loadedMaps.clear();
    }

    public static TrainingMap[] getMaps(){
        return loadedMaps.toArray(new TrainingMap[loadedMaps.size()]);
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
