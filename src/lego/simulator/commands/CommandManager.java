package lego.simulator.commands;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 20:30
 */
public class CommandManager {

    public static Command getInstanceOf(String commandName){
        Command result = null;

        if("man".equalsIgnoreCase(commandName)){
            result = new Man();
        }else if("help".equalsIgnoreCase(commandName)){
            result = new Help();
        }else if("loadMaps".equalsIgnoreCase(commandName)){
            result = new LoadMaps();
        }else if("showStackedMaps".equalsIgnoreCase(commandName)){
            result = new ShowStackedMaps();
        }else if("saveMaps".equalsIgnoreCase(commandName)){
            result = new SaveMaps();
        }else if("clearMaps".equalsIgnoreCase(commandName)){
            result = new ClearMaps();
        }else if("clearBrainz".equalsIgnoreCase(commandName)){
            result = new ClearBrainz();
        }else if("showStackedBrainz".equalsIgnoreCase(commandName)){
            result = new ShowStackedBrainz();
        }else if("prepareBrain".equalsIgnoreCase(commandName)){
            result = new PrepareBrain();
        }else if("set".equalsIgnoreCase(commandName)){
            result = new Set();
        }else if("train".equalsIgnoreCase(commandName)){
            result = new Train();
        }else if("exit".equalsIgnoreCase(commandName)){
            result = new Exit();
        }else if("quit".equalsIgnoreCase(commandName)){
            result = new Quit();
        }


        return result;
    }

    public static Command[] getAvailableCommands(){
        return new Command[]{
                new Help(),
                new Man(),

                new LoadMaps(),
                new ShowStackedMaps(),
                new SaveMaps(),
                new ClearMaps(),

                new ClearBrainz(),
                new ShowStackedBrainz(),
                new PrepareBrain(),

                new Set(),

                new Train(),

                new Quit()
        };
    }

}
