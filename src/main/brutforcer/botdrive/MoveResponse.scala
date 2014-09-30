package main.brutforcer.botdrive

import main.robotcontrol.{AbstractRobot, RobotResponse}
import main.robotcontrol.constants.{RelativeHeading, RelativeMovement, AbsoluteMovement, AbsoluteHeading}

/**
 * Created by jIRKA on 30.9.2014.
 */
object MoveResponse{

  /**
   * Absolute movement doesn't touch or check heading stuff!
   * See MoveRespobse.constructTurnResponse
   * @param direction
   * @return
   */
  def constructMoveResponse(direction: AbsoluteMovement): RobotResponse = {
    direction match {
      case AbsoluteMovement.UP => new absMoveUp()
      case AbsoluteMovement.DOWN => new absMoveDown()
      case AbsoluteMovement.LEFT => new absMoveLeft()
      case AbsoluteMovement.RIGHT => new absMoveRight()
    }
  }

  /**
   * Warning: This is entirely about movement, even relative direction.left/right does not rotate but moves
   * See MoveResponse.constructTurnResponse for turning
   * @param direction
   * @return
   */
  def constructMoveResponse(direction: RelativeMovement): RobotResponse = {
    direction match {
      case RelativeMovement.FORWARD => new relMoveForward()
      case RelativeMovement.BACKWARD => new relMoveBackward()
      case RelativeMovement.LEFT => new relMoveLeft()
      case RelativeMovement.RIGHT => new relMoveRight()
    }
  }

  def constructTurnResponse(turnTo: AbsoluteHeading):RobotResponse = {
    new absTurn(turnTo)
  }

  def constructTurnResponse(turnRelative: RelativeHeading, multiply:Int):RobotResponse = {
    new relTurn(turnRelative, multiply)
  }

}

abstract class MoveResponse extends RobotResponse

class absMoveUp () extends MoveResponse{

  override def execute(robot:AbstractRobot){
    if(robot.getEnvironment.getY > 0){
      //TODO collision check
      //In actual robot here should be actual movement
      robot.getEnvironment.moveBy(0,-1)
    }
  }

}
class absMoveDown () extends MoveResponse{

  override def execute(robot:AbstractRobot){
    if(robot.getEnvironment.getY < robot.getEnvironment.mazeHeight - 1){
      //TODO collision check
      //In actual robot here should be actual movement
      robot.getEnvironment.moveBy(0,1)
    }
  }

}
class absMoveLeft () extends MoveResponse{

  override def execute(robot:AbstractRobot){
    if(robot.getEnvironment.getX > 0){
      //TODO collision check
      //In actual robot here should be actual movement
      robot.getEnvironment.moveBy(-1,0)
    }
  }

}
class absMoveRight () extends MoveResponse{

  override def execute(robot:AbstractRobot){
    if(robot.getEnvironment.getX < robot.getEnvironment.mazeWidth - 1){
      //TODO collision check
      //In actual robot here should be actual movement
      robot.getEnvironment.moveBy(1, 0)
    }
  }

}

class relMoveForward () extends MoveResponse{

  override def execute(robot:AbstractRobot){
    robot.getEnvironment.getHeading match {
      case AbsoluteHeading.UP => new absMoveUp().execute(robot)
      case AbsoluteHeading.DOWN => new absMoveDown().execute(robot)
      case AbsoluteHeading.LEFT => new absMoveLeft().execute(robot)
      case AbsoluteHeading.RIGHT => new absMoveRight().execute(robot)
    }
  }

}
class relMoveBackward () extends MoveResponse{

  override def execute(robot:AbstractRobot){
    robot.getEnvironment.getHeading match {
      case AbsoluteHeading.UP => new absMoveDown().execute(robot)
      case AbsoluteHeading.DOWN => new absMoveUp().execute(robot)
      case AbsoluteHeading.LEFT => new absMoveRight().execute(robot)
      case AbsoluteHeading.RIGHT => new absMoveLeft().execute(robot)
    }
  }

}
class relMoveLeft () extends MoveResponse{

  override def execute(robot:AbstractRobot){
    robot.getEnvironment.getHeading match {
      case AbsoluteHeading.UP => new absMoveLeft().execute(robot)
      case AbsoluteHeading.DOWN => new absMoveRight().execute(robot)
      case AbsoluteHeading.LEFT => new absMoveDown().execute(robot)
      case AbsoluteHeading.RIGHT => new absMoveUp().execute(robot)
    }
  }

}
class relMoveRight () extends MoveResponse{

  override def execute(robot:AbstractRobot){
    robot.getEnvironment.getHeading match {
      case AbsoluteHeading.UP => new absMoveRight().execute(robot)
      case AbsoluteHeading.DOWN => new absMoveLeft().execute(robot)
      case AbsoluteHeading.LEFT => new absMoveUp().execute(robot)
      case AbsoluteHeading.RIGHT => new absMoveDown().execute(robot)
    }
  }

}

class relTurn (val deltaHeading:RelativeHeading, val count:Int) extends MoveResponse{

  override  def execute(robot: AbstractRobot){
    //In actual robot here should be actual movement
    robot.getEnvironment.rotateBy(deltaHeading,count)
  }

}
class absTurn (val targetHeading:AbsoluteHeading) extends MoveResponse{

  override  def execute(robot: AbstractRobot){
    //In actual robot here should be actual movement
    robot.getEnvironment.rotateTo(targetHeading)
  }

}