
package lego.util;

/** Simple high performance queue.
 *
 * Created by jIRKA on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class BatchQueue<T> {
	private T[] internal;
	private int writePosition = 0;
	private int readPosition = 0;

	public BatchQueue(int initialSize) {
		internal = (T[])new Object[initialSize];
	}

	public boolean isEmpty () {
		return readPosition >= writePosition;
	}

	public boolean nonEmpty(){
		return readPosition < writePosition;
	}

	public T remove() {
		T result = internal[readPosition];
		internal[readPosition] = null;
		readPosition++;
		return result;
	}

	public T peek(){
		return internal[readPosition];
	}

	public void add(T value) {
		int currentSize = internal.length;
		if (writePosition == currentSize) {
			T[] newInternal = (T[])new Object[currentSize << 2];
			System.arraycopy(internal, 0, newInternal, 0, currentSize);
			internal = newInternal;
		}
		internal[writePosition] = value;
		writePosition++;
	}

	public void clear () {
		writePosition = 0;
		readPosition = 0;
	}
}
