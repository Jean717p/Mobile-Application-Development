package com.mad18.nullpointerexception.takeabook.requestBook;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.info.InfoUser;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.MyAtomicCounter;
import com.mad18.nullpointerexception.takeabook.util.OnCounterChangeListener;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ShowRequest extends AppCompatActivity {
    private final String TAG = "ShowRequest";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Loan loan ;
    private User owner ;
    private User applicant;
    private Book requested_book;
    private String loanRef;
    private User myUser;
    private MyAtomicCounter myAtomicCounter;
    private String requestType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent()==null || getIntent().getExtras()==null){
            Log.d(TAG,"Error passing parameters");
        }
        setContentView(R.layout.activity_request_book);
        Toolbar toolbar = findViewById(R.id.request_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loanRef = getIntent().getStringExtra("loanRef");
        myUser = new User((UserWrapper) getIntent().getParcelableExtra("thisUser"));
        requestType = getIntent().getStringExtra("requestType");
        if(loanRef==null || loanRef.equals("") || myUser == null || requestType == null){
            Log.d(TAG, "Extras String loanref not found");
            onBackPressed();
        }
        myAtomicCounter = new MyAtomicCounter(4);
        myAtomicCounter.setListener(new OnCounterChangeListener() {
            @Override
            public void onCounterReachZero() {
                if(applicant!=null && owner!=null && requested_book!=null){
                    fillViews();
                }
                else{
                    Log.d(TAG,"Fail listener atomic counter");
                    onBackPressed();
                }
            }
        });
        db.collection("requests").document(loanRef)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                loan = documentSnapshot.toObject(Loan.class);
                myAtomicCounter.decrement();
                if(loan.getOwnerId().equals(myUser.getUsr_id())){
                    db.collection("users").document(loan.getApplicantId())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            applicant = documentSnapshot.toObject(User.class);
                            myAtomicCounter.decrement();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Applicant "+loan.getApplicantId()+" not present");
                            onBackPressed();
                        }
                    });
                    owner = myUser;
                    myAtomicCounter.decrement();
                }
                else{
                    db.collection("users").document(loan.getOwnerId())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            owner = documentSnapshot.toObject(User.class);
                            myAtomicCounter.decrement();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Owner "+loan.getOwnerId()+" not present");
                            onBackPressed();
                        }
                    });
                    applicant = myUser;
                    myAtomicCounter.decrement();
                }
                db.document(loan.getBookId())
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        requested_book = documentSnapshot.toObject(Book.class);
                        myAtomicCounter.decrement();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Requested book "+loan.getBookId()+" not present");
                        onBackPressed();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Loan not present");
                onBackPressed();
            }
        });
    }

    private void fillViews(){
        TextView tv = findViewById(R.id.request_book_message);
        tv.setText(loan.getRequestText());
        tv.setEnabled(false);
        ImageView iw = findViewById(R.id.request_book_main_image);
        tv = findViewById(R.id.request_book_title);
        tv.setText(requested_book.getBook_title());
        GlideApp.with(this).load(requested_book.getBook_thumbnail_url())
                .placeholder(R.drawable.ic_thumbnail_cover_book).into(iw);
        if(myUser.getUsr_id().equals(loan.getOwnerId())){
            tv = findViewById(R.id.request_book_label_owner);
            tv.setText(R.string.show_request_applicant);
            tv = findViewById(R.id.request_book_owner);
            tv.setText(applicant.getUsr_name());
            tv.setTextColor(Color.BLUE);
            tv.setClickable(true);
            tv.setOnClickListener(view -> {
                Intent toInfoUser = new Intent(ShowRequest.this, InfoUser.class);
                toInfoUser.putExtra("otherUser", new UserWrapper(applicant));
                startActivity(toInfoUser);
            });
            setButtonsParametersOwner();
            tv = findViewById(R.id.request_book_status);
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            if(requestType.equals("archived")==false) {
                if (loan.getRequestStatus()) { //A
                    if (loan.getExchangedApplicant()) { //B
                        if (loan.getExchangedOwner()) { //C
                            tv.setText(R.string.request_book_status_on_loan);
                        } else {
                            tv.setText(R.string.request_book_status_exchg_confirm);
                        }
                    } else {
                        tv.setText(R.string.request_book_status_exchg_applicant);
                    }
                } else {
                    tv.setText(R.string.request_book_status_pending);
                }
            }
            else{
                tv.setText(R.string.request_book_status_closed);
                tv = findViewById(R.id.request_book_start_date);
                tv.setText(formatter.format(loan.getStartDate()));
                tv = findViewById(R.id.request_book_label_start_date);
                tv.setText(R.string.request_book_label_start_date_loan);
            }
        }
        else{
            tv = findViewById(R.id.request_book_owner);
            tv.setText(owner.getUsr_name());
            tv.setTextColor(Color.BLUE);
            tv.setClickable(true);
            tv.setOnClickListener(view -> {
                Intent toInfoUser = new Intent(ShowRequest.this, InfoUser.class);
                toInfoUser.putExtra("otherUser", new UserWrapper(owner));
                startActivity(toInfoUser);
            });
            setButtonsParametersApplicant();
        }
        switch (requestType) {
            case "sent":
                break;
            case "received":
                break;
            case "archived":
                break;
        }
    }

    private void setButtonsParametersOwner(){

        Button acceptReq = findViewById(R.id.request_book_send);
        if(loan.requestStatus == false) {
            acceptReq.setText(R.string.show_request_accept_request);
            acceptReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: completa seconda e fai terza fase
                    //loan.setRequestStatus(true);
                    db.collection("requests").document(loanRef)
                            .update("requestStatus", true);

                    Snackbar.make(findViewById(R.id.request_book_send),
                            R.string.show_request_accepted_request, Snackbar.LENGTH_LONG).show();
                    acceptReq.setClickable(false);
                    acceptReq.setText(R.string.show_request_wait_applicant);
                }
            });
        }
        if(loan.requestStatus == true){
            acceptReq.setClickable(false);
            acceptReq.setText(R.string.show_request_wait_applicant);
            if(loan.exchangedApplicant==true){
                acceptReq.setText("Confirm exchange");
                acceptReq.setClickable(true);
                acceptReq.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        db.collection("requests").document(loanRef)
                                .update("exchangedOwner", true);

                        Snackbar.make(findViewById(R.id.request_book_send),
                                R.string.show_request_accepted_request, Snackbar.LENGTH_LONG).show();
                        acceptReq.setClickable(false);
                        //TODO: fase 3
                    }
                });
            }
        }

        Button cancel = findViewById(R.id.request_book_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteRequest();
            }
        });
    }

    private void setButtonsParametersApplicant(){

        Button acceptReq = findViewById(R.id.request_book_send);
        //acceptReq.setVisibility(View.GONE);

        if(loan.getRequestStatus()==false){
            acceptReq.setClickable(false);
            acceptReq.setText("Wait for owner");
        }

        if(loan.getRequestStatus()==true){
            acceptReq.setText("Confirm exchange");
            acceptReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.collection("requests").document(loanRef)
                            .update("exchangedApplicant", true);
                    Snackbar.make(findViewById(R.id.request_book_send),
                            R.string.show_request_accepted_request, Snackbar.LENGTH_LONG).show();
                    acceptReq.setClickable(false);
                    acceptReq.setText("Wait for owner");
                    //TODO:fase 3
                }
            });
        }

        Button cancel = findViewById(R.id.request_book_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteRequest();
            }
        });
    }

    private void DeleteRequest(){
        if(loanRef.length()==0){
            onBackPressed();
            return;
        }
        DocumentReference newReqRef = db.collection("requests").document(loanRef);
        newReqRef.delete();
        DocumentReference reqMe = db.collection("users").document(owner.getUsr_id())
                .collection("requests").document(loanRef);
        reqMe.delete();
        DocumentReference reqOwner = db.collection("users").document(applicant.getUsr_id())
                .collection("requests").document(loanRef);
        reqOwner.delete();
        onBackPressed();
    }
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
}
