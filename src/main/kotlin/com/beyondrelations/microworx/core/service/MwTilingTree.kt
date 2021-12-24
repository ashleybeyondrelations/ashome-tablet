package com.beyondrelations.microworx.core.service

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.Json

import kotlinx.serialization.decodeFromString

import java.awt.Rectangle
import kotlinx.serialization.*

@Serializable
@SerialName("MwTilingWindowManagerTree")
class MwTwnTreeUtil(val data: TwmNode ) {
    companion object{
    }
    init {

    }
}



@Serializable
@SerialName("TwmNode")
public data class TwmNode(
        val id: Long = 0L,
        val typedef : TwmNodeTypeEnum = TwmNodeTypeEnum.root,
        val orientation: String ="", //TwmOrientationEnum
        val scratchpad_state: String? = null, //i3 ":"none"
        val percent: Double? = null,
            val urgent: Boolean = false,
            val marks: List<TwmMarks> = listOf(),
            val focused: Boolean = false,
            val layout: TwmLayoutEnum = TwmLayoutEnum.output,
            val workspace_layout: String?=null,  //i3 ":"default"
        val border: String="", //none,normal
        val current_border_width: Int = 0,
        public val rect: TwmRectangle =TwmRectangle(0,0,0,0),
        public val deco_rect: TwmRectangle=TwmRectangle(0,0,0,0),
        public val window_rect: TwmRectangle=TwmRectangle(0,0,0,0),
        public val geometry: TwmRectangle=TwmRectangle(0,0,0,0),
        val name: String?=null,
        val window: Int?=null,

        val nodes: List<TwmNode> = listOf(),
        val floating_nodes: List<TwmNode> = listOf(),
        val focus: List<Long> = listOf(),

        val fullscreen_mode: Int=0,
        val sticky: Boolean=false,
        val window_icon_padding: Int?=null,
        val floating: String?=null,
//    val swallows:List<>? //i3

        //for different types
        val last_split_layout:TwmLayoutEnum?=null,
        //String?=null, //TwmOrientationEnum?

        val window_properties: TwmI3Properties? =null ,

        //sway props
        val pid: Int? =null,
        val app_id: String? =null ,
        val visible: Boolean? = null ,
        val max_render_time: Int? = null,
        val shell: String?= null,
        val inhibit_idle: Boolean?=null,
        val idle_inhibitors: TwmSwayInhibtor? = null
) {

        companion object {
            fun deserialize(serialData: String?): TwmNode? {
                serialData ?: return null
                val json = Json {
                    ignoreUnknownKeys = true //continue when unknown key is encountered
                    allowSpecialFloatingPointValues = true
                    isLenient = true
//                    context = TwmSerializersModule

                }
//                val serializer = TwmNode.serializer()
                val replaced = serialData
                        .replace("\"type\":","\"typedef\":")
                        .replace("\"class\":","\"proccessClass\":")
                val tree = json.decodeFromString<TwmNode>(replaced)
                tree.setParentNode()
                return tree
            }
        }

    fun serialize(): String {
//            val json = Json {
//                ignoreUnknownKeys = true
//                allowSpecialFloatingPointValues = true
//                isLenient = true

        // NOTE allows for unknown keys when deserializing, https://github.com/Kotlin/kotlinx.serialization/blob/master/runtime/commonMain/src/kotlinx/serialization/json/JsonConfiguration.kt#L16
//                ,context = servletSerializerModule + coreSerializerModule) //might need to add a context
//            }
//            json.context = TwmSerializersModule
//            val serializer = TwmNode.serializer()
        return Json.encodeToString(this)
        //            json.encodeToString(serializer,this)
    }



    @Transient
    var parent : TwmNode?=null
    fun setParentNode()
    {
        for (curNode in this.nodes)
        {
            curNode.parent = this
            curNode.setParentNode()
        }
    }

    fun getAllDescendantNodes() : List<TwmNode>
    {
        val retList:MutableList<TwmNode> = mutableListOf()
        for (curNode in this.nodes)
        {
            retList.add(curNode)
            retList.addAll(curNode.getAllDescendantNodes())
        }
        for (curNode in this.floating_nodes)
        {
            retList.add(curNode)
            retList.addAll(curNode.getAllDescendantNodes())
        }


        return retList
    }

    }

@Serializable
@SerialName("TwmRectangle")
public data class TwmRectangle(
        public val x : Int = 0,
        public val y : Int = 0,
        public val width : Int = 0,
        public val height : Int = 0
) {
}

    @Serializable
    @SerialName("TwmMarks")
    public  class TwmMarks {
    }

    @Serializable
    @SerialName("TwmSwayInhibtor")
    public  data class TwmSwayInhibtor(
            val user: String = "",
            val application: String=""
    )
    @Serializable
    @SerialName("TwmI3Properties")
    public data class TwmI3Properties(
            val proccessClass : String ="",// //this has a bad name....
            val instance: String="",
            val machine: String="",
            val title: String="",
            val transient_for: String?=null
)

    @Serializable
    @SerialName("TwmNodeType")
    public     enum class TwmNodeTypeEnum {
        output,
        floating_con,
        con, //i3
        workspace, //i3
        dockarea, //i3
        root
    }


    @Serializable
    @SerialName("TwmOrientation")
    public    enum class TwmOrientationEnum {
        NONE,
        horizontal,
        vertical
    }

    @Serializable
    @SerialName("TwmLayout")
    public   enum class TwmLayoutEnum {
        dockarea,
        output,
        splith,
        splitv,
        none
    }
