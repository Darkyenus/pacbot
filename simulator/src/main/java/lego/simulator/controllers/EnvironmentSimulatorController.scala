package lego.simulator.controllers

import lego.api.controllers.EnvironmentController
import lego.api.controllers.EnvironmentController._
import lego.simulator.{Simulator, MapTile, MazeMap}

/**
 * Private property.
 * User: Darkyen
 * Date: 24/10/14
 * Time: 15:24
 */
class EnvironmentSimulatorController(map:MazeMap,onStatusChanged:(EnvironmentSimulatorController)=>Unit,val onError:(Byte)=>Unit) extends EnvironmentController {

  override def moveByXAsync(xMove: Byte): MoveFieldsTask = {
    val direction:Byte = xMove.signum.toByte
    var remaining = xMove
    var moved = 0

    while(remaining != 0){ //Used to work only in positive direction
      val nextX:Byte = (x + direction).toByte
      map(nextX,y) match {
        case MapTile.START =>
          x = nextX
          moved += 1
          //No exploring, this is known
          remaining = (remaining - direction).toByte
        case MapTile.FREE =>
          x = nextX
          moved += 1
          setField(x,y,FREE_VISITED)
          remaining = (remaining - direction).toByte
        case MapTile.OBSTACLE =>
          //No moving
          setField(nextX,y,OBSTACLE)
          if(Math.abs(remaining) < 100 - EnvironmentController.mazeWidth) {
            Simulator.printMessage("Collides: " + {
              if (direction > 0) "RIGHT" else "LEFT"
            } + ", " + Math.abs(remaining) + " to go.");
          }
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
          if (direction < 0) {
            y = nextY.toByte
            moved += 1

            //No exploring, this is known
            remaining = (remaining - direction).toByte
          } else {
            Simulator.printMessage("Collides: DOWN, " + Math.abs(remaining) + " to go.");
            remaining = 0
          }
        case MapTile.FREE =>
          if (direction < 0 && map(x, y) == MapTile.START) {
            Simulator.printMessage("Collides: UP, " + Math.abs(remaining) + " to go.");
            remaining = 0
          } else {
            y = nextY.toByte
            moved += 1
            setField(x, y, FREE_VISITED)
            remaining = (remaining - direction).toByte
          }
        case MapTile.OBSTACLE =>
          //No moving
          setField(x,nextY.toByte,OBSTACLE)
          if(Math.abs(remaining) < 100 - EnvironmentController.mazeHeight) { // Move until obstacle moves by 100 fields
            Simulator.printMessage("Collides: " + {
              if (direction > 0) "DOWN" else "UP"
            } + ", " + Math.abs(remaining) + " to go.");
          }
          remaining = 0
      }
    }

    new MockMoveFieldTask(moved.toByte)
  }

  private class MockMoveFieldTask(movedFields:Byte) extends MoveFieldsTask {

    onStatusChanged(EnvironmentSimulatorController.this)

    override def isDone: Boolean = true

    override def waitUntilDone(): Unit = {}

    override def moved(): Byte = movedFields
  }

  override def onError(error: Byte): Unit = onError.apply(error)
}
