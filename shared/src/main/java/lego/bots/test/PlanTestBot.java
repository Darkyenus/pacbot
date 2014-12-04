
package lego.bots.test;

import lego.api.Bot;
import lego.api.BotEvent;
import lego.api.controllers.EnvironmentController;
import lego.api.controllers.PlannedController;
import lego.util.Latch;

/** Bot class created only for testing some subsystems of controllers. Use and edit as you wish
 *
 * Private property. User: Darkyen Date: 23/10/14 Time: 10:23 */
@SuppressWarnings("UnusedDeclaration")
public final class PlanTestBot extends Bot<PlannedController> {

	private boolean continueRunning = true;
	private final Latch startLatch = new Latch();

	@Override
	public synchronized void run () {
		startLatch.pass();
		while (continueRunning) {
			// tightLoop((byte)2);
			thereAndBackAgain((byte)2);
			controller.travelPath();
		}
	}

	private void thereAndBackAgain (byte dist) {
		controller.addYPath(dist);
		controller.addYPath((byte)-dist);
		controller.addXPath(dist);
		controller.addXPath((byte)-dist);
	}

	private void tightLoop (byte dist) {
		controller.addYPath(dist);
		controller.addXPath(dist);
		controller.addYPath((byte)-dist);
		controller.addXPath((byte)-dist);
	}

	private void figureEight (byte dist) {
		controller.addYPath(dist);
		controller.addXPath((byte)-dist);
		controller.addYPath((byte)-dist);
		controller.addXPath((byte)-dist);
		controller.addYPath(dist);
		controller.addXPath(dist);
		controller.addYPath((byte)-dist);
		controller.addXPath(dist);
	}

	@Override
	public void onEvent (BotEvent event) {
		switch (event) {
		case RUN_ENDED:
			continueRunning = false;
			startLatch.open();
			break;
		case RUN_STARTED:
			continueRunning = true;
			startLatch.open();
			break;
		}
	}
}
