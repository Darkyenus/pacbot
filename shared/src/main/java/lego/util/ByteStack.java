package lego.util;

/**
 * High performance lightweight stack.
 *
 * Created by Darkyen on 11.11.2014.
 */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class ByteStack {
    private Byte[] internal;
    private int nextPosition = 0;

    public ByteStack(int initialSize) {
        internal = new Byte[initialSize];
    }

    public boolean isEmpty(){
        return nextPosition == 0;
    }

    public void clear(){
        nextPosition = 0;
    }

    /**
     * Pops head of stack. Will throw an exception on underflow.
     * @return head of the stack
     */
    public Byte pop(){
        nextPosition--;
        Byte result = internal[nextPosition];
        return result;
    }

    public Byte peek(){
        return internal[nextPosition - 1];
    }

    public void push(Byte value){
        int currentSize = internal.length;
        if(nextPosition == currentSize){
            Byte[] newInternal = new Byte[currentSize<<2];
            System.arraycopy(internal,0,newInternal,0,currentSize);
            internal = newInternal;
        }
        internal[nextPosition] = value;
        nextPosition++;
    }

    public Byte[] getCopyAsArray(){
        Byte[] res = new Byte[nextPosition];
        System.arraycopy(internal, 0, res, 0, nextPosition);
        return res;
    }

}
