package lego.simulator

import java.io.File

import com.google.common.base.Charsets
import com.google.common.io.Files
import lego.api.Bot
import lego.api.controllers.EnvironmentController
import org.reflections.Reflections

import scala.collection.convert.wrapAll._
import scala.util.Try

/**
 * Private property.
 * User: Darkyen
 * Date: 30/10/14
 * Time: 12:59
 */
object TerminalMain extends App {
  val mapPointerFile = new File("mappointer")

  def readOrDefault(default:String):String = {
    val in = readLine()
    if(in.trim.isEmpty){
      default
    }else {
      in
    }
  }

  def readIntOrDefault(default:Int):Int = {
    var in = readLine()
    while(!in.forall(_.isDigit)){
      println("That is not a valid number. Try again.")
      in = readLine()
    }
    if(in.isEmpty){
      default
    }else{
      in.toInt
    }
  }

  val mapPointerContent = Try(Files.readFirstLine(mapPointerFile,Charsets.UTF_8)).getOrElse("all")
  println("Enter Map Pointer/s ["+mapPointerContent+"]:")

  val maps:Seq[Char] = {
    val input = readOrDefault(mapPointerContent).trim
    if(input == "app"){
      ('1' to '8').toSeq
    }else if(input.isEmpty){
      sys.error("No maps to simulate on specified.")
    }else{
      input.split(" ").map(_.charAt(0))
    }
  }

  val botPackage = new Reflections("lego.bots")
  val availableBots = botPackage.getSubTypesOf(classOf[Bot[_ <: EnvironmentController]]).toSeq.sortBy(_.getSimpleName)

  if(availableBots.isEmpty){
    sys.error("No bots found on classpath. Bots must be placed in lego.bots package and must support EnvironmentController.")
  }
  val lastBot = 1 //TODO load last bot
  println("Choose a bot["+lastBot+"]:")
  for((bot,index) <- availableBots.zipWithIndex){
    println(index +": "+bot.getSimpleName)
  }

  val bot = availableBots(readIntOrDefault(lastBot))

  for(map <- maps){
    Simulator.simulate(bot,map)
  }
}
