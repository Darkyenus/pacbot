package lego.util;

/**
 * Private property.
 * User: Darkyen
 * Date: 14/11/14
 * Time: 11:54
 */
@SuppressWarnings("UnusedDeclaration")
public final class PositionStack {
    private byte[] internalX;
    private byte[] internalY;
    private int lastPosition = -1;

    public PositionStack(int initialSize) {
        internalX = new byte[initialSize];
        internalY = new byte[initialSize];
    }

    public boolean isEmpty(){
        return lastPosition == -1;
    }

    /**
     * Pops head of stack. Will NOT throw an exception on underflow. That will happen later.
     */
    public void pop(){
        lastPosition--;
    }

    public void push(byte x,byte y){
        int currentSize = internalX.length;
        if(lastPosition + 1 == currentSize){
            byte[] newInternalX = new byte[currentSize<<2];
            byte[] newInternalY = new byte[currentSize<<2];
            System.arraycopy(internalX,0,newInternalX,0,currentSize);
            System.arraycopy(internalY,0,newInternalY,0,currentSize);
            internalX = newInternalX;
            internalY = newInternalY;
        }
        lastPosition++;
        internalX[lastPosition] = x;
        internalY[lastPosition] = y;
    }

    public byte peekX(){
        return internalX[lastPosition];
    }

    public byte peekY(){
        return internalY[lastPosition];
    }

    public void clear(){
        lastPosition = -1;
    }
}
