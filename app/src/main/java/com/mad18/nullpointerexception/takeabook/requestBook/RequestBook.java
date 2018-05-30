package com.mad18.nullpointerexception.takeabook.requestBook;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;


import java.util.HashMap;
import java.util.Map;

public class RequestBook extends AppCompatActivity {
    private BookWrapper requested_book;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        requested_book = (BookWrapper) bundle.getParcelable("requested_book");
        setContentView(R.layout.request_book);

        Button send = findViewById(R.id.request_book_send);
        send.setOnClickListener(view -> {
            DocumentReference newReqRef = db.collection("requests").document();
            Map<String, Object> data = new HashMap<>();
            data.put("book_id", requested_book.getBook_id());
            data.put("book_owner", requested_book.getUser_id());
            data.put("requester", user.getUid());
            data.put("status", "pending");
            data.put("req_id", newReqRef);
            newReqRef.set(data)
                    .addOnSuccessListener(documentReference -> Log.d("Request Book", "DocumentSnapshot written with ID: "));
            DocumentReference userOwner = db.collection("users").document(requested_book.getUser_id());
            // Update one field, creating the document if it does not already exist.
//            Map<Object, Boolean> data2 = new HashMap<>();
//            data2.put(newReqRef, true);
//            Map<String, Object> requested = new HashMap<>();
//            requested.put("requested",data2);
//
//            db.collection("users").document(requested_book.getUser_id())
//                    .set(requested, SetOptions.merge());
            
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fillRequestBookViews();
    }

    private void fillRequestBookViews(){
        TextView tv;
        tv = findViewById(R.id.request_book_title);
        tv.setText(requested_book.getTitle());
        tv = findViewById(R.id.request_book_author);
        String tmp;
        tmp = requested_book.getAuthors().toString();
        if (tmp.length() > 2) {
            tv.setText(tmp.substring(1, tmp.length() - 1));
        }
        tv = findViewById(R.id.request_book_ISBN);
        tv.setText(requested_book.getISBN());

        tv = findViewById(R.id.request_book_editionYear);
        if (requested_book.getEditionYear() == 0) {
            tv.setText(R.string.add_book_info_not_available);
        } else {
            tv.setText(Integer.toString(requested_book.getEditionYear()));
        }

        tv = findViewById(R.id.request_book_pages);
        if (requested_book.getPages() == 0) {
            tv.setText(R.string.add_book_info_not_available);
        } else {
            tv.setText(Integer.toString(requested_book.getPages()));

        }


        tv = findViewById(R.id.request_book_publisher);
        if (requested_book.getPublisher().length() == 0) {
            tv.setText(getString(R.string.add_book_info_not_available));
        } else {
            tv.setText(requested_book.getPublisher());
        }


        tv = findViewById(R.id.request_book_categories);
        tmp = requested_book.getCategories().toString();
        if (tmp.length() > 2) {
            tv.setText(tmp.substring(1, tmp.length() - 1));
        }

        ImageView iw = findViewById(R.id.request_book_main_image);
        Book book = new Book(requested_book);

        tv = findViewById(R.id.request_book_book_conditions);
        switch (book.getBook_condition()) {

            case 0:
                tv.setText(getString(R.string.add_book_info_not_available));
                break;
            case 1:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[1]);
                break;
            case 2:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[2]);
                break;
            case 3:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[3]);
                break;
        }
        Glide.with(this).load(book.getBook_thumbnail_url()).into(iw);


    }
}
