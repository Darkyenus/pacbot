package lego.simulator

/**
 * Private property.
 * User: Darkyen
 * Date: 28/11/14
 * Time: 21:36
 */
class PrintGrid(width:Int, height:Int) extends CharSequence {
  private val grid = Array.fill(height,width)(' ')
  private var cursorX = 0
  private var cursorY = 0

  private var subViewX = 0
  private var subViewY = 0
  private var subViewW = width
  private var subViewH = height

  def print(char:Char): Unit = {
    if(char == '\n'){
      cursorX = subViewX
      cursorY += 1
    }else{
      if(cursorX >= subViewX + subViewW){
        cursorX = subViewX
        cursorY += 1
      }
      if(cursorY < subViewY + subViewH){
        //Cursor has valid position
        grid(cursorY)(cursorX) = char
        cursorX += 1
      }
    }
  }

  def print(charSequence: CharSequence): Unit ={
    for(c <- 0 until charSequence.length()){
      print(charSequence.charAt(c))
    }
  }

  def println(charSequence: CharSequence): Unit ={
    print(charSequence)
    println()
  }

  def println(): Unit ={
    print('\n')
  }

  def clear(c:Char = ' '): Unit ={
    for(x <- subViewX until subViewX+subViewW; y <- subViewY until subViewY+subViewH){
      grid(y)(x) = c
    }
  }

  def resetSubgrid(): Unit ={
    subViewX = 0
    subViewY = 0
    subViewW = width
    subViewH = height
  }

  def setSubgrid(x:Int,y:Int,width:Int,height:Int): Unit ={
    subViewX = x//x max 0 min (width - 1)
    subViewY = y//y max 0 min (height - 1)
    subViewW = width//width max 0 min (this.width - subViewX)
    subViewH = height//height max 0 min (this.height - subViewY)
    cursorX = subViewX
    cursorY = subViewY
  }

  /**
   * Will make ascii frame inside subgrid and will shrink subgrid by one from every side.
   */
  def frameSubgrid(title:CharSequence = ""): Unit = {
    for(x <- subViewX + 1 until subViewX + subViewW - 1){
      grid(subViewY)(x) = '-'
      grid(subViewY + subViewH - 1)(x) = '-'
    }
    for(y <- subViewY + 1 until subViewY + subViewH - 1){
      grid(y)(subViewX) = '|'
      grid(y)(subViewX + subViewW - 1) = '|'
    }

    grid(subViewY)(subViewX) = '+'
    grid(subViewY)(subViewX + subViewW - 1) = '+'
    grid(subViewY + subViewH - 1)(subViewX) = '+'
    grid(subViewY + subViewH - 1)(subViewX + subViewW - 1) = '+'

    val titleWidth = title.length() min subViewW
    val titleStart = subViewX + subViewH/2 - titleWidth/2
    for(i <- 0 until titleWidth){
      grid(subViewY)(titleStart+i) = title.charAt(i)
    }

    setSubgrid(subViewX+1,subViewY+1,subViewW-2,subViewH-2)
  }

  /**
   * Prints the whole grid out to stdout.
   */
  def printOut(): Unit ={
    for(y <- 0 until height){
      Console.out.print(grid(y))
      Console.out.print('\n')
    }
    Console.out.flush()
  }

  /**
   * In ideal world, this would print some backspaces to clear old stuff. But in most consoles here, it won't work.
   */
  def printClearOut(): Unit ={
    for(_ <- 0 until (width * height + height)){
      Console.out.print('\b')
    }
  }

  //--------------- CharSequence stuff ----------------------------

  override val length: Int = width * height + height

  override def charAt(index: Int): Char = {
    val x = index % (width+1)
    if(x == width){
      '\n'
    }else{
      val y = index / (width+1)
      grid(x)(y)
    }
  }

  override def subSequence(start: Int, end: Int): CharSequence = {
    val result = new StringBuilder
    for(i <- start until end){
      result.append(charAt(i))
    }
    result
  }
}
