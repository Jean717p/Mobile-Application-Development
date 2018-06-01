package com.mad18.nullpointerexception.takeabook.requestBook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.MyAtomicCounter;
import com.mad18.nullpointerexception.takeabook.util.OnCounterChangeListener;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RequestList extends AppCompatActivity {
    private final String TAG = "RequestList";
    private final int SHOW_REQUEST = 5;
    private RequestRecyclerViewAdapter myAdapter;
    private User myUser;
    private List<Loan> requests;
    private MyAtomicCounter myAtomicCounter;
    private Boolean isArchive;
    private Context context;
    private String lastItemSelectedId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);
        Toolbar toolbar = findViewById(R.id.request_list_toolbar);
        Bundle bundle = getIntent().getExtras();
        if(bundle==null || getIntent().hasExtra("requestType") == false){
            onBackPressed();
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        myUser = new User((UserWrapper) bundle.getParcelable("thisUser"));
        String type = bundle.getString("requestType");
        requests = new LinkedList<>();
        Query query;
        isArchive = false;
        switch (type){
            case "sent":
                query = FirebaseFirestore.getInstance().collection("users")
                        .document(myUser.getUsr_id()).collection("requests")
                        .whereEqualTo("owned",false);
                break;
            case "received":
                query = FirebaseFirestore.getInstance().collection("users")
                        .document(myUser.getUsr_id()).collection("requests")
                        .whereEqualTo("owned",true);
                break;
            case "archived":
                query = FirebaseFirestore.getInstance().collection("users")
                        .document(myUser.getUsr_id()).collection("archive");
                isArchive = true;
                break;
                default:
                    onBackPressed();
                    return;
        }
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.getDocuments().size()>0){
                    myAtomicCounter = new MyAtomicCounter(queryDocumentSnapshots.getDocuments().size());
                    if(isArchive){
                        myAtomicCounter.setListener(new OnCounterChangeListener() {
                            @Override
                            public void onCounterReachZero() {
                                Collections.sort(requests, new Comparator<Loan>() {
                                    @Override
                                    public int compare(Loan a, Loan b) {
                                        Date ad,bd;
                                        ad = a.getOwnerId().equals(myUser.getUsr_id()) ? a.endLoanOwner : a.getEndLoanApplicant();
                                        bd = b.getOwnerId().equals(myUser.getUsr_id()) ? b.endLoanOwner : b.getEndLoanApplicant();
                                        return bd.compareTo(ad);
                                    }
                                });
                                updateView(requests);
                            }
                        });
                    }
                    else{
                        myAtomicCounter.setListener(new OnCounterChangeListener() {
                            @Override
                            public void onCounterReachZero() {
                                Collections.sort(requests, new Comparator<Loan>() {
                                    @Override
                                    public int compare(Loan a, Loan b) {
                                        return b.getStartDate().compareTo(a.getStartDate());
                                    }
                                });
                                updateView(requests);
                            }
                        });
                    }
                    for(DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()){
                        FirebaseFirestore.getInstance().collection("requests")
                                .document(doc.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful() && task.getResult()!=null){
                                    requests.add(task.getResult().toObject(Loan.class));
                                }
                                myAtomicCounter.decrement();
                            }
                        });
                    }
                }
            }
        });
        RecyclerView rec = findViewById(R.id.request_list_recycler_view);
        myAdapter = new RequestRecyclerViewAdapter(this, requests,
                myUser,
                new RequestRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Loan item) {
                        lastItemSelectedId = item.getLoanId();
                        Intent intent = new Intent(context,ShowRequest.class);
                        intent.putExtra("loanRef",item.getLoanId());
                        intent.putExtra("thisUser",new UserWrapper(myUser));
                        intent.putExtra("requestType",type);
                        startActivityForResult(intent,SHOW_REQUEST);
                    }
                });
        rec.setLayoutManager(new GridLayoutManager(this, 3));
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateView(List<Loan> list){
        if(myAdapter==null || list == null){
            return;
        }
        myAdapter.setData(list);
        myAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SHOW_REQUEST:
                if(resultCode == RESULT_OK){
                    for(Iterator<Loan> iterator = requests.listIterator(); iterator.hasNext(); ){
                        Loan l = iterator.next();
                        if(l.getLoanId().equals(lastItemSelectedId)){
                            iterator.remove();
                            break;
                        }
                    }
                    updateView(requests);
                }
        }
    }
}
