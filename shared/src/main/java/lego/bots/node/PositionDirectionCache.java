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
    private int[] bloom;
    private int size = 0;

    public PositionDirectionCache(int initialSize) {
        cache = new int[initialSize];
        bloom = new int[8];
    }

    private int key(byte x, byte y, Direction direction){
        return x | y << 8 | direction.ordinal() << 16;
    }

    public boolean contains(byte x, byte y, Direction direction){
        final int key = key(x,y,direction);
        final int keyHash = key * 61;
        if ((bloom[(key * 41) % bloom.length] & keyHash) != keyHash) {
            return false;
        }

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
        final int key = key(x, y, direction);
        cache[size] = key;
        bloom[(key * 41) % bloom.length] |= key * 61;

        size++;
    }

    public void clear () {
        size = 0;
    }

}
