package lego.nxt.bootstrap;

import lego.bots.clever.CleverBot;
import lego.nxt.controllers.CartesianEnvironmentRobotController;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 11:01
 */
public class CleverBootstrap extends NXTBootstrap {
    public static void main(String[] args){
        main(new CleverBot(), new CartesianEnvironmentRobotController());
    }
}
