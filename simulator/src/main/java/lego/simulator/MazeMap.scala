package lego.simulator

import lego.api.controllers.EnvironmentController

/**
 * Private property.
 * User: Darkyen
 * Date: 24/10/14
 * Time: 15:25
 */
class MazeMap {
  val maze:Array[Array[MapTile]] = Array.fill(EnvironmentController.mazeWidth,EnvironmentController.mazeWidth)(MapTile.FREE)

  //Init basic tiles.
  import EnvironmentController.{startX,startY}
  maze(startX)(startY) = MapTile.START
  maze(startX - 1)(startY) = MapTile.OBSTACLE
  maze(startX + 1)(startY) = MapTile.OBSTACLE
  maze(startX)(startY + 1) = MapTile.FREE

}
