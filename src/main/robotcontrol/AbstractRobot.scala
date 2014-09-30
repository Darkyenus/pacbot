package main.robotcontrol

import main.robotcontrol.constants.AbsoluteHeading

/**
 * Created by jIRKA on 30.9.2014.
 */
trait AbstractRobot {

  def onNextStep: RobotResponse

  def getEnvironment: Environment

}
