package com.mad18.nullpointerexception.takeabook.mainActivity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.firestore.GeoPoint;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.info.InfoBook;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.searchBook.SearchBook;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Main_HomeBooks_Fragment extends Fragment {
    private FloatingActionMenu fam;
    private FloatingActionButton fabTitle, fabAuthor, fabIsbn;
    CoordinatorLayout home_coordinatorLayout;
    HomeRecyclerViewAdapter myAdapter;

    public Main_HomeBooks_Fragment() {
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
        View v = inflater.inflate(R.layout.fragment_main_home, container, false);
        RecyclerView rec = v.findViewById(R.id.home_recycler_view);
            home_coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.main_home_coordinator_layout);
            fabTitle = v.findViewById(R.id.top_floating_action_menu_title);
            fabAuthor =  v.findViewById(R.id.top_floating_action_menu_author);
            fabIsbn = v.findViewById(R.id.top_floating_action_menu_ISBN);
            fam = v.findViewById(R.id.top_floating_action_menu);
            myAdapter = new HomeRecyclerViewAdapter(getActivity(), MainActivity.homeBooks,
                    new HomeRecyclerViewAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Book item) {
                            Intent intent = new Intent(getActivity(), InfoBook.class);
                            BookWrapper bw = new BookWrapper(item);
                            //bw.setBook_userid(item.getBook_userid());
                            intent.putExtra("bookToShow",bw);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
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
                        // Hiding FAB
                        fam.hideMenuButton(false);

                        // ...
                    } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        // Showing FAB
                        // ...
                        fam.showMenuButton(false);
                    }
                }
            });


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

            if(fabIsbn !=null){
                fabIsbn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        Intent search = new Intent(getActivity(), SearchBook.class);
                        search.putExtra("action", "ISBN");
                        if(mainActivity.thisUser!=null){
                            if(mainActivity.thisUser.getUsr_geoPoint()!=null){
                                search.putExtra("user_long",mainActivity.thisUser.getUsr_geoPoint().getLongitude());
                                search.putExtra("user_lat",mainActivity.thisUser.getUsr_geoPoint().getLatitude());
                            }
                        }
                        startActivity(search);
                    }
                });
            }
        return v;
    }

    public void updateView(List<Book> books, GeoPoint user_geo){
        if(myAdapter==null){
            return;
        }
        myAdapter.setData(books);
        myAdapter.setUser_loc(user_geo);
        myAdapter.notifyDataSetChanged();
    }

    public static Fragment newInstance(int page, String title) {
        Main_HomeBooks_Fragment main_topBooks_fragment = new Main_HomeBooks_Fragment();
        Bundle args = new Bundle();
        args.putInt("pageID", page);
        args.putString("pageTitle", title);
        main_topBooks_fragment.setArguments(args);
        return main_topBooks_fragment;
    }




}