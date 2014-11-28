package lego.simulator

import java.io.File

import com.google.common.base.Charsets
import com.google.common.io.Files
import lego.api.{BotEvent, Bot}
import lego.api.controllers.EnvironmentController
import lego.api.controllers.EnvironmentController._
import lego.simulator.controllers.EnvironmentSimulatorController
import lego.util.Latch

/**
 * Private property.
 * User: Darkyen
 * Date: 28/11/14
 * Time: 19:27
 */
object Simulator {

  private val controllerMapPointerFile = new File("mappointer")

  private val onChanged:(Array[Array[MapTile]]) => (EnvironmentSimulatorController) => Unit
  = (maze:Array[Array[MapTile]]) => (controller:EnvironmentSimulatorController) => {
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
          result.append((mindMaze(x)(y) & 0xC0).toByte match {
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

  private val onError = (error:Byte) => {
    println("On Error: "+error)
  }

  /**
   * Simulates given bot on a given map.
   * Blocks until complete.
   */
  def simulate(botClass:Class[_ <: Bot[_ <: EnvironmentController]],mapName:Char): Unit ={
    val map = MazeMap(mapName)

    println("Loaded map \""+mapName+"\"")
    println(map.toPrintableString)
    println()

    Files.write(mapName.toString,controllerMapPointerFile,Charsets.UTF_8)

    val bot = botClass.newInstance().asInstanceOf[Bot[EnvironmentSimulatorController]]

    val controller = new EnvironmentSimulatorController(map,onChanged(map.maze),onError)

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
    robotThread.join()//Block until stops
  }
}
