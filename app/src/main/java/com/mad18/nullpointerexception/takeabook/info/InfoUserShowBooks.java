package com.mad18.nullpointerexception.takeabook.info;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.MyAtomicCounter;
import com.mad18.nullpointerexception.takeabook.util.OnCounterChangeListener;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class InfoUserShowBooks extends AppCompatActivity {
    private final String TAG = "InfoUserShowBooks";
    private CoordinatorLayout mainContent;
    private ShowBooksRecyclerViewAdapter myAdapter;
    ArrayList<String> books = new ArrayList<>();
    List<Book> showBooks = new LinkedList<>();
    private FirebaseFirestore db;
    private final int REQUEST_ADDBOOK = 3;
    private final int BOOK_EFFECTIVELY_ADDED = 31;
    private final int REQUEST_REMOVE_BOOK = 40;
    private final int BOOK_EFFECTIVELY_REMOVED = 41;
    private MyAtomicCounter bookCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_user_show_books);
        Toolbar toolbar = findViewById(R.id.info_user_show_books_toolbar);
        Bundle bundle = getIntent().getExtras();
        UserWrapper userWrapper = bundle.getParcelable("user");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(userWrapper.getUser_wrapper_name()+" 's books");
        Locale current = getResources().getConfiguration().locale;
        switch (current.getLanguage()) {
            case "it":
                setTitle("Libri di " + userWrapper.getUser_wrapper_name());
                break;
            case "en":
                setTitle(userWrapper.getUser_wrapper_name() + " 's books");
                break;
        }

        db = FirebaseFirestore.getInstance();
        RecyclerView rec = findViewById(R.id.info_user_show_books_recycle_view);
        mainContent = findViewById(R.id.info_user_show_books_coordinator_layout);
        books = bundle.getStringArrayList("UserBooks");
        myAdapter = new ShowBooksRecyclerViewAdapter(this, showBooks,
                new ShowBooksRecyclerViewAdapter.OnItemClickListener() {
                    User u ;

                    @Override
                    public void onItemClick(Book item) {
                        Intent intent = new Intent(InfoUserShowBooks.this, InfoBook.class);
                        BookWrapper bw = new BookWrapper(item);
                        UserWrapper uw;
                        uw = bundle.getParcelable("user");
                        bw.setUser_id(uw.getUser_wrapper_id());
                        intent.putExtra("bookToShow",bw);
                        startActivity(intent);
                        InfoUserShowBooks.this.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    }
                });
        rec.setLayoutManager(new GridLayoutManager(this, 3));
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
        bookCounter = new MyAtomicCounter(books.size());
        bookCounter.setListener(new OnCounterChangeListener() {
            @Override
            public void onCounterReachZero() {
                updateView(showBooks);
            }
        });
        for (String x : books) {
            db.collection("books").document(x).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful() && task.getResult()!=null) {
                        DocumentSnapshot bookDoc = task.getResult();
                        Book book = bookDoc.toObject(Book.class);
                        if(book!=null){
                            showBooks.add(book);
                        }
                    }
                    bookCounter.decrement();
                }
            });
        }
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

    public void updateView(List<Book> books){
        if(myAdapter==null){
            return;
        }
        myAdapter.setData(books);
        myAdapter.notifyDataSetChanged();
    }
}
