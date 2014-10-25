package lego.simulator.ui

import java.awt.BorderLayout
import javax.swing.{JButton, JTextField, JToolBar, JFrame}

/**
 * Private property.
 * User: Darkyen
 * Date: 25/10/14
 * Time: 10:52
 */
object UIMain extends App {

  //----------------------------------------- DECLARATION ----------------------------------------------
  val frame = new JFrame("NXT MazeBot Simulator")
  val menu = new JToolBar()

  val botSelection = new JTextField()
  val controllerSelection = new JTextField("lego.simulator.controllers.EnvironmentSimulatorController")

  val loadButton = new JButton("Load")
  val step = new JButton("Step")
  val run = new JButton("Run")
  val fastForward = new JButton("FastForward")

  val displayPanel = new MazeDisplayPanel

  //------------------------------------------ ADDING & PROPERTIES ---------------------------------------------------

  frame.getContentPane.setLayout(new BorderLayout())

  menu.setFloatable(false)

  menu.add(botSelection)
  menu.add(controllerSelection)
  menu.add(loadButton)
  menu.addSeparator()
  menu.add(step)
  menu.add(run)
  menu.add(fastForward)

  frame.getContentPane.add(menu,BorderLayout.PAGE_START)
  frame.getContentPane.add(displayPanel,BorderLayout.CENTER)

  //--------------------------------------------- LISTENERS ------------------------------------------------



  //--------------------------------------------- INITIALIZE ----------------------------------------------

  frame.setSize(800,600)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.setLocationByPlatform(true)
  frame.setVisible(true)
}
