package lego.training.commands;

import lego.training.TrainingMap;
import lego.training.TrainingsMain;
import lego.training.userinterface.Print;
import lego.util.Constants;

import java.io.*;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 18:19
 */
public class SaveMaps implements Command{

    @Override
    public void execute(String[] args) {
        if(args.length == 1){
            try {
                TrainingMap[] maps = TrainingsMain.getMaps();
                if(maps.length != 0) {
                    String fileName = args[0];

                    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));

                    for (TrainingMap map : maps) {
                        bw.write("+---------------------------+\n");
                        for (int y = 0; y < 6; y++) {
                            bw.write("|");
                            for (int x = 0; x < 9; x++) {
                                if (map.getMaze()[x][y].isStart) {
                                    bw.write(Constants.RENDER_START);
                                } else if (map.getMaze()[x][y].isBlock) {
                                    bw.write(Constants.RENDER_BLOCK);
                                } else if (map.getMaze()[x][y].visitedTimes == 0) {
                                    bw.write(Constants.RENDER_PAC_DOT);
                                }
                            }
                            bw.write("|\n");
                        }
                        bw.write("+---------------------------+\n");
                    }
                    bw.flush();
                    bw.close();

                    Print.success("Saved " + maps.length + " map(s) to file.");
                }else{
                    Print.warn("No map(s) loaded in stack. Have a look at 'loadMaps' command or 'generateMaps' command.");
                }

            }catch (IOException e){
                Print.error("Some problem writing to file (IOException).");
            }
        }else{
            Print.error("Incorrect use. "+messageTypos);
        }

    }

    @Override
    public String getName() {
        return "saveMaps";
    }

    @Override
    public String getShortDesc() {
        return "Save map(s) from stack to file.";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "saveMaps save map(s) from stack to file.",
                "How to objectively compare which one of algorithms is best without using same maps?",
                "Once you generated ton of maps, how to export them for later use?",
                "Of course the answer is saveMaps command. This command exports maps from stack to file",
                "so that loadMaps can load them later.",
                "",
                "use: saveMaps <file>",
                "",
                "Where 'file' is file name from where map should be saved. Use either absolute path",
                "or path relative to this software's main .jar file."
        };
    }
}