package com.mad18.nullpointerexception.takeabook.requestBook;

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

import java.util.LinkedList;
import java.util.List;

public class RequestList extends AppCompatActivity {
    private final String TAG = "RequestList";
    private RequestRecyclerViewAdapter myAdapter;
    private User myUser;
    private List<Loan> requests;
    private MyAtomicCounter myAtomicCounter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.request_list_toolbar);
        Bundle bundle = getIntent().getExtras();
        if(bundle==null || getIntent().hasExtra("requestType") == false){
            onBackPressed();
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myUser = new User((UserWrapper) bundle.getParcelable("thisUser"));
        String type = bundle.getString("requestType");
        requests = new LinkedList<>();
        Query query;
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
                default:
                    onBackPressed();
                    return;
        }
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.getDocuments().size()>0){
                    myAtomicCounter = new MyAtomicCounter(queryDocumentSnapshots.getDocuments().size());
                    myAtomicCounter.setListener(new OnCounterChangeListener() {
                        @Override
                        public void onCounterReachZero() {
                            updateView(requests);
                        }
                    });
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
                new RequestRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Loan item) {
//                        Intent intent = new Intent();
//                        intent.putExtra("requestRef",item.getLoanId());
//                        startActivity(intent);
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
}
