package lego.bots.node;

import lego.api.controllers.EnvironmentController.Direction;

/**
 * Private property.
 * User: jIRKA
 * Date: 4.12.2014
 * Time: 17:52
 */
final class PositionDirectionCache {

    private int[] cache;
    private int size = 0;

    public PositionDirectionCache(int initialSize) {
        cache = new int[initialSize];
    }

    private int key(byte x, byte y, Direction direction){
        return x | y << 8 | direction.ordinal() << 16;
    }

    public boolean contains(byte x, byte y, Direction direction){
        final int key = key(x,y,direction);

        for(int i = size - 1; i >= 0; i--){
            if(key == cache[i]){
                return true;
            }
        }
        return false;
    }

    public void add(byte x, byte y, Direction direction) {
        int currentSize = cache.length;
        if (size == currentSize) {
            int[] newCache = new int[currentSize << 1];
            System.arraycopy(cache,0,newCache,0,currentSize);
            cache = newCache;
        }
        cache[size] = key(x, y, direction);
        size++;
    }

    public void clear () {
        size = 0;
    }

}
