package main.brutforcer

import main.brutforcer.maze.TrainingMap

/**
 * Created by jIRKA on 30.9.2014.
 */
object Main extends App{

  val m = new TrainingMap(null)

  render(m)

  val block = "[x]"
  val empty = " • "
  val eaten = " o "
  val robot = "(-)"
  val start = " v "

  def render(maze:TrainingMap) {
    println("+---------------------------+")
    for(y <- 0 until 6) {
      print("|")
      for (x <- 0 until 9) {
        if(m.maze(x)(y).mazeCellType == 'Start){
          println(start)
        } else if(m.robot.getEnvironment.getX == x && m.robot.getEnvironment.getY == y){
          println(robot)
        }else if(m.maze(x)(y).mazeCellType == 'Block){
          println(block)
        }else if(m.maze(x)(y).visitedTimes == 0){
          println(empty)
        }else{
          println(eaten)
        }
      }
      print("|")
    }
    println("+---------------------------+")
  }

}
