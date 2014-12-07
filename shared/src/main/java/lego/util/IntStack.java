
package lego.util;

/** High performance lightweight stack.
 *
 * Created by Darkyen on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class IntStack {
	private int[] internal;
	private int nextPosition = 0;

	public IntStack(int initialSize) {
		internal = new int[initialSize];
	}

	public boolean isEmpty () {
		return nextPosition == 0;
	}

	public boolean nonEmpty() {
		return nextPosition != 0;
	}

	public int size(){
		return nextPosition;
	}

	public void clear () {
		nextPosition = 0;
	}

	/** Pops head of stack. Will throw an exception on underflow.
	 * @return head of the stack */
	public int pop () {
		nextPosition--;
		return internal[nextPosition];
	}

	public int peek () {
		return internal[nextPosition - 1];
	}

	public void push (int value) {
		int currentSize = internal.length;
		if (nextPosition == currentSize) {
			int[] newInternal = new int[currentSize << 1];
			System.arraycopy(internal, 0, newInternal, 0, currentSize);
			internal = newInternal;
		}
		internal[nextPosition] = value;
		nextPosition++;
	}

	/**
	 * @return Content of this stack in a new array
	 * @deprecated Creates garbage, use #getCopyAsArray(int[])
	 */
	@Deprecated
	public int[] getCopyAsArray () {
		int[] res = new int[nextPosition];
		System.arraycopy(internal, 0, res, 0, nextPosition);
		return res;
	}

	/**
	 * Copies this array into given array.
	 * Given array must be same size or bigger than size of this.
	 * @param to which array copy
	 * @return to for convenience
	 */
	public int[] getCopyAsArray (int[] to) {
		System.arraycopy(internal, 0, to, 0, nextPosition);
		return to;
	}
}
