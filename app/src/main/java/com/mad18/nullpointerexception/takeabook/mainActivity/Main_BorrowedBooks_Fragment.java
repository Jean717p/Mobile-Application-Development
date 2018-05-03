package com.mad18.nullpointerexception.takeabook.mainActivity;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mad18.nullpointerexception.takeabook.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class Main_BorrowedBooks_Fragment extends Fragment {


    public Main_BorrowedBooks_Fragment() {
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_borrowed_books, container, false);
    }

}
