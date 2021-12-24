package com.ashome.tablet.gesture.model

import com.beyondrelations.microworx.core.service.MwSystemCall
import com.beyondrelations.microworx.core.service.TwmNode
import com.beyondrelations.microworx.core.service.TwmNodeTypeEnum
import mu.KotlinLogging
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.event.MouseInputListener
import java.awt.Color
import java.awt.event.*
import javax.swing.JLabel


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
}

interface AhContextualInterface{
    val contextualParent: AhContextualInterface?
    fun gainedFocus()
}

open class AhContextualPane(override val contextualParent:AhContextualInterface?,image: BufferedImage?=null,maintainAspectRatio:Boolean = false) :JPanel(), MouseMotionListener, MouseInputListener,AhContextualInterface
{

    companion object
    {
        private val GFX_CONFIG =
                GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration

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

    override fun paintComponent(g: Graphics)
    {
        super.paintComponent(g)
        paintBackground(g)
    }

    fun paintBackground(g: Graphics) {
        //the third param is an ImageObserver. It allows for async images, which sound nice for this context
        g.drawImage(bgimage,0,0,bgiWidth,bgiHeight,null)

    }
    fun paintOverlay(g: Graphics) {
        (g as Graphics2D).stroke = BasicStroke(10F)

        if (lastHeld != null) {
            g.color = Color.yellow
//                g.fillOval(current!!.x - 10, current!!.y - 10, 20, 20)
            g.drawOval(lastHeld!!.x-50, lastHeld!!.y-50,100,100)
        }
        if (lastClicked != null) {
            g.color = Color.red
            g.drawOval(lastClicked!!.x-50, lastClicked!!.y-50,100,100)
        }
        if (lastReleased != null) {
            g.color = Color.green
            g.drawOval(lastReleased!!.x-50, lastReleased!!.y-50,100,100)
        }
    }


    private var bgiWidth:Int=0
    private var bgiHeight:Int=0
    private var bgiMaintainAspectRatio:Boolean = false
        set(value) {
            field = value
            adjustImageToPanel()
        }


    private fun adjustImageToPanel()
    {
        val image = bgimage
        if (!bgiMaintainAspectRatio || image == null)
        {
            bgiWidth = width
            bgiHeight = height
        }
        else
        {
            bgiWidth = width
            bgiHeight = width * image.height/image.width
            if (bgiHeight > height) {
                bgiHeight = height
                bgiWidth = height * image.width / image.height
            }
        }
        val x = bgiWidth
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        super.setBounds(x, y, width, height)
        adjustImageToPanel()
    }
    open fun setImage(image:BufferedImage)
    {
        this.bgimage = image
        adjustImageToPanel()

        this.repaint()
    }

    private var bgimage: BufferedImage? = null
        set(value) {
            field = AhContextualPane.toCompatibleImage(value)
        }
    init {
        this.layout = null
        this.isVisible = true
        bgimage = image
        bgiMaintainAspectRatio = maintainAspectRatio
    }

    var lastClicked : Point? = null
    var lastHeld : Point? = null
    var lastReleased : Point? = null

    override fun mouseClicked(p0: MouseEvent){}
    override fun mousePressed(p0: MouseEvent){ lastClicked = p0.point ; gainedFocus();this.repaint()}
    override fun mouseReleased(p0: MouseEvent){ lastReleased = p0.point; this.repaint() }
    override fun mouseEntered(p0: MouseEvent) {}
    override fun mouseExited(p0: MouseEvent) {}
    override fun mouseDragged(p0: MouseEvent) {lastHeld = p0.point; this.repaint()}
    override fun mouseMoved(p0: MouseEvent) {}
    var isWaiting = false
    override fun gainedFocus() {
        contextualParent?.gainedFocus()
    }
}

class AhWindowSelector(override val contextualParent:AhContextualInterface,maintainAspectRatio: Boolean) : AhContextualPane(contextualParent=contextualParent,maintainAspectRatio = maintainAspectRatio)
{

    init {
        this.addMouseListener(this)
        this.addMouseMotionListener(this)
    }
    val windows:MutableList<TwmNode> = mutableListOf()

    var scale:Double = 1.0


