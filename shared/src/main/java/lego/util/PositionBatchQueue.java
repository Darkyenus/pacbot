
package lego.util;

/**
 * Created by jIRKA on 11.11.2014. */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class PositionBatchQueue {

	private byte[] internalX;
	private byte[] internalY;
	private int writePosition = 0;
	private int readPosition = 0;

	public PositionBatchQueue(int initialSize) {
		internalX = new byte[initialSize];
		internalY = new byte[initialSize];
	}

	public boolean isEmpty () {
		return readPosition >= writePosition;
	}

	public boolean nonEmpty(){
		return readPosition < writePosition;
	}

	public int size () {
		return writePosition - readPosition;
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

    public void insertAfter(int index, byte x, byte y){
        int currentSize = internalX.length;
        byte[] newInternalX = new byte[currentSize + 1];
        byte[] newInternalY = new byte[currentSize + 1];

        System.arraycopy(internalX,index + readPosition + 1,newInternalX,index + readPosition + 2,currentSize - index - 1);
        System.arraycopy(internalX,0,newInternalX,0,index + readPosition + 1);
        newInternalX[index + 1] = x;

        System.arraycopy(internalY,index + readPosition + 1,newInternalY,index + readPosition + 2,currentSize - index - 1);
        System.arraycopy(internalY,0,newInternalY,0,index + readPosition + 1);
        newInternalY[index + 1] = y;

        internalX = newInternalX;
        internalY = newInternalY;
        writePosition += 1;
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
