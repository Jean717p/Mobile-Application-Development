package com.mad18.nullpointerexception.takeabook.chatActivity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.firestore.ListenerRegistration
import com.mad18.nullpointerexception.takeabook.R
import com.mad18.nullpointerexception.takeabook.chatActivity.AppConstants.USER_ID
import com.mad18.nullpointerexception.takeabook.chatActivity.AppConstants.USER_NAME
import com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview.chatMemberItem
import com.mad18.nullpointerexception.takeabook.chatActivity.*
import com.mad18.nullpointerexception.takeabook.util.FirestoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import org.jetbrains.anko.support.v4.startActivity
import kotlinx.android.synthetic.main.activity_list_of_chat.*
import org.jetbrains.anko.startActivity


class listOfChatActivity : AppCompatActivity() {

    private lateinit var userListenerRegistration: ListenerRegistration

    private var shouldInitRecyclerView = true

    private lateinit var listOfChatSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_chat)
        setSupportActionBar(findViewById(R.id.main_toolbar))
        // TODO: Qua devo prendere la lista di utenti da firebase

    }


    override fun onDestroy() {
        super.onDestroy()
        //FirestoreUtil.removeListener(userListenerRegistration)
        shouldInitRecyclerView = true
    }

    private fun updateRecyclerView(items: List<Item>) {

        fun init() {

            list_of_chat_recycler_view.apply {
                adapter = GroupAdapter<ViewHolder>().apply {
                    listOfChatSection = Section(items)
                    add(listOfChatSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = listOfChatSection.update(items)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()
    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is chatMemberItem) {
            // TODO Fare partire la activity in cui si visualizza la chat
            startActivity<ChatActivity>(
                    USER_NAME to item.person.usr_name,
                    USER_ID to item.userId)
        }
    }

}
