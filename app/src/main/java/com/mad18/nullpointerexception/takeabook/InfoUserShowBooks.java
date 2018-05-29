package com.mad18.nullpointerexception.takeabook;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.addBook.BookWrapper;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.mainActivity.MyLibraryRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.myBooks;

public class InfoUserShowBooks extends AppCompatActivity {
    private CoordinatorLayout mainContent;
    private ShowBooksRecyclerViewAdapter myAdapter;
    ArrayList<String> books = new ArrayList<>();
    List<Book> showBooks = new LinkedList<>();
    private FirebaseFirestore db;
    private final int REQUEST_ADDBOOK = 3;
    private final int BOOK_EFFECTIVELY_ADDED = 31;
    private final int REQUEST_REMOVE_BOOK = 40;
    private final int BOOK_EFFECTIVELY_REMOVED = 41;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_user_show_books);
        Toolbar toolbar = findViewById(R.id.info_user_show_books_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Books");
        db = FirebaseFirestore.getInstance();
        RecyclerView rec = findViewById(R.id.info_user_show_books_recycle_view);
        mainContent = findViewById(R.id.info_user_show_books_coordinator_layout);
        Bundle bundle = getIntent().getExtras();
        books = bundle.getStringArrayList("UserBooks");

        for (String x : books) {

            db.collection("books").document(x).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot bookDoc = task.getResult();
                    Book book = bookDoc.toObject(Book.class);
                    showBooks.add(bookDoc.toObject(Book.class));
                }

            });
        }


        myAdapter = new ShowBooksRecyclerViewAdapter(InfoUserShowBooks.this, showBooks,
                new ShowBooksRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Book item) {
                        /*Intent intent = new Intent(InfoUserShowBooks.this, InfoBook.class);
                        BookWrapper bw = new BookWrapper(item);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        bw.setUser_id(user.getUid());
                        intent.putExtra("bookToShow",bw);
                        startActivity(intent);
                        InfoUserShowBooks.this.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);*/
                    }
                });

            myAdapter.setData(showBooks);

    }



}
