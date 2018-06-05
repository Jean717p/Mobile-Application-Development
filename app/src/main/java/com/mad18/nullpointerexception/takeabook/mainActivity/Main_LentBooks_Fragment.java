package com.mad18.nullpointerexception.takeabook.mainActivity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.Loan;
import com.mad18.nullpointerexception.takeabook.requestBook.RequestRecyclerViewAdapter;
import com.mad18.nullpointerexception.takeabook.requestBook.ShowRequest;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Main_LentBooks_Fragment extends Fragment {
    private final String TAG = "Main_LentBooks_Fragment";
    private final int SHOW_REQUEST = 5;
    private CoordinatorLayout lent_coordinatorLayout;
    private RequestRecyclerViewAdapter myAdapter;
    private int lastItemSelectedPosition;
    private ListenerRegistration lentListener;
    private String ownerId;

    public Main_LentBooks_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ownerId = FirebaseAuth.getInstance().getUid();
        lentListener = FirebaseFirestore.getInstance().collection("requests")
                .whereEqualTo("ownerId",ownerId)
                .whereEqualTo("exchangedOwner",true)
                .whereEqualTo("endLoanOwner",null)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.d(TAG,"Error listener");
                    return;
                }
                List<Loan> data = new LinkedList<>();
                for(DocumentSnapshot doc:querySnapshot.getDocuments()){
                    data.add(doc.toObject(Loan.class));
                }
                Collections.sort(data, new Comparator<Loan>() {
                    @Override
                    public int compare(Loan a, Loan b) {
                        return b.getStartDate().compareTo(a.getStartDate());
                    }
                });
                updateView(data);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lentListener.remove();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_lent_books, container, false);
        lent_coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.main_home_coordinator_layout);
        RecyclerView rec = v.findViewById(R.id.lent_recycler_view);
        myAdapter = new RequestRecyclerViewAdapter(getContext(), new LinkedList<Loan>(),
                FirebaseAuth.getInstance().getUid(), false,
                new RequestRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Loan item, int position) {
                        lastItemSelectedPosition = position;
                        Intent intent = new Intent(getContext(),ShowRequest.class);
                        intent.putExtra("loanRef",item.getLoanId());
                        intent.putExtra("thisUser",new UserWrapper(MainActivity.thisUser));
                        intent.putExtra("requestType","received");
                        startActivityForResult(intent,SHOW_REQUEST);
                    }
                });
        rec.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));
        rec.setScrollContainer(true);
        rec.setVerticalScrollBarEnabled(true);
        rec.setAdapter(myAdapter);
        rec.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // ...
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // ...
                }
            }
        });
        return v;
    }

    private void updateView(List<Loan> list){
        if(myAdapter==null){
            return;
        }
        myAdapter.setData(list);
        myAdapter.notifyDataSetChanged();
    }

    public static Fragment newInstance(int page, String title) {
        Main_LentBooks_Fragment fragment = new Main_LentBooks_Fragment();
        Bundle args = new Bundle();
        args.putInt("pageID", page);
        args.putString("pageTitle", title);
        fragment.setArguments(args);
        return fragment;
    }
}
