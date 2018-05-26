package com.mad18.nullpointerexception.takeabook.chatActivity


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mad18.nullpointerexception.takeabook.R
import com.mad18.nullpointerexception.takeabook.chatActivity.AppConstants.USER_ID
import com.mad18.nullpointerexception.takeabook.chatActivity.AppConstants.USER_NAME
import com.mad18.nullpointerexception.takeabook.chatActivity.model.ImageMessage
import com.mad18.nullpointerexception.takeabook.chatActivity.model.MessageType
import com.mad18.nullpointerexception.takeabook.chatActivity.model.TextMessage
import com.mad18.nullpointerexception.takeabook.util.FirestoreUtil
import com.mad18.nullpointerexception.takeabook.util.StorageUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.util.Calendar

private const val RC_SELECT_IMAGE = 2
private const val RC_CAMERA_CAPTURE = 3
private const val REQUEST_PERMISSION_GALLERY = 4
private const val REQUEST_PERMISSION_CAMERA = 5

class ChatActivity : AppCompatActivity() {

    private lateinit var currentChannelId: String
    private lateinit var menu: Menu
    private lateinit var messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section
    private lateinit var otherUserId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(findViewById(R.id.chat_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(USER_NAME)

        otherUserId = intent.getStringExtra(USER_ID)
        FirestoreUtil.getOrCreateChatChannel_2(otherUserId) { channelId,otherUserId ->
            currentChannelId = channelId
            messagesListenerRegistration =
                    FirestoreUtil.addChatMessagesListener(channelId, this, this::updateRecyclerView)
            chat_imageView_send.setOnClickListener {
                val textToSend = chat_editText_message.text.toString()
                if(textToSend.isNotEmpty()){
                    val messageToSend =
                            TextMessage(textToSend, Calendar.getInstance().time,
                                    FirebaseAuth.getInstance().currentUser!!.uid, MessageType.TEXT)
                    chat_editText_message.setText("")
                    FirestoreUtil.sendMessage(messageToSend, channelId,otherUserId)
                }
            }
            chat_fab_send_image.setOnClickListener {
                selectUserImg()
            }
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("engagedChatChannels").document(otherUserId)
                    .update("pending",false)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().currentUser!!.uid).collection("engagedChatChannels")
                .document(otherUserId).update("lastReadByUser",Calendar.getInstance().time)
        super.onBackPressed()
    }

    private fun selectUserImg() {
        val pictureDialog = AlertDialog.Builder(this)
        val pictureDialogItems = arrayOf(getString(R.string.photo_from_gallery), getString(R.string.photo_from_camera))
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> choosePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    fun choosePhotoFromGallery() {
        if (ActivityCompat.checkSelfPermission(this@ChatActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@ChatActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_GALLERY)
            return
        }
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
    }

    private fun choosePhotoFromCamera() {
        if (ActivityCompat.checkSelfPermission(this@ChatActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this@ChatActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@ChatActivity, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CAMERA)
            return
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, RC_CAMERA_CAPTURE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        //dopo che l'utente ci ha fornito la risposta alla richesta di permessi
        when (requestCode) {
            REQUEST_PERMISSION_GALLERY -> if (grantResults.size > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePhotoFromGallery()
                }
            }
            REQUEST_PERMISSION_CAMERA -> if (grantResults.size > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    choosePhotoFromCamera()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
                data != null && data.data != null) {
            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectedImageBytes = outputStream.toByteArray()
            StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
                val messageToSend =
                        ImageMessage(imagePath, Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid)
                FirestoreUtil.sendMessage(messageToSend, currentChannelId,otherUserId)
            }
        }
        if(requestCode == RC_CAMERA_CAPTURE && resultCode == Activity.RESULT_OK &&
                data != null){
            var photoImg = data.getExtras()!!.get("data") as Bitmap
            val outputStream = ByteArrayOutputStream()
            photoImg.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
                val messageToSend =
                        ImageMessage(imagePath, Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid)
                FirestoreUtil.sendMessage(messageToSend, currentChannelId,otherUserId)
            }
        }
    }

    private fun updateRecyclerView(messages: List<Item>) {
        fun init() {

            chat_recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    messagesSection = Section(messages)
                    this.add(messagesSection)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = messagesSection.update(messages)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()

        chat_recycler_view_messages.scrollToPosition(chat_recycler_view_messages.adapter.itemCount - 1)
    }
}
