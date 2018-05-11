package com.mad18.nullpointerexception.takeabook.mainActivity;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.Book;
import com.mad18.nullpointerexception.takeabook.InfoBook;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.addBook.AddBook;
import com.mad18.nullpointerexception.takeabook.addBook.BookWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.support.v7.widget.GridLayoutManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;
import static com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.thisUser;
import static java.util.stream.Collectors.toList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Main_MyLibrary_Fragment extends Fragment {

    private String title;
    private final int REQUEST_ADDBOOK = 3;
    private int page;
    private View myFragmentView;
    CoordinatorLayout mainContent;
    boolean mIsHiding = false;
    private FloatingActionButton floatingActionButton;
    private int viewSize;
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
                        bw.setBook_userid(item.getBook_userid());
                        intent.putExtra("bookToShow",bw);
                        startActivity(intent);
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
                    Intent addbook = new Intent(getActivity(), AddBook.class);
                    //startActivityForResult(addbook,REQUEST_ADDBOOK);
                    startActivity(addbook);
                }
            });
        }
        return myFragmentView;
    }

    public void updateView(List<Book> books){
        if(myAdapter==null){
            return;
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

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        List<Book> books = myAdapter.getData();
//        switch (requestCode){
//            case REQUEST_ADDBOOK:
//                if(resultCode == RESULT_OK){
//                    if(data!=null){
//                        Bundle extras = data.getExtras();
//                        if(extras!=null){
//                            BookWrapper bookWrapper = extras.getParcelable("newbook");
//                            if(bookWrapper!=null){
//                                books.add(new Book(bookWrapper));
//                                updateView(books);
//                            }
//                        }
//                    }
//                }
//                break;
//        }
//    }
}
