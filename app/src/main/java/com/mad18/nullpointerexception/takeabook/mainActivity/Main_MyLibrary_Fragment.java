package com.mad18.nullpointerexception.takeabook.mainActivity;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mad18.nullpointerexception.takeabook.AddBook;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.info.InfoBook;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import static com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.myBooks;
import static java.util.stream.Collectors.toList;

//import com.github.clans.fab.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 */
public class Main_MyLibrary_Fragment extends Fragment {

    private String title;
    private final int REQUEST_ADDBOOK = 3;
    private final int BOOK_EFFECTIVELY_ADDED = 31;
    private final int BOOK_EFFECTIVELY_MODIFIED = 81;
    private final int REQUEST_REMOVE_BOOK = 40;
    private final int BOOK_EFFECTIVELY_REMOVED = 41;
    private int page;
    private View myFragmentView;
    CoordinatorLayout mainContent;
    boolean mIsHiding = false;
    private FloatingActionButton floatingActionButton;
    MyLibraryRecyclerViewAdapter myAdapter;

    public static Main_MyLibrary_Fragment newInstance(int page,String title) {
        Bundle args = new Bundle();
        Main_MyLibrary_Fragment fragment = new Main_MyLibrary_Fragment();
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
        myFragmentView = inflater.inflate(R.layout.fragment_main_my_library, container, false);
        // Inflate the layout for this fragment
        RecyclerView rec = myFragmentView.findViewById(R.id.my_library_recycle_view);
        mainContent = (CoordinatorLayout) myFragmentView.findViewById(R.id.main_library_coordinator_layout);
        floatingActionButton = (FloatingActionButton) myFragmentView.findViewById(R.id.fab_add);

        myAdapter = new MyLibraryRecyclerViewAdapter(getActivity(), MainActivity.myBooks,
                new MyLibraryRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Book item) {
                        Intent intent = new Intent(getActivity(), InfoBook.class);
                        BookWrapper bw = new BookWrapper(item);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        bw.setUser_id(user.getUid());
                        intent.putExtra("bookToShow",bw);
                        startActivityForResult(intent, REQUEST_REMOVE_BOOK);
                        //startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    }
                });

        rec.setLayoutManager(new GridLayoutManager(getActivity(), 3));
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
                    // Hiding FAB
                    hideFabWithObjectAnimator();
                    // ...
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Showing FAB
                    // ...
                    showFabWithObjectAnimator();
                }
            }
        });

        if(floatingActionButton!=null){
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(MainActivity.thisUser==null){
                        Snackbar.make(floatingActionButton,getActivity().getString(R.string.no_internet),Snackbar.LENGTH_LONG);
                        return;
                    }
                    Intent addbook = new Intent(getActivity(), AddBook.class);
                    startActivityForResult(addbook,REQUEST_ADDBOOK);
                   // startActivity(addbook);
                }
            });
        }
        return myFragmentView;
    }

    public void updateView(List<Book> books){
        if(myAdapter==null || books==null){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Comparator<Book> byTitle = Comparator.comparing(Book::getBook_title);
            Comparator<Book> byAuthor = Comparator.comparing(Book::getBook_first_author);
            books = books.stream().sorted(byAuthor.thenComparing(byTitle)).collect(toList());
        }
        else{
            Collections.sort(books, (a, b) -> {
                if(a.getBook_first_author().equals(b.getBook_first_author())){
                    return a.getBook_title().compareTo(b.getBook_title());
                }
                else{
                    return a.getBook_first_author().compareTo(b.getBook_first_author());
                }
            });
        }
        myAdapter.setData(books);
        myAdapter.notifyDataSetChanged();
    }

   public void hideFabWithObjectAnimator(){
        AnimatorSet scaleSet = new AnimatorSet();
        ObjectAnimator xScaleAnimator = ObjectAnimator.ofFloat(floatingActionButton, View.SCALE_X, 0);
        ObjectAnimator yScaleAnimator = ObjectAnimator.ofFloat(floatingActionButton, View.SCALE_Y, 0);
        scaleSet.setDuration(200);
        scaleSet.setInterpolator(new LinearInterpolator());
        scaleSet.playTogether(xScaleAnimator, yScaleAnimator);
        scaleSet.start();
    }

    public void showFabWithObjectAnimator(){
        AnimatorSet scaleSet = new AnimatorSet();
        ObjectAnimator xScaleAnimator = ObjectAnimator.ofFloat(floatingActionButton, View.SCALE_X, 1);
        ObjectAnimator yScaleAnimator = ObjectAnimator.ofFloat(floatingActionButton, View.SCALE_Y, 1);
        scaleSet.setDuration(400);
        scaleSet.setInterpolator(new OvershootInterpolator());
        scaleSet.playTogether(xScaleAnimator, yScaleAnimator);
        scaleSet.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_REMOVE_BOOK:
                if(resultCode == BOOK_EFFECTIVELY_REMOVED){
                    if(data!=null){
                        Bundle extras = data.getExtras();
                        BookWrapper bookWrapper = extras.getParcelable("book_removed");
                        if(bookWrapper!=null){
                            Book b = new Book(bookWrapper);
                            for (ListIterator<Book> iter = myBooks.listIterator(); iter.hasNext(); ) {
                                Book element = iter.next();
                                if(element.getBook_id().equals(b.getBook_id())){
                                    iter.remove();
                                    break;
                                }
                            }
                            Snackbar.make(getActivity().findViewById(R.id.main_library_coordinator_layout),
                                    R.string.info_book_deleted, Snackbar.LENGTH_LONG).show();
                            updateView(myBooks);
                        }
                    }
                }
                if(resultCode == BOOK_EFFECTIVELY_MODIFIED){
                    if(data!=null){
                        Bundle extras = data.getExtras();
                        BookWrapper bookWrapper = extras.getParcelable("book_modified");
                        if(bookWrapper!=null){
                            Book b = new Book(bookWrapper);
                            for (ListIterator<Book> iter = myBooks.listIterator(); iter.hasNext(); ) {
                                Book element = iter.next();
                                if(element.getBook_id().equals(b.getBook_id())){
                                    iter.remove();
                                    break;
                                }
                            }
                            myBooks.add(new Book(bookWrapper));
                            updateView(myBooks);
                            Snackbar.make(getActivity().findViewById(R.id.main_library_coordinator_layout),
                                    R.string.info_book_modified, Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
                break;
            case REQUEST_ADDBOOK:
                if(resultCode == BOOK_EFFECTIVELY_ADDED){
                    if(data!=null){
                        Bundle extras = data.getExtras();
                        if(extras!=null){
                            BookWrapper bookWrapper = extras.getParcelable("new_book");
                            if(bookWrapper!=null){
                                myBooks.add(new Book(bookWrapper));
                                updateView(myBooks);
                                Snackbar.make(getActivity().findViewById(R.id.main_library_coordinator_layout),
                                        R.string.add_book_added, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                break;
        }
    }

}
