package com.ashome.tablet.gesture.model

import com.beyondrelations.microworx.core.service.MwSystemCall
import mu.KotlinLogging
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.event.MouseInputListener


data class AhGesture(val desc:String, val systemCall: MwSystemCall)
{
    companion object
    {
        private val logger = KotlinLogging.logger {}
    }
    init {
    }


}

class AhGestureBackground
{
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    fun start(){

        logger.info{ "checking grim" }

        val grimCheck = MwSystemCall(program = "grim",arguments = listOf(AhGestureRecorder.screenCapPath), waitForCompletion = true )
        grimCheck.actionOnSuccess = {
            logger.info{ "background starting" }
            val action = {
                logger.info { "Running grim in background" }
                while (true)
                {
                    Thread.sleep(1 * 1000)
                    if (!AhGestureRecorder.static.isVisible)
                        MwSystemCall(program = "grim",arguments = listOf(AhGestureRecorder.screenCapPath), waitForCompletion = true ).execute()
                }
            }
            Thread(action).start()
        }
        grimCheck.actionOnError = {
            logger.info{ "background failed" }
        }
        grimCheck.execute()
    }
}
class AhGestureRecorder():JFrame("gestureControl")
{

    companion object {
        private val logger = KotlinLogging.logger {}
        val screenCapPath = "${System.getProperty("user.home")}/.local/state/ashux/screen.png"

        val static = AhGestureRecorder()

    }

    private val backgroundPane = ImagePanel()

    private val frame = this
    private val overlay = AhGestureOverlay(frame)

    init{
        frame.add(backgroundPane)

        if (GraphicsEnvironment.
                getLocalGraphicsEnvironment().defaultScreenDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT))
        {
            frame.setUndecorated(true)
            frame.opacity = .0f
        }
        else{
            logger.info { "DOES NOT SUPPORT TRANSPARENCY" }
        }
//        frame.isOpaque = false
//        this.add(JLabel("Should show"))

        frame.contentPane.addMouseListener(overlay)
        frame.contentPane.addMouseMotionListener(overlay)

        frame.glassPane = overlay

        this.addWindowListener(object : WindowAdapter() {
            override fun windowDeactivated(e: WindowEvent?) {
                logger.info { "deactivated" }
                frame.isVisible = false
            }
            override fun windowClosing(evt: WindowEvent) {
                logger.info { "closing" }
                frame.isVisible = false
            }
        })
    }


    val robot = Robot()
    var screenSize = Toolkit.getDefaultToolkit().getScreenSize()

    fun launch() {
//        frame.minimumSize = screenSize.size

//            screenSize.width,screenSize.height)
        updateScreenImage()
        frame.pack()
        frame.setBounds(0,0,screenSize.width,screenSize.height)

//        showTest()
        frame.isVisible = true
        overlay.isVisible = true
//        backgroundPane.isVisible = true
    }
    fun showTest()
    {
        val testPanel = JFrame("gestureControl")
        val rectangle = Rectangle(Toolkit.getDefaultToolkit().getScreenSize())
//        testPanel.setGlassPane(TouchPanel(rectangle))
//        testPanel.add(GestureControl.getScreenImage()?.let { com.ashome.tablet.ImagePanel(it) })
        val overlay = AhGestureOverlay(testPanel)
        testPanel.add(backgroundPane)
        testPanel.contentPane.addMouseListener(overlay)
        testPanel.contentPane.addMouseMotionListener(overlay)

        testPanel.glassPane = overlay

        testPanel.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(evt: WindowEvent) {
                logger.info { "closing" }
                testPanel.isVisible = false
            }
        })

        testPanel.pack()
        testPanel.setBounds(rectangle.x,rectangle.y,rectangle.width/2,rectangle.height/2)

        overlay.isVisible =true
        testPanel.isVisible =true
    }
