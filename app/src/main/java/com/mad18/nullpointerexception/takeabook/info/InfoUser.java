package com.mad18.nullpointerexception.takeabook.info;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.chatActivity.AppConstants;
import com.mad18.nullpointerexception.takeabook.chatActivity.ChatActivity;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.ArrayList;

public class InfoUser extends AppCompatActivity {
    private FirebaseFirestore db;
    private User otherUser;
    private Menu menu;
    User u;
    private Context context;
    private Button chat_fab;

    CardView showBooks;
    CardView showReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_user);
        db = FirebaseFirestore.getInstance();
        Toolbar toolbar = findViewById(R.id.info_user_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_info_user);
        context = getApplicationContext();
        toolbar.setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        chat_fab = findViewById(R.id.info_user_button_message);
        showBooks = findViewById(R.id.info_user_library_cv);
        showReviews = findViewById(R.id.info_user_reviews_cv);
        UserWrapper userWrapper = getIntent().getExtras().getParcelable("otherUser");
        otherUser = new User(userWrapper);
        fillInfoUserViews();

        if(chat_fab!=null){
            chat_fab.setOnClickListener(v -> {
                Intent chatIntent = new Intent(this, ChatActivity.class);
                chatIntent.putExtra(AppConstants.USER_NAME, otherUser.getUsr_name());
                chatIntent.putExtra(AppConstants.USER_ID, otherUser.getUsr_id());
                startActivity(chatIntent);
            });
        }
        ArrayList<String> userBooks = new ArrayList<>();
        db.collection("users").document(otherUser.getUsr_id()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot userdoc = task.getResult();
               User user  = userdoc.toObject(User.class);
               u = user;
               for(String x : user.getUsr_books().keySet()){

                   userBooks.add(x);
               }

               if(showBooks!=null){
                   showBooks.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Intent showBooksIntent = new Intent(InfoUser.this ,InfoUserShowBooks.class);
                           Bundle bundle = new Bundle();
                           UserWrapper userWrapper = new UserWrapper(u);
                           bundle.putParcelable("user",userWrapper);
                           bundle.putStringArrayList("UserBooks", userBooks);
                           showBooksIntent.putExtras(bundle);
                           startActivity(showBooksIntent);
                       }
                   });
               }


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillInfoUserViews();
    }

    private void fillInfoUserViews(){
        TextView tv = findViewById(R.id.info_user_username);
        tv.setText(otherUser.getUsr_name());
        tv = findViewById(R.id.info_user_city);
        tv.setText(otherUser.getUsr_city());
        tv = findViewById(R.id.info_user_about_me);
        tv.setText(otherUser.getUsr_about());
        if (otherUser.getUsr_about().length() > 0) {
            tv.setText(otherUser.getUsr_about());
        }else{
            CardView cardView = findViewById(R.id.info_user_about_cv);
            cardView.setVisibility(View.GONE);
        }
        if(otherUser.getProfileImgStoragePath().length() > 0){
            ImageView iv = findViewById(R.id.info_user_photo_profile);
            StorageReference mImageRef = FirebaseStorage.getInstance().getReference(otherUser.getProfileImgStoragePath());
            GlideApp.with(this).load(mImageRef).placeholder(R.drawable.ic_account_circle_white_48px).into(iv);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

