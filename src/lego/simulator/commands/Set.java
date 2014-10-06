package lego.simulator.commands;

import lego.simulator.userinterface.Print;
import lego.simulator.userinterface.RenderPermissions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 20:31
 */
public class Set implements Command {

    @Override
    public void execute(String[] args) {
        if(args.length >= 2) {
            boolean silent = false;
            for(int i = 2; i < args.length; i++){
                if("-s".equals(args[i].toLowerCase()) || "--silent".equals(args[i].toLowerCase())){
                    silent = true;
                }
            }

            switch (args[0].toLowerCase()) {
                case "enablecolors":
                    Print.setColorsEnabled("true".equals(args[1].toLowerCase()) || "1".equals(args[1]));
                    if(!silent) {
                        if (Print.isColorsEnabled()) {
                            Print.success("Colors has been enabled in terminal" + "\n");
                        } else {
                            Print.success("Colors has been disabled in terminal" + "\n");
                        }
                    }
                    break;
                case "hideoutputs":
                    switch (args[1].toLowerCase()) {
                        case "info":
                            RenderPermissions.setRenderInfos(false);
                            if(!silent) {
                                Print.success("Info channel has been hidden");
                            }
                            break;
                        case "success":
                            RenderPermissions.setRenderSuccesses(false);
                            if(!silent) {
                                Print.success("Success channel has been hidden");
                            }
                            break;
                        case "error":
                            RenderPermissions.setRenderErrors(false);
                            if(!silent) {
                                Print.success("Error channel has been hidden");
                            }
                            break;
                        case "warn":
                            RenderPermissions.setRenderWarns(false);
                            if(!silent) {
                                Print.success("Warning channel has been hidden");
                            }
                            break;
                        case "maze":
                            RenderPermissions.setRenderTrainingMaps(false);
                            if(!silent) {
                                Print.success("Training map channel has been hidden");
                            }
                            break;
                        case "stats":
                            RenderPermissions.setRenderStats(false);
                            if(!silent) {
                                Print.success("Statistics channel has been hidden");
                            }
                            break;
                        case "all":
                            RenderPermissions.setRenderForAll(false);
                            if(!silent) {
                                Print.success("All channels has been hidden");
                            }
                            break;
                        default:
                            Print.error("No such argument. " + messageTypos + "\n");
                    }
                    break;
                case "showoutputs":
                    switch (args[1].toLowerCase()) {
                        case "info":
                            RenderPermissions.setRenderInfos(true);
                            if(!silent) {
                                Print.success("Info channel has been shown");
                            }
                            break;
                        case "success":
                            RenderPermissions.setRenderSuccesses(true);
                            if(!silent) {
                                Print.success("Info Success has been shown");
                            }
                            break;
                        case "error":
                            RenderPermissions.setRenderErrors(true);
                            if(!silent) {
                                Print.success("Error channel has been shown");
                            }
                            break;
                        case "warn":
                            RenderPermissions.setRenderWarns(true);
                            if(!silent) {
                                Print.success("Warning channel has been shown");
                            }
                            break;
                        case "maze":
                            RenderPermissions.setRenderTrainingMaps(true);
                            if(!silent) {
                                Print.success("Training map channel has been shown");
                            }
                            break;
                        case "stats":
                            RenderPermissions.setRenderStats(true);
                            if(!silent) {
                                Print.success("Statistics channel has been shown");
                            }
                            break;
                        case "all":
                            RenderPermissions.setRenderForAll(true);
                            if(!silent) {
                                Print.success("All channels has been shown");
                            }
                            break;
                        default:
                            Print.error("No such argument. " + messageTypos + "\n");
                    }
                    break;
                case "logfile":
                    if ("none".equals(args[1])) {
                        Print.changeOutputStream(null);
                        if(!silent) {
                            Print.success("Logging has been disabled." + "\n");
                        }
                    } else {
                        try {
                            Print.changeOutputStream(new FileOutputStream(args[1]));
                            if(!silent) {
                                Print.success("Logging has been enabled." + "\n");
                            }
                        } catch (FileNotFoundException e) {
                            Print.error("Cannot use that file for some reason (Already opened stream? Not a file but directory?)" + "\n");
                        }
                    }

                default:
                    Print.error("Wrong use. " + messageTypos + "\n");
            }
        }else{
            Print.error("Wrong use. "+messageTypos+"\n");
        }
    }



    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getShortDesc() {
        return "Sets internal variable.";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "set if powerful command that sets internal variable to value, which",
                "users provides. Please note that all of these are defaulted at every start",
                "of program. If you want to save values, just use autorun and put setter command",
                "to it. ",
                "",
                "use: set <variable> <value> [flags]",
                "",
                "Where 'variable' is name of variable which value you would like change and ",
                "'value' is new value of variable. Here is the list of variables and allowed values:",
                "    enablecolors         true|false          Turns colorful output in terminal on or off",
                "                                             please note that not all of terminals supports",
                "                                             colorful text.",
                "    logfile              none|filename       Turns logging off or on. Use either absolute path",
                "                                             or path relative to this software's main .jar file.",
                "                                             When the file doesn't exists, it's created automatically.",
                "    showoutputs          info|success|warn   Show outputs and Hide outputs are inverted commands",
                "                         maze|states|all     which allows you to customize what you want to be",
                "                                             shown or not shown. For example, when you are using",
                "                                             autorun, you can put line at the beginning of file",
                "                                             which turns all output off and at the end of file",
                "                                             again. (You don't want to see those annoying",
                "                                             messages all the time, do you?",
                "    hideoutputs          info|success|warn   Show outputs and Hide outputs are inverted commands",
                "                         maze|states|all     which allows you to customize what you want to be",
                "                                             shown or not shown. For example, when you are using",
                "                                             autorun, you can put line at the beginning of file",
                "                                             which turns all output off and at the end of file",
                "                                             again. (You don't want to see those annoying",
                "                                             messages all the time, do you?",
                "",
                "And here is list of possible flags:",
                "    -s|--silent                              Don't outputs anything, unless error occoured."
        };
    }
}
