package com.mad18.nullpointerexception.takeabook.requestBook;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.Calendar;
import java.util.Date;

public class RequestReview extends AppCompatActivity {

    User otherUser, myUser;
    Book bookToReview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_review);
        Toolbar toolbar = findViewById(R.id.request_review_toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.request_review_review));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        UserWrapper uw = (UserWrapper) bundle.getParcelable("otherUser");
        otherUser = new User(uw);
        BookWrapper bookWrapper = (BookWrapper) bundle.getParcelable("bookToReview");
        bookToReview = new Book(bookWrapper);
        UserWrapper uw1 = (UserWrapper) bundle.getParcelable("thisUser");
        myUser = new User(uw1);
        fillCardViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.request_review_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.request_review_action_send:
                sendDataFirebase();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillCardViews(){
        TextView textView =findViewById(R.id.request_review_username);
        textView.setText(otherUser.getUsr_name());
        if(otherUser.getProfileImgStoragePath().length()>0){
            GlideApp.with(this).load(otherUser.getProfileImgStoragePath())
                    .placeholder(R.drawable.ic_account_circle_white_48px)
                    .into((ImageView) findViewById(R.id.request_review_user_image));
        }
        if(myUser.getUsr_id().equals(bookToReview.getBook_userid())){
            CardView cw = findViewById(R.id.request_review_cardview_book);
            cw.setVisibility(View.GONE);
        }
        else{
            textView = findViewById(R.id.request_review_book_title);
            textView.setText(bookToReview.getBook_title());
            textView = findViewById(R.id.request_review_author);
            textView.setText(bookToReview.getBook_first_author());
            if(bookToReview.getBook_thumbnail_url().length()>0){
                GlideApp.with(this).load(bookToReview.getBook_thumbnail_url())
                        .placeholder(R.drawable.ic_thumbnail_cover_book)
                        .into((ImageView)findViewById(R.id.request_review_book_image));
            }
        }
    }

    private void sendDataFirebase(){
        String user_text, book_text;
        int stars_book, stars_user;
        EditText et = findViewById(R.id.request_review_user_edit_text);
        user_text = et.getText().toString();
        et = findViewById(R.id.request_review_book_edit_text);
        book_text = et.getText().toString();
        RatingBar rb = findViewById(R.id.request_review_ratingbar);
        stars_user = rb.getNumStars();
        rb = findViewById(R.id.request_review_ratingbar_user);
        stars_book = rb.getNumStars();
        Date myDate = Calendar.getInstance().getTime();
        if(stars_book != 0){
            Review rw_book = new Review(myUser.getUsr_id(), myUser.getUsr_name(),myUser.getProfileImgStoragePath(), book_text, stars_book,myDate);
            FirebaseFirestore.getInstance().collection("books").document(bookToReview.getBook_id())
                    .collection("reviews").add(rw_book);
        }
        else{
            if(book_text.length() > 0){
                Toast.makeText(this, R.string.request_review_no_rating,Toast.LENGTH_SHORT).show();
            }
        }
        if(stars_user != 0){
            Review rw_user = new Review(myUser.getUsr_id(), myUser.getUsr_name(),myUser.getProfileImgStoragePath(), user_text, stars_user,myDate);
            FirebaseFirestore.getInstance().collection("user").document(otherUser.getUsr_id()).collection("reviews")
                    .add(rw_user);
        }
        else{
            if(user_text.length() != 0){
                Toast.makeText(this, R.string.request_review_no_rating,Toast.LENGTH_SHORT).show();
            }
        }

    }
}

