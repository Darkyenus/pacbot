package lego.training.commands;

import lego.training.TrainingsMain;
import lego.training.TrainingMap;
import lego.training.userinterface.Print;

import java.io.*;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 18:19
 */
public class LoadMaps implements Command{

    @Override
    public void execute(String[] args) {
        if(args.length == 1){
            try {
                String fileName = args[0];

            BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
            TrainingMap newMap;
            int count = 0;
            int errorID = 1;
            while(br.ready()){
                try {
                    newMap = new TrainingMap(br);
                    TrainingsMain.addMap(newMap);
                    count ++;
                    errorID ++;
                }catch (Error e){
                    Print.error("Problem loading map#"+errorID+": "+e.getMessage());
                    errorID ++;
                }
            }
            br.close();
            Print.success("Loaded " + count + " map(s) from file.");
            }catch (FileNotFoundException e) {
                Print.error("This file doesn't exists. "+messageTypos);
            }catch (IOException e){
                Print.error("Some problem reading file (IOException).");
            }
        }else{
            Print.error("Incorrect use. "+messageTypos);
        }

    }

    @Override
    public String getName() {
        return "loadMaps";
    }

    @Override
    public String getShortDesc() {
        return "Loads map(s) from file to stack.";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "loadMaps loads map(s) from file to stack.",
                "It is very important to have some maps in stack, because every simulated",
                "robot has to have somewhere to drive. Of course, loadMaps isn't the only",
                "way how to put new maps in stack. Have a look at generateMaps command.",
                "",
                "use: loadMaps <file>",
                "",
                "Where 'file' is file name from which map should be loaded. Use either absolute path",
                "or path relative to this software's main .jar file."
        };
    }
}