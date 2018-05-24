package com.mad18.nullpointerexception.takeabook.util

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mad18.nullpointerexception.takeabook.User
import com.mad18.nullpointerexception.takeabook.chatActivity.model.*

import com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview.ImageMessageItem
import com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview.TextMessageItem
import com.xwray.groupie.kotlinandroidextensions.Item
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity


object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    private val chatChannelsCollectionRef = firestoreInstance.collection("chatChannels")

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun getOrCreateChatChannel(otherUserId: String,
                               onComplete: (channelId: String) -> Unit) {
        currentUserDocRef.collection("engagedChatChannels")
                .document(otherUserId).get().addOnSuccessListener {
                    if (it.exists()) {
                        onComplete(it["channelId"] as String)
                        return@addOnSuccessListener
                    }

                    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                    val newChannel = chatChannelsCollectionRef.document()
                    newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                    currentUserDocRef
                            .collection("engagedChatChannels")
                            .document(otherUserId)
                            .set(mapOf("channelId" to newChannel.id))

                    firestoreInstance.collection("users").document(otherUserId)
                            .collection("engagedChatChannels")
                            .document(currentUserId)
                            .set(mapOf("channelId" to newChannel.id))

                    onComplete(newChannel.id)
                }
    }

    fun addChatMessagesListener(channelId: String, context: Context,
                                onListen: (List<Item>) -> Unit): ListenerRegistration {
        return chatChannelsCollectionRef.document(channelId).collection("messages")
                .orderBy("time")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "ChatMessagesListener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if (it["type"] == MessageType.TEXT)
                            items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                        else
                            items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))
                        return@forEach
                    }
                    onListen(items)
                }
    }

    fun sendMessage(message: Message, channelId: String,otherUserId: String) {
        chatChannelsCollectionRef.document(channelId)
                .collection("messages")
                .add(message)
        FirebaseFirestore.getInstance().collection("users").document(otherUserId)
                .get().addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot==null || documentSnapshot.exists()==false){
                        return@addOnSuccessListener
                    }
                    val otherUser = documentSnapshot.toObject(User::class.java)
                    val myUser:User = MainActivity.thisUser
                    if(myUser==null){
                        FirebaseFirestore.getInstance().collection("users")
                                .document(FirebaseAuth.getInstance().uid!!).get().addOnSuccessListener {
                            documentSnapshot ->
                            if(documentSnapshot == null || !documentSnapshot.exists()){
                                return@addOnSuccessListener
                            }
                            val myUser = documentSnapshot.toObject(User::class.java)
                            otherUser!!.registrationTokens.forEach {
                                val notification = NotificationMessage(myUser!!.usr_name, message.time,
                                        FirebaseAuth.getInstance().uid.toString(), it, "New Message!",
                                        "","Book Circle")
                                FirebaseFirestore.getInstance().collection("notifications").add(notification)
                             }

                                }
                    }
                    else {
                        otherUser!!.registrationTokens.forEach {
                            val notification = NotificationMessage(myUser.usr_name, message.time,
                                    FirebaseAuth.getInstance().uid.toString(), it, "New Message!","","Book Circle")
                            FirebaseFirestore.getInstance().collection("notifications").add(notification)
                        }
                    }
                }
    }

    fun getChatOfUser(otherUserId: String, myUserId: String){
        val db = FirebaseFirestore.getInstance()
        val userRef:DocumentReference = db.collection("user").document(myUserId)
//        userRef.get().addOnSuccessListener {  documentSnapshot ->
//            val user = documentSnapshot.toObject(User::class.java)
//            val channelList:MutableMap<String,String> = user.getChannels()
//            val channel = channelList.get(otherUserId);
//            db.collection("chats").document(channel).get().addOnSuccessListener { documentSnapshot ->
//                documentSnapshot.get("");
//                documentSnapshot.toObject(chat::java.class);
//            }
//
//
//        }


    }


    //region FCM

    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        var userCollection = db.collection("users")
        userCollection.document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
        val db = FirebaseFirestore.getInstance()
        var userCollection = db.collection("users")
        userCollection.document(FirebaseAuth.getInstance().currentUser!!.uid).update(mapOf("registrationTokens" to registrationTokens))
    }
    //endregion FCM
}
