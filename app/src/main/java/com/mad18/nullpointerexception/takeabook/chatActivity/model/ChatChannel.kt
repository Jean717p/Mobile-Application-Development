package com.mad18.nullpointerexception.takeabook.chatActivity.model


data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}