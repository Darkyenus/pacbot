
package lego.util;

import lego.api.controllers.EnvironmentController;

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
		return internalX[index + readPosition];
	}

	public byte getYAt (int index) {
		return internalY[index + readPosition];
	}

    public void changeValue(int index, byte x, byte y){
        if(index < writePosition && index >= readPosition){
            internalX[readPosition + index] = x;
            internalY[readPosition + index] = y;
        }
    }

    public void compress(){
        byte[] newInternalX = new byte[writePosition - readPosition];
        byte[] newInternalY = new byte[writePosition - readPosition];
        byte lastX = -1;
        byte lastY = -1;
        int shift = 0;

        for(int i = readPosition; i < writePosition; i++){
            if(lastX != internalX[i] || lastY != internalY[i]){
                newInternalX[i - readPosition - shift] = internalX[i];
                newInternalY[i - readPosition - shift] = internalY[i];
            }else{
                shift ++;
            }
            lastX = internalX[i];
            lastY = internalY[i];
        }

        internalX = newInternalX;
        internalY = newInternalY;
        writePosition -= readPosition + shift;
        readPosition = 0;
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

    public short computePrice(byte priceMove, byte priceTurnAround, byte priceTurn){
        short price = 0;

        EnvironmentController.Direction actualDir = null;

        byte prevX = 0;
        byte prevY = 0;
        byte nextX, nextY = 0;

        if(!isEmpty()) {
            prevX = internalX[readPosition];
            prevY = internalY[readPosition];
        }

        for(byte i = 1; i < size(); i++){
            nextX = internalX[i + readPosition];
            nextY = internalY[i + readPosition];

            if (nextX == prevX && nextY == prevY + 1) {
                if (actualDir == EnvironmentController.Direction.DOWN) {
                    price += priceMove;
                } else if(actualDir == EnvironmentController.Direction.UP) {
                    price += priceTurnAround;
                    price += priceMove;
                } else {
                    price += priceTurn;
                    price += priceMove;
                }
                actualDir = EnvironmentController.Direction.DOWN;
            }
            if (nextX == prevX && nextY == prevY - 1) {
                if (actualDir == EnvironmentController.Direction.UP) {
                    price += priceMove;
                } else if(actualDir == EnvironmentController.Direction.DOWN) {
                    price += priceTurnAround;
                    price += priceMove;
                } else {
                    price += priceTurn;
                    price += priceMove;
                }
                actualDir = EnvironmentController.Direction.UP;
            }
            if (nextX == prevX - 1 && nextY == prevY) {
                if (actualDir == EnvironmentController.Direction.LEFT) {
                    price += priceMove;
                } else if(actualDir == EnvironmentController.Direction.RIGHT) {
                    price += priceTurnAround;
                    price += priceMove;
                } else {
                    price += priceTurn;
                    price += priceMove;
                }
                actualDir = EnvironmentController.Direction.LEFT;
            }
            if (nextX == prevX + 1 && nextY == prevY) {
                if (actualDir == EnvironmentController.Direction.RIGHT) {
                    price += priceMove;
                } else if(actualDir == EnvironmentController.Direction.LEFT) {
                    price += priceTurnAround;
                    price += priceMove;
                } else {
                    price += priceTurn;
                    price += priceMove;
                }
                actualDir = EnvironmentController.Direction.RIGHT;
            }

            prevX = nextX;
            prevY = nextY;
        }

        return price;
    }
}
