package com.ashome.tablet.model

import com.ashome.tablet.gesture.model.AhGestureRecorder
import com.beyondrelations.microworx.core.service.MwSystemCall
import mu.KotlinLogging
import java.awt.Dimension
import java.io.File
import java.io.FileFilter
import java.io.InputStream
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule

internal fun <type> getMapFromTypedList(list : List<AhTyped<type>>):Map<type,AhTyped<type>>
{
    val retVal = mutableMapOf<type,AhTyped<type>>()
    for (curObj in list)
        if (!retVal.contains(curObj.type))
            retVal[curObj.type] = curObj

    return retVal
}

data class AhTablet (val buttons : Map<AhTabletInputType,AhTabletInput>) {
    companion object{
        private val logger = KotlinLogging.logger {}
    }
    constructor(buttonList : List<AhTabletInput>) : this(buttons = getMapFromTypedList(buttonList) as Map<AhTabletInputType,AhTabletInput>)
    init {

        var curLayout = 0

        buttons[AhTabletInputType.HOME]?.addAction {evt:AhButtonEvent->
            if (evt.pressed && evt.numberOfClicks == 1)
            {
            }
            if (evt.held && evt.numberOfClicks == 1)
            {
                AhGestureRecorder.static.launch()
            }
            else if (evt.releasedClick && evt.numberOfClicks == 1)
            {
                keyboard.toggle()
            }
            if (evt.held && evt.numberOfClicks == 2)
            {
                this.screen.rotate(calcScreenRotation(true))
            }
            else if (evt.releasedClick && evt.numberOfClicks == 2)
            {
                keyboard.showDouble()
            }



//            MwSystemCall(program = "pkill", arguments= listOf("-f wvkbd-mobint"),waitForCompletion = false).execute()

        }
    }


    val keyboard = AhVirtualKeyboard()
    val xOrientation = AhTabletAnalogFile("in_accel_x_raw" , Short.MIN_VALUE/2.0)
    val yOrientation = AhTabletAnalogFile("in_accel_y_raw" , Short.MIN_VALUE/2.0)
    val zOrientation = AhTabletAnalogFile("in_accel_z_raw" , Short.MIN_VALUE/2.0)
    val screen = AhScreen()

    //rotation occurs in 90 degree increments clockwise
    var currentRotation = 0

    fun calcScreenRotation(forceRotate : Boolean) : Int
    {
        val changeRotation = Math.abs(Math.abs(xOrientation.value) - Math.abs(yOrientation.value)) > .3
        logger.info { "current accel is ${xOrientation.value} x ${yOrientation.value}"  }
        if (!changeRotation&&!forceRotate)
            return currentRotation

        if (Math.abs(xOrientation.value) > Math.abs(yOrientation.value))
            if (xOrientation.value<0)
                currentRotation = 0
            else
                currentRotation = 180
        else
            if (yOrientation.value<0)
                currentRotation = 270
            else
                currentRotation = 90

        return currentRotation
    }




    fun getButtonByKey(key : String) : AhTabletInput?
    {
        for (curButton in buttons.values)
        {
            if (curButton.key==key)
                return curButton
        }

        return null
    }
}


//wraps the wvkbd-mobintl keyboard which is wayland compliant
class AhVirtualKeyboard
{
    companion object{
        final val keyboardProgram = "wvkbd-mobintl"
    }

    init{
        close()
    }

    fun showBasic()
    {
        close()
        keyboardState = true
        MwSystemCall(program = "wvkbd-mobintl", arguments= listOf("-l","full,special,dialer"),waitForCompletion = false).execute()
    }
    fun showDouble()
    {
        close()
        keyboardState = true
        MwSystemCall(program = "wvkbd-mobintl", arguments= listOf("-l","dialer"),waitForCompletion = false).execute()
        MwSystemCall(program = "wvkbd-mobintl", arguments= listOf("-l","full,special"),waitForCompletion = false).execute()
    }

    var keyboardState:Boolean = false
    fun toggle()
    {
        if (keyboardState)
            close()
        else
            showBasic()
    }

    fun close(){
        keyboardState = false
        MwSystemCall(program = "pkill", arguments= listOf("-f",keyboardProgram),waitForCompletion = true).execute()
    }

}
enum class AhVirtualKeyboardLayers{
    full,
    special,
    simple,
    simplegrid,
    dialer,
    nav,
    landscape

    /*
    ComposeMath,
    ComposePunctuation,
    composebracket","numLayouts""
            Cyrillic,
    Arabic,
    Emoji,
    ComposeE,
    ComposeY,
    ComposeU,
    ComposeI,
    ComposeO,
    ComposeW,
    ComposeR,
    ComposeT,
    ComposeP,
    ComposeS,
    ComposeD,
    ComposeF,
    ComposeG,
    ComposeH,
    ComposeJ,
    ComposeK,
    ComposeL,
    ComposeZ,
    ComposeX,
    ComposeC,
    ComposeV,
    ComposeB,
    ComposeN,
    ComposeM,
*/

}

class AhScreen()
{
//command to rotate
//    swaymsg -- output  DSI-1 transform 0

    companion object{
        private val logger = KotlinLogging.logger {}
    }

