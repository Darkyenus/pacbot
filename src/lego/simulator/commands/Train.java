package lego.simulator.commands;

import lego.robot.api.RobotStrategy;
import lego.robot.brain.testificate.TestificateMain;
import lego.simulator.SimulatorMain;
import lego.simulator.SimulatorRobotInterface;
import lego.simulator.TrainingMap;
import lego.simulator.TrainingStatistics;
import lego.simulator.userinterface.Print;
import lego.simulator.userinterface.Render;
import lego.simulator.userinterface.UserInput;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 19:25
 */
public class Train implements Command{

    @Override
    public void execute(String[] args) {
        boolean ffmode = false;
        boolean all = false;
        int startIndex = 0;

        String awaitingArgForFlag = null;
        for(String arg:args){
            if(awaitingArgForFlag == null) {
                switch (arg) {
                    case "-ff":
                    case "--fast-forward":
                        ffmode = true;
                        break;
                    case "-a":
                    case "--all":
                        all = true;
                    case "-f":
                    case "--from":
                        awaitingArgForFlag = "--from";
                        break;
                    default:
                        Print.error("Unknown flag (" + arg + "). "+messageTypos);
                }
            }else{
                switch (awaitingArgForFlag){
                    case "--from":
                        try {
                            startIndex = Integer.parseInt(arg)-1;
                        }catch (NumberFormatException e){
                            Print.error("Argument after flag '-f|--from' has to be valid number. "+messageTypos);
                        }
                        awaitingArgForFlag = null;
                        break;
                }
            }
        }
        if(awaitingArgForFlag != null){
            Print.error("Some argument was expecting and got nothing. "+messageTypos+"\n");
            return;
        }

        TrainingMap[] maps = SimulatorMain.getMaps();

        if(maps.length != 0 && maps.length > startIndex) {
            do {
                TrainingMap map = maps[startIndex];
                SimulatorRobotInterface sim = new SimulatorRobotInterface(map);
                RobotStrategy strategy = new TestificateMain(sim);
                sim.setFastForwardMode(ffmode);
                sim.getReady(strategy);
                Render.trainingMap(map, false, "Map#"+(startIndex+1)+" ");
                Print.line("");
                UserInput.waitForEnter(true);
                Print.line("");
                strategy.run();
                if (!ffmode) {
                    TrainingStatistics stats = map.getStatistics(strategy);
                    Render.statistics(stats);
                    if(maps.length > startIndex + 1) {
                        System.out.println();
                        all = UserInput.askQuestion("Continue with next map?");
                        System.out.println();
                    }
                }

                map.reset();

                startIndex ++;
            } while (all && maps.length > startIndex);
        }else{
            Print.warn("No map(s) loaded in stack. Have a look at 'loadMaps' command or 'generateMaps' command.");
        }
    }

    @Override
    public String getName() {
        return "train";
    }

    @Override
    public String getShortDesc() {
        return "trains robot(s) on map(s)";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "Please note that this command is not completely done yet",
                "and may change a lot. As well as its arguments and documentation.",
                "Use with care!",
                "",
                "Train command is responsible for actual robot training. You specify",
                "a brain to use (TODO) and map set. By default is used first map in stack.",
                "",
                "use: train [flags] ...",
                "",
                messageFlagsTitle,
                "",
                "    -ff|--fast-forward             Enforces fast forward mode. It means that",
                "                                   that the simulation will be as fast as possible",
                "                                   without any text output. As opposite for this",
                "                                   is (default) mode which breaks after every movement",
                "                                   done by RobotStrategy and renders map with some",
                "                                   additional information.",
                "    -a|--all                       Performs robot training for all stacked maps, starting",
                "                                   at first, unless otherwise specified.",
                "    -f|--from                      Defines starting first index in map stack.",
                "                                   When in --all mode, this is new starting position,",
                "                                   otherwise this is the id of the only one maze trained."


        };
    }
}
