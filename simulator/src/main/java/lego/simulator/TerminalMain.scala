package lego.simulator

import java.io
import java.io.FileInputStream

import lego.api.controllers.EnvironmentController
import lego.api.controllers.EnvironmentController._
import lego.api.{Bot, BotEvent}
import lego.simulator.controllers.EnvironmentSimulatorController
import lego.util.Latch
import org.reflections.Reflections
import scala.collection.convert.wrapAll._

/**
 * Private property.
 * User: Darkyen
 * Date: 30/10/14
 * Time: 12:59
 */
object TerminalMain extends App {

  def readOrDefault(default:String):String = {
    val in = readLine()
    if(in.trim.isEmpty){
      default
    }else {
      in
    }
  }

  val defaultMap = new FileInputStream(new io.File("mappointer")).read().toChar.toString
  println("Enter Map Pointer ["+defaultMap+"]:")

  val map = MazeMap(readOrDefault(defaultMap))

  val onChanged:(EnvironmentSimulatorController) => Unit = (controller:EnvironmentSimulatorController) => {
    val maze = map.maze
    val mindMaze:Array[Array[Byte]] = controller.getMindMaze

    val result = new StringBuilder
    result.append("+").append("-" * (EnvironmentController.mazeWidth*3)).append("+").append(" +").append("-" * (EnvironmentController.mazeWidth*3)).append("+").append('\n')
    for(y <- 0 until EnvironmentController.mazeHeight){
      result.append('|')
      for(x <- 0 until EnvironmentController.mazeWidth){
        if(controller.getX == x && controller.getY == y){
          result.append("(-)")
        }else{
          result.append(" "+maze(x)(y)+" ")
        }
      }
      result.append("| |")
      for(x <- 0 until EnvironmentController.mazeWidth){
        if(controller.getX == x && controller.getY == y){
          result.append("(-)")
        }else{
          result.append(mindMaze(x)(y) match {
            case FREE_UNVISITED => " o "
            case FREE_VISITED => "   "
            case OBSTACLE => "[X]"
            case START => " v "
          })
        }
      }
      result.append('|')
      result.append('\n')
    }
    result.append("+").append("-" * (EnvironmentController.mazeWidth*3)).append("+").append(" +").append("-" * (EnvironmentController.mazeWidth*3)).append("+")

    println(result.toString())
    readLine()
  }

  val onError = (error:Byte) => {
    println("On Error: "+error)
  }

  println(map.toPrintableString)
  println()

  val botPackage = new Reflections("lego.bots")
  val availableBots = botPackage.getSubTypesOf(classOf[Bot[_ <: EnvironmentController]]).toSeq

  if(availableBots.nonEmpty){
    println("Available bots: ")
  }
  for((bot,index) <- availableBots.zipWithIndex){
    println(index+": "+bot.getSimpleName)
  }

  val defaultRobotMain = "lego.bots.clever.CleverBot"
  println("Enter robot main ["+defaultRobotMain+"]:")

  val bot = Class.forName({
    val result = readOrDefault(defaultRobotMain)
    if(result.forall(_.isDigit)){
      availableBots(result.toInt).getCanonicalName
    }else{
      result
    }
  }).newInstance().asInstanceOf[Bot[EnvironmentSimulatorController]]

  val controller = new EnvironmentSimulatorController(map,onChanged,onError)

  val initLatch = new Latch()

  val robotThread = new Thread(){
    override def run(): Unit = {
      controller.initialize()
      bot.controller = controller
      initLatch.open()
      bot.run()
      controller.deinitialize()
    }
  }
  robotThread.setDaemon(false)
  robotThread.setName("RobotThread")
  robotThread.start()

  initLatch.pass() //Wait for bot to initialize. Should be instant.

  println("Preparing run.\n")
  val now = System.currentTimeMillis()
  bot.onEvent(BotEvent.RUN_PREPARE)
  println("\nRun prepared in " + (((System.currentTimeMillis() - now) / 100) / 10f) + "s.\n")
  bot.onEvent(BotEvent.RUN_STARTED)
}