    fun rotate(degrees : Int)
    {
        logger.info{"swaymsg -- output  DSI-1 transform $degrees"}
        MwSystemCall(program = "swaymsg", arguments= listOf("--","output DSI-1 transform $degrees"),waitForCompletion = true).execute()

    }

}
data class AhTabletAnalogFile(val name : String ,val max : Double, val devicesFolder: File = File("/sys/bus/iio/devices/"))
{
    val file : File?
    companion object{
        private val logger = KotlinLogging.logger {}
    }

    init{
        var foundFile:File? = null
        for (curDir in devicesFolder.listFiles(FileFilter { it.isDirectory }))
            if (curDir.listFiles(FileFilter{it.name == name}).isNotEmpty())
            {
                foundFile = curDir.listFiles(FileFilter{it.name == name})[0]
                break
            }
        file = foundFile
    }
    val value : Double
    get() {
        if (file!=null)
                return ((file.inputStream().bufferedReader().use{ it.readText() }).toDoubleOrNull() ?: 0.0 )/max
        else return 0.0
    }

//    return FileUtils.listFiles(directory, WildcardFileFilter("in_accel_y_raw"), null);
    //Short.MAX/2
}



class AhTabletInput(override val type : AhTabletInputType, val key:String, val device : String ="") : AhTyped<AhTabletInputType>,  AhButtonEventHandler()
{
    companion object{
        private val logger = KotlinLogging.logger {}
    }

    override fun triggerStateChange(state: Boolean)
    {
        logger.info { "setting ${type.name} to $state" }
        super.triggerStateChange(state)
    }
}
interface AhTyped<type>{
    val type : type
}
enum class  AhTabletType(val tablet:AhTablet)
{
    GALAXY_TAB_A(AhTablet(
        buttonList = listOf(
            AhTabletInput(AhTabletInputType.POWER, "XF86PowerOff"),
            AhTabletInput(AhTabletInputType.VOLUME_UP, "XF86AudioRaiseVolume"),
            AhTabletInput(AhTabletInputType.VOLUME_DOWN, "XF86AudioLowerVolume"),
            AhTabletInput(AhTabletInputType.HOME, "XF86HomePage")
        )
    ))
}

enum class  AhTabletInputType
{
    POWER,
    VOLUME_UP,
    VOLUME_DOWN,
    HOME
}

open class AhButtonEventHandler() : AhButtonListener   {
    companion object{
        val millisBetweenEvents = 400L
        val millisTillHold = 250L
        private val logger = KotlinLogging.logger {}

    }
    private val actions: MutableList<(AhButtonEvent)->Unit> = mutableListOf()
    fun addAction(toAdd: (AhButtonEvent)->Unit) {
        actions.add(toAdd)
    }

    private var lastPressed : LocalDateTime = LocalDateTime.now().minusDays(1)
    private var lastReleased : LocalDateTime = LocalDateTime.now()
    private var currentState : Boolean = false
    private var clicks : Int = 0
    private var pressTimer : Timer= Timer()
    private var holdTimer : Timer= Timer()
    private var releaseTimer : Timer= Timer()


    override fun triggerStateChange(state: Boolean) {

        currentState = state

        if (currentState)
        {
            lastPressed =  LocalDateTime.now()
            clicks ++

            val evt = AhButtonEvent(clicks,0,currentState)

            pressTimer.cancel()
            pressTimer=Timer()
            releaseTimer.cancel()
            releaseTimer=Timer()
            pressTimer.schedule(millisBetweenEvents)
            {
                sendEvent(evt)
            }

            val longevt = AhButtonEvent(clicks,millisTillHold,currentState)
            holdTimer.cancel()
            holdTimer=Timer()
            holdTimer.schedule(millisTillHold)
            {
                sendEvent(longevt)
            }

        }
        else
        {
            lastReleased =  LocalDateTime.now()
            val millisHeld = Duration.between(lastPressed,lastReleased).toMillis()
            val evt = AhButtonEvent(clicks,millisHeld,currentState)
            releaseTimer.cancel()
            holdTimer.cancel()
            releaseTimer=Timer()
            holdTimer=Timer()
            releaseTimer.schedule(Math.max(0,millisBetweenEvents-millisHeld ))
            {
                clicks = 0
                sendEvent(evt)
            }

        }

    }
    fun sendEvent(evt:AhButtonEvent)
    {
        logger.info{"sending $evt"}
        for (curAction in actions)
            curAction.invoke(evt)
    }
}
interface AhButtonListener {
    fun triggerStateChange(state : Boolean )
}


data class AhButtonEvent (
    val numberOfClicks : Int,
    val millisHeld : Long,
    val pressed : Boolean,
    )
{
    val held : Boolean
    get() = pressed && millisHeld >= AhButtonEventHandler.millisTillHold

    val releasedHold : Boolean
        get() = released && millisHeld >= AhButtonEventHandler.millisTillHold

    val releasedClick : Boolean
        get() = released && millisHeld < AhButtonEventHandler.millisTillHold

    val released : Boolean
        get() = !pressed

}
open class AhRegistersButtons {
    private val listeners: MutableList<AhButtonListener> = mutableListOf()
    fun addListener(toAdd: AhButtonListener) {
        listeners.add(toAdd)
    }

    fun pressButton() {
        for (hl in listeners) hl.triggerStateChange(true)
    }
    fun releaseButton() {
        for (hl in listeners) hl.triggerStateChange(false)
    }
}

