package lego.robot.brain.clever;

import lego.util.TupleIntInt;
import lego.util.Util;

/**
 * @deprecated Old API
 */
@Deprecated
class MappedMap {
    private BlockType[][] map;
    public void init(TupleIntInt startPos){
        map = new BlockType[9][6];
        for (int x = 0; x < map.length; x++){
            for (int y = 0; y < map[x].length; y++){
                map[x][y] = BlockType.UNKNOWN;
                if (x == startPos.getX () && y == startPos.getY ()){
                     map[x][y] = BlockType.START;
                }
            }
        }
    }
    public BlockType getBlockType(TupleIntInt where){
        if (Util.isWithinMapBounds(where)){
            return map[where.getX ()][where.getY()];
        } else {
            return BlockType.BLOCK;
        }
    }
    public BlockType[][] getMap(){
        return map;
    }
    public boolean mark(TupleIntInt where, BlockType what){
        if (Util.isWithinMapBounds(where)){
            map[where.getX()][where.getY()] = what;
            return true;
        } else {
            return false;
        }
    }
}
