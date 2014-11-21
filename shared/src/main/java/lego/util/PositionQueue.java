package lego.util;

/**
 * High performance lightweight stack.
 *
 * Created by jIRKA on 11.11.2014.
 */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class PositionQueue {
    private byte[] internalX;
    private byte[] internalY;
    private int writePosition = 0;
    private int readPosition = 0;

    public PositionQueue(int initialSize) {
        internalX = new byte[initialSize];
        internalY = new byte[initialSize];
    }

    public boolean isEmpty(){
        return readPosition >= writePosition;
    }

    public byte retreiveFirstX(){
        if(readPosition >= writePosition)
            return -1;

        return internalX[readPosition];
    }

    public byte retreiveFirstY(){
        if(readPosition >= writePosition)
            return -1;

        return internalY[readPosition];
    }

    public void moveReadHead(){
        readPosition ++;
    }

    public void pushNext(byte valueX, byte valueY){
        int currentSize = internalX.length;
        if(writePosition == currentSize){
            byte[] newInternalX = new byte[currentSize<<2];
            byte[] newInternalY = new byte[currentSize<<2];
            System.arraycopy(internalX,0,newInternalX,0,currentSize);
            System.arraycopy(internalY,0,newInternalY,0,currentSize);
            internalX = newInternalX;
            internalY = newInternalY;
        }
        internalX[writePosition] = valueX;
        internalY[writePosition] = valueY;
        writePosition++;
    }

    public void clear(){
        writePosition = 0;
        readPosition = 0;
    }
}
