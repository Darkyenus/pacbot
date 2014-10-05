package lego.training;

import lego.robot.api.RobotStrategy;
import lego.robot.brain.testificate.TestificateMain;
import lego.training.userinterface.ConsoleColors;
import lego.training.userinterface.Print;
import lego.training.userinterface.Render;
import lego.training.userinterface.UserInput;
import lego.util.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by jIRKA on 30.9.2014.
 */
public class TrainingMain {

    private static boolean exit = false;
    private static ArrayList<TrainingMap> loadedMaps = new ArrayList<TrainingMap>();

    public static void main(String[] args) {

        Print.color("Welcome!\n", ConsoleColors.CYAN);

        while(!exit){
            Scanner sc = new Scanner(System.in);
            String line = sc.nextLine();
            if(line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")){
                Print.info("Quitting...");
                exit = true;
            }else{
                int status = handleCommand(line);
                if(status == 0){ //Success, continue

                }else if(status == 1){ //Some error, still can continue

                }else if(status == 2){ //Some error which we can't fight with.
                    Print.error("App has crashed!");
                    Print.info("Quitting...");
                    exit = true;
                }

            }
        }


    }

    public static int handleCommand(String cmd){

        if(cmd == null)
            return 2;

        if("help".equals(cmd) || "?".equals(cmd)){
            System.out.println("There is no help implemented (yet). Type 'help' for help (Even if it seems tiny little bit recursive, try it, who knows...).");
        }else if (cmd.startsWith("loadMaps ")){
            try {
                String fileName = cmd.substring(9);
                BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
                TrainingMap newMap;

                int count = 0;

                while(br.ready()){
                    try {
                        newMap = new TrainingMap(br);
                        loadedMaps.add(newMap);
                        count ++;
                    }catch (Error e){

                    }
                }
                br.close();
                System.out.println("Successfully loaded "+count+" map(s) from file.");

            }catch (FileNotFoundException e){
                System.out.println("[Error] This file doesn't exists. Please check your first argument.");
            }catch (StringIndexOutOfBoundsException e){
                System.out.println("[Error] You should use this syntax: 'loadMap <filename>'");
            }catch (IOException e){
                System.out.println("[Error] Some problem reading file (IOException).");
            }
        }else if (cmd.startsWith("saveMaps ")){
            try {
                if(!loadedMaps.isEmpty()) {
                    String fileName = cmd.substring(9);

                    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));

                    for(int i = 0; i < loadedMaps.size(); i++){
                        TrainingMap map = loadedMaps.get(i);
                        bw.write("+---------------------------+\n");
                        for(int y = 0; y < 6; y ++) {
                            bw.write("|");
                            for (int x = 0; x < 9; x ++) {
                                if(map.getMaze()[x][y].isStart){
                                    bw.write(Constants.RENDER_START);
                                }else if(map.getMaze()[x][y].isBlock){
                                    bw.write(Constants.RENDER_BLOCK);
                                }else if(map.getMaze()[x][y].visitedTimes == 0){
                                    bw.write(Constants.RENDER_PAC_DOT);
                                }
                            }
                            bw.write("|\n");
                        }
                        bw.write("+---------------------------+\n");
                    }
                    bw.flush();
                    bw.close();

                    System.out.println("Successfully saved " + loadedMaps.size() + " map(s) to file.");
                }else{
                    System.out.println("[Error] No map(s) loaded in stack. Have a look at 'loadMaps' command or 'generateMaps' command.");
                }

            }catch (StringIndexOutOfBoundsException e){
                System.out.println("[Error] You should use this syntax: 'loadMap <filename>'");
            }catch (IOException e){
                System.out.println("[Error] Some problem reading file (IOException).");
            }
        }else if ("clearMaps".equals(cmd)){
            loadedMaps.clear();
            System.out.println("All map(s) cleared!");
        }else if ("showLoadedMaps".equals(cmd)){
            if(loadedMaps.isEmpty()){
                System.out.println("[Error] No map(s) loaded in stack. Have a look at 'loadMaps' command or 'generateMaps' command.");
            }else{
                for(int i = 0; i < loadedMaps.size(); i++){
                    Render.trainingMap(loadedMaps.get(i), false);
                }
            }
        }else if(cmd.startsWith("train")){
            boolean ffmode = false;
            boolean all = false;
            int startIndex = 0;
            if(cmd.contains(" -ff ") || cmd.contains(" --fastForward ")){
                ffmode = true;
            }
            if(cmd.contains(" -a ") || cmd.contains(" --all ")){
                all = true;
            }
            if(cmd.contains(" -f ") || cmd.contains(" --from ")){
                if(cmd.contains(" --from ")){
                    String number = cmd.substring(cmd.indexOf(" --from ")+8);
                    number = number.substring(0, number.indexOf(" "));
                    startIndex = Integer.parseInt(number);
                }else if(cmd.contains(" -f ")) {
                    String number = cmd.substring(cmd.indexOf(" -f ") + 4);
                    number = number.substring(0, number.indexOf(" "));
                    startIndex = Integer.parseInt(number);
                }
            }

            if(!loadedMaps.isEmpty() && loadedMaps.size() > startIndex) {
                do {
                    TrainingMap map = loadedMaps.get(startIndex);
                    SimulatorRobotInterface sim = new SimulatorRobotInterface(map);
                    RobotStrategy strategy = new TestificateMain(sim);
                    sim.setFastForwardMode(ffmode);
                    sim.getReady(strategy);
                    Render.trainingMap(map, false);
                    System.out.println();
                    UserInput.waitForEnter(true);
                    System.out.println();
                    strategy.run();
                    if (!ffmode) {
                        TrainingStatistics stats = map.getStatistics();
                        System.out.println("Robot algorithm has ended! Here are some stats:");
                        System.out.println();
                        System.out.println("Passed:                 " + (stats.hasPassed() ? "Yes" : "No"));
                        System.out.println("Efficiency:             " + stats.getEfficiency() + "%");
                        System.out.println("Total movements:        " + stats.getTotalMovements());
                        System.out.println("Total turns:            " + stats.getTotalTurns());
                        System.out.println("Total obstacle hinders: " + stats.getTotalObstacleHinders());
                        System.out.println();
                        System.out.println();
                        if(loadedMaps.size() > startIndex + 1) {
                            System.out.println();
                            all = UserInput.askQuestion("Continue with next map?");
                            System.out.println();
                        }
                    }

                    map.reset();

                    startIndex ++;
                } while (all && loadedMaps.size() > startIndex);
            }else{
                System.out.println("[Error] No map(s) loaded in stack. Have a look at 'loadMaps' command or 'generateMaps' command.");
            }

        }else {
            System.out.println("[Error] Unknown command. Type 'help' for help.");
            return 1;
        }


        return 0;
    }

}
