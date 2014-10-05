package lego.util;

import lego.training.userinterface.ConsoleColors;

/**
 * Created by jIRKA on 4.10.2014.
 */
public class Constants {

    //TODO: Some changing, getter + setter

    public static String RENDER_BLOCK = "[x]";
    public static String RENDER_PAC_DOT = " • ";
    public static String RENDER_PAC_DOT_EATEN = " o ";
    public static String RENDER_ROBOT = "(-)";
    public static String RENDER_START = " v ";

    public static ConsoleColors COLOR_MAZE_BLOCK = ConsoleColors.BRIGHT_RED;
    public static ConsoleColors COLOR_MAZE_PAC_DOT = ConsoleColors.BRIGHT_YELLOW;
    public static ConsoleColors COLOR_MAZE_PAC_DOT_EATEN = ConsoleColors.YELLOW;
    public static ConsoleColors COLOR_MAZE_ROBOT = ConsoleColors.GREEN;
    public static ConsoleColors COLOR_MAZE_START = ConsoleColors.MAGENTA;

    public static ConsoleColors COLOR_TAG_ERROR = ConsoleColors.RED;
    public static ConsoleColors COLOR_TAG_SUCCESS = ConsoleColors.GREEN;
    public static ConsoleColors COLOR_TAG_INFO = ConsoleColors.CYAN;
    public static ConsoleColors COLOR_TAG_WARN = ConsoleColors.YELLOW;
    public static ConsoleColors COLOR_TAG_INTERACTION = ConsoleColors.BRIGHT_BLUE;

}
