package lego.nxt.bootstrap;

import lego.bots.clever.CleverBot;
//import lego.nxt.controllers.CartesianEnvironmentRobotController;
import lego.bots.random.RandomBot;
import lego.nxt.controllers.DifferentialEnvironmentRobotController;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 11:01
 */
public class MainBootstrap extends NXTBootstrap {
    public static void main(String[] args){
        main(
                new CleverBot()
                //new RandomBot()
                ,
                //new CartesianEnvironmentRobotController()
                new DifferentialEnvironmentRobotController()
        );
    }
}
