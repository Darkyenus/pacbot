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
  val lastBotFile = new File("target/lastbotpointer")

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
    if(input == "all"){
      ('0' to '9').toSeq
    }else if(input.contains("to")){
      val pre = input.charAt(0) //Ugly workaround to be space insensitive
      val post = input.charAt(input.length - 1)
      (pre to post).toSeq
    }else if(input.isEmpty){
      sys.error("No maps to simulate on specified.")
    }else{
      input.replaceAll("\\s","") //Remove all whitespace (Unlikely to be name of map)
    }
  }

  val botPackage = new Reflections("lego.bots")
  val availableBots = botPackage.getSubTypesOf(classOf[Bot[_ <: EnvironmentController]]).toSeq.sortBy(_.getSimpleName)

  if(availableBots.isEmpty){
    sys.error("No bots found on classpath. Bots must be placed in lego.bots package and must support EnvironmentController.")
  }
  val lastBot = {
    if(lastBotFile.isFile){
      Files.readFirstLine(lastBotFile,Charsets.UTF_8).toInt //Load last selected bot
    }else{
      0
    }
  }
  println("Choose a bot ["+lastBot+"]:")
  for((bot,index) <- availableBots.zipWithIndex){
    println(index +": "+bot.getSimpleName)
  }
  println()

  val selectedBot = readIntOrDefault(lastBot)
  val bot = availableBots(selectedBot)
  Files.write(selectedBot.toString,lastBotFile,Charsets.UTF_8) //Save last selected bot

  for(map <- maps){
    Simulator.simulate(bot,map)
  }
}
