package lego.util;

/**
 * High performance lightweight stack.
 *
 * Created by jIRKA on 11.11.2014.
 */
@SuppressWarnings({"unchecked", "UnusedDeclaration"})
public final class Queue<T> {
    private T[] internal;
    private int writePosition = 0;
    private int readPosition = 0;

    public Queue(int initialSize) {
        internal = (T[]) new Object[initialSize];
    }

    public boolean isEmpty(){
        return readPosition >= writePosition;
    }

    public T retreiveFirst(){
        if(readPosition >= writePosition)
            return null;

        T result = internal[readPosition];
        internal[readPosition] = null;
        readPosition++;
        return result;
    }

    public void pushNext(T value){
        int currentSize = internal.length;
        if(writePosition == currentSize){
            T[] newInternal = (T[]) new Object[currentSize<<2];
            System.arraycopy(internal,0,newInternal,0,currentSize);
            internal = newInternal;
        }
        internal[writePosition] = value;
        writePosition++;
    }

    public void clear(){
        writePosition = 0;
        readPosition = 0;
    }
}
