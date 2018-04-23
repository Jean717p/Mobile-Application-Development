package com.mad18.nullpointerexception.takeabook.mainActivity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mad18.nullpointerexception.takeabook.R;

import android.view.Menu;
import android.view.MenuInflater;


/**
 * A simple {@link Fragment} subclass.
 */
public class Main_TopBooks_Fragment extends Fragment {


    public Main_TopBooks_Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main__top, container, false);





        return v;

    }


}