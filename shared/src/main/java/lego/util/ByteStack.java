
package lego.util;

/** High performance lightweight stack.
 *
 * Created by Darkyen on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class ByteStack {
	private byte[] internal;
	private byte nextPosition = 0;

	public ByteStack (int initialSize) {
		internal = new byte[initialSize];
	}

	public boolean isEmpty () {
		return nextPosition == 0;
	}

	public boolean nonEmpty() {
		return nextPosition != 0;
	}

	public void clear () {
		nextPosition = 0;
	}

	/** Pops head of stack. Will throw an exception on underflow.
	 * @return head of the stack */
	public byte pop () {
		nextPosition--;
		byte result = internal[nextPosition];
		return result;
	}

	public byte peek () {
		return internal[nextPosition - 1];
	}

	public void push (byte value) {
		int currentSize = internal.length;
		if (nextPosition == currentSize) {
			byte[] newInternal = new byte[currentSize << 2];
			System.arraycopy(internal, 0, newInternal, 0, currentSize);
			internal = newInternal;
		}
		internal[nextPosition] = value;
		nextPosition++;
	}

	public byte[] getCopyAsArray () {
		byte[] res = new byte[nextPosition];
		System.arraycopy(internal, 0, res, 0, nextPosition);
		return res;
	}

}