    override fun paintComponent(g: Graphics) {

        super.paintComponent(g)
        (g as Graphics2D).stroke = BasicStroke(2F)
        val alpha = 175 // 50% transparent

        for (curNode in windows)
        {
            val convertedRect = Rectangle(
                    (scale*curNode.rect.x).toInt(),
                    (scale*curNode.rect.y).toInt(),
                    (scale*curNode.rect.width).toInt(),
                    (scale*curNode.rect.height).toInt()
            )
            g.color = Color(70, 100, 200, alpha)

            if (lastClicked!=null )
            {
                if (convertedRect.contains(lastClicked))
                     g.color = Color(200, 100, 70, alpha)
            }

            g.fillRect(convertedRect.x,convertedRect.y,convertedRect.width,convertedRect.height)

        }
        super.paintOverlay(g)
    }



}
 class AhWorkspaceManager(val parent : JFrame) : AhContextualPane(null)
{
    companion object{
        private val logger = KotlinLogging.logger {}

    }
    private val workspaceSelector = AhWindowSelector(maintainAspectRatio = true, contextualParent = this)
    init {
//        workspaceSelector.isVisible = true
        this.addMouseListener(this)
        this.addMouseMotionListener(this)
        this.add(workspaceSelector)
        this.background = Color.DARK_GRAY;
    }

    var wmTree: TwmNode? = null
        set(value) {
            field=value
            val activeWorkspace = wmTree!!.getAllDescendantNodes().filter{it.typedef== TwmNodeTypeEnum.workspace && (it.focused || it.getAllDescendantNodes().filter{it.focused}.isNotEmpty()) }.firstOrNull()
            workspaceSelector.windows.clear()
            if (activeWorkspace!=null)
                workspaceSelector.windows.addAll( activeWorkspace.getAllDescendantNodes().filter{ (it.typedef==TwmNodeTypeEnum.con || it.typedef==TwmNodeTypeEnum.floating_con) && it.nodes.isEmpty() })

            logger.info { "found ${workspaceSelector.windows.size} windows" }

        }

    final val selectorSizeRatio = .85

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        super.setBounds(x, y, width, height)
        workspaceSelector.setBounds(
                (0 + (1 - selectorSizeRatio) * width / 2).toInt(),
                (0 + (1 - selectorSizeRatio) * height / 2).toInt(),
                (width * selectorSizeRatio).toInt(),
                (height * selectorSizeRatio).toInt()
        )
        workspaceSelector.scale=workspaceSelector.width.toDouble()/parent.width.toDouble()
    }

    fun setWorkspaceImage(image:BufferedImage)
    {
        workspaceSelector.setImage(image = image)
    }
    override fun mouseMoved(p0: MouseEvent) {
        super.mouseMoved(p0)
    }
    override fun mousePressed(p0: MouseEvent) {
        super.mousePressed(p0)
    }

    override fun mouseReleased(p0: MouseEvent) {
        super.mouseReleased(p0)
        parent.dispose()
//        this.repaint()
        //           this.repaint(0,0,this.width,this.height)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
//        workspaceSelector.repaint()
        //the third param is an ImageObserver. It allows for async images, which sound nice for this context

        super.paintOverlay(g)

    }

    override fun gainedFocus() {
        super.gainedFocus()
        this.setBounds(0,0,parent.width,parent.height)
    }
}
class AhTest():JPanel() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
//        workspaceSelector.repaint()
        //the third param is an ImageObserver. It allows for async images, which sound nice for this context
        logger.info { "painting test ${this.x}, ${this.y} - ${this.width} x ${this.height}"}
        g.color = Color.BLUE
        g.fillRect(0,0,100,100)

    }

}
class AhGestureRecorder():JFrame("gestureControl")
{

    companion object {
        private val logger = KotlinLogging.logger {}
        val screenCapPath = "${System.getProperty("user.home")}/.local/state/ashux/screen.png"
    }

    private val workspaceManagement = AhWorkspaceManager(this)

    private val frame = this
//    private val overlay = AhGestureOverlay(this)
val testLabel = JLabel("SHOULD SHOW!!!")
    val backgroundPane = JPanel()

