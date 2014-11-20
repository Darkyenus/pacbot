package lego.nxt.bootstrap;

import lego.bots.node.NodeBot;
import lego.nxt.controllers.CartesianEnvironmentRobotController;

//import lego.nxt.controllers.CartesianEnvironmentRobotController;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 11:01
 */
public class MainBootstrap extends NXTBootstrap {
    public static void main(String[] args){
        main(
                //new CheatyBot()
                new NodeBot()
                //new RandomBot()
                //new WeightNavBot()
                ,
                new CartesianEnvironmentRobotController()
                //new DifferentialEnvironmentRobotController()
        );
    }
}
