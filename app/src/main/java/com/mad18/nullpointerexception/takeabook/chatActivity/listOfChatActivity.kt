package com.mad18.nullpointerexception.takeabook.chatActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.mad18.nullpointerexception.takeabook.R
import com.mad18.nullpointerexception.takeabook.User
import com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview.ChatMemberItem
import com.mad18.nullpointerexception.takeabook.myProfile.editProfile
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_list_of_chat.*
import org.jetbrains.anko.startActivity


class listOfChatActivity : AppCompatActivity() {

    private lateinit var userListenerRegistration: ListenerRegistration

    private var shouldInitRecyclerView = true
    private lateinit var context:Context
    private lateinit var listOfChatSection: Section
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_chat)
        context = this
        setSupportActionBar(findViewById(R.id.list_of_chat_toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.mychat)
        userListenerRegistration = addUsersListener(this,this::updateRecyclerView)
        //list_of_chat_recycler_view
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_of_chat_toolbar, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                return true
            }
            R.id.action_refresh_chat -> {
                userListenerRegistration = addUsersListener(this,this::updateRecyclerView)
                val snackbar = Snackbar
                        .make(findViewById(R.id.list_of_chat_framelayout), getText(R.string.chat_updated), Snackbar.LENGTH_LONG)
                snackbar.show()
            }
        }


        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeListener(userListenerRegistration)
        shouldInitRecyclerView = true
    }

    private fun updateRecyclerView(items: List<Item>) {

        fun init() {
            list_of_chat_recycler_view.apply {
                layoutManager = LinearLayoutManager(this@listOfChatActivity.context)
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
        if (item is ChatMemberItem) {
            // TODO Fare partire la activity in cui si visualizza la chat
            startActivity<ChatActivity>(
                    AppConstants.USER_NAME to item.person.usr_name,
                    AppConstants.USER_ID to item.userId)
        }
    }

    private fun removeListener(registration: ListenerRegistration) = registration.remove()

    private fun addUsersListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        val engagedUserId = mutableListOf<String>()
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        var userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId.toString())
        userDocRef.collection("engagedChatChannels").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                return@addSnapshotListener
            }

            querySnapshot!!.documents.forEach {
                if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                    engagedUserId.add(it.id)
            }

        }
        return FirebaseFirestore.getInstance().collection("users")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if(engagedUserId.contains(it.id) && it.id != userId.toString()){
                            items.add(ChatMemberItem(it.toObject(User::class.java)!!, it.id, context))
                        }
                        //if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                    }
                    onListen(items)
                }
    }

}
