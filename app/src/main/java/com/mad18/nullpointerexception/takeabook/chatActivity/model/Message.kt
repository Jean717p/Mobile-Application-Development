package com.mad18.nullpointerexception.takeabook.chatActivity.model

import java.util.Date


object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
}

interface Message {
    val time: Date
    val senderId: String
    val type: String
}