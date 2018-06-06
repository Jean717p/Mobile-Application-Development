package com.mad18.nullpointerexception.takeabook.info;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.Review;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

public class ShowReviews extends AppCompatActivity {
    private final String TAG="ShowReviews";
    private ShowReviewsRecyclerViewAdapter myAdapter;
    private Book myBook;
    private User myUser;
    private List<Review> reviews;
    private String type;
    private ListenerRegistration reviewsListener;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_show_reviews);
        Toolbar toolbar = findViewById(R.id.show_reviews_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.show_profile_reviews);
        Bundle bundle = getIntent().getExtras();
        if(bundle==null){
            Log.d(TAG,"Error bundle");
            finish();
            return;
        }
        type = bundle.getString("type");
        if(type == null){
            Log.d(TAG,"Error type");
            finish();
            return;
        }
        myAdapter = new ShowReviewsRecyclerViewAdapter(this, new LinkedList<>());
        if(type.equals("book")){
            BookWrapper bookWrapper = bundle.getParcelable("thisBook");
            if(bookWrapper==null){
                Log.d(TAG,"Error book");
                finish();
                return;
            }
            myBook=new Book(bookWrapper);
            reviewsListener = FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(myBook.getBook_id())
                    .collection("reviews")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if(e!=null){
                                Log.d(TAG,"Error snap");
                                return;
                            }
                            List<Review> revs = new LinkedList<>();
                            for(DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()){
                                if(doc.exists()){
                                    revs.add(doc.toObject(Review.class));
                                }
                            }
                            updateView(revs);
                        }
                    });
        }
        else{
            UserWrapper userWrapper = bundle.getParcelable("thisUser");
            if(userWrapper==null){
                Log.d(TAG,"Error book");
                finish();
                return;
            }
            myUser = new User(userWrapper);
            reviewsListener = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(myUser.getUsr_id())
                    .collection("reviews")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if(e!=null){
                                Log.d(TAG,"Error snap");
                                return;
                            }
                            List<Review> revs = new LinkedList<>();
                            for(DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()){
                                if(doc.exists()){
                                    revs.add(doc.toObject(Review.class));
                                }
                            }
                            updateView(revs);
                        }
                    });
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.app_name)); //TODO: change activity name
        RecyclerView rec = findViewById(R.id.show_reviews_recycle_view);
        rec.setLayoutManager(new LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false));
        rec.setScrollContainer(true);
        rec.setVerticalScrollBarEnabled(true);
        rec.setAdapter(myAdapter);
        rec.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // ...
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // ...
                }
            }
        });
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    private void updateView(List<Review> list){
        if(myAdapter ==null){
            return;
        }
        Collections.sort(list, new Comparator<Review>() {
            @Override
            public int compare(Review a, Review b) {
                return b.getReviewDate().compareTo(a.getReviewDate());
            }
        });
        myAdapter.setData(list);
        myAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reviewsListener.remove();
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


