package lego.simulator.ui

import java.awt.{Color, Graphics, Dimension}
import javax.swing.JPanel

import scala.util.Random

/**
 * Private property.
 * User: Darkyen
 * Date: 25/10/14
 * Time: 11:05
 */
class MazeDisplayPanel extends JPanel {
  setPreferredSize(new Dimension(700,800))

  override def paint(g: Graphics): Unit = {
    g.setColor(new Color(Random.nextInt()))
    g.fillRect(0,0,getWidth,getHeight)
  }
}
