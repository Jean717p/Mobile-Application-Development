package com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.mad18.nullpointerexception.takeabook.R
import com.mad18.nullpointerexception.takeabook.util.User
import com.mad18.nullpointerexception.takeabook.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_chat_member.*
import java.text.SimpleDateFormat
import java.util.Date


data class ChatMemberItem(val person: User,
                          val userId: String,
                          var lastReadByUser: Date,
                          var lastMessageTimeStamp: Date,
                          var unreadMessages: Int,
                          var lastMessage: String,
                          var context: Context)
    : Item(),Comparable<ChatMemberItem> {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.item_chat_member_name.text = person.usr_name
        if(person.profileImgStoragePath.length>0){
            Glide.with(context).load(StorageUtil.pathToReference(person.profileImgStoragePath))
                    .into(viewHolder.item_chat_member_profile_picture)
        }
        else {
            viewHolder.item_chat_member_profile_picture.setImageResource(R.drawable.ic_account_circle_white_48px)
        }
        if(lastMessageTimeStamp.after(Date(0))){
            val dateFormat = SimpleDateFormat("yyyy/MM/dd, HH:mm")
            viewHolder.item_chat_member_time.text = dateFormat.format(lastMessageTimeStamp)
            viewHolder.item_chat_member_last_msg.text = lastMessage
        }
        else{
            viewHolder.item_chat_member_time.text = ""
        }
        if(unreadMessages>0){
            viewHolder.item_chat_member_unread_msg.visibility = View.VISIBLE
            viewHolder.item_chat_member_unread_msg.text = unreadMessages.toString()
        }
        else{
            viewHolder.item_chat_member_unread_msg.visibility = View.INVISIBLE
        }
    }

    override fun compareTo(other: ChatMemberItem): Int {
        return this.lastMessageTimeStamp.compareTo(other.lastMessageTimeStamp)
    }

    override fun getLayout() = R.layout.item_chat_member
}