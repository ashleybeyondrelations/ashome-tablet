package com.ashome.tablet.model

import com.ashome.tablet.gesture.model.AhGestureRecorder
import com.beyondrelations.microworx.core.service.MwSystemCall
import mu.KotlinLogging
import java.awt.Dimension
import java.io.File
import java.io.FileFilter
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

//command to get accelerometer
    /*
//    #!/usr/bin/env sh
    ROTATION_GRAVITY="${ROTATION_GRAVITY:-"16374"}"
    ROTATION_THRESHOLD="${ROTATION_THRESHOLD:-"400"}"
    POLL_TIME=1
    RIGHT_SIDE_UP="$(echo "$ROTATION_GRAVITY - $ROTATION_THRESHOLD" | bc)"
    UPSIDE_DOWN="$(echo "-$ROTATION_GRAVITY + $ROTATION_THRESHOLD" | bc)"
    //FILE_Y="$(find /sys/bus/iio/devices/iio:device-iname in_accel_y_raw)"
    //FILE_X="$(find /sys/bus/iio/devices/iio:device* -iname in_accel_x_raw)"

*/

    fun rotateBasedOnAccelerometer()
    {

    }

}
data class AhTabletAnalogFile(val name : String ,val range : Dimension, val devicesFolder: File = File("/sys/bus/iio/devices/"))
{
    val file : File?

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

//    return FileUtils.listFiles(directory, WildcardFileFilter("in_accel_y_raw"), null);
    //Short.MAX/2
}

data class AhTablet (val buttons : Map<AhTabletInputType,AhTabletInput>) {
    companion object{
        private val logger = KotlinLogging.logger {}
    }
    val keyboard = AhVirtualKeyboard()
    val xOrientation = AhTabletAnalogFile("in_accel_x_raw" , Dimension(Short.MIN_VALUE/2,Short.MAX_VALUE/2))
    val yOrientation = AhTabletAnalogFile("in_accel_y_raw" , Dimension(Short.MIN_VALUE/2,Short.MAX_VALUE/2))
    val zOrientation = AhTabletAnalogFile("in_accel_z_raw" , Dimension(Short.MIN_VALUE/2,Short.MAX_VALUE/2))


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
                AhGestureRecorder.static.launch()
            }

//            MwSystemCall(program = "pkill", arguments= listOf("-f wvkbd-mobint"),waitForCompletion = false).execute()

        }
    }


    fun getButtonByKey(key : String) : AhTabletInput?
    {
        for (curButton in buttons.values)
        {
            logger.info { "is $curButton a $key" }
            if (curButton.key==key)
                return curButton
        }

        return null
    }
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
        val millisTillHold = 400L
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

