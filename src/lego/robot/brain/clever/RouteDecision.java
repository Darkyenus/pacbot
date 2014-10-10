package lego.robot.brain.clever;

/**
 * Private property.
 * User: jIRKA
 * Date: 10.10.2014
 * Time: 16:51
 */
public class RouteDecision {


    public static Action caseOnePacdotted(BlockType frontBlock, BlockType leftBlock, BlockType rightBlock, BlockType backBlock) {
        if(frontBlock == BlockType.PAC_DOT && leftBlock != BlockType.PAC_DOT && rightBlock != BlockType.PAC_DOT && backBlock != BlockType.PAC_DOT){
            return Action.MOVE_FORWARD;
        }else if(frontBlock != BlockType.PAC_DOT && leftBlock == BlockType.PAC_DOT && rightBlock != BlockType.PAC_DOT && backBlock != BlockType.PAC_DOT){
            return Action.MOVE_FORWARD;
        }else if(frontBlock != BlockType.PAC_DOT && leftBlock != BlockType.PAC_DOT && rightBlock == BlockType.PAC_DOT && backBlock != BlockType.PAC_DOT){
            return Action.MOVE_FORWARD;
        }else if(frontBlock != BlockType.PAC_DOT && leftBlock != BlockType.PAC_DOT && rightBlock != BlockType.PAC_DOT && backBlock == BlockType.PAC_DOT){
            return Action.MOVE_FORWARD;
        }
        return Action.NONE;
    }

    public static Action caseMorePacdotted(MappedMap map, CleverMain cm, BlockType frontBlock, BlockType leftBlock, BlockType rightBlock, BlockType backBlock) {

        int count = 0;
        if(frontBlock == BlockType.PAC_DOT)
            count ++;
        if(leftBlock == BlockType.PAC_DOT)
            count ++;
        if(rightBlock == BlockType.PAC_DOT)
            count ++;
        if(backBlock == BlockType.PAC_DOT)
            count ++;

        if(count > 1){

            //TODO potential dead end check
            //If true, go there (or to the most dead end looking way)

            //TODO target closer to border check, somehow should take it account robot's heading
            // goto pacdot closer to border

            //TODO brain preferences
            //in case of hesitation use argument value

            return Action.NONE;

        }else {
            return Action.NONE;
        }
    }

    public static Action casePlannedRoute(BlockType frontBlock, BlockType leftBlock, BlockType rightBlock, BlockType backBlock) {
        if(frontBlock == BlockType.PLANNED){
            return Action.MOVE_FORWARD;
        }else if(leftBlock == BlockType.PLANNED){
            return Action.MOVE_LEFT;
        }else if(rightBlock == BlockType.PLANNED){
            return Action.MOVE_RIGHT;
        }else if(backBlock == BlockType.PLANNED){
            return Action.MOVE_BACKWARD;
        }else{
            return Action.NONE;
        }
    }

    public static Action caseNoRoute(BlockType frontBlock, BlockType leftBlock, BlockType rightBlock, BlockType backBlock) {

        int count = 0;
        if(frontBlock != BlockType.BLOCK && frontBlock != BlockType.DEAD_END && frontBlock != BlockType.START)
            count ++;
        if(leftBlock != BlockType.BLOCK && leftBlock != BlockType.DEAD_END && leftBlock != BlockType.START)
            count ++;
        if(rightBlock != BlockType.BLOCK && rightBlock != BlockType.DEAD_END && rightBlock != BlockType.START)
            count ++;
        if(backBlock != BlockType.BLOCK && backBlock != BlockType.DEAD_END && backBlock != BlockType.START)
            count ++;

        if(count == 1){
            if(frontBlock != BlockType.BLOCK && frontBlock != BlockType.DEAD_END && frontBlock != BlockType.START){
                return Action.MOVE_FORWARD;
            } else if(leftBlock != BlockType.BLOCK && leftBlock != BlockType.DEAD_END && leftBlock != BlockType.START){
                return Action.MOVE_LEFT;
            } else if(rightBlock != BlockType.BLOCK && rightBlock != BlockType.DEAD_END && rightBlock != BlockType.START){
                return Action.MOVE_RIGHT;
            } else if(backBlock != BlockType.BLOCK && backBlock != BlockType.DEAD_END && backBlock != BlockType.START){
                return Action.MOVE_BACKWARD;
            }else{
                return Action.NONE;
            }
        }else{
            return Action.NONE;
        }

    }
}
