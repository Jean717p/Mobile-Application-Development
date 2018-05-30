package com.mad18.nullpointerexception.takeabook.requestBook;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RequestBook extends AppCompatActivity {
    private final String TAG = "RequestBook";
    private User user;
    private User bookOwner;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Book requested_book;
    private String requestDocId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent()==null || getIntent().getExtras()==null){
            Log.d(TAG,"Error passing parameters");
        }
        setContentView(R.layout.activity_request_book);
        Toolbar toolbar = findViewById(R.id.request_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        BookWrapper bookWrapper = (BookWrapper) bundle.getParcelable("requested_book");
        UserWrapper uw = (UserWrapper) bundle.getParcelable("otherUser");
        bookOwner = new User(uw);
        requested_book = new Book(bookWrapper);
        requestDocId = "";
        if(MainActivity.thisUser!=null){
            user = MainActivity.thisUser;
        }
        else{
            db.collection("users").document(FirebaseAuth.getInstance().getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                   user = documentSnapshot.toObject(User.class);
                }
            });
        }
        fillRequestBookViews();
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

    @Override
    protected void onResume() {
        super.onResume();
        fillRequestBookViews();
    }

    private void fillRequestBookViews(){
        TextView tv;
        tv = findViewById(R.id.request_book_title);
        tv.setText(requested_book.getBook_title());
        tv = findViewById(R.id.request_book_owner);
        tv.setText(bookOwner.getUsr_name());
        tv.setTextColor(Color.BLUE);
        if(requested_book.getBook_thumbnail_url().length()>0){
            ImageView iw = findViewById(R.id.request_book_main_image);
            GlideApp.with(this).load(requested_book.getBook_thumbnail_url()).placeholder(R.drawable.ic_thumbnail_cover_book).into(iw);
        }
        Button send = findViewById(R.id.request_book_send);
        send.setOnClickListener(view -> {
            DocumentReference newReqRef = db.collection("requests").document();
            TextView textView = findViewById(R.id.request_book_message);
            Loan loan = new Loan(bookOwner.getUsr_id(),user.getUsr_id(),bookOwner.getUsr_name(),user.getUsr_name(),
                    requested_book.getBook_title(),requested_book.getBook_id(),
                    textView.getText().toString(), Calendar.getInstance().getTime(),
                    newReqRef.getId()
                    );
            requestDocId = newReqRef.getId();
            newReqRef.set(loan).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Map<String,Boolean> toUpdate = new HashMap<>();
                    toUpdate.put("owned",false);
                    DocumentReference reqMe = db.collection("users").document(user.getUsr_id())
                            .collection("requests").document(requestDocId);
                    reqMe.set(toUpdate);
                    toUpdate.put("owned",true);
                    DocumentReference reqOwner = db.collection("users").document(bookOwner.getUsr_id())
                            .collection("requests").document(requestDocId);
                    reqOwner.set(toUpdate);
                }
            });
            send.setVisibility(View.GONE);
            textView.setEnabled(false);
            Snackbar.make(findViewById(R.id.request_book_send),
                    R.string.request_book_sent, Snackbar.LENGTH_LONG).show();
        });
        Button cancel = findViewById(R.id.request_book_cancel);
        cancel.setOnClickListener(view -> {
            if(requestDocId.length()==0){
                onBackPressed();
                return;
            }
            DocumentReference newReqRef = db.collection("requests").document(requestDocId);
            newReqRef.delete();
            DocumentReference reqMe = db.collection("users").document(user.getUsr_id())
                    .collection("requests").document(requestDocId);
            reqMe.delete();
            DocumentReference reqOwner = db.collection("users").document(bookOwner.getUsr_id())
                    .collection("requests").document(requestDocId);
            reqOwner.delete();
            onBackPressed();
        });
    }
}
