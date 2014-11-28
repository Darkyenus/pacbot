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

  private val MapViewWidth = EnvironmentController.mazeWidth*3 + 2
  private val MapViewHeight = EnvironmentController.mazeHeight + 2
  private val MessagesViewWidth = 40
  private val printGrid = new PrintGrid(2 * MapViewWidth + MessagesViewWidth + 4,MapViewHeight)
  prepareMassageFrame()

  def prepareMassageFrame(): Unit ={
    printGrid.setSubgrid(MapViewWidth*2+4,0,MessagesViewWidth,MapViewHeight)
    printGrid.frameSubgrid(" Messages ")
    printGrid.clear()
  }

  private val onChanged:(Array[Array[MapTile]]) => (EnvironmentSimulatorController) => Unit
  = (maze:Array[Array[MapTile]]) => (controller:EnvironmentSimulatorController) => {
    val mindMaze:Array[Array[Byte]] = controller.getMindMaze

    printGrid.setSubgrid(0,0,MapViewWidth,MapViewHeight)
    printGrid.frameSubgrid(" Overview ")
    for(y <- 0 until EnvironmentController.mazeHeight){
      for(x <- 0 until EnvironmentController.mazeWidth){
        if(controller.getX == x && controller.getY == y){
          printGrid.print("(-)")
        }else{
          printGrid.print(" "+maze(x)(y)+" ")
        }
      }
    }
    printGrid.setSubgrid(MapViewWidth+2,0,MapViewWidth,MapViewHeight)
    printGrid.frameSubgrid(" Bot Memory ")
    for(y <- 0 until EnvironmentController.mazeHeight) {
      for (x <- 0 until EnvironmentController.mazeWidth) {
        if (controller.getX == x && controller.getY == y) {
          printGrid.print("(-)")
        } else {
          printGrid.print((mindMaze(x)(y) & 0xC0).toByte match {
            case FREE_UNVISITED => " o "
            case FREE_VISITED => "   "
            case OBSTACLE => "[X]"
            case START => " v "
          })
        }
      }
    }

    printGrid.printOut()
    printGrid.clear()
    prepareMassageFrame()

    readLine()
  }

  private val onError = (error:Byte) => {
    error match {
      case ERROR_SET_OUT_OF_BOUNDS =>
        printGrid.println("ERR: Set Out of bounds")
      case ERROR_SET_DEFINITIVE =>
        printGrid.println("ERR: Set Definitive")
      case ERROR_CAL_BLOCK_EXPECTED =>
        printGrid.println("ERR: Calibration block expected")
      case ERROR_STUCK_IN_LOOP =>
        printGrid.println("ERR: Stuck in loop")
      case ERROR_LOADING_INVALID_START =>
        printGrid.println("ERR: Load: Invalid start")
      case ERROR_LOADING_FILE_NOT_FOUND =>
        printGrid.println("ERR: Load: maps file not found")
      case ERROR_LOADING_MAP_NOT_FOUND =>
        printGrid.println("ERR: Load: Map not found")
      case ERROR_LOADING_MAP_CORRUPTED =>
        printGrid.println("ERR: Load: Map corrupted")
      case ERROR_LOADING_IOEXCEPTION =>
        printGrid.println("ERR: Load: IO Exception")
      case ERROR_LOADING_POINTER_FILE_MISSING =>
        printGrid.println("ERR: Load: Pointer file missing")
      case ERROR_LOADING_POINTER_FILE_CORRUPTED =>
        printGrid.println("ERR: Load: Pointer file corrupted")
      case WARNING_TOOK_TOO_LONG_TIME_TO_COMPUTE =>
        printGrid.println("WRN: Took too long to compute")
      case WARNING_ALERT =>
        printGrid.println("WRN: ALERT!")
      case SUCCESS_PATH_COMPUTED =>
        printGrid.println("Success: Path computed")
      case _ =>
        printGrid.println("ERROR: "+error)
    }
  }

  /**
   * Simulates given bot on a given map.
   * Blocks until complete.
   */
  def simulate(botClass:Class[_ <: Bot[_ <: EnvironmentController]],mapName:Char): Unit ={
    val map = MazeMap(mapName)

    printGrid.println("Loaded map \""+mapName+"\"")
    printGrid.println()

    Files.write(mapName.toString,controllerMapPointerFile,Charsets.UTF_8)

    val bot = botClass.newInstance().asInstanceOf[Bot[EnvironmentSimulatorController]]

    val controller = new EnvironmentSimulatorController(map,onChanged(map.maze),onError)

    val initLatch = new Latch()

    val robotThread = new Thread(){
      override def run(): Unit = {
        try {
          controller.initialize()
          bot.controller = controller
          initLatch.open()
          bot.run()
          controller.deinitialize()
        } catch {
          case e:Exception =>
            println(s"Bot ${botClass.getCanonicalName} threw an exception on map $mapName:")
            e.printStackTrace()
        }
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
