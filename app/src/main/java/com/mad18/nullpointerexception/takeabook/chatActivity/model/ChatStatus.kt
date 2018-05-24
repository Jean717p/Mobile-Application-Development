package com.mad18.nullpointerexception.takeabook.chatActivity.model

import java.util.*

data class ChatStatus(
        val otherUserId:String,
        val timeStamp:Date,
        var unreadMessages:Int,
        val text:String
) : Comparable<ChatStatus>{
    constructor() : this("",Date(0),0, "")

    override fun compareTo(other: ChatStatus): Int {
        return this.timeStamp.compareTo(other.timeStamp)
    }
}