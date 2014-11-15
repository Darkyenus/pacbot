package lego.nxt.bootstrap;

import lego.bots.weightnav.WeightNavBot;
//import lego.nxt.controllers.CartesianEnvironmentRobotController;
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
                new WeightNavBot()
                //new RandomBot()
                ,
                //new CartesianEnvironmentRobotController()
                new DifferentialEnvironmentRobotController()
        );
    }
}
