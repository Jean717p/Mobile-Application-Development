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
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.mad18.nullpointerexception.takeabook.R
import com.mad18.nullpointerexception.takeabook.util.User
import com.mad18.nullpointerexception.takeabook.chatActivity.model.ImageMessage
import com.mad18.nullpointerexception.takeabook.chatActivity.model.MessageType
import com.mad18.nullpointerexception.takeabook.chatActivity.model.TextMessage
import com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview.ChatMemberItem
import com.mad18.nullpointerexception.takeabook.util.FirestoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_list_of_chat.*
import org.jetbrains.anko.startActivity
import java.util.*
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction3
import kotlin.reflect.KFunction4

class ListOfChatActivity : AppCompatActivity() {

    private var shouldInitRecyclerView = true
    private lateinit var context:Context
    private lateinit var listOfChatSection: Section
    private lateinit var menu: Menu
    private var engagedUserId = mutableMapOf<String,Date>()
    private lateinit var firestoreListener: ListenerRegistration

    private companion object {
        var itemsMap:MutableMap<String,ChatMemberItem> = mutableMapOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_chat)
        context = this
        setSupportActionBar(findViewById(R.id.list_of_chat_toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.mychat)
        //list_of_chat_recycler_view
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId.toString())
        if(shouldInitRecyclerView && itemsMap.size>0){
            itemsMap.values.forEach { chatMemberItem: ChatMemberItem ->
                chatMemberItem.context = this
            }
            sortAndUpdate(this, itemsMap.values.toList(),this::updateRecyclerView)
        }
        firestoreListener = userDocRef.collection("engagedChatChannels")
                .addSnapshotListener(EventListener{ querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                        return@EventListener
                    }
                    querySnapshot!!.documents.forEach {
                        if (it.id != FirebaseAuth.getInstance().currentUser?.uid) {
                            val datetmp:Date
                            if(it.get("lastReadByUser") != null){
                                datetmp = it.get("lastReadByUser") as Date
                            }
                            else{
                                datetmp = Date(0)
                            }
                            engagedUserId.put(it.id,datetmp)
                        }
                    }
                    updateItemsMap(this,this::checkNewMessages,this::sortAndUpdate,this::updateRecyclerView)
                })
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
                checkChannelChanges()
                val snackbar = Snackbar
                        .make(findViewById(R.id.list_of_chat_framelayout), getString(R.string.Updating), Snackbar.LENGTH_LONG)
                snackbar.show()
