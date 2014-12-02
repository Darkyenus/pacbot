
package lego.api.controllers;

/** Private property. User: Darkyen Date: 23/10/14 Time: 10:57 */
@SuppressWarnings("UnusedDeclaration")
public abstract class EnvironmentController extends MapAwareController {

	/** Instruct robot to move by given amount of fields.
	 * @param x direction and distance
	 * @return amount of actually travelled fields */
	public byte moveByX (byte x) {
		return moveByXAsync(x).moved();
	}

	/** Same as moveByX but asynchronous. */
	public abstract MoveFieldsTask moveByXAsync (byte x);

	/** Instruct robot to move by given amount of fields.
	 * @param y direction and distance
	 * @return amount of actually travelled fields */
	public byte moveByY (byte y) {
		return moveByYAsync(y).moved();
	}

	/** Same as moveByY but asynchronous. */
	public abstract MoveFieldsTask moveByYAsync (byte y);

	/** Instruct robot to move in given direction until it encounters an obstacle
	 * @param in direction
	 * @return amount of actually travelled fields */
	public byte move (Direction in) {
		return moveAsync(in).moved();
	}

	/** Same as move but asynchronous. You may want to override this, if you need better implementation. */
	public MoveFieldsTask moveAsync (Direction in) {
		switch (in) {
		case UP:
			return moveByYAsync((byte)-100);
		case DOWN:
			return moveByYAsync((byte)100);
		case LEFT:
			return moveByXAsync((byte)-100);
		case RIGHT:
			return moveByXAsync((byte)100);
		default:
			// This will never return, above match is exhaustive. Unless in is null. Then you have bigger problems.
			throw new Error();
		}
	}

	public static enum Direction {
		UP((byte)0, (byte)-1), RIGHT((byte)1, (byte)0), DOWN((byte)0, (byte)1), LEFT((byte)-1, (byte)0);

		public final byte x;
		public final byte y;
		public Direction right;
		public Direction left;

		Direction (byte x, byte y) {
			this.x = x;
			this.y = y;
		}

		static { // I am not 100% sure this will work on nxt. But it should.
			Direction[] values = values();
			int length = values.length;
			for (int i = 0; i < length; i++) {
				values[i].right = values()[(i + 1) % length];
				values[i].left = values()[(i + length - 1) % length];
			}
		}
	}

	public static interface MoveFieldsTask {

		/** Returns whether or not is this task already done. */
		public boolean isDone ();

		/** Blocks until task is done, then returns by how much tiles it did move. */
		public byte moved ();

		/** Blocks until isDone returns true. */
		public void waitUntilDone ();
	}
}