//    var image:BufferedImage? = null
    fun updateScreenImage()
    {

//"-t png",
        /*
        if (File(screenCapPath).exists()){
            val attr = Files.readAttributes(File(screenCapPath).toPath(), BasicFileAttributes::class.java)
            if (Duration.between(
                    LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault()),
                    LocalDateTime.now()).toSeconds()<4)
            {
                backgroundPane.image = ImageIO.read( File(screenCapPath))
                backgroundPane.repaint()
            }
            return
        }
         */

        val systemCall = MwSystemCall(program = "grim",arguments = listOf("-s .2",screenCapPath), waitForCompletion = true,timeout = 2000 )
//        val systemCall = MwSystemCall(program = "grim",arguments = listOf("-","| wl-copy"), waitForCompletion = true,timeout = 2000 )

        val setViaRobot = {
            val image = robot.createScreenCapture(Rectangle(Toolkit.getDefaultToolkit().screenSize))
            backgroundPane.update(image)
//            logger.info{ "size from robot ${backgroundPane!!.image!!.width} x ${backgroundPane!!.image!!.height} " }
//            backgroundPane.minimumSize=Dimension(image!!.width,image!!.height)
//            backgroundPane.repaint()
//            showTest()
        }
        val setViaFile = {
            val image = ImageIO.read( File(screenCapPath))
//            val image = ImagePanel.getImageFromClipboard()
            if (image!=null)
            backgroundPane.update(image,this.width,this.height)
//            backgroundPane.image = image
//            logger.info{ "size from file ${backgroundPane!!.image!!.width} x ${backgroundPane!!.image!!.height} " }
//            backgroundPane.minimumSize=Dimension(image!!.width,image!!.height)
//            backgroundPane.paint(backgroundPane.graphics)
//            backgroundPane.repaint()
//            showTest()
        }

        systemCall.actionOnSuccess = {
            setViaFile.invoke()
        }
        systemCall.actionOnError = {
            setViaRobot.invoke()
        }
        systemCall.execute()
    }



    internal class ImagePanel(image: BufferedImage?=null) : JPanel()
    {

        companion object
        {
            private val GFX_CONFIG =
                GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration

//            @Throws(Exception::class)
            fun getImageFromClipboard(): BufferedImage? {
                return try {
                    val transferable = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
                    transferable.getTransferData(DataFlavor.imageFlavor) as BufferedImage

                } catch (e:Exception){
                    null
                }
            }

            fun toCompatibleImage(image: BufferedImage?): BufferedImage? {
                /*
     * if image is already compatible and optimized for current system settings, simply return it
     */
                if (image == null || image.colorModel == GFX_CONFIG.colorModel) {
                    return image
                }

                // image is not optimized, so create a new image that is
                val new_image = GFX_CONFIG.createCompatibleImage(image.width, image.height, image.transparency)

                // get the graphics context of the new image to draw the old image on
                val g2d = new_image.graphics as Graphics2D

                // actually draw the image and dispose of context no longer needed
                g2d.drawImage(image, 0, 0, null)
                g2d.dispose()

                // return the new optimized image
                return new_image
            }
        }
        var lastPaintedImage : BufferedImage? = null
        override fun paintComponent(g: Graphics) {
//        override fun paintComponent(g: Graphics) {
//            super.paintComponent(g)
            //the third param is an ImageObserver. It allows for async images, which sound nice for this context
            if (image!=null)// && (lastPaintedImage!=image || lastPaintedImage ==null))
            {
//                image.graphics
                val localWidth = width ?: image!!.width
                val localHeight = height ?: image!!.height
                g.drawImage(image,0,0,localWidth,localHeight,null)
            }
        }
        init{
        }

        private var width:Int?=null
        private var height:Int?=null
        fun update(image:BufferedImage,width:Int?=null,height:Int?=null)
        {
            this.image=image
            this.width = width
            this.height = height
            this.repaint()
        }
        private var image: BufferedImage? = null
            set(value) {
            field = ImagePanel.toCompatibleImage(value)
        }

        init {
            this.image = image
        }
    }


    internal class AhGestureOverlay(val parentFrame : JFrame) : JComponent(), MouseMotionListener, MouseInputListener {
        var current: Point? = null
        var clicked: Point? = null
        var released: Point? = null

        //React to change button clicks.
        override fun paintComponent(g: Graphics) {

            (g as Graphics2D).stroke = BasicStroke(10F)

            if (current != null) {
                g.color = Color.yellow
//                g.fillOval(current!!.x - 10, current!!.y - 10, 20, 20)
                g.drawOval(current!!.x-50, current!!.y-50,100,100)
            }
            if (clicked != null) {
                g.color = Color.red
                g.drawOval(clicked!!.x-50, clicked!!.y-50,100,100)
            }
            if (released != null) {
                g.color = Color.green
                g.drawOval(released!!.x-50, released!!.y-50,100,100)
            }
        }


        init {

//        val listener = CBListener(
//                this, contentPane)
        }

        override fun mouseMoved(e: MouseEvent) {
        }

        override fun mouseDragged(e: MouseEvent) {
            this.current = e.point
//        this.paintComponent(this.graphics)
//            this.repaint(0,0,this.width,this.height)
            this.repaint()
        }

        override fun mouseClicked(e: MouseEvent) {
        }

        override fun mouseEntered(e: MouseEvent) {
        }

        override fun mouseExited(e: MouseEvent) {
        }

        override fun mousePressed(e: MouseEvent) {
            this.clicked = e.point
            System.out.println("${this.isVisible} $width x $height")
//            this.repaint(0,0,this.width,this.height)
            this.repaint()
        }

        override fun mouseReleased(e: MouseEvent) {
            this.released = e.point
            parentFrame.dispose()
//        this.repaint()
 //           this.repaint(0,0,this.width,this.height)
        }

    }


}



