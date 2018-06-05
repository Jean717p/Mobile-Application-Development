package com.mad18.nullpointerexception.takeabook.requestBook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.info.InfoUser;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.Loan;
import com.mad18.nullpointerexception.takeabook.util.MyAtomicCounter;
import com.mad18.nullpointerexception.takeabook.util.OnCounterChangeListener;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.annotation.Nullable;

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
    private ListenerRegistration loanListener;
    private Context context;
    private int resultCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        resultCode = RESULT_CANCELED;
        if(getIntent()==null || getIntent().getExtras()==null){
            Log.d(TAG,"Error passing parameters");
        }
        setContentView(R.layout.activity_request_book);
        context = this;
        Toolbar toolbar = findViewById(R.id.request_book_toolbar);
        toolbar.setTitle(R.string.title_activity_show_request);
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
                if(applicant!=null && owner!=null && requested_book!=null && loan!=null){
                    if(requestType.equals("archived")){
                        fillViewsArchived();
                    }
                    else{
                        loanListener = db.collection("requests")
                                .document(loanRef).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot loanSnap, @Nullable FirebaseFirestoreException e) {
                                        if(e!=null){
                                            Log.d(TAG,"Error onEvent");
                                            finish();
                                        }
                                        loan = loanSnap.toObject(Loan.class);
                                        if(loan!=null) {
                                            updateView();
                                        }
                                        if(loan==null){
                                            finish();
                                        }
                                    }
                                });
                        updateView();
                    }
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
                db.collection("books").document(loan.getBookId())
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
        Button cancel = findViewById(R.id.request_book_cancel);
        Button accept = findViewById(R.id.request_book_send);
        RelativeLayout.LayoutParams acceptParams = (RelativeLayout.LayoutParams) accept.getLayoutParams();
        RelativeLayout.LayoutParams cancelParams = (RelativeLayout.LayoutParams) cancel.getLayoutParams();
        acceptParams.removeRule(RelativeLayout.BELOW);
        acceptParams.addRule(RelativeLayout.BELOW,R.id.request_book_tv_message);
        cancelParams.removeRule(RelativeLayout.BELOW);
        cancelParams.addRule(RelativeLayout.BELOW,R.id.request_book_tv_message);
        findViewById(R.id.request_book_tv_message).setVisibility(View.VISIBLE);
        findViewById(R.id.request_book_label_tv_message).setVisibility(View.VISIBLE);
        findViewById(R.id.request_book_label_message).setVisibility(View.GONE);
    }

    private void updateView(){
        if(myUser.getUsr_id().equals(owner.getUsr_id())){
            if(loan.getEndLoanOwner()!=null){
                fillViewsArchived();
                return;
            }
        }
        else{
            if(loan.getEndLoanApplicant()!=null){
                fillViewsArchived();
                return;
            }
        }
        if(loan.getRequestStatus()){ //A
            if(loan.getExchangedApplicant()){ //B
                if(loan.getExchangedOwner()){ //C
                    fillViewLoan();
                }
                else{
                    fillViewPendingExchangeOwner();
                }
            }
            else{
                fillViewsPendingExchangeApplicant();
            }
        }
        else{
            fillViewsPendingRequest();
        }
    }

    private void fillViewsArchived(){
        fillCommonViews();
        TextView tv = findViewById(R.id.request_book_label_start_date);
        tv.setText(R.string.request_book_label_start_date_loan);
        tv = findViewById(R.id.request_book_status);
        tv.setText(R.string.request_book_archived);
        tv = findViewById(R.id.request_book_label_end_date_owner);
        tv.setText(R.string.request_book_label_end_date);
        tv.setVisibility(View.VISIBLE);
        Button cancel = findViewById(R.id.request_book_cancel);
        cancel.setText(R.string.info_book_delete);
        Button button = findViewById(R.id.request_book_send);
        button.setVisibility(View.GONE);
    }

    private void fillViewsPendingRequest(){
        fillCommonViews();
        Button acceptReq = findViewById(R.id.request_book_send);
        TextView tv;
        tv = findViewById(R.id.request_book_status);
        if(myUser.getUsr_id().equals(loan.getOwnerId())){ //sono l'owner
            tv.setText(R.string.request_book_applicant_has_sent_request);
            acceptReq.setText(R.string.request_book_accept_request);
            acceptReq.setClickable(true);
            acceptReq.setOnClickListener((View view) -> {
                AlertDialog myQuittingDialogBox = new AlertDialog.Builder(ShowRequest.this)
                        //set message, title, and icon
                        .setTitle(R.string.request_book_accept_request)
                        .setMessage(R.string.sure_question)
                        .setIcon(R.drawable.ic_done_white_24px)
                        .setPositiveButton(R.string.affermative_response, (dialog, whichButton) -> {
                            //your code
                            TextView textView = findViewById(R.id.request_book_status);
                            Button button = findViewById(R.id.request_book_send);
                            button.setVisibility(View.GONE);
                            textView.setText(R.string.request_book_status_request_accepted);
                            Snackbar.make(findViewById(R.id.request_book_send),
                                    R.string.request_book_waiting_for_applicant_exchange_confirmation, Snackbar.LENGTH_LONG).show();
                            db.collection("requests").document(loanRef)
                                    .update("requestStatus", true);
                            dialog.dismiss();
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }
        else{ //sono l'applicant
            tv.setText(R.string.request_book_waiting_for_owner_to_accept);
            acceptReq.setVisibility(View.GONE);
        }
    }
    private void fillViewsPendingExchangeApplicant() {
        fillCommonViews();
        Button acceptReq = findViewById(R.id.request_book_send);
        TextView tv = findViewById(R.id.request_book_status);
        //
        if (myUser.getUsr_id().equals(loan.getOwnerId())) { //sono l'owner
            //status pending aspetta l'applicant
            tv.setText(R.string.request_book_waiting_for_applicant_exchange_confirmation);
            acceptReq.setVisibility(View.GONE);

        } else {//sono l'applicant
            //conferma scambio
            tv.setText(R.string.request_book_status_request_accepted);
            acceptReq.setText(R.string.request_book_confirm_exchange);
            acceptReq.setVisibility(View.VISIBLE);
            acceptReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog myQuittingDialogBox = new AlertDialog.Builder(ShowRequest.this)
                            //set message, title, and icon
                            .setTitle(R.string.request_book_accept_request)
                            .setMessage(R.string.sure_question)
                            .setIcon(R.drawable.ic_done_white_24px)
                            .setPositiveButton(R.string.affermative_response, (dialog, whichButton) -> {
                                //your code
                                TextView textView = findViewById(R.id.request_book_status);
                                Button button = findViewById(R.id.request_book_send);
                                button.setVisibility(View.GONE);
                                textView.setText(R.string.request_book_waiting_for_owner_exchange_confirmation);
                                Snackbar.make(findViewById(R.id.request_book_send),
                                        R.string.request_book_waiting_for_owner_exchange_confirmation, Snackbar.LENGTH_LONG).show();
                                db.collection("requests").document(loanRef)
                                        .update("exchangedApplicant", true);
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
        }
    }
    private void fillViewPendingExchangeOwner(){
        fillCommonViews();
        TextView tv = findViewById(R.id.request_book_status);
        Button acceptReq = findViewById(R.id.request_book_send);
        if (myUser.getUsr_id().equals(loan.getOwnerId())) { //sono l'owner
            acceptReq.setVisibility(View.VISIBLE);
            acceptReq.setText(R.string.request_book_confirm_exchange);
            tv.setText(R.string.request_book_applicant_has_confirmed_exchange);
            acceptReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog myQuittingDialogBox = new AlertDialog.Builder(ShowRequest.this)
                            //set message, title, and icon
                            .setTitle(R.string.request_book_confirm_exchange)
                            .setMessage(R.string.sure_question)
                            .setIcon(R.drawable.ic_done_white_24px)
                            .setPositiveButton(R.string.affermative_response, (dialog, whichButton) -> {
                                //your code
                                db.collection("requests").document(loanRef)
                                        .update("exchangedOwner", true,
                                                "startDate", Calendar.getInstance().getTime()
                                        );
                                db.collection("books").document(requested_book.getBook_id())
                                        .update("book_status",true);
                                TextView textView = findViewById(R.id.request_book_status);
                                Button button = findViewById(R.id.request_book_send);
                                button.setVisibility(View.GONE);
                                textView.setText(R.string.request_book_status_on_loan);
                                Snackbar.make(findViewById(R.id.request_book_send),
                                        R.string.request_book_status_on_loan, Snackbar.LENGTH_LONG).show();
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
        }
        else{ //sono l'applicant
            acceptReq.setVisibility(View.GONE);
            tv.setText(R.string.request_book_waiting_for_owner_exchange_confirmation);
        }
    }
    private void fillViewLoan(){
        fillCommonViews();
        TextView tv = findViewById(R.id.request_book_status);
        tv.setText(R.string.request_book_status_on_loan);
        tv = findViewById(R.id.request_book_label_start_date);
        tv.setText(R.string.request_book_start_date_loan);
        Button btn = findViewById(R.id.request_book_send);
        btn.setVisibility(View.VISIBLE);
        btn.setText(R.string.request_book_close_loan);
        if (myUser.getUsr_id().equals(loan.getOwnerId())) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog myQuittingDialogBox = new AlertDialog.Builder(ShowRequest.this)
                            //set message, title, and icon
                            .setTitle(R.string.request_book_close_loan)
                            .setMessage(R.string.sure_question)
                            .setIcon(R.drawable.ic_done_white_24px)
                            .setPositiveButton(R.string.affermative_response, (dialog, whichButton) -> {
                                TextView textView = findViewById(R.id.request_book_status);
                                Button button = findViewById(R.id.request_book_send);
                                button.setVisibility(View.GONE);
                                textView.setText(R.string.request_book_archived);
                                Snackbar.make(findViewById(R.id.request_book_send),
                                        R.string.request_book_archived, Snackbar.LENGTH_LONG).show();
                                db.collection("requests").document(loanRef)
                                        .update("endLoanOwner", Calendar.getInstance().getTime());
                                DocumentReference doc = db.collection("users").document(loan.getOwnerId())
                                        .collection("requests").document(loan.getLoanId());
                                doc.delete();
                                HashMap<String,Boolean> toLoad = new HashMap<>();
                                toLoad.put("owned",true);
                                db.collection("users").document(loan.getOwnerId())
                                        .collection("archive").document(loanRef)
                                        .set(toLoad);
                                db.collection("books").document(loan.getBookId())
                                        .update("book_status",false);
                                Intent intent = new Intent(context,RequestReview.class);
                                intent.putExtra("otherUser", new UserWrapper(applicant));
                                intent.putExtra("bookToReview", new BookWrapper(requested_book));
                                intent.putExtra("thisUser", new UserWrapper(myUser));
                                startActivity(intent);
                                setResult(RESULT_OK);
                                finish();
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
        }
        else{
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog myQuittingDialogBox = new AlertDialog.Builder(ShowRequest.this)
                            .setTitle(R.string.request_book_close_loan)
                            .setMessage(R.string.sure_question)
                            .setIcon(R.drawable.ic_done_white_24px)
                            .setPositiveButton(R.string.affermative_response, (dialog, whichButton) -> {
                                TextView textView = findViewById(R.id.request_book_status);
                                Button button = findViewById(R.id.request_book_send);
                                button.setVisibility(View.GONE);
                                textView.setText(R.string.request_book_archived);
                                Snackbar.make(findViewById(R.id.request_book_send),
                                        R.string.request_book_archived, Snackbar.LENGTH_LONG).show();
                                db.collection("requests").document(loanRef)
                                        .update("endLoanApplicant", Calendar.getInstance().getTime());
                                DocumentReference doc = db.collection("users").document(loan.getApplicantId())
                                        .collection("requests").document(loan.getLoanId());
                                doc.delete();
                                HashMap<String,Boolean> toLoad = new HashMap<>();
                                toLoad.put("owned",false);
                                db.collection("users").document(loan.getApplicantId())
                                        .collection("archive").document(loanRef)
                                        .set(toLoad);
                                Intent intent = new Intent(context,RequestReview.class);
                                intent.putExtra("otherUser", new UserWrapper(owner));
                                intent.putExtra("bookToReview", new BookWrapper(requested_book));
                                intent.putExtra("thisUser", new UserWrapper(myUser));
                                startActivity(intent);
                                setResult(RESULT_OK);
                                finish();
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
        }
    }

    //Setto il messaggio, la thumbnail, il titolo del libro, il nome e il link al profilo dell'altro utente
    private  void fillCommonViews(){
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
        TextView tv = findViewById(R.id.request_book_tv_message);
        tv.setText(loan.getRequestText());
        if(tv.length()==0){
            tv.setVisibility(View.INVISIBLE);
            TextView t = findViewById(R.id.request_book_label_tv_message);
            t.setVisibility(View.INVISIBLE);
        }
        ImageView iw = findViewById(R.id.request_book_main_image);
        tv = findViewById(R.id.request_book_start_date);
        tv.setText(formatter.format(loan.getStartDate()));
        tv = findViewById(R.id.request_book_title);
        tv.setText(requested_book.getBook_title());
        if(requested_book.getBook_thumbnail_url().length()>0){
            GlideApp.with(this).load(requested_book.getBook_thumbnail_url())
                    .placeholder(R.drawable.ic_thumbnail_cover_book)
                    .into(iw);
        }
        if(myUser.getUsr_id().equals(loan.getOwnerId())){
            if(loan.getEndLoanOwner()!=null){
                tv = findViewById(R.id.request_book_end_date_owner);
                tv.setText(formatter.format(loan.getEndLoanOwner()));
                tv.setVisibility(View.VISIBLE);
                tv = findViewById(R.id.request_book_label_end_date_owner);
                tv.setVisibility(View.VISIBLE);
            }
            tv = findViewById(R.id.request_book_label_owner);
            tv.setText(R.string.request_book_applicant);
            tv = findViewById(R.id.request_book_owner);
            tv.setText(applicant.getUsr_name());
            tv.setTextColor(Color.BLUE);
            tv.setClickable(true);
            tv.setOnClickListener(view -> {
                Intent toInfoUser = new Intent(ShowRequest.this, InfoUser.class);
                toInfoUser.putExtra("otherUser", new UserWrapper(applicant));
                startActivity(toInfoUser);
            });
        }
        else{
            if(loan.getEndLoanApplicant()!=null){
                tv = findViewById(R.id.request_book_end_date_owner);
                tv.setText(formatter.format(loan.getEndLoanApplicant()));
                tv.setVisibility(View.VISIBLE);
                tv = findViewById(R.id.request_book_label_end_date_owner);
                tv.setVisibility(View.VISIBLE);
            }
            tv = findViewById(R.id.request_book_owner);
            tv.setText(owner.getUsr_name());
            tv.setTextColor(Color.BLUE);
            tv.setClickable(true);
            tv.setOnClickListener(view -> {
                Intent toInfoUser = new Intent(ShowRequest.this, InfoUser.class);
                toInfoUser.putExtra("otherUser", new UserWrapper(owner));
                startActivity(toInfoUser);
            });
        }
        Button cancel = findViewById(R.id.request_book_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog myQuittingDialogBox = new AlertDialog.Builder(ShowRequest.this)
                        //set message, title, and icon
                        .setTitle(R.string.info_book_delete_this_book)
                        .setMessage(R.string.sure_question)
                        .setIcon(R.drawable.ic_done_white_24px)
                        .setPositiveButton(R.string.affermative_response, (dialog, whichButton) -> {
                            DocumentReference docLoanRef = db.collection("requests").document(loanRef);
                            DocumentReference loanOwner = db.collection("users").document(loan.getOwnerId())
                                    .collection("requests").document(loanRef);
                            DocumentReference loanApplicant = db.collection("users").document(loan.getApplicantId())
                                    .collection("requests").document(loanRef);
                            if(loan.getExchangedOwner()==false){
                                loanOwner.delete();
                                loanApplicant.delete();
                                docLoanRef.delete();
                            }
                            else{
                                if(myUser.getUsr_id().equals(owner.getUsr_id())){
                                    loanOwner.delete();
                                    if(loan.getEndLoanApplicant() != null){
                                        docLoanRef.update("endLoanOwner",null);
                                    }
                                    else{
                                        docLoanRef.delete();
                                    }
                                }
                                else{
                                    loanApplicant.delete();
                                    if(loan.getEndLoanOwner() != null){
                                        docLoanRef.update("endLoanApplicant",null);
                                    }
                                    else{
                                        docLoanRef.delete();
                                    }
                                }
                            }
                            setResult(RESULT_OK);
                            resultCode = RESULT_OK;
                            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                            finish();
                            dialog.dismiss();
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(loanListener!=null){
            loanListener.remove();
        }
        super.onDestroy();
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
    public void onBackPressed() {
        Intent intent = new Intent();
        if(loan!=null){
            intent.putExtra("A",loan.getRequestStatus());
            intent.putExtra("B",loan.getExchangedApplicant());
            intent.putExtra("C",loan.getExchangedOwner());
            setResult(resultCode,intent);
        }
        super.onBackPressed();
    }
}
