package lego.robot.brain.clever;

import lego.robot.api.AbstractRobotInterface;
import lego.robot.api.RobotStrategy;
import lego.robot.api.constants.RelativeMovement;
import lego.util.TupleIntInt;
import lego.util.Util;

/**
 * Private property.
 * User: jIRKA
 * Date: 3.10.2014
 * Time: 20:30
 *
 * @deprecated Uses old API
 */
@Deprecated
public class CleverMain extends RobotStrategy{

    public CleverMain(AbstractRobotInterface ari){
        super(ari,"'Clever' - Fully clever");
    }

    private boolean run = true;

    // Settable
    private boolean cartesian = false;
    private boolean preferLeft = false;
    private boolean preferRight = false;
    private boolean preferTop = false;
    private boolean preferBottom = false;
    private boolean preferHorizontal = false;
    private boolean preferVertical = false;

    @Override
    public void run() {
        run = true;

        if(initialData.containsKey("cartesian")){
            cartesian = initialData.get("cartesian").equalsIgnoreCase("true");
        }
        if(initialData.containsKey("vertical")){
            preferTop = initialData.get("vertical").equalsIgnoreCase("top");
            preferBottom = initialData.get("vertical").equalsIgnoreCase("bottom");
        }
        if(initialData.containsKey("horizontal")){
            preferLeft = initialData.get("horizontal").equalsIgnoreCase("left");
            preferRight = initialData.get("horizontal").equalsIgnoreCase("right");
        }
        if(initialData.containsKey("axis")){
            preferHorizontal = initialData.get("axis").equalsIgnoreCase("horizontal");
            preferVertical = initialData.get("axis").equalsIgnoreCase("vertical");
        }


        MappedMap map = new MappedMap();
        map.init(robotEnvironment.getPos());

        while(run){

            TupleIntInt actualPos = robotEnvironment.getPos();
            TupleIntInt frontPos = Util.getTransformedPos(actualPos,robotEnvironment.getHeading(), RelativeMovement.FORWARD);
            TupleIntInt leftPos = Util.getTransformedPos(actualPos,robotEnvironment.getHeading(), RelativeMovement.LEFT);
            TupleIntInt rightPos = Util.getTransformedPos(actualPos,robotEnvironment.getHeading(), RelativeMovement.RIGHT);
            TupleIntInt backPos = Util.getTransformedPos(actualPos,robotEnvironment.getHeading(), RelativeMovement.BACKWARD);

            BlockType frontBlock = map.getBlockType(frontPos);
            BlockType leftBlock = map.getBlockType(leftPos);
            BlockType rightBlock = map.getBlockType(rightPos);
            BlockType backBlock = map.getBlockType(backPos);

            if(frontBlock == BlockType.UNKNOWN || leftBlock == BlockType.UNKNOWN || rightBlock == BlockType.UNKNOWN || backBlock == BlockType.UNKNOWN){
                robotInterface.waitUntilQueueIsEmpty();

                CleverUtil.logScan(map, frontPos, !robotInterface.scanFront(), leftPos, !robotInterface.scanLeft(), rightPos, !robotInterface.scanRight(), actualPos);

                frontBlock = map.getBlockType(frontPos);
                leftBlock = map.getBlockType(leftPos);
                rightBlock = map.getBlockType(rightPos);
                backBlock = map.getBlockType(backPos);
            }else{
                if(map.getBlockType(actualPos) == BlockType.PAC_DOT || map.getBlockType(actualPos) == BlockType.PLANNED){
                    map.mark(actualPos, BlockType.VISITED);
                }
            }

            Action action;

            action = RouteDecision.caseOnePacdotted(frontBlock, leftBlock, rightBlock, backBlock);

            if(action == Action.NONE){
                action = RouteDecision.caseMorePacdotted(map, this, frontBlock, leftBlock, rightBlock, backBlock);
            }

            if(action == Action.NONE){
                action = RouteDecision.casePlannedRoute(frontBlock, leftBlock, rightBlock, backBlock);
            }

            if(action == Action.NONE){
                action = RouteDecision.caseNoRoute(frontBlock, leftBlock, rightBlock, backBlock);
                if(action != Action.NONE){
                    map.mark(actualPos, BlockType.DEAD_END);
                }
            }

            if(action == Action.NONE){
                Pathfinding.planRoute(map, robotEnvironment.getPos(), this);
                action = RouteDecision.casePlannedRoute(frontBlock, leftBlock, rightBlock, backBlock);
            }


            switch (action){
                case MOVE_FORWARD:
                    CleverUtil.generateDebugInfo(map, this);
                    robotInterface.queueMoveForward();
                    break;
                case MOVE_BACKWARD:
                    CleverUtil.generateDebugInfo(map, this);
                    robotInterface.queueMoveBackward();
                    break;
                case MOVE_LEFT:
                    CleverUtil.generateDebugInfo(map, this);
                    if(cartesian) {
                        robotInterface.queueMoveLeft();
                    } else {
                        robotInterface.queueTurnLeft();
                        if(run) {
                            CleverUtil.generateDebugInfo(map, this);
                            robotInterface.queueMoveForward();
                        }
                    }
                    break;
                case MOVE_RIGHT:
                    CleverUtil.generateDebugInfo(map, this);
                    if(cartesian) {
                        robotInterface.queueMoveRight();
                    } else {
                        robotInterface.queueTurnRight();
                        if(run) {
                            CleverUtil.generateDebugInfo(map, this);
                            robotInterface.queueMoveForward();
                        }
                    }
                    break;

                case TURN_AROUND:
                    CleverUtil.generateDebugInfo(map, this);
                    robotInterface.queueTurnLeft();
                    if(run) {
                        CleverUtil.generateDebugInfo(map, this);
                        robotInterface.queueTurnLeft();
                    }
                    break;
                case TURN_LEFT:
                    CleverUtil.generateDebugInfo(map, this);
                    robotInterface.queueTurnLeft();
                    break;
                case TURN_RIGHT:
                    CleverUtil.generateDebugInfo(map, this);
                    robotInterface.queueTurnRight();
                    break;
                case NONE:
                    CleverUtil.generateDebugErrorMessage("Robot action not specified", this);
                    robotInterface.doNothing();
            }

        }
    }

    public boolean getHintTop(){
        return preferTop || !preferBottom && Math.random() > 0.5f;
    }
    public boolean getHintLeft(){
        return preferLeft || !preferRight && Math.random() > 0.5f;
    }
    public boolean getHintHorizontal(){
        return preferHorizontal || !preferVertical && Math.random() > 0.5f;
    }

    @Override
    public void stop() {
        this.run = false;
    }
}
