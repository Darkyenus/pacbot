package lego.util;

import lego.api.controllers.EnvironmentController.Direction;

/**
 * Private property.
 * User: jIRKA
 * Date: 4.12.2014
 * Time: 17:52
 */
public class PositionDirectionArrayList {

    private byte[] internalX;
    private byte[] internalY;
    private Direction[] internalDir;
    private int writePosition = 0;
    private int readPosition = 0;

    public PositionDirectionArrayList(int initialSize) {
        internalX = new byte[initialSize];
        internalY = new byte[initialSize];
        internalDir = new Direction[initialSize];
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

    public byte getXAt (int index) {
        return internalX[index];
    }

    public byte getYAt (int index) {
        return internalY[index];
    }

    public Direction getDirAt(int index){
        return internalDir[index];
    }

    public void moveReadHead () {
        readPosition++;
    }

    public boolean containsCombination(byte valueX, byte valueY, Direction valueDir){
        for(int i = readPosition; i < writePosition; i ++){
            if(valueX == internalX[i] && valueY == internalY[i] && valueDir == internalDir[i]){
                return true;
            }
        }
        return false;
    }

    public void addCombination(byte valueX, byte valueY, Direction valueDir) {
        if(!containsCombination(valueX, valueY, valueDir)) {
            int currentSize = internalX.length;
            if (writePosition == currentSize) {
                byte[] newInternalX = new byte[currentSize << 2];
                byte[] newInternalY = new byte[currentSize << 2];
                Direction[] newInternalDir = new Direction[currentSize << 2];
                System.arraycopy(internalX, readPosition, newInternalX, 0, currentSize);
                System.arraycopy(internalY, readPosition, newInternalY, 0, currentSize);
                System.arraycopy(internalDir, readPosition, newInternalDir, 0, currentSize);
                internalX = newInternalX;
                internalY = newInternalY;
                internalDir = newInternalDir;
                writePosition -= readPosition;
                readPosition = 0;
            }
            internalX[writePosition] = valueX;
            internalY[writePosition] = valueY;
            internalDir[writePosition] = valueDir;
            writePosition++;
        }
    }

    public void clear () {
        writePosition = 0;
        readPosition = 0;
    }

}
