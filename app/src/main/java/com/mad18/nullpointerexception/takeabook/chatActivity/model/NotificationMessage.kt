package com.mad18.nullpointerexception.takeabook.chatActivity.model

import java.util.Date

data class NotificationMessage(val senderName: String,
                               val dayTimestamp: Date,
                               val fromToken: String,
                               val toToken: String,
                               val body: String,
                               val photoUrl: String,
                               val title: String)
{
    constructor() : this("", Date(0),"", "", "", "","")
    /* Notification Icon set by the default as icon app*/

}