//                val snackbar = Snackbar
//                        .make(findViewById(R.id.list_of_chat_framelayout), getText(R.string.chat_updated), Snackbar.LENGTH_LONG)
//                snackbar.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListener.remove()
        shouldInitRecyclerView = true
    }

    fun updateRecyclerView(items: List<ChatMemberItem>) {

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

        fun updateItems() {
            listOfChatSection.update(items)
            listOfChatSection.notifyChanged()
        }

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()

    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is ChatMemberItem) {
            startActivity<ChatActivity>(
                    AppConstants.USER_NAME to item.person.usr_name,
                    AppConstants.USER_ID to item.userId)
        }
    }

    private fun checkChannelChanges(){
        val userDocRef = FirebaseFirestore.getInstance()
                .collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        userDocRef.collection("engagedChatChannels")
                .get().addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if(task==null || task.isSuccessful==false || task.result.isEmpty){
                        return@addOnCompleteListener
                    }
                    var toUpdate:Boolean = false
                    task.getResult().forEach { queryDocumentSnapshot: QueryDocumentSnapshot? ->
                        if(queryDocumentSnapshot!!.id != FirebaseAuth.getInstance().currentUser!!.uid){
                            val datetmp:Date
                            if(queryDocumentSnapshot.get("lastReadByUser") != null){
                                datetmp = queryDocumentSnapshot.get("lastReadByUser") as Date
                            }
                            else{
                                datetmp = Date(0)
                            }
                            if(engagedUserId.containsKey(queryDocumentSnapshot.id)==false ||
                                    engagedUserId[queryDocumentSnapshot.id]!!.before(datetmp)){
                                engagedUserId.put(queryDocumentSnapshot.id,datetmp)
                                toUpdate = true
                            }
                        }
                    }
                    if(toUpdate){
                        updateItemsMap(this,this::checkNewMessages,this::sortAndUpdate,this::updateRecyclerView)
                    }
                }
    }

    private fun updateItemsMap(myActivity: Activity,
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
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseFirestore.getInstance().collection("users")
                .get().addOnCompleteListener { task1 ->
                    if (task1.isSuccessful) {
                        val snap1 = task1.getResult()
                        snap1!!.documents.forEach {
                            if (engagedUserId.containsKey(it.id) && it.id != userId.toString()) {
                                val x = ChatMemberItem(it.toObject(User::class.java)!!, it.id, engagedUserId[it.id]!!, Date(0), 0, "", myActivity)
                                if (itemsMap.containsKey(it.id) == false) {
                                    itemsMap.put(it.id, x)
                                } else {
                                    itemsMap.get(it.id)!!.lastReadByUser = engagedUserId[it.id]!!
                                    itemsMap.get(it.id)!!.person.usr_name = x.person.usr_name
                                    itemsMap.get(it.id)!!.person.profileImgStoragePath = x.person.profileImgStoragePath
                                }
                            }
                        }
                        checkListen(myActivity, itemsMap.values.toList(), sortListen, updateListen)
                    }
                }
    }

    fun checkNewMessages(myActivity: Activity,
                         items: List<ChatMemberItem>,
                         sortListen: KFunction3<
                                 @ParameterName(name = "myActivity") Activity,
                                 @ParameterName(name = "items") List<ChatMemberItem>,
                                 @ParameterName(name = "updateListen") (List<ChatMemberItem>) -> Unit, Unit>,
                         updateListen: KFunction1<@ParameterName(name = "items") List<ChatMemberItem>, Unit>) {
        items.forEach {
            FirestoreUtil.getOrCreateChatChannel_2(it.userId) { channelId,otherUserId ->
                FirebaseFirestore.getInstance().collection("chatChannels")
                        .document(channelId).collection("messages")
                        .orderBy("time",Query.Direction.DESCENDING)
                        .limit(1)
                        .get().addOnCompleteListener { myTask ->
                            val snapshot:QuerySnapshot
                            if(myTask== null || myTask.isSuccessful==false){
                                sortListen(myActivity,items,updateListen)
                                return@addOnCompleteListener
                            }
                            else{
                                snapshot = myTask.getResult()
                            }
                            if (snapshot == null || snapshot.isEmpty || snapshot.documents.isEmpty()) {
                                return@addOnCompleteListener
                            }
                            val lastMessage = if (snapshot.documents[snapshot.documents.size - 1]
                                            .getString("type").equals(MessageType.TEXT)) {
                                snapshot.documents[snapshot.documents.size - 1].toObject(TextMessage::class.java)
                            } else {
                                snapshot.documents[snapshot.documents.size - 1].toObject(ImageMessage::class.java)
                            }
                            val x = itemsMap[otherUserId]!!
                            if(lastMessage!!.time.after(x.lastMessageTimeStamp)){
                                //if new message update and check how many new msg are there...
                                val msg: String = if (lastMessage is TextMessage) {
                                    lastMessage.text
                                } else {
                                    getString(R.string.chat_photo)
                                }
                                x.lastMessageTimeStamp = lastMessage!!.time
                                x.lastMessage = msg
                                FirestoreUtil.getOrCreateChatChannel_2(x.userId) { channelId,otherUserId ->
                                    FirebaseFirestore.getInstance().collection("chatChannels")
                                            .document(channelId).collection("messages")
                                            .whereGreaterThan("time", itemsMap[otherUserId]!!.lastReadByUser)
                                            .orderBy("time")
                                            .get()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val snap = task.getResult()
                                                    if (snap == null || snap.isEmpty || snap.documents.isEmpty()) {
                                                        itemsMap[otherUserId]!!.unreadMessages = 0
                                                        sortListen(myActivity,itemsMap.values.toList(),updateListen)
                                                        return@addOnCompleteListener
                                                    }
                                                    itemsMap[otherUserId]!!.unreadMessages = snap.documents.size
                                                    sortListen(myActivity,itemsMap.values.toList(),updateListen)
                                                }
                                                else{
                                                    itemsMap[otherUserId]!!.unreadMessages = 0
                                                    sortListen(myActivity,itemsMap.values.toList(),updateListen)
                                                    return@addOnCompleteListener
                                                }
                                            }
                                }
                            }
                            else if(x.lastMessageTimeStamp.before(x.lastReadByUser)
                                    && x.unreadMessages > 0){
                                x.unreadMessages = 0
                                sortListen(myActivity, itemsMap.values.toList(),updateListen)
                            }
                        }
            }
        }
    }

    fun sortAndUpdate(myActivity: Activity,
                      items:List<ChatMemberItem>,
                      updateListen: (List<ChatMemberItem>) -> Unit){
        val tmp = mutableListOf<ChatMemberItem>()
        tmp.addAll(items)
        val sorted = tmp.sortedByDescending { chatMemberItem: ChatMemberItem -> chatMemberItem }
        updateListen(sorted)
    }
}
