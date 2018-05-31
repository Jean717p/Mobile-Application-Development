package com.mad18.nullpointerexception.takeabook.requestBook;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.info.InfoBook;
import com.mad18.nullpointerexception.takeabook.info.InfoUser;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

public class ShowRequest extends AppCompatActivity {
    private final String TAG = "ShowRequest";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Loan loan ;
    private User owner ;
    private User applicant;
    private Book requested_book;
    private String loanRef;

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
        if(loanRef.equals("")){
            Log.d(TAG, "Extras String loanref not found");
            finish();
        }
        db.collection("requests").document(loanRef)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                loan = documentSnapshot.toObject(Loan.class);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Loan not present");
                finish();
            }
        });

        db.collection("users").document(loan.getOwnerId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                owner = documentSnapshot.toObject(User.class);
            }
        });

        db.collection("users").document(loan.getApplicantId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                applicant = documentSnapshot.toObject(User.class);
            }
        });

        db.collection("books").document(loan.getBookId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                requested_book = documentSnapshot.toObject(Book.class);
            }
        });


        fillViews();
    }

    private void fillViews(){
        TextView tv = findViewById(R.id.request_book_message);
        if(loan.getRequestText().length()>0){
            tv.setText(loan.getRequestText());
        }
        tv.setEnabled(false);

        if(MainActivity.thisUser!=null){
            if(MainActivity.thisUser.getUsr_id().equals(loan.getOwnerId())){
                tv = findViewById(R.id.request_book_label_owner);
                tv.setText(R.string.show_request_applicant);
                tv = findViewById(R.id.request_book_owner);
                tv.setText(applicant.getUsr_name());
                tv.setTextColor(Color.BLUE);
                tv.setClickable(true);
                UserWrapper userWrapper = new UserWrapper(applicant);
                tv.setOnClickListener(view -> {
                    Intent toInfoUser = new Intent(ShowRequest.this, InfoUser.class);
                    toInfoUser.putExtra("otherUser", userWrapper);
                    startActivity(toInfoUser);
                });

                setButtonsParametersOwner();
            }
            else{
                tv = findViewById(R.id.request_book_owner);
                tv.setText(owner.getUsr_name());
                tv.setTextColor(Color.BLUE);
                tv.setClickable(true);
                UserWrapper userWrapper = new UserWrapper(owner);
                tv.setOnClickListener(view -> {
                    Intent toInfoUser = new Intent(ShowRequest.this, InfoUser.class);
                    toInfoUser.putExtra("otherUser", userWrapper);
                    startActivity(toInfoUser);
                });

                setButtonsParametersApplicant();
            }
        }
        else{
            tv = findViewById(R.id.request_book_owner);
            tv.setText(owner.getUsr_name());
            tv.setTextColor(Color.BLUE);
            tv.setClickable(true);
            UserWrapper userWrapper = new UserWrapper(owner);
            tv.setOnClickListener(view -> {
                Intent toInfoUser = new Intent(ShowRequest.this, InfoUser.class);
                toInfoUser.putExtra("otherUser", userWrapper);
                startActivity(toInfoUser);
            });
        }

        tv.findViewById(R.id.request_book_title);
        tv.setText(loan.getBookTitle());

        if(requested_book.getBook_thumbnail_url().length()>0){
            ImageView iw = findViewById(R.id.request_book_main_image);
            GlideApp.with(this).load(requested_book.getBook_thumbnail_url()).placeholder(R.drawable.ic_thumbnail_cover_book).into(iw);
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

    @Override
    protected void onResume() {
        super.onResume();
        fillViews();
    }
}
