package com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview

import android.content.Context
import com.bumptech.glide.Glide
import com.mad18.nullpointerexception.takeabook.R
import com.mad18.nullpointerexception.takeabook.MyGlideModule
import com.mad18.nullpointerexception.takeabook.User
import com.mad18.nullpointerexception.takeabook.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_chat_member.*


class chatMemberItem(val person: User,
                 val userId: String,
                 private val context: Context)
    : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {


    }

    override fun getLayout() = R.layout.item_chat_member
}