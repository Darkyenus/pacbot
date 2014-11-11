package lego.simulator.controllers

import lego.api.controllers.EnvironmentController
import lego.api.controllers.EnvironmentController.{FieldStatus, MoveFieldsTask}
import lego.simulator.{MapTile, MazeMap}

/**
 * Private property.
 * User: Darkyen
 * Date: 24/10/14
 * Time: 15:24
 */
class EnvironmentSimulatorController(map:MazeMap,onStatusChanged:()=>Unit,val onError:(Byte)=>Unit) extends EnvironmentController {

  override def moveByXAsync(xMove: Byte): MoveFieldsTask = {
    val direction:Byte = xMove.signum.toByte
    var remaining = xMove
    var moved = 0

    while(remaining != 0){ //Used to work only in positive direction
      val nextX = x + direction
      map(nextX,y) match {
        case MapTile.START =>
          x = nextX.toByte
          moved += 1
          //No exploring, this is known
          remaining = (remaining - direction).toByte
        case MapTile.FREE =>
          x = nextX.toByte
          moved += 1
          setField(x,y,FieldStatus.OBSTACLE)
          maze(x)(y) = FieldStatus.FREE_VISITED
          remaining = (remaining - direction).toByte
        case MapTile.OBSTACLE =>
          //No moving
          setField(nextX.toByte,y,FieldStatus.OBSTACLE)
          remaining = 0
      }
    }

    new MockMoveFieldTask(moved.toByte)
  }

  override def moveByYAsync(yMove: Byte): MoveFieldsTask = {
    val direction:Byte = yMove.signum.toByte
    var remaining = yMove
    var moved = 0

    while(remaining != 0){ //Used to work only in positive direction
      val nextY = y + direction

      map(x,nextY) match {
        case MapTile.START =>
          y = nextY.toByte
          moved += 1
          //No exploring, this is known
          remaining = (remaining - direction).toByte
        case MapTile.FREE =>
          y = nextY.toByte
          moved += 1
          setField(x,y,FieldStatus.OBSTACLE)
          maze(x)(y) = FieldStatus.FREE_VISITED
          remaining = (remaining - direction).toByte
        case MapTile.OBSTACLE =>
          //No moving
          setField(x,nextY.toByte,FieldStatus.OBSTACLE)
          remaining = 0
      }
    }

    new MockMoveFieldTask(moved.toByte)
  }

  private class MockMoveFieldTask(movedFields:Byte) extends MoveFieldsTask {

    onStatusChanged()

    override def isDone: Boolean = true

    override def waitUntilDone(): Unit = {}

    override def moved(): Byte = movedFields
  }

  override protected def onError(error: Byte): Unit = onError.apply(error)
}
