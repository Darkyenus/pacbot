package main.brutforcer.maze

import main.robotcontrol.AbstractRobot

/**
 * Created by jIRKA on 30.9.2014.
 */
class TrainingMap (val robot:AbstractRobot) {

  val mazeWidth = 9
  val mazeHeight = 6

  val maze = new Array[Array[MazeCell]](9)
  for (i <- 0 until 9){
    maze(i) = new Array[MazeCell](6)
  }

  //TODO generate maze


}
