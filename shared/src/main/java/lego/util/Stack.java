
package lego.util;

/** High performance lightweight stack.
 *
 * Created by Darkyen on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class Stack<T> {
	private T[] internal;
	private int nextPosition = 0;

	public Stack (int initialSize) {
		internal = (T[])new Object[initialSize];
	}

	public boolean isEmpty () {
		return nextPosition == 0;
	}

	/** Pops head of stack. Will throw an exception on underflow.
	 * @return head of the stack */
	public T pop () {
		nextPosition--;
		T result = internal[nextPosition];
		internal[nextPosition] = null;
		return result;
	}

	public T peek () {
		return internal[nextPosition - 1];
	}

	public void push (T value) {
		int currentSize = internal.length;
		if (nextPosition == currentSize) {
			T[] newInternal = (T[])new Object[currentSize << 2];
			System.arraycopy(internal, 0, newInternal, 0, currentSize);
			internal = newInternal;
		}
		internal[nextPosition] = value;
		nextPosition++;
	}

}
