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

        switch (commandName.toLowerCase()){
            case "man": result = new Man(); break;
            case "help": result = new Help(); break;

            case "loadmaps": result = new LoadMaps(); break;
            case "showstackedmaps": result = new ShowStackedMaps(); break;
            case "savemaps": result = new SaveMaps(); break;
            case "clearmaps": result = new ClearMaps(); break;

            case "clearbrainz": result = new ClearBrainz(); break;
            case "showstackedbrainz": result = new ShowStackedBrainz(); break;
            case "preparebrain": result = new PrepareBrain(); break;

            case "set": result = new Set(); break;

            case "train": result = new Train(); break;

            case "exit": result = new Exit(); break;
            case "quit": result = new Quit(); break;
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
