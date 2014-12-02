package lego.simulator.controllers

import lego.api.controllers.MapAwareController._
import lego.api.controllers.{MapAwareController, PlannedController}
import lego.simulator.{MazeMap, Simulator, MapTile}

/**
 * Private property.
 * User: Darkyen
 * Date: 02/12/14
 * Time: 17:53
 */
class PlannedSimulatorController(map:MazeMap,onStatusChanged:(PlannedSimulatorController)=>Unit,val onError:(Byte)=>Unit) extends PlannedController {

  override def travelX(xMove: Byte): Byte = {
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
          if(Math.abs(remaining) < 100 - MapAwareController.mazeWidth) {
            Simulator.printMessage("Collides: " + {
              if (direction > 0) "RIGHT" else "LEFT"
            } + ", " + Math.abs(remaining) + " to go.")
          }
          remaining = 0
      }
    }
    onStatusChanged(this)
    moved.toByte
  }

  override def travelY(yMove: Byte): Byte = {
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
            Simulator.printMessage("Collides: DOWN, " + Math.abs(remaining) + " to go.")
            remaining = 0
          }
        case MapTile.FREE =>
          if (direction < 0 && map(x, y) == MapTile.START) {
            Simulator.printMessage("Collides: UP, " + Math.abs(remaining) + " to go.")
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
          if(Math.abs(remaining) < 100 - MapAwareController.mazeHeight) { // Move until obstacle moves by 100 fields
            Simulator.printMessage("Collides: " + {
              if (direction > 0) "DOWN" else "UP"
            } + ", " + Math.abs(remaining) + " to go.")
          }
          remaining = 0
      }
    }
    onStatusChanged(this)
    moved.toByte
  }

  override def onError(error: Byte): Unit = onError.apply(error)
}
