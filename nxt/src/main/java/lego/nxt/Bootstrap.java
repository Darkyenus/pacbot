
package lego.nxt;

import lego.api.Bot;
import lego.api.BotController;
import lego.bots.load.LoadBot;
import lego.bots.test.PlanTestBot;
import lego.bots.weightnav.WeightNavBot;
import lego.nxt.controllers.DifferentialEnvironmentRobotController;
import lego.nxt.controllers.DifferentialPlannedRobotController;

/** Private property. User: Darkyen Date: 23/10/14 Time: 11:01 */
public final class Bootstrap {
	public static void main (String[] args) {
		main(
			//new CheatyBot()
			//new CleverBot()
			//new NodeBot()
			//new TestificateBot()
			//new WeightNavBot()
			new PlanTestBot()
            ,
            //new DifferentialEnvironmentRobotController()
			new DifferentialPlannedRobotController()
        );
	}

	/** Call this method from non-abstract bootstrap with selected bot and controller. */
	public static <C extends BotController> void main (final Bot<C> bot, final C controller) {
		controller.initialize();
		bot.controller = controller;
		bot.run();
		controller.deinitialize();

		System.exit(0);
	}
}
