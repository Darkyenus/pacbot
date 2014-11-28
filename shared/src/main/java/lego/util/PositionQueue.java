
package lego.util;

/** High performance lightweight stack.
 *
 * Created by jIRKA on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class PositionQueue {

	private byte[] internalX;
	private byte[] internalY;
	private int writePosition = 0;
	private int readPosition = 0;

	public PositionQueue (int initialSize) {
		internalX = new byte[initialSize];
		internalY = new byte[initialSize];
	}

	public boolean isEmpty () {
		return readPosition >= writePosition;
	}

	public int size () {
		return writePosition;
	}

	public byte retreiveFirstX () {
		if (readPosition >= writePosition) return -1;

		return internalX[readPosition];
	}

	public byte retreiveFirstY () {
		if (readPosition >= writePosition) return -1;

		return internalY[readPosition];
	}

	public byte getXAt (int index) {
		return internalX[index];
	}

	public byte getYAt (int index) {
		return internalY[index];
	}

	public void insertAfter (int index, byte[] xs, byte[] ys) {
		if (xs.length != ys.length) throw new IllegalArgumentException("Size of x array has to be same as size of y array");
		if (xs.length != 0) {
			int currentSize = internalX.length;
			byte[] newInternalX = new byte[currentSize + xs.length];
			byte[] newInternalY = new byte[currentSize + xs.length];

			System.arraycopy(internalX, index + readPosition, newInternalX, index + xs.length + readPosition, currentSize - index);
			System.arraycopy(internalX, 0, newInternalX, 0, index + readPosition);
			System.arraycopy(xs, 0, newInternalX, index, xs.length);

			System.arraycopy(internalY, index + readPosition, newInternalY, index + xs.length + readPosition, currentSize - index);
			System.arraycopy(internalY, 0, newInternalY, 0, index + readPosition);
			System.arraycopy(ys, 0, newInternalY, index, xs.length);

			internalX = newInternalX;
			internalY = newInternalY;
			writePosition += xs.length;
		}
	}

	public void moveReadHead () {
		readPosition++;
	}

	public void pushNext (byte valueX, byte valueY) {
		int currentSize = internalX.length;
		if (writePosition == currentSize) {
			byte[] newInternalX = new byte[currentSize << 2];
			byte[] newInternalY = new byte[currentSize << 2];
			System.arraycopy(internalX, readPosition, newInternalX, 0, currentSize);
			System.arraycopy(internalY, readPosition, newInternalY, 0, currentSize);
			internalX = newInternalX;
			internalY = newInternalY;
			writePosition -= readPosition;
			readPosition = 0;
		}
		internalX[writePosition] = valueX;
		internalY[writePosition] = valueY;
		writePosition++;
	}

	public void clear () {
		writePosition = 0;
		readPosition = 0;
	}
}
