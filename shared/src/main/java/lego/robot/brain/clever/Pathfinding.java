package lego.robot.brain.clever;

import lego.robot.api.constants.AbsoluteHeading;
import lego.util.TupleIntInt;

/**
 * Private property.
 * User: jIRKA
 * Date: 10.10.2014
 * Time: 17:27
 *
 * @deprecated Uses old API (and has zero usable code anyway)
 */
@Deprecated
class Pathfinding {

    public static void planRoute(MappedMap map, TupleIntInt pos, CleverMain cm){

        PlanningBlock start = new PlanningBlock(null, 0, pos, cm.getRobotEnvironment().getHeading());

        PlanningBlock found = null;

        PlanningBlock iter = start;

        while(found != null) {
            if (map.getBlockType(iter.pos) == BlockType.UNKNOWN || map.getBlockType(iter.pos) == BlockType.PAC_DOT) {
                // Our goal to get here
                found = iter;
            }else{
                // We are on non-goal position, continue searching
                TupleIntInt top = new TupleIntInt(pos.getX(), pos.getY() - 1);
                TupleIntInt left = new TupleIntInt(pos.getX() - 1, pos.getY());
                TupleIntInt right = new TupleIntInt(pos.getX() + 1, pos.getY());
                TupleIntInt bottom = new TupleIntInt(pos.getX(), pos.getY() + 1);
            }

        }

    }



    private static class PlanningBlockMark{

    }

    private static class PlanningBlock{

        private PlanningBlock from;
        private int turns;
        private AbsoluteHeading dir;
        private TupleIntInt pos;

        public PlanningBlock(PlanningBlock from, int turns, TupleIntInt pos, AbsoluteHeading dir){
            this.from = from;
            this.turns = turns;
            this.pos = pos;
            this.dir = dir;
        }

    }

}
