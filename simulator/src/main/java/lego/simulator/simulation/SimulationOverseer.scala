package lego.simulator.simulation

import lego.api.{AbstractBootstrap, Bot}
import lego.simulator.MazeMap
import lego.simulator.controllers.EnvironmentSimulatorController

/**
 * Private property.
 * User: Darkyen
 * Date: 25/10/14
 * Time: 11:18
 */
class SimulationOverseer {
  var mazeMap:MazeMap = _
  var bot:Bot[EnvironmentSimulatorController] = _
  var controller:EnvironmentSimulatorController = _

  def initialize(): Unit ={
    AbstractBootstrap.main(bot,controller)
  }

  def onStatusChanged(): Unit ={

  }

  def onError(error:Byte){}
}

object SimulationOverseer {
  def apply(bot:String,controller:String,mazeMap:MazeMap,onStatusChanged:()=>Unit,onError:(Byte)=>Unit):Either[String,SimulationOverseer] = {
    try{
      val result = new SimulationOverseer
      result.mazeMap = mazeMap
      val botClass = Class.forName(bot)
      val controllerClass = Class.forName(controller)

      result.bot = botClass.newInstance().asInstanceOf[Bot[EnvironmentSimulatorController]]

      //SimulatorController constructor: map:MazeMap,onStatusChanged:()=>Unit,val onError:(Byte)=>Unit
      result.controller = controllerClass.getConstructor(classOf[MazeMap],classOf[()=>Unit],classOf[(Byte)=>Unit]).newInstance(mazeMap,()=>{result.onStatusChanged()},(err:Byte)=>{result.onError(err)}).asInstanceOf[EnvironmentSimulatorController]

      Right(result)
    }catch{
      case exception:Exception =>
        Left(exception.toString)
    }
  }
}
