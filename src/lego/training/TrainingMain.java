package lego.training;

import lego.robot.api.RobotStrategy;
import lego.robot.brain.testificate.TestificateMain;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by jIRKA on 30.9.2014.
 */
public class TrainingMain {

    public static String RENDER_BLOCK = "[x]";
    public static String RENDER_EMPTY = " • ";
    public static String RENDER_EATEN = " o ";
    public static String RENDER_ROBOT = "(-)";
    public static String RENDER_START = " v ";

    private static boolean exit = false;
    private static ArrayList<TrainingMap> loadedMaps = new ArrayList<TrainingMap>();

    public static void main(String[] args) {

        while(!exit){
            Scanner sc = new Scanner(System.in);
            String line = sc.nextLine();
            if(line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")){
                System.out.println("Quitting...");
                exit = true;
            }else{
                int status = handleCommand(line);
                if(status == 0){ //Success, continue

                }else if(status == 1){ //Some error, still can continue

                }else if(status == 2){ //Some error which we can't fight with.
                    System.out.println("Crashed!");
                    exit = true;
                }

            }
        }


    }

    public static void waitForEnter(boolean displayMessage){
        if(displayMessage){
            System.out.println("[Press ENTER to continue]");
        }
        try {
            System.in.read();
            if (System.in.available() > 0) {
                System.in.read(new byte[System.in.available()]);
            }
        }catch(IOException e){}
    }

    public static boolean askQuestion(String question){
        System.out.println("[Question] "+question+" (yes/no): ");
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        if("yes".equals(line.toLowerCase())){
            return true;
        }else if("no".equalsIgnoreCase(line.toLowerCase())){
            return false;
        }else{
            System.out.println("[Error] Invalid answer, please answer yes or no, nothing else.");
            return askQuestion(question);
        }
    }

    public static void renderMessage(String[] message, boolean instant){
        boolean skipAnimation = instant;

        for (int line = 0; line < message.length; line++){
            int char_ = 0;
            for (; char_ < message[line].length() && !skipAnimation; char_++){
                System.out.print(message[line].charAt(char_));
                System.out.flush();
                try {
                    Thread.sleep(190);
                    if(System.in.available() > 0){
                        System.in.read(new byte[System.in.available()]);
                        skipAnimation = true;
                    }

                } catch (InterruptedException e) {
                } catch (IOException e) {}

            }
            if(skipAnimation){
                System.out.println(message[line].substring(char_));
                System.out.flush();
            }else {
                System.out.println();
                System.out.flush();
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
                                    bw.write(RENDER_START);
                                }else if(map.getMaze()[x][y].isBlock){
                                    bw.write(RENDER_BLOCK);
                                }else if(map.getMaze()[x][y].visitedTimes == 0){
                                    bw.write(RENDER_EMPTY);
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
                    renderMap(loadedMaps.get(i), false);
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
                    renderMap(map, false);
                    System.out.println();
                    waitForEnter(true);
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
                            all = askQuestion("Continue with next map?");
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


    public static void renderMap(TrainingMap map, boolean renderRobot) {
        System.out.println("+---------------------------+");
        for(int y = 0; y < 6; y ++) {
            System.out.print("|");
            for (int x = 0; x < 9; x ++) {
                if(map.getMaze()[x][y].isStart){
                    System.out.print(RENDER_START);
                }else if(renderRobot && map.robotPos.getX() == x && map.robotPos.getY() == y){
                    System.out.print(RENDER_ROBOT);
                }else if(map.getMaze()[x][y].isBlock){
                    System.out.print(RENDER_BLOCK);
                }else if(map.getMaze()[x][y].visitedTimes == 0){
                    System.out.print(RENDER_EMPTY);
                }else{
                    System.out.print(RENDER_EATEN);
                }
            }
            System.out.println("|");
        }
        System.out.println("+---------------------------+");
    }
}
