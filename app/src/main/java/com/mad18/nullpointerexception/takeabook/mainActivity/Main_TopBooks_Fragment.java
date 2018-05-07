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
import com.mad18.nullpointerexception.takeabook.SearchBook;

import android.view.Menu;
import android.view.MenuInflater;


/**
 * A simple {@link Fragment} subclass.
 */
public class Main_TopBooks_Fragment extends Fragment {
    private FloatingActionMenu fam;
    private FloatingActionButton fabTitle, fabCategory, fabAuthor, fabIsbn;

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
            fabTitle = v.findViewById(R.id.top_floating_action_menu_title);
            fabAuthor =  v.findViewById(R.id.top_floating_action_menu_author);
            fabIsbn = v.findViewById(R.id.top_floating_action_menu_ISBN);
            fam = v.findViewById(R.id.top_floating_action_menu);

            if(fabTitle !=null){
                fabTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent search = new Intent(getActivity(), SearchBook.class);
                        search.putExtra("action", "Title");
                        startActivity(search);
                    }
                });


            }

            if(fabAuthor !=null){
                fabAuthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent search = new Intent(getActivity(), SearchBook.class);
                        search.putExtra("action", "Author");
                        startActivity(search);
                    }
                });


            }

            if(fabCategory !=null){
                fabCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent search = new Intent(getActivity(), SearchBook.class);
                        search.putExtra("action", "Category");
                        startActivity(search);
                    }
                });


            }

            if(fabIsbn !=null){
                fabIsbn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent search = new Intent(getActivity(), SearchBook.class);
                        search.putExtra("action", "ISBN");
                        startActivity(search);
                    }
                });


            }


        return v;

    }


}