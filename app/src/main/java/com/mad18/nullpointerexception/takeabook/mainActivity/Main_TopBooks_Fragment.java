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
        View v = inflater.inflate(R.layout.fragment_main_top, container, false);





        return v;

    }


}