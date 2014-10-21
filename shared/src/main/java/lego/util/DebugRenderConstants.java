package lego.util;

import lego.simulator.userinterface.ConsoleColors;

/**
 * Created by jIRKA on 4.10.2014.
 */
public class DebugRenderConstants {

    public static final String RENDER_BLOCK = "[x]";
    public static final String RENDER_PAC_DOT = " # ";
    public static final String RENDER_PAC_DOT_EATEN = "   ";
    public static final String RENDER_ROBOT = "(-)";
    public static final String RENDER_START = " v ";

    public static final ConsoleColors COLOR_MAZE_BLOCK = ConsoleColors.BRIGHT_RED;
    public static final ConsoleColors COLOR_MAZE_PAC_DOT = ConsoleColors.BRIGHT_YELLOW;
    public static final ConsoleColors COLOR_MAZE_PAC_DOT_EATEN = ConsoleColors.YELLOW;
    public static final ConsoleColors COLOR_MAZE_ROBOT = ConsoleColors.GREEN;
    public static final ConsoleColors COLOR_MAZE_START = ConsoleColors.BRIGHT_MAGENTA;

    public static final ConsoleColors COLOR_TAG_ERROR = ConsoleColors.RED;
    public static final ConsoleColors COLOR_TAG_SUCCESS = ConsoleColors.GREEN;
    public static final ConsoleColors COLOR_TAG_INFO = ConsoleColors.CYAN;
    public static final ConsoleColors COLOR_TAG_WARN = ConsoleColors.YELLOW;
    public static final ConsoleColors COLOR_TAG_INTERACTION = ConsoleColors.BRIGHT_BLUE;

}
