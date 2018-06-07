package com.mad18.nullpointerexception.takeabook.mainActivity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.displaySearchOnMap.DisplaySearchOnMap;
import com.mad18.nullpointerexception.takeabook.info.InfoBook;
import com.mad18.nullpointerexception.takeabook.searchBook.SearchBookAlgolia;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.MyAtomicCounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.thisUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class Main_HomeBooks_Fragment extends Fragment {
    private final String TAG ="Main_HomeBooks_Fragment";
    private final int FINE_LOCATION_PERMISSION = 7;
    private FloatingActionMenu fam;
    private FloatingActionButton fabTitle, fabAuthor, fabIsbn;
    private CoordinatorLayout home_coordinatorLayout;
    private HomeRecyclerViewAdapter myAdapter;
    private MyAtomicCounter myAtomicCounter;

    public Main_HomeBooks_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_home, container, false);
        RecyclerView rec = v.findViewById(R.id.home_recycler_view);
        myAtomicCounter = new MyAtomicCounter(1);
        home_coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.main_home_coordinator_layout);
        fabTitle = v.findViewById(R.id.top_floating_action_menu_title);
        fabAuthor =  v.findViewById(R.id.top_floating_action_menu_author);
        fabIsbn = v.findViewById(R.id.top_floating_action_menu_ISBN);
        fam = v.findViewById(R.id.top_floating_action_menu);
        myAdapter = new HomeRecyclerViewAdapter(getActivity(), new LinkedList<Book>(),
                new HomeRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Book item) {
//                            double lat = 0.0144927536231884;
//                            double lon = 0.0181818181818182;
//                            double distance = 186.411; //circa 300Km
//                            double lowerLat = thisUser.getUsr_geoPoint().getLatitude() - (lat * distance);
//                            double lowerLon = thisUser.getUsr_geoPoint().getLongitude() - (lon * distance);
//                            double greaterLat = thisUser.getUsr_geoPoint().getLatitude() + (lat * distance);
//                            double greaterLon = thisUser.getUsr_geoPoint().getLongitude() + (lon * distance);
//                            GeoPoint lowerBoundGeo = new GeoPoint(lowerLat,lowerLon);
//                            GeoPoint upperBoundGeo = new GeoPoint(greaterLat,greaterLon);
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
                            return;
                        }
                        int i = myAtomicCounter.getAndIncrement();
                        if(i>1){
                            myAtomicCounter.decrement();
                            return;
                        }
                        FirebaseFirestore.getInstance().collection("books")
                                .whereEqualTo("book_ISBN",item.getBook_ISBN())
                                .whereEqualTo("book_status",false)
//                                    .whereGreaterThanOrEqualTo("book_location",lowerBoundGeo)
//                                    .whereLessThanOrEqualTo("book_location",upperBoundGeo);
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if(querySnapshot.size()==0){
                                    return;
                                }
                                List<Book> x = new LinkedList<>();
                                Location user_loc;
                                double user_lat=0,user_long=0;
                                user_lat = thisUser.getUsr_geoPoint().getLatitude();
                                user_long = thisUser.getUsr_geoPoint().getLongitude();
                                user_loc = new Location("Provider");
                                user_loc.setLatitude(user_lat);
                                user_loc.setLongitude(user_long);
                                for(DocumentSnapshot doc:querySnapshot.getDocuments()){
                                    Book b = doc.toObject(Book.class);
                                    if(b.getBook_userid().equals(thisUser.getUsr_id())==false){
                                        x.add(b);
                                    }
                                }
                                if(x.size()==1){
                                    Intent intent = new Intent(getActivity(), InfoBook.class);
                                    intent.putExtra("bookToShow",new BookWrapper(x.get(0)));
                                    startActivity(intent);
                                    myAtomicCounter.decrement();
                                    return;
                                }
                                Collections.sort(x, (a, b) -> {
                                    Location book_loc_a = new Location("Provider");
                                    Location book_loc_b = new Location("Provider");
                                    book_loc_a.setLatitude(a.getBook_location().getLatitude());
                                    book_loc_a.setLongitude(a.getBook_location().getLongitude());
                                    book_loc_b.setLatitude(b.getBook_location().getLatitude());
                                    book_loc_b.setLongitude(b.getBook_location().getLongitude());
                                    return Float.compare(book_loc_a.distanceTo(user_loc),book_loc_b.distanceTo(user_loc));
                                });
                                ArrayList<BookWrapper> xWrapped = new ArrayList<>();
                                for(Book b:x){
                                    xWrapped.add(new BookWrapper(b));
                                }
                                Intent intent = new Intent(getActivity(), DisplaySearchOnMap.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelableArrayList("bookToShow",xWrapped);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                myAtomicCounter.decrement();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"failed");
                                myAtomicCounter.decrement();
                            }
                        });
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
                        if(MainActivity.thisUser==null){
                            Snackbar.make(fabTitle,getActivity().getString(R.string.no_internet),Snackbar.LENGTH_LONG);
                            return;
                        }
                        Intent search = new Intent(getActivity(), SearchBookAlgolia.class);
                        search.putExtra("action", "Title");
                        startActivity(search);
                    }
                });


            }

            if(fabAuthor !=null){
                fabAuthor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(MainActivity.thisUser==null){
                            Snackbar.make(fabAuthor,getActivity().getString(R.string.no_internet),Snackbar.LENGTH_LONG);
                            return;
                        }
                        Intent search = new Intent(getActivity(), SearchBookAlgolia.class);
                        search.putExtra("action", "Author");
                        startActivity(search);
                    }
                });


            }

            if(fabIsbn !=null){
                fabIsbn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(MainActivity.thisUser==null){
                            Snackbar.make(fabIsbn,getActivity().getString(R.string.no_internet),Snackbar.LENGTH_LONG);
                            return;
                        }
                        MainActivity mainActivity = (MainActivity) getActivity();
                        Intent search = new Intent(getActivity(), SearchBookAlgolia.class);
                        search.putExtra("action", "ISBN");
                        if(thisUser.getUsr_geoPoint()!=null){
                            search.putExtra("user_long", thisUser.getUsr_geoPoint().getLongitude());
                            search.putExtra("user_lat", thisUser.getUsr_geoPoint().getLatitude());
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
        Location user_loc = new Location("provider");
        user_loc.setLongitude(user_geo.getLongitude());
        user_loc.setLatitude(user_geo.getLatitude());
        Collections.sort(books, (a, b) -> {
                    Location book_loc_b = new Location("Provider");
                    Location book_loc_a = new Location("Provider");
                    book_loc_b.setLatitude(b.getBook_location().getLatitude());
                    book_loc_b.setLongitude(b.getBook_location().getLongitude());
                    book_loc_a.setLatitude(a.getBook_location().getLatitude());
                    book_loc_a.setLongitude(a.getBook_location().getLongitude());
                    return Float.compare(book_loc_a.distanceTo(user_loc),book_loc_b.distanceTo(user_loc));
                });
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