package lego.simulator

import java.io.File

import lego.api.controllers.EnvironmentController
import lego.api.controllers.EnvironmentController.FieldStatus
import lego.api.{BotEvent, AbstractBootstrap, Bot}
import lego.simulator.controllers.EnvironmentSimulatorController

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



  val defaultMap = "defaultmap.txt"
  println("Enter Map Path ["+defaultMap+"]:")
  val map = MazeMap(new File({
    val in = readLine()
    if(in.isEmpty){
      defaultMap
    }else in
  }))

  val onChanged = () => {
    val maze = map.maze
    val mindMaze:Array[Array[FieldStatus]] = controller.getMinMaze

    val result = new StringBuilder
    result.append("+").append("-" * (EnvironmentController.mazeWidth*3)).append("+").append(" +").append("-" * (EnvironmentController.mazeWidth*3)).append("+").append('\n')
    for(y <- 0 until EnvironmentController.mazeHeight){
      result.append('|')
      for(x <- 0 until EnvironmentController.mazeWidth){
        if(controller.getX == x && controller.getY == y){
          result.append(" X ")
        }else{
          result.append(" "+maze(x)(y)+" ")
        }
      }
      result.append("| |")
      for(x <- 0 until EnvironmentController.mazeWidth){
        if(controller.getX == x && controller.getY == y){
          result.append(" X ")
        }else{
          result.append(mindMaze(x)(y) match {
            case FieldStatus.FREE_UNVISITED => " U "
            case FieldStatus.FREE_VISITED => "   "
            case FieldStatus.OBSTACLE => " O "
            case FieldStatus.START => " S "
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
  val defaultRobotMain = "lego.bots.random.RandomBot"
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

  bot.onEvent(BotEvent.ENTER_PRESSED)
}
