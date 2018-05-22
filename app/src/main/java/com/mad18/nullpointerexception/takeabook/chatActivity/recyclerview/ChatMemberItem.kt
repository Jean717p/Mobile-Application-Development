package com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview

import android.content.Context
import com.bumptech.glide.Glide
import com.mad18.nullpointerexception.takeabook.R
import com.mad18.nullpointerexception.takeabook.User
import com.mad18.nullpointerexception.takeabook.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_chat_member.*


class ChatMemberItem(val person: User,
                     val userId: String,
                     private val context: Context)
    : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.item_chat_member_name.text = person.usr_name
        if(person.profileImgStoragePath.length>0){
            Glide.with(context).load(StorageUtil.pathToReference(person.profileImgStoragePath))
                    .into(viewHolder.item_chat_member_profile_picture)
        }
        else {
            viewHolder.item_chat_member_profile_picture.setImageResource(R.drawable.ic_person_black_24dp)
        }
    }

    override fun getLayout() = R.layout.item_chat_member
}