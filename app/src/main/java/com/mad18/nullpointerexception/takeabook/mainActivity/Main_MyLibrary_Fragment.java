package com.mad18.nullpointerexception.takeabook.mainActivity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.Book;
import com.mad18.nullpointerexception.takeabook.InfoBook;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.addBook.BookWrapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.support.v7.widget.GridLayoutManager;
import android.widget.Toast;

import static com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.thisUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class Main_MyLibrary_Fragment extends Fragment {

    List<Book> lstBook ;
    private View myFragmentView;
    private FloatingActionButton fab;
    private int viewSize;


    public Main_MyLibrary_Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);


    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_main_my_library, container, false);
        // Inflate the layout for this fragment

       // viewSize = 3 > MainActivity.myBooks.size() ? MainActivity.myBooks.size() : 3;
        //viewSize = 1> viewSize ? 1 : viewSize;
        RecyclerView rec = myFragmentView.findViewById(R.id.my_library_recycle_view);
       /* fab = (FloatingActionButton) myFragmentView.findViewById(R.id.fab_add);
        rec.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy >0 && fab.getVisibility() == View.VISIBLE){
                    fab.hide(true);
                }else if(dy < 0 && fab.getVisibility() != View.VISIBLE){

                    fab.show(true);
                }
            }
        });*/
        RecyclerViewAdapter myAdapter = new RecyclerViewAdapter(getActivity(), MainActivity.myBooks,
                new RecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Book item) {
                        Intent intent = new Intent(getActivity(), InfoBook.class);
                        intent.putExtra("bookToShow",new BookWrapper(item));
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    }
                });
        rec.setLayoutManager(new GridLayoutManager(getActivity(),3));
        rec.setAdapter(myAdapter);
        return myFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
