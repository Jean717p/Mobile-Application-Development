package com.mad18.nullpointerexception.takeabook.chatActivity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.mad18.nullpointerexception.takeabook.R
import com.mad18.nullpointerexception.takeabook.User
import com.mad18.nullpointerexception.takeabook.chatActivity.model.*
import com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview.ChatMemberItem
import com.mad18.nullpointerexception.takeabook.util.FirestoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_list_of_chat.*
import org.jetbrains.anko.startActivity
import java.util.Date
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction3
import kotlin.reflect.KFunction4

class ListOfChatActivity : AppCompatActivity() {

    private lateinit var userListenerRegistration: ListenerRegistration

    private var shouldInitRecyclerView = true
    private lateinit var context:Context
    private lateinit var listOfChatSection: Section
    private lateinit var menu: Menu

    companion object {
        var chatStatusMap:MutableMap<String,ChatStatus> = mutableMapOf<String,ChatStatus>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_chat)
        context = this
        setSupportActionBar(findViewById(R.id.list_of_chat_toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.mychat)
        userListenerRegistration = addUsersListener(this,this::checkNewMessages,this::sortAndUpdate,this::updateRecyclerView)
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
                userListenerRegistration = addUsersListener(this,this::checkNewMessages,this::sortAndUpdate,this::updateRecyclerView)
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

    private fun updateRecyclerView(items: List<ChatMemberItem>) {

        fun init() {
            list_of_chat_recycler_view.apply {
                layoutManager = LinearLayoutManager(this@ListOfChatActivity.context)
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
            item.unreadMessages =  0
            item.notifyChanged()
        }
    }

    private fun removeListener(registration: ListenerRegistration) = registration.remove()

    private fun addUsersListener(myActivity: Activity,
                                 checkListen: KFunction4<
                                         @ParameterName(name = "myActivity") Activity,
                                         @ParameterName(name = "items") List<ChatMemberItem>,
                                         @ParameterName(name = "sortListen") KFunction3<
                                                 @ParameterName(name = "myActivity") Activity,
                                                 @ParameterName(name = "items") List<ChatMemberItem>,
                                                 @ParameterName(name = "updateListen") (List<ChatMemberItem>) -> Unit, Unit>,
                                         @ParameterName(name = "updateListen") KFunction1<
                                                 @ParameterName(name = "items") List<ChatMemberItem>, Unit>, Unit>,
                                 sortListen: KFunction3<@ParameterName(name = "myActivity") Activity,
                                         @ParameterName(name = "items") List<ChatMemberItem>,
                                         @ParameterName(name = "updateListen") (List<ChatMemberItem>) -> Unit, Unit>,
                                 updateListen: KFunction1<@ParameterName(name = "items") List<ChatMemberItem>, Unit>
    )
            : ListenerRegistration {
        val engagedUserId = mutableListOf<String>()
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        var userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId.toString())
        userDocRef.collection("engagedChatChannels").addSnapshotListener(myActivity) { querySnapshot, firebaseFirestoreException ->
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
                    val items = mutableListOf<ChatMemberItem>()
                    querySnapshot!!.documents.forEach {
                        if(engagedUserId.contains(it.id) && it.id != userId.toString()){
                            //REVIEW: Da controllare nuove chat a runtime
                            items.add(ChatMemberItem(it.toObject(User::class.java)!!, it.id,Date(0),0,"", myActivity))
                            if(chatStatusMap.containsKey(it.id)==false){
                                chatStatusMap.put(it.id,ChatStatus(it.id, Date(0),0,""))
                            }
                        }
                    }
                    checkListen(myActivity,items,sortListen,updateListen)
                }
    }

    private fun checkNewMessages(myActivity: Activity,
                                 items: List<ChatMemberItem>,
                                 sortListen: KFunction3<
                                         @ParameterName(name = "myActivity") Activity,
                                         @ParameterName(name = "items") List<ChatMemberItem>,
                                         @ParameterName(name = "updateListen") (List<ChatMemberItem>) -> Unit, Unit>,
                                 updateListen: KFunction1<@ParameterName(name = "items") List<ChatMemberItem>, Unit>) {
        items.forEach {
            FirestoreUtil.getOrCreateChatChannel(it.userId) { channelId ->
                val otherUserId: String = it.userId
                FirebaseFirestore.getInstance().collection("chatChannels")
                        .document(channelId).collection("messages")
                        .whereGreaterThan("time", it.lastMessageTimeStamp)
                        .orderBy("time")
                        .addSnapshotListener(myActivity) { snapshot, firebaseFirestoreException ->
                            if (firebaseFirestoreException != null || snapshot == null) {
                                Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                                return@addSnapshotListener
                            }
                            if (snapshot.isEmpty || snapshot.documents.isEmpty()) {
                                return@addSnapshotListener
                            }
                            val lastMessage = if(snapshot.documents[snapshot.documents.size - 1]
                                            .getString("type").equals(MessageType.TEXT)){
                                snapshot.documents[snapshot.documents.size - 1].toObject(TextMessage::class.java)
                            }else{
                                snapshot.documents[snapshot.documents.size - 1].toObject(ImageMessage::class.java)
                            }
                            val msg: String = if (lastMessage is TextMessage) {
                                lastMessage.text
                            } else {
                                getString(R.string.chat_photo)
                            }
                            ListOfChatActivity.chatStatusMap.put(otherUserId,
                                    ChatStatus(otherUserId, lastMessage!!.time, snapshot.size(), msg))
                            sortListen(myActivity,items,updateListen)
                            //REVIEW: Da eliminare il listener dopo l'esecuzione mi sa...
                        }
            }
        }
    }
    private fun sortAndUpdate(myActivity: Activity,
                                 items:List<ChatMemberItem>,
                                 updateListen: (List<ChatMemberItem>) -> Unit){
        val tmp = mutableListOf<ChatMemberItem>()
        val sorted = ListOfChatActivity.chatStatusMap.entries.sortedByDescending {
            entry: MutableMap.MutableEntry<String, ChatStatus> ->
            entry.value
        }
        ListOfChatActivity.chatStatusMap = sorted.map {
            entry: MutableMap.MutableEntry<String, ChatStatus> -> entry.key to entry.value
        }.toMap().toMutableMap()
        for(x in sorted){
            val s:String = x.key
            for(y:ChatMemberItem in items){
                if(s.equals(y.userId)){
                    tmp.add(ChatMemberItem(y.person,y.userId,x.value.timeStamp,x.value.unreadMessages,x.value.text,myActivity))
                    break
                }
            }
        }
        updateListen(tmp)
    }
}
