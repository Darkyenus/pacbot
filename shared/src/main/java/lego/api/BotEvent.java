
package lego.api;

/** Private property. User: Darkyen Date: 19/10/14 Time: 13:43 */
public enum BotEvent {
	/** Called before anything else on high priority thread.
	 * Do calculating, loading and similar preparation stuff here.
	 * Should block until everything is completed. */
	RUN_PREPARE,
	/** Called after RUN_PREPARE. Should not block.
	 * Robot shall start moving after this event. */
	RUN_STARTED,
	/** Called anytime, signalling that run is ended (for whatever reason) and everything should stop. */
	RUN_ENDED,
	/** Called when BotController determines, that the bot has done something incorrectly, and does no longer know where it is.
	 * Bot can use this for change of plans. */
	BOT_LOST
}
