package lego.nxt.bootstrap;

import lego.bots.random.RandomBot;
import lego.nxt.controllers.CartesianEnvironmentRobotController;

/**
 * Private property.
 * User: Darkyen
 * Date: 23/10/14
 * Time: 11:01
 */
public class RandomBootstrap extends NXTBootstrap {
    public static void main(String[] args){
        main(new RandomBot(), new CartesianEnvironmentRobotController());
    }
}
