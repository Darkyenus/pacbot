package lego.simulator

import java.io.{IOException, FileNotFoundException, FileInputStream, File}

import lego.api.controllers.EnvironmentController
import lego.api.controllers.EnvironmentController.FieldStatus
import lego.api.{BotEvent, AbstractBootstrap, Bot}
import lego.simulator.controllers.EnvironmentSimulatorController
import lego.api.controllers.EnvironmentController.FieldStatus._
import java.io

/**
 * Private property.
 * User: Darkyen
 * Date: 30/10/14
 * Time: 12:59
 */
object TerminalMain extends App {
  def create(bot:String,mazeMap:MazeMap,onStatusChanged:()=>Unit,onError:(Byte)=>Unit):(Bot[EnvironmentSimulatorController],EnvironmentSimulatorController) = {
    val botClass = Class.forName(bot)

    val botInstance = botClass.newInstance().asInstanceOf[Bot[EnvironmentSimulatorController]]

    val controllerInstance = new EnvironmentSimulatorController(mazeMap,onStatusChanged,onError)

    (botInstance,controllerInstance)
  }



  val defaultMap = new FileInputStream(new io.File("mappointer")).read().toChar.toString
  println("Enter Map Pointer ["+defaultMap+"]:")

  val map = MazeMap({
    val in = readLine()
    if(in.isEmpty){
      defaultMap
    }else in
  })

  val onChanged = () => {
    val maze = map.maze
    val mindMaze:Array[Array[FieldStatus]] = controller.getMindMaze

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
            case FieldStatus.FREE_UNVISITED => " o "
            case FieldStatus.FREE_VISITED => "   "
            case FieldStatus.OBSTACLE => "[X]"
            case FieldStatus.START => " v "
            case FieldStatus.UNKNOWN => " ? "
          })
        }
      }
      result.append('|')
      result.append('\n')
    }
    result.append("+").append("-" * (EnvironmentController.mazeWidth*3)).append("+").append(" +").append("-" * (EnvironmentController.mazeWidth*3)).append("+")

    println(result.toString())
    val input = readLine()
  }

  val onError = (error:Byte) => {
    println("On Error: "+error)
  }

  println(map.toPrintableString)
  println()
  val defaultRobotMain = "lego.bots.clever.CleverBot"
  println("Enter robot main ["+defaultRobotMain+"]:")
  val (bot,controller:EnvironmentSimulatorController) = create({
    val in = readLine()
    if(in.isEmpty){
      defaultRobotMain
    }else{
      in
    }
  },map,onChanged,onError)


  val robotThread = new Thread(){
    override def run(): Unit = {
      AbstractBootstrap.main(bot,controller)
    }
  }
  robotThread.setDaemon(false)
  robotThread.setName("RobotThread")
  robotThread.start()

  while(!controller.isInitialized()){
    Thread.sleep(50) //Wait until bot is not initialized
  }
  Thread.sleep(10) //Race condition, but nevermind

  println("Preparing run.\n")
  val now = System.currentTimeMillis()
  bot.onEvent(BotEvent.RUN_PREPARE)
  println("\nRun prepared in " + (((System.currentTimeMillis() - now) / 100) / 10f) + "s.\n")
  Thread.sleep(100)
  bot.onEvent(BotEvent.RUN_STARTED)
}
