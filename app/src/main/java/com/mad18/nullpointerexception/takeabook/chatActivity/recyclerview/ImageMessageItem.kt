package com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview

import android.content.Context
import com.bumptech.glide.Glide
import com.mad18.nullpointerexception.takeabook.R
import com.mad18.nullpointerexception.takeabook.chatActivity.model.ImageMessage
import com.mad18.nullpointerexception.takeabook.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_image_message.*


class ImageMessageItem(val message: ImageMessage,
                       val context: Context)
    : MessageItem(message) {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.item_image_message_imageView_message_image.setImageResource(R.drawable.ic_image_black_24dp)
        Glide.with(context)
                .load(StorageUtil.pathToReference(message.imagePath))
                .into(viewHolder.item_image_message_imageView_message_image)
    }

    override fun getLayout() = R.layout.item_image_message

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is ImageMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? ImageMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}