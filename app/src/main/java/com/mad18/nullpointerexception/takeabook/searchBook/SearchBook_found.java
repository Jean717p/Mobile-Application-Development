package com.mad18.nullpointerexception.takeabook.searchBook;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.Book;
import com.mad18.nullpointerexception.takeabook.Book_generic_info;
import com.mad18.nullpointerexception.takeabook.InfoBook;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.addBook.AddBook;
import com.mad18.nullpointerexception.takeabook.addBook.BookWrapper;
import com.mad18.nullpointerexception.takeabook.displaySearchOnMap.DisplaySearchOnMap;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.mainActivity.Main_MyLibrary_Fragment;
import com.mad18.nullpointerexception.takeabook.mainActivity.MyLibraryRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class SearchBook_found extends Fragment {
    private String title;
    private int page;
    private View myFragmentView;
    CoordinatorLayout mainContent;
    private int viewSize;
    SearchBookRecyclerViewAdapter myAdapter;

    public static SearchBook_found newInstance(int page, String title) {
        Bundle args = new Bundle();
        SearchBook_found fragment = new SearchBook_found();
        args.putInt("pageID",page);
        args.putString("pageTitle",title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("pageID");
        title = getArguments().getString("pageTitle");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_search_book_found, container, false);
        // Inflate the layout for this fragment
        RecyclerView rec = myFragmentView.findViewById(R.id.search_book_recycler_view);
        mainContent = (CoordinatorLayout) myFragmentView.findViewById(R.id.search_book_found_coordinator_layout);
        Bundle bundle = new Bundle();
        myAdapter = new SearchBookRecyclerViewAdapter(getActivity(), new LinkedList<Book>(),
                new SearchBookRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Book item) {
                        ArrayList<BookWrapper> book_copies = new ArrayList<>();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference booksRef = db.collection("books");
                        Query query_ISBN = booksRef.whereEqualTo("book_ISBN", item.getBook_ISBN());
                        Task<QuerySnapshot> bookToShow = query_ISBN.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                Book tmp_book;
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                for (DocumentSnapshot documentSnapshot : documents) {
                                    if (documentSnapshot != null) {
                                        if (documentSnapshot.exists()) {
                                            tmp_book = documentSnapshot.toObject(Book.class);
                                            book_copies.add(new BookWrapper(tmp_book));
                                        }
                                    }
                                }
                                Intent intent = new Intent(getActivity(), DisplaySearchOnMap.class);
                                bundle.putParcelableArrayList("bookToShow", book_copies);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                            }

                        });
                    }
                }, new SearchBookRecyclerViewAdapter.OnItemClickInfoListener() {
            @Override
            public void onItemInfoClick(Book item) {
                Intent intent = new Intent(getActivity(), Book_generic_info.class);
                intent.putExtra("bookToShow",new BookWrapper(item));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        rec.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        rec.setScrollContainer(true);
        rec.setVerticalScrollBarEnabled(true);
        rec.setAdapter(myAdapter);
        rec.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                // While RecyclerView enters dragging state
                // (scroll, fling) we want our FAB to disappear.
                // Similarly when it enters idle state we want
                // our FAB to appear back.

                // (Just uncomment corresponding hide-show methods
                // which you want to use)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // ...
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // ...
                }
            }
        });
        return myFragmentView;
    }

    public void updateView(List<Book> books){
        if(myAdapter==null){
            return;
        }
        myAdapter.setData(books);
        myAdapter.notifyDataSetChanged();
    }
}