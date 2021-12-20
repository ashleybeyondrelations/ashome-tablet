package com.ashome.tablet

import mu.KotlinLogging
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.event.MouseInputAdapter


//private val logger = KotlinLogging.logger {}

// run from command line using :
// java -jar ah-gesture-control-1.0.0.jar com.ashome.tablet.GestureControlKt


/*
fun main(args: Array<String>) {
//    logger.info("In Startup ")

//    val file =  File("screen-capture.png");
//    val status = ImageIO.write(bufferedImage, "png", file);
//    System.out.println("Screen Captured ? " + status + " File Path:- " + file.getAbsolutePath());


    val testPanel = JFrame("gestureControl")
        val rectangle = Rectangle(Toolkit.getDefaultToolkit().getScreenSize())
    testPanel.setGlassPane(TouchPanel(rectangle))
    testPanel.add(GestureControl.getScreenImage()?.let { ImagePanel(it) })
    testPanel.pack()
    testPanel.setBounds(rectangle.x,rectangle.y,rectangle.width,rectangle.height)


    testPanel.isVisible =true

}

 */
internal class ImagePanel(val img: BufferedImage) : Panel(){
    override fun paint(g: Graphics) {
        // Draws the img to the BackgroundPanel.
        g.drawImage(img, 0, 0, null)
    }
}
internal class TouchPanel(bounds :Rectangle) : JPanel(), MouseListener, MouseMotionListener {
    // The Image to store the background image in.
    var lastClick : Point?=Point(300,300)
    var lastRelease : Point?=null
    var currentPoint : Point?=null
        // painting behavior (opaque honored, etc.)
        override fun paint(g: Graphics) {
//            super.paintComponent(g)

/*            val g2d: Graphics2D = g as Graphics2D
            g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            )
            g2d.setComposite(
                AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.8f
                )
            )

 */
        // Draws the img to the BackgroundPanel.
//        g.drawImage(img, 0, 0, null)

        (g as Graphics2D).stroke = BasicStroke(10F)

        g.setColor(Color.GREEN)
        if (lastClick!=null)
            g.drawOval(lastClick!!.x-50, lastClick!!.y-50,100,100)

        g.setColor(Color.RED)
        if (lastRelease!=null)
            g.drawOval(lastRelease!!.x-50, lastRelease!!.y-50,100,100)

        g.setColor(Color.YELLOW)
        if (currentPoint!=null)
            g.drawOval(currentPoint!!.x-50, currentPoint!!.y-50,100,100)

    }

    init {
        // background should be black, except it's not opaque, so
        // background will not be drawn
 //       this.setOpaque( false)
        this.setBounds(0,0,bounds.width-bounds.x,bounds.height-bounds.y)
//        this.setBackground(Color.black);
        this.addMouseListener(this)
        this.addMouseMotionListener(this)
        // Loads the background image and stores in img object.
    }

    override fun mouseClicked(p0: MouseEvent) {
//        lastClick = p0.point
    }

    override fun mousePressed(p0: MouseEvent) {
        lastClick = p0.point
        this.repaint()
    }

    override fun mouseReleased(p0: MouseEvent) {
        lastRelease = p0.point
        this.repaint()
    }

    override fun mouseEntered(p0: MouseEvent?) {
        TODO("Not yet implemented")
    }

    override fun mouseExited(p0: MouseEvent?) {
        TODO("Not yet implemented")
    }

    override fun mouseDragged(p0: MouseEvent) {
        currentPoint = p0.point
        this.repaint()
    }

    override fun mouseMoved(p0: MouseEvent?) {
//        TODO("Not yet implemented")
    }
}
class GestureControl {
    companion object{
        private val logger = KotlinLogging.logger {}

        fun getScreenImage() : BufferedImage?
        {
            var screenImage : BufferedImage?=null
            //if we have grim, use that
            if (File("/usr/bin/grim").exists())
//            if (true)
            {
                val screenCapPath = "${System.getProperty("user.home")}/.local/state/ashux/screen.png"
//                val threadFactory = ProcessBuilder(listOf("grim","-o DSI-1",screenCapPath))
                val threadFactory = ProcessBuilder(listOf("grim",screenCapPath))
//                val threadFactory = ProcessBuilder(listOf("zenity","--info","--text='grim $screenCapPath'"))
//            if (workingDirectory != null) threadFactory.directory(workingDirectory.toFile())
                threadFactory.inheritIO()
                val binaryThread: Process = threadFactory.start()
//                val waitForCompletion =true
//            if (waitForCompletion) {
                val osExitFlag = binaryThread.waitFor()
                if (//osExitFlag == 0 &&
                    File(screenCapPath).exists()) {
                    screenImage = ImageIO.read(File(screenCapPath))
                }
                else
                {
//                    throw RuntimeException("Expected exit flag 0, found " + osExitFlag)
                }

            }

            if (screenImage == null)
            {
                try {
                    val robot = Robot();
                    val rectangle = Rectangle(Toolkit.getDefaultToolkit().getScreenSize())
                    screenImage = robot.createScreenCapture(rectangle)
                }
                catch (e:Exception)
                {

                }
            }

            return screenImage
        }
    }

}