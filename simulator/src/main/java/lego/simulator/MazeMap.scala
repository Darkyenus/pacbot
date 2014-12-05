package lego.simulator

import java.io.{File, FileInputStream, FileNotFoundException, IOException}

import com.google.common.base.Charsets
import com.google.common.io.Files
import lego.api.controllers.EnvironmentController
import lego.api.controllers.MapAwareController._

/**
 * Private property.
 * User: Darkyen
 * Date: 24/10/14
 * Time: 15:25
 */
class MazeMap {
  val maze:Array[Array[MapTile]] = Array.fill(mazeWidth,mazeHeight)(MapTile.FREE)

  //Init basic tiles.
  maze(startX)(startY) = MapTile.START
  maze(startX - 1)(startY) = MapTile.OBSTACLE
  maze(startX + 1)(startY) = MapTile.OBSTACLE
  maze(startX)(startY + 1) = MapTile.FREE

  def toPrintableString:String = {
    val result = new StringBuilder
    result.append("+").append("-" * mazeWidth * 3).append("+").append('\n')
    for(y <- 0 until mazeHeight){
      result.append('|')
      for(x <- 0 until mazeWidth){
        result.append(" "+maze(x)(y)+" ")
      }
      result.append('|')
      result.append('\n')
    }
    result.append("+").append("-" * mazeWidth * 3).append("+")
    result.toString()
  }

  def apply(x:Int,y:Int):MapTile = {
    if(x < 0 || y < 0 || x >= mazeWidth || y >= mazeHeight){
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
  def apply(mapIndex:Char):MazeMap = {
    val map = loadSavedMap(mapIndex)
    if(map.isDefined) {
      val result = new MazeMap

      for (x <- 0 until mazeWidth; y <- 0 until mazeHeight) {
        result.maze(x)(y) = map.get(x)(y) //BECAUSE
      }
      result
    }else{
      null
    }
  }

  private def loadSavedMap(mapName: Int): Option[Array[Array[MapTile]]] = {
    var input: FileInputStream = null
    var readingName: Boolean = false
    var readingMap: Boolean = false
    var fieldToRead: Int = 0
    val maze:Array[Array[MapTile]] = Array.fill(mazeWidth,mazeHeight)(MapTile.FREE)
    try {
      val mapsFile: File = new File("maps")
      input = new FileInputStream(mapsFile)
      var i: Int = input.read
      val PRENAME_CHAR: Int = '#'
      val OBSTACLE_CHAR: Int = 'X'
      val FREE_SPACE_CHAR: Int = '_'
      val START_CHAR: Int = 'S'
      val FIELDS_TO_READ: Int = mazeWidth * mazeHeight
      while (i != -1) {
        if (readingMap) {
          i match {
            case OBSTACLE_CHAR =>
              maze(fieldToRead % mazeWidth)(fieldToRead / mazeWidth) = MapTile.OBSTACLE
              fieldToRead += 1
            case FREE_SPACE_CHAR =>
              maze(fieldToRead % mazeWidth)(fieldToRead / mazeWidth) = MapTile.FREE
              fieldToRead += 1
            case START_CHAR =>
              maze(fieldToRead % mazeWidth)(fieldToRead / mazeWidth) = MapTile.START
              fieldToRead += 1
            case _ =>
          }
          if (fieldToRead == FIELDS_TO_READ) {
            if (maze(startX)(startY) != MapTile.START) {
              println("ERROR: map loading invalid start")
            }
            return Some(maze)
          }
        }else if (readingName) {
          if (i == mapName) {
            readingMap = true
          }
        }else {
          if (i == PRENAME_CHAR) {
            readingName = true
          }
        }
        i = input.read
      }
    } catch {
      case notFound: FileNotFoundException =>
        println("ERROR: Loading file not found")
      case e: IOException =>
        println("ERROR: Loading IO exception")
    } finally {
      if (input != null) {
        try {
          input.close()
        }
        catch {
          case ignored: Throwable =>
        }
      }
    }
    if (readingMap || readingName) {
      println("ERROR: File corrupted")
    }else {
      println("ERROR: Map not found")
    }
    None
  }

  def apply(lines:Iterable[String]):MazeMap = {
    val maze = lines.take(mazeHeight).map(_.take(mazeWidth).map({
      case 'O' => MapTile.OBSTACLE
      case ' ' | 'F' => MapTile.FREE
      case 'S' => MapTile.START
    }).toArray).toArray

    val result = new MazeMap

    for(x <- 0 until mazeWidth; y <- 0 until mazeHeight){
      result.maze(x)(y) = maze(y)(x)//BECAUSE
    }
    result
  }
}
