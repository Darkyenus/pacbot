
package lego.util;

/** High performance lightweight stack.
 *
 * Created by Darkyen on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class ByteStack {
	private byte[] internal;
	private int nextPosition = 0;

	public ByteStack (int initialSize) {
		internal = new byte[initialSize];
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
	public byte pop () {
		nextPosition--;
		return internal[nextPosition];
	}

	public byte peek () {
		return internal[nextPosition - 1];
	}

	public void push (byte value) {
		int currentSize = internal.length;
		if (nextPosition == currentSize) {
			byte[] newInternal = new byte[currentSize << 1];
			System.arraycopy(internal, 0, newInternal, 0, currentSize);
			internal = newInternal;
		}
		internal[nextPosition] = value;
		nextPosition++;
	}

	/**
	 * @return Content of this stack in a new array
	 * NOTE: Creates garbage, use #getCopyAsArray(byte[]) instead
	 */
	public byte[] getCopyAsArray () {
		byte[] res = new byte[nextPosition];
		System.arraycopy(internal, 0, res, 0, nextPosition);
		return res;
	}

	/**
	 * Copies this array into given array.
	 * Given array must be same size or bigger than size of this.
	 * @param to which array copy
	 * @return to for convenience
	 */
	public byte[] getCopyAsArray (byte[] to) {
		System.arraycopy(internal, 0, to, 0, nextPosition);
		return to;
	}

	public byte[] getUnderlyingArray(){
		return internal;
	}
}
