package lego.simulator.commands;

import lego.robot.api.RobotStrategy;
import lego.simulator.BrainStatistic;
import lego.simulator.Storage;
import lego.simulator.simulationmodule.Brain;
import lego.simulator.simulationmodule.Simulator;
import lego.simulator.simulationmodule.TrainingMap;
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
        boolean allMaps = false;
        boolean allBrainz = false;
        int startMapIndex = 0;
        int startBrainIndex = 0;

        String awaitingArgForFlag = null;
        for(String arg:args){
            if(awaitingArgForFlag == null) {
                switch (arg) {
                    case "-ff":
                    case "--fast-forward":
                        ffmode = true;
                        break;
                    case "-am":
                    case "--all-maps":
                        allMaps = true;
                        break;
                    case "-ab":
                    case "--all-brainz":
                        allBrainz = true;
                        break;
                    case "-fm":
                    case "--from-map":
                        awaitingArgForFlag = "--from-maps";
                        break;
                    case "-fb":
                    case "--from-brain":
                        awaitingArgForFlag = "--from-brainz";
                        break;
                    default:
                        Print.error("Unknown flag (" + arg + "). "+messageTypos);
                }
            }else{
                switch (awaitingArgForFlag){
                    case "--from-maps":
                        try {
                            startMapIndex = Integer.parseInt(arg)-1;
                        }catch (NumberFormatException e){
                            Print.error("Argument after flag '-fm|--from-maps' has to be valid number. "+messageTypos+"\n");
                        }
                        awaitingArgForFlag = null;
                        break;
                    case "--from-brainz":
                        try {
                            startBrainIndex = Integer.parseInt(arg)-1;
                        }catch (NumberFormatException e){
                            Print.error("Argument after flag '-fb|--from-brainz' has to be valid number. "+messageTypos+"\n");
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

        TrainingMap[] maps = Storage.getMaps();


        Brain[] brainz = Storage.getBrainz();

        if(brainz.length > startBrainIndex) {
            do {
                int actualMapIndex = startMapIndex;
                if (maps.length != 0 && maps.length > actualMapIndex) {

                    do {
                        TrainingMap map = maps[actualMapIndex];

                        Simulator sim = new Simulator(map);
                        sim.setFastForwardMode(ffmode);

                        RobotStrategy strategy = brainz[startBrainIndex].getInstance(sim.getSimulatorRobotInterface());

                        sim.getReady(strategy);
                        Render.trainingMap(map, null, "Map#" + (actualMapIndex + 1) + " ");
                        Print.line("");
                        UserInput.waitForEnter(true);
                        Print.line("");
                        strategy.run();

                        if (!ffmode) {
                            BrainStatistic stats = sim.getStatistics(strategy);
                            Render.statistics(stats);
                            if (maps.length > actualMapIndex + 1) {
                                Print.line("");
                                allMaps = UserInput.askQuestion("Continue with next map?");
                                Print.line("");
                            }
                        }

                        map.reset();

                        actualMapIndex++;
                    } while (allMaps && maps.length > actualMapIndex);
                } else {
                    Print.warn("Not enough maps loaded in stack. Have a look at 'loadMaps' command or 'generateMaps' command.\n");
                }

                startBrainIndex ++;

                if(startBrainIndex < brainz.length) {
                    Print.line("");
                    Print.info("One brain has ended. Using other one.");
                    Print.line("");
                }

            } while(allBrainz && startBrainIndex < brainz.length);
        }else{
            Print.warn("Not enough brainz prepared in stack. Have a look at 'prepareBrain' command.\n");
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
                "    -am|--all-maps                 Performs robot training for all stacked maps, starting",
                "                                   at first, unless otherwise specified.",
                "    -fm|--from-map                 Defines starting first index in map stack.",
                "                                   When in --all mode, this is new starting position,",
                "                                   otherwise this is the id of the only one maze trained.",
                "    -ab|--all-brainz               Performs robot training for all prepared brainz, starting",
                "                                   at first, unless otherwise specified.",
                "    -fb|--from-brain               Defines starting first index in brain stack.",
                "                                   When in --all mode, this is new starting position,",
                "                                   otherwise this is the id of the only one brain trained."


        };
    }
}
