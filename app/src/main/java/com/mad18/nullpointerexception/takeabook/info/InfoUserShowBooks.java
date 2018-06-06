package com.mad18.nullpointerexception.takeabook.info;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class InfoUserShowBooks extends AppCompatActivity {
    private final String TAG = "InfoUserShowBooks";
    private ShowBooksRecyclerViewAdapter myAdapter;
    private FirebaseFirestore db;
    private User owner;
    private MyAtomicCounter bookCounter;
    private List<Book> showBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_user_show_books);
        Toolbar toolbar = findViewById(R.id.info_user_show_books_toolbar);
        Bundle bundle = getIntent().getExtras();
        UserWrapper userWrapper = bundle.getParcelable("owner");
        if(userWrapper==null){
            finish();
            Log.d(TAG,"error bundle");
        }
        owner = new User(userWrapper);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switch (getResources().getConfiguration().locale.getLanguage()) {
            case "it":
                setTitle("Libri di " + userWrapper.getUser_wrapper_name());
                break;
            case "en":
                setTitle(userWrapper.getUser_wrapper_name() + " 's books");
                break;
                default:
                    setTitle(userWrapper.getUser_wrapper_name() + " 's books");
                    break;
        }
        showBooks = new LinkedList<>();
        db = FirebaseFirestore.getInstance();
        RecyclerView rec = findViewById(R.id.info_user_show_books_recycle_view);
        myAdapter = new ShowBooksRecyclerViewAdapter(this, showBooks,
                new ShowBooksRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Book item) {
                        Intent intent = new Intent(InfoUserShowBooks.this, InfoBook.class);
                        intent.putExtra("bookToShow",new BookWrapper(item));
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
        bookCounter = new MyAtomicCounter(owner.getUsr_books().size());
        bookCounter.setListener(new OnCounterChangeListener() {
            @Override
            public void onCounterReachZero() {
                updateView(showBooks);
            }
        });
        for (String x : owner.getUsr_books().keySet()) {
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

    public void updateView(List<Book> booksToUpdate){
        if(myAdapter==null || booksToUpdate == null || booksToUpdate.size()==0){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Comparator<Book> byTitle = Comparator.comparing(b->b.getBook_title());
            Comparator<Book> byAuthor = Comparator.comparing(b->b.getBook_first_author());
            booksToUpdate = booksToUpdate.stream().sorted(byAuthor.thenComparing(byTitle)).collect(toList());
        }
        else{
            Collections.sort(booksToUpdate, (a, b) -> {
                if(a.getBook_first_author().equals(b.getBook_first_author())){
                    return a.getBook_title().compareTo(b.getBook_title());
                }
                else{
                    return a.getBook_first_author().compareTo(b.getBook_first_author());
                }
            });
        }
        myAdapter.setData(booksToUpdate);
        myAdapter.notifyDataSetChanged();
    }
}
