package lego.simulator

import java.io.File
import java.util

import com.google.common.base.Charsets
import com.google.common.io.Files
import lego.api.controllers.EnvironmentController

/**
 * Private property.
 * User: Darkyen
 * Date: 24/10/14
 * Time: 15:25
 */
class MazeMap {
  val maze:Array[Array[MapTile]] = Array.fill(EnvironmentController.mazeWidth,EnvironmentController.mazeHeight)(MapTile.FREE)

  //Init basic tiles.
  import EnvironmentController.{startX,startY}
  maze(startX)(startY) = MapTile.START
  maze(startX - 1)(startY) = MapTile.OBSTACLE
  maze(startX + 1)(startY) = MapTile.OBSTACLE
  maze(startX)(startY + 1) = MapTile.FREE

  def toPrintableString:String = {
    val result = new StringBuilder
    result.append("+").append("-" * EnvironmentController.mazeWidth).append("+").append('\n')
    for(y <- 0 until EnvironmentController.mazeHeight){
      result.append('|')
      for(x <- 0 until EnvironmentController.mazeWidth){
        result.append(maze(x)(y))
      }
      result.append('|')
      result.append('\n')
    }
    result.append("+").append("-" * EnvironmentController.mazeWidth).append("+")
    result.toString()
  }

  def apply(x:Int,y:Int):MapTile = {
    if(x < 0 || y < 0 || x >= EnvironmentController.mazeWidth || y >= EnvironmentController.mazeHeight){
      MapTile.OBSTACLE
    }else{
      maze(x)(y)
    }
  }
}

object MazeMap {
  def apply(from:File):MazeMap = {
    apply(scala.collection.convert.wrapAsScala.iterableAsScalaIterable(Files.readLines(from,Charsets.UTF_8)))
  }

  def apply(lines:Iterable[String]):MazeMap = {
    val maze = lines.take(EnvironmentController.mazeHeight).map(_.take(EnvironmentController.mazeWidth).map({
      case 'O' => MapTile.OBSTACLE
      case ' ' | 'F' => MapTile.FREE
      case 'S' => MapTile.START
    }).toArray).toArray

    val result = new MazeMap

    for(x <- 0 until EnvironmentController.mazeWidth; y <- 0 until EnvironmentController.mazeHeight){
      result.maze(x)(y) = maze(y)(x)//BECAUSE
    }
    result
  }
}
