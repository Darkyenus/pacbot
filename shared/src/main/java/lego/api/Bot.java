
package lego.api;

/** Private property. User: Darkyen Date: 19/10/14 Time: 13:41 */
public abstract class Bot<C extends BotController> {

	public static Bot active = null;

	{
		active = this;
	}

	/** This should be written to only from Bootstrap.
	 *
	 * Tl;dr: READ ONLY */
	public C controller;

	public abstract void run ();

	public abstract void onEvent (BotEvent event);
}
