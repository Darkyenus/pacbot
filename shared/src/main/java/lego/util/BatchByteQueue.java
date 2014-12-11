
package lego.util;

/** Simple high performance queue.
 *
 * Created by jIRKA on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class BatchByteQueue {
	private byte[] internal;
	private int writePosition = 0;
	private int readPosition = 0;

	public BatchByteQueue(int initialSize) {
		internal = new byte[initialSize];
	}

	public boolean isEmpty () {
		return readPosition >= writePosition;
	}

	public boolean nonEmpty(){
		return readPosition < writePosition;
	}

	public byte remove() {
		byte result = internal[readPosition];
		readPosition++;
		if(readPosition == writePosition){
			writePosition = 0;//Reset to save memory
			readPosition = 0;
		}
		return result;
	}

	public byte peek(){
		return internal[readPosition];
	}

	public void add(byte value) {
		int currentSize = internal.length;
		if (writePosition == currentSize) {
			byte[] newInternal = new byte[currentSize << 1];
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

	public int size(){
		return writePosition - readPosition;
	}
}
