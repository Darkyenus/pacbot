package lego.simulator

import java.io.File
import java.lang.reflect.ParameterizedType
import java.util.concurrent.Semaphore

import com.google.common.base.Charsets
import com.google.common.io.Files
import lego.api.controllers.MapAwareController
import lego.api.controllers.MapAwareController._
import lego.api.{BotController, Bot, BotEvent}
import lego.util.Latch
import org.reflections.Reflections

import scala.concurrent.util.Unsafe

/**
 * Private property.
 * User: Darkyen
 * Date: 28/11/14
 * Time: 19:27
 */
object Simulator {

  private val controllerMapPointerFile = new File("mappointer")

  private val MapViewWidth = MapAwareController.mazeWidth * 3 + 2
  private val MapViewHeight = MapAwareController.mazeHeight + 2
  private val MessagesViewWidth = 40
  private val printGrid = new PrintGrid(2 * MapViewWidth + MessagesViewWidth + 4, MapViewHeight)
  prepareMassageFrame()

  def prepareMassageFrame(): Unit = {
    printGrid.setSubgrid(MapViewWidth * 2 + 4, MapViewHeight - 1, MessagesViewWidth, 1)
    printGrid.print("Q:uit F:astForward S:ilentFastForward")
    printGrid.setSubgrid(MapViewWidth * 2 + 4, 0, MessagesViewWidth, MapViewHeight - 1)
    printGrid.frameSubgrid(" Messages ")
    printGrid.clear()
    printGrid.offsetGridInwards(1, 1, 0, 0)
  }

  def printMessage(message: CharSequence) {
    printGrid.println(message)
  }

  private val onChanged: (Array[Array[MapTile]],Boolean) => (BotController) => Unit
  = {
    (maze: Array[Array[MapTile]],sFastForward:Boolean) => {
      var fastForward = false
      var silentFastForward = sFastForward
      (botController: BotController) => {
        if (!silentFastForward) {
          botController match {
            case controller: MapAwareController =>
              val mindMaze: Array[Array[Byte]] = controller.getMindMaze

              printGrid.setSubgrid(0, 0, MapViewWidth, MapViewHeight)
              printGrid.frameSubgrid(" Map ")
              for (y <- 0 until MapAwareController.mazeHeight) {
                for (x <- 0 until MapAwareController.mazeWidth) {
                  if (controller.getX == x && controller.getY == y) {
                    printGrid.print("(-)")
                  } else {
                    printGrid.print(" " + maze(x)(y) + " ")
                  }
                }
              }
              printGrid.setSubgrid(MapViewWidth + 2, 0, MapViewWidth, MapViewHeight)
              printGrid.frameSubgrid(" Bot Memory ")
              for (y <- 0 until MapAwareController.mazeHeight) {
                for (x <- 0 until MapAwareController.mazeWidth) {
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
            case nonMapAwareController =>
              printGrid.setSubgrid(0, 0, MapViewWidth * 2 + 2, MapViewHeight)
              printGrid.frameSubgrid("")
              printGrid.clear()

              printGrid.offsetGridInwards(5, 0, MapViewHeight / 3, 0)
              printGrid.print("No map info available.")
          }


          printGrid.printOut()
          printGrid.clear()
          prepareMassageFrame()

          if (!fastForward)
            readLine() match {
              case "q" | "Q" | "quit" =>
                throw SimulatorEndThrowable //This is being catched up there somewhere
              case "f" | "F" | "fastforward" | "skip" =>
                fastForward = true
              case "s" | "S" | "silentfastforward" =>
                silentFastForward = true
              case _ =>
              //Continue normally
            }
        }
      }
    }
  }

  private val onError = (error: Byte) => {
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
        printGrid.println("ERROR: " + error)
    }
  }

  private val simulatorControllerPackage = new Reflections("lego.simulator.controllers")

  private final val SimulateLock = new Semaphore(1)

  /**
   * Simulates given bot on a given map.
   * Blocks until complete.
   */
  def simulate(botClass: Class[_ <: Bot[_ <: BotController]], mapName: Char, parallel:Boolean = false): Unit = {
    val map = MazeMap(mapName)

    if(map != null) {

      printGrid.println("Loaded map \"" + mapName + "\"")
      printGrid.println()

      var bot: Bot[BotController] = null
      var robotThread:Thread = null

      SimulateLock.acquire()
        println("SyncEnter "+Thread.currentThread().getName+" on "+SimulateLock)
        //This must happen sequentially, so controller can load that file. It is stupid, but it works. In theory.
        Files.write(mapName.toString, controllerMapPointerFile, Charsets.UTF_8)

        bot = botClass.newInstance().asInstanceOf[Bot[BotController]]

        //http://stackoverflow.com/questions/3403909/get-generic-type-of-class-at-runtime > Magic
        val botControllerBase = botClass.getGenericSuperclass.asInstanceOf[ParameterizedType].getActualTypeArguments()(0).asInstanceOf[Class[_]]
        val possibleControllers = simulatorControllerPackage.getSubTypesOf(botControllerBase)
        if (possibleControllers.isEmpty) {
          sys.error("No controllers that extend " + botControllerBase.getCanonicalName + " found. Please make some.")
        } else if (possibleControllers.size() > 1) {
          println("WARNING: More than one controller that extends " + botControllerBase.getCanonicalName + " found. Using first one found.")
        }

        val controllerClass = possibleControllers.iterator().next()
        val constrollerClassConstructor = controllerClass.getConstructor(classOf[MazeMap], classOf[(BotController => Unit)], classOf[(Byte) => Unit])

        val controller: BotController = constrollerClassConstructor.newInstance(map, onChanged(map.maze,parallel), onError).asInstanceOf[BotController]
        //Above code is extremely type safe and nothing can go wrong. Ever.

        val initLatch = new Latch()

        robotThread = new Thread() {
          override def run(): Unit = {
            try {
              controller.initialize()
              bot.controller = controller
              initLatch.open()
              try {
                bot.run()
              } catch {
                case SimulatorEndThrowable =>
                  println("Simulation stopped.")
              }

              controller.deinitialize()
            } catch {
              case e: Exception =>
                println(s"Bot ${botClass.getCanonicalName} threw an exception on map $mapName:")
                e.printStackTrace()
            }
          }
        }
        robotThread.setDaemon(false)
        robotThread.setName("RobotThread")
        robotThread.start()

        initLatch.pass() //Wait for bot to initialize. Should be instant.
        println("SyncLeave "+Thread.currentThread().getName+" on "+SimulateLock)
      SimulateLock.release()

      try {
        println("Preparing run of #" + mapName)
        val now = System.currentTimeMillis()
        bot.onEvent(BotEvent.RUN_PREPARE)
        println("Run of #" + mapName + " prepared in " + (((System.currentTimeMillis() - now) / 100) / 10f) + "s")
        bot.onEvent(BotEvent.RUN_STARTED)
      } catch {
        case e: Exception =>
          println(s"Bot ${botClass.getCanonicalName} threw an exception during preparation on map $mapName:")
          e.printStackTrace()
      }
      robotThread.join() //Block until stops
    }
  }
}

object SimulatorEndThrowable extends Exception