    init{

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
        backgroundPane.background = Color.BLACK;

        frame.layout = null
//        frame.add(workspaceManagement)
//        test.isVisible = true
//        frame.add(test)
//        frame.layeredPane.removeAll()
        frame.contentPane.removeAll()
//        frame.add(test)
        frame.add(workspaceManagement)
        frame.add(backgroundPane)
//        frame.contentPane = test

        frame.contentPane.addMouseListener(workspaceManagement)
        frame.contentPane.addMouseMotionListener(workspaceManagement)

//        frame.contentPane.addMouseListener(overlay)
//        frame.contentPane.addMouseMotionListener(workspaceManagement)

//        frame.glassPane = overlay

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

    val workspaceScale:Double = 0.8

/*    fun fullToInset(prect : Rectangle) : Rectangle
    {
        val x = Math.round((1-backgroundScale) * screenSize.width + prect.x * (backgroundScale)).toInt()
        val y = Math.round((1-backgroundScale) * screenSize.height + prect.y * (backgroundScale)).toInt()
        val width = Math.round( prect.width * (backgroundScale)).toInt()
        val height = Math.round( prect.height * (backgroundScale)).toInt()
        return Rectangle(x,y,width, height)
    }
 */
    val robot = Robot()
    var screenSize = Toolkit.getDefaultToolkit().getScreenSize()

    fun display() {
//        frame.minimumSize = screenSize.size

//            screenSize.width,screenSize.height)
        logger.info{"updating screen"}
        updateScreenImage()
        frame.pack()
        frame.setBounds(0,0,screenSize.width,screenSize.height)

        backgroundPane.setBounds(0,0,screenSize.width,screenSize.height)

        val insetWidth = Math.round(((1-workspaceScale) * screenSize.width)/2).toInt()
        val insetHeight = Math.round(((1-workspaceScale) * screenSize.height)/2).toInt()

//        showTest()



        frame.isVisible = true
        workspaceManagement.setBounds(insetWidth,insetHeight,screenSize.width-insetWidth*2,screenSize.height-insetHeight*2)
//        test.setBounds(0,0,1000,1000)

        frame.repaint()
//        overlay.isVisible = true
        workspaceManagement.isVisible = true
        workspaceManagement.repaint()
//        test.isVisible = true
//        test.repaint()
//        overlay.reset()
//        backgroundPane.isVisible = true
    }
//    var image:BufferedImage? = null
    suspend fun updateUI(tree : TwmNode?)
    {
        logger.info { tree }

        workspaceManagement.wmTree = tree
        workspaceManagement.repaint()
    }
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

        val systemCall = MwSystemCall(program = "grim", arguments = listOf("-s .2",screenCapPath), waitForCompletion = true, timeout = 2000 )
//        val systemCall = MwSystemCall(program = "grim",arguments = listOf("-","| wl-copy"), waitForCompletion = true,timeout = 2000 )

        val setViaRobot = {
            val image = robot.createScreenCapture(Rectangle(Toolkit.getDefaultToolkit().screenSize))
            workspaceManagement.setWorkspaceImage(image)
//            logger.info{ "size from robot ${backgroundPane!!.image!!.width} x ${backgroundPane!!.image!!.height} " }
//            backgroundPane.minimumSize=Dimension(image!!.width,image!!.height)
//            backgroundPane.repaint()
//            showTest()
        }
        val setViaFile = {
            val image = ImageIO.read( File(screenCapPath))
//            val image = ImagePanel.getImageFromClipboard()
            if (image!=null)
            {
                workspaceManagement.setWorkspaceImage(image)
                workspaceManagement.repaint()
            }
//            backgroundPane.image = image
            logger.info{ "size from file ${image!!.width} x ${image!!.height} " }
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







    internal class AhGestureOverlay(val parent : AhGestureRecorder) : JComponent(), MouseMotionListener, MouseInputListener {
        var current: Point? = null
        var clicked: Point? = null
        var released: Point? = null

        fun reset()
        {
             current = null
             clicked = null
             released = null
        }


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
            parent.dispose()
//        this.repaint()
 //           this.repaint(0,0,this.width,this.height)
        }

    }


}



