package com.ashome.tablet.model

import com.ashome.tablet.gesture.model.AhGestureRecorder
import com.beyondrelations.microworx.core.service.MwSystemCall
import mu.KotlinLogging
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
    landscape,
    composea,
    composemath,
    composepunctuation,
    composebracket,
    numlayouts
}

data class AhTablet (val buttons : Map<AhTabletInputType,AhTabletInput>) {
    companion object{
        private val logger = KotlinLogging.logger {}
    }
    val keyboard = AhVirtualKeyboard()
    val keyboardLayers  = listOf<String>(
        "full",
        "special",
        "simple",
        "simplegrid",
        "dialer",
        "nav",
        "landscape",
        "composea",
        "composemath",
        "composepunctuation",
        "composebracket",
        "numlayouts"
    )
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


    constructor(buttonList : List<AhTabletInput>) : this(buttons = getMapFromTypedList(buttonList) as Map<AhTabletInputType,AhTabletInput>)
    init {

        var curLayout = 0

        buttons[AhTabletInputType.HOME]?.addAction {evt:AhButtonEvent->
            if (curLayout> keyboardLayers.size -1)
                curLayout=0
            if (evt.held && evt.numberOfClicks == 1)
            {
                AhGestureRecorder.static.launch()
            }
            else if (evt.releasedClick && evt.numberOfClicks == 1)
            {
                keyboard.close()
//                MwSystemCall(program = "pkill", arguments= listOf("-f","wvkbd-mobintl"),waitForCompletion = true).execute()
                MwSystemCall(program = "wvkbd-mobintl", arguments= listOf("-l",keyboardLayers[curLayout]),waitForCompletion = false).execute()
                logger.info { "showing ${keyboardLayers[curLayout]}" }
                curLayout++
/*
                if (keyboardState)
                {
                    keyboardState = false
                    MwSystemCall(program = "pkill", arguments= listOf("-f wvkbd-mobint"),waitForCompletion = false).execute()
                }
                else
                {

                    keyboardState = true
                    MwSystemCall(program = "wvkbd-mobint", arguments= listOf("-l wvkbd-mobint"),waitForCompletion = false).execute()

                }

 */
            }

//            MwSystemCall(program = "pkill", arguments= listOf("-f wvkbd-mobint"),waitForCompletion = false).execute()

        }
    }


    fun getButtonByKey(key : String) : AhTabletInput?
    {
        for (curButton in buttons.values)
            if (curButton.key==key)
                return curButton

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
            releaseTimer.schedule(Math.max(0,millisHeld-millisBetweenEvents ))
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

