package com.ashome.tablet.gesture.model

import com.beyondrelations.microworx.core.service.MwSystemCall
import mu.KotlinLogging
import java.awt.*
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

class AhGestureRecorder : JFrame("gestureControl")
{

    companion object {
        private val logger = KotlinLogging.logger {}
        val static = AhGestureRecorder()
    }

    private val backgroundPane = ImagePanel()

    init{
        val robot = Robot()
//        val screenShot = robot.createScreenCapture(Rectangle(Toolkit.getDefaultToolkit().getScreenSize()))
//        this.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        this.add(backgroundPane)
        val overlay = AhGestureOverlay(this)

        contentPane.addMouseListener(overlay)
        contentPane.addMouseMotionListener(overlay)

        this.glassPane = overlay

        val frame = this
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(evt: WindowEvent) {
                logger.info { "closing" }
                frame.isVisible = false
            }
        })
    }


    val robot = Robot()
    var screenSize = Toolkit.getDefaultToolkit().getScreenSize()

    fun launch() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize()
        this.setBounds(0,0,screenSize.width,screenSize.height)
        updateScreenImage()
        this.isVisible = true
        glassPane.isVisible = true
    }
    fun updateScreenImage()
    {
        val screenCapPath = "${System.getProperty("user.home")}/.local/state/ashux/screen.png"

        val systemCall = MwSystemCall(program = "grim",arguments = listOf(screenCapPath), waitForCompletion = false )

        val setViaRobot = {
            backgroundPane.image = robot.createScreenCapture(Rectangle(Toolkit.getDefaultToolkit().getScreenSize()))
            backgroundPane.repaint()
        }
        val setViaFile = {
            backgroundPane.image = ImageIO.read( File(screenCapPath))
            backgroundPane.repaint()
        }

        systemCall.actionOnSuccess = {
            try{ setViaFile.invoke() }
            catch (e:Exception){ setViaRobot.invoke() }
        }
        systemCall.actionOnError = {
            setViaRobot.invoke()
        }
        systemCall.execute()
    }



    internal class ImagePanel(image: BufferedImage?=null) : JPanel()
    {
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            //the third param is an ImageObserver. It allows for async images, which sound nice for this context
            if (image!=null)
                g.drawImage(image,0,0, null)
        }
        init{
            this.isVisible = true
        }

        var image: BufferedImage?=null
        set(value) {field = value}
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
            if (current != null) {
                g.color = Color.yellow
                g.fillOval(current!!.x - 10, current!!.y - 10, 20, 20)
            }
            if (clicked != null) {
                g.color = Color.red
                g.fillOval(clicked!!.x - 10, clicked!!.y - 10, 20, 20)
            }
            if (released != null) {
                g.color = Color.green
                g.fillOval(released!!.x - 10, released!!.y - 10, 20, 20)
            }
        }


        init {
            isVisible = true

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



