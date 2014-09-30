package main.robotcontrol

import main.robotcontrol.constants.{RelativeHeading, AbsoluteHeading}

/**
 * Created by jIRKA on 30.9.2014.
 */
class Environment {

  def mazeWidth = 9
  def mazeHeight = 6

  private var x = 0
  private var y = 0

  /**
   * @return X coord relative to upper left corner (That one is x = 0)
   */
  def getX = this.x

  /**
   * @return Y coord relative to upper left corner (That one is y = 0)
   */
  def getY = this.y

  private var heading = AbsoluteHeading.DOWN

  def getHeading = this.heading

  def deploy(x:Int, y:Int){
    this.x = x
    this.y = y
  }

  def moveTo(x:Int, y:Int){
    if(x < 0 || x >= mazeWidth || y < 0 || y >= mazeHeight){
      throw new IllegalArgumentException("Coordinates out of bounds. (x: "+x+", y: "+y+")")
    }
    this.x = x
    this.y = y
  }

  def moveBy(x:Int, y:Int){
    if(x + this.x< 0 || x + this.x >= mazeWidth || y + this.y < 0 || y + this.y >= mazeHeight){
      throw new IllegalArgumentException("Coordinates out of bounds. (x: "+x+", y: "+y+")")
    }
    this.x += x
    this.y += y
  }

  def rotateTo(heading:AbsoluteHeading){
    this.heading = heading
  }
  def rotateBy(headingDelta: RelativeHeading, count:Int = 1){
    if(count < 1){
      throw new IllegalArgumentException("'Count' have to be greater then or equals 1.")
    }
    if(headingDelta == RelativeHeading.LEFT){
      for(i <- 1 to count){
        heading = heading match {
          case AbsoluteHeading.DOWN => AbsoluteHeading.RIGHT
          case AbsoluteHeading.RIGHT => AbsoluteHeading.UP
          case AbsoluteHeading.UP => AbsoluteHeading.LEFT
          case AbsoluteHeading.LEFT => AbsoluteHeading.DOWN
        }
      }
    }else{
      for(i <- 1 to count){
        heading = heading match {
          case AbsoluteHeading.DOWN => AbsoluteHeading.LEFT
          case AbsoluteHeading.LEFT => AbsoluteHeading.UP
          case AbsoluteHeading.UP => AbsoluteHeading.RIGHT
          case AbsoluteHeading.RIGHT => AbsoluteHeading.DOWN
        }
      }
    }
  }

}
