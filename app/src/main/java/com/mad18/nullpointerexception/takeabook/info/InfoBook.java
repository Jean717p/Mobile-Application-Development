package com.mad18.nullpointerexception.takeabook.info;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.EditBook;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.displaySearchOnMap.DisplaySearchOnMap;
import com.mad18.nullpointerexception.takeabook.requestBook.RequestBook;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.ImageViewPopUpHelper;
import com.mad18.nullpointerexception.takeabook.util.Review;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class InfoBook extends AppCompatActivity {

    private final int BOOK_EFFECTIVELY_REMOVED = 41;
    private final int BOOK_EFFECTIVELY_MODIFIED = 81;
    private final int FINE_LOCATION_PERMISSION = 7;
    private final int idTextViewIds[] = new int[]{R.id.info_book_title, R.id.info_book_author, R.id.info_book_ISBN,
            R.id.info_book_editionYear, R.id.info_book_publisher, R.id.info_book_categories, R.id.info_book_description,
            R.id.info_book_pages};
    private final int REQUEST_BOOK = 7;
    private final int MODIFY_BOOK = 8;

    private BookWrapper bookToShowInfoOf;
    private FirebaseAuth mAuth;
    private Menu menu;
    private LinearLayout horizontal_photo_list;
    private View horizontal_photo_list_element;
    private List<String> for_me;
    private User bookOwner;
    private SharedPreferences sharedPref;
    private Context context;
    private AppCompatActivity myActivity;
    private Book myBook;
    private FirebaseFirestore db;
    CardView showReviews;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        myActivity = this;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.info_book);
        Toolbar toolbar = findViewById(R.id.info_book_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_activity_info_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        showReviews = findViewById(R.id.info_book_reviews_cv);
        bookToShowInfoOf = getIntent().getExtras().getParcelable("bookToShow");
        myBook = new Book(bookToShowInfoOf);
        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        fillInfoBookViews();
    }


    private void fillInfoBookViews() {
        TextView tv;
        FirebaseUser user = mAuth.getCurrentUser();
        tv = findViewById(R.id.info_book_title);
        tv.setText(bookToShowInfoOf.getTitle());
        tv = findViewById(R.id.info_book_author);
        String tmp;
        tmp = bookToShowInfoOf.getAuthors().toString();
        if (tmp.length() > 2) {
            tv.setText(tmp.substring(1, tmp.length() - 1));
        }
        tv = findViewById(R.id.info_book_ISBN);
        tv.setText(bookToShowInfoOf.getISBN());
        tv = findViewById(R.id.info_book_editionYear);
        if (bookToShowInfoOf.getEditionYear() == 0) {
            tv.setText(R.string.add_book_info_not_available);
        } else {
            tv.setText(Integer.toString(bookToShowInfoOf.getEditionYear()));
        }
        tv = findViewById(R.id.info_book_pages);
        if (bookToShowInfoOf.getPages() == 0) {
            tv.setText(R.string.add_book_info_not_available);
        } else {
            tv.setText(Integer.toString(bookToShowInfoOf.getPages()));

        }
        tv = findViewById(R.id.info_book_description);
        if (bookToShowInfoOf.getDescription().length() == 0) {
            tv.setText(getString(R.string.add_book_no_description));
        } else {
            tv.setText(bookToShowInfoOf.getDescription());
        }
        tv = findViewById(R.id.info_book_publisher);
        if (bookToShowInfoOf.getPublisher().length() == 0) {
            tv.setText(getString(R.string.add_book_info_not_available));
        } else {
            tv.setText(bookToShowInfoOf.getPublisher());
        }
        tv = findViewById(R.id.info_book_book_status);
        if(bookToShowInfoOf.getStatus()){
            tv.setText(R.string.request_book_status_on_loan);
            tv.setTextColor(Color.parseColor("#D50000"));
        }
        else{
            tv.setText(R.string.info_book_status_free);
            tv.setTextColor(Color.parseColor("#4CAF50"));

        }
        tv = findViewById(R.id.info_book_categories);
        tmp = bookToShowInfoOf.getCategories().toString();
        if (tmp.length() > 2) {
            tv.setText(tmp.substring(1, tmp.length() - 1));
        }
        ImageView iw = findViewById(R.id.info_book_main_image);
        Book book = new Book(bookToShowInfoOf);
        tv = findViewById(R.id.info_book_book_conditions);
        switch (book.getBook_condition()) {
            case 0:
                tv.setText(getString(R.string.add_book_info_not_available));
                break;
            case 1:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[1]);
                break;
            case 2:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[2]);
                break;
            case 3:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[3]);
                break;
        }
        GlideApp.with(this).load(book.getBook_thumbnail_url())
                .placeholder(R.drawable.ic_thumbnail_cover_book)
                .into(iw);
        Button mapButton = findViewById(R.id.info_book_map_button);
        if(!myBook.getBook_userid().equals(user.getUid())){
            if(myBook.getBook_status()){
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mapButton.getLayoutParams();
                params.removeRule(RelativeLayout.BELOW);
                params.addRule(RelativeLayout.BELOW,R.id.info_book_map_button);
            }
            mapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(myActivity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
                        return;
                    }
                    Intent intent = new Intent(myActivity, DisplaySearchOnMap.class);
                    Bundle bundle = new Bundle();
                    ArrayList<BookWrapper> arrayList = new ArrayList<>();
                    arrayList.add(new BookWrapper(myBook));
                    bundle.putParcelableArrayList("bookToShow",arrayList);
                    bundle.putBoolean("isFromInfoBook",true);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
        else{
            mapButton.setVisibility(View.GONE);
        }
        horizontal_photo_list = (LinearLayout) findViewById(R.id.info_book_list_photo_container);
        for_me = new LinkedList<>(book.getBook_photo_list().keySet());
        for (int i = 0; i < book.getBook_photo_list().size(); i++) {
            horizontal_photo_list_element = getLayoutInflater().inflate(R.layout.cell_in_image_list, null);
            ImageView imageView = (ImageView) horizontal_photo_list_element.findViewById(R.id.image_in_horizontal_list_cell);
            StorageReference mImageRef = FirebaseStorage.getInstance().getReference(for_me.get(i));
            GlideApp.with(context).load(mImageRef).placeholder(R.drawable.ic_thumbnail_cover_book).into(imageView);
            GlideApp.with(context).asDrawable().load(mImageRef).into(new SimpleTarget<Drawable>(SimpleTarget.SIZE_ORIGINAL, SimpleTarget.SIZE_ORIGINAL) {
                private ImageView iwPopUp;
                @Override
                public void onStart() {
                    super.onStart();
                    iwPopUp = imageView;
                }
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    if(iwPopUp==null||myActivity==null){
                        return;
                    }
                    ImageViewPopUpHelper.enablePopUpOnClick(myActivity,iwPopUp,resource);
                }
            });
            horizontal_photo_list.addView(horizontal_photo_list_element);
        }
        DocumentReference user_doc;
        user_doc = db.collection("users").document(bookToShowInfoOf.getUser_id());
        user_doc.get().addOnCompleteListener(task -> {
            DocumentSnapshot doc = task.getResult();
            if(doc==null){
                return;
            }
            bookOwner = doc.toObject(User.class);
            TextView tv2 = findViewById(R.id.info_book_owner);
            final Button request_button = findViewById(R.id.info_book_request_book_button);
            if(!bookToShowInfoOf.getUser_id().equals(user.getUid()) &&
                    !bookToShowInfoOf.getStatus()) {
                request_button.setClickable(true);
                request_button.setVisibility(View.VISIBLE);
                request_button.setOnClickListener( (View view) -> checkAlreadyRequested("request"));
                tv2.setText(bookOwner.getUsr_name());
                tv2.setTextColor(Color.BLUE);
                tv2.setClickable(true);
                tv2.setOnClickListener(view -> {
                    Intent toInfoUser = new Intent(InfoBook.this, InfoUser.class);
                    toInfoUser.putExtra("otherUser", new UserWrapper(bookOwner));
                    startActivity(toInfoUser);
                });
            }
            else{
                tv2.setVisibility(View.INVISIBLE);
                tv2.setHeight(0);
                tv2 = findViewById(R.id.info_book_label_owner);
                tv2.setHeight(0);
                tv2.setVisibility(View.INVISIBLE);
                request_button.setVisibility(View.INVISIBLE);
                request_button.setHeight(0);
            }
        });
      showReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(myActivity,ShowReviews.class);
                intent.putExtra("type","book");
                intent.putExtra("thisBook",new BookWrapper(myBook));
                startActivity(intent);
            }
        });
        db.collection("books")
                .document(myBook.getBook_id())
                .collection("reviews")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                Float mean;
                RatingBar ratingBar = findViewById(R.id.info_book_rating_bar);
                if(querySnapshot.getDocuments().size()==0){
                    findViewById(R.id.info_book_reviews_cv).setVisibility(View.GONE);
                    return;
                }
                ratingBar.setVisibility(View.VISIBLE);
                mean = 0f;
                for(DocumentSnapshot doc:querySnapshot.getDocuments()){
                    Review review = doc.toObject(Review.class);
                    mean += review.getRating();
                }
                mean/=querySnapshot.size();
                ratingBar.setRating(mean);
                TextView textView = findViewById(R.id.info_book_rating_count);
                String s = "("+querySnapshot.getDocuments().size()+")";
                textView.setText(s);
                textView.setVisibility(View.VISIBLE);
                textView = findViewById(R.id.info_book_rating_average);
                textView.setText(String.format(Locale.US,"%.1f",mean));
                textView.setVisibility(View.VISIBLE);
            }
        });
    }

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(R.string.info_book_delete_this_book)
                .setMessage(R.string.info_book_delete_want_delete)
                .setIcon(R.drawable.ic_delete_white_24px)
                .setPositiveButton(R.string.info_book_delete_this_book, (dialog, whichButton) -> {
                    //your deleting code
                    DeleteBook(myBook);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
        return myQuittingDialogBox;
    }

    private void DeleteBook(Book bookToDelete) {
        deleteIndexToAlgolia(bookToDelete);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books").document(bookToDelete.getBook_id()).delete().addOnSuccessListener(aVoid -> {
                Log.d("delete","deleted from books");
                DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(bookToDelete.getBook_userid());
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User owner = documentSnapshot.toObject(User.class);
                        owner.getUsr_books().remove(bookToDelete.getBook_id());
                        docRef.update("usr_books",owner.getUsr_books());
                    }
                });
                List<String> x = new LinkedList<String>(bookToDelete.getBook_photo_list().keySet());
                if(bookToDelete.getBook_photo_list().keySet().size() == 0) {
                    Intent bookRemovedintent = new Intent();
                    bookRemovedintent.putExtra("book_removed", new BookWrapper(bookToDelete));
                    setResult(BOOK_EFFECTIVELY_REMOVED,bookRemovedintent);
                    finish();
                }
                for (int i = 0; i < bookToDelete.getBook_photo_list().size(); i++) {
                    StorageReference mImageRef = FirebaseStorage.getInstance().getReference(x.get(i));
                    mImageRef.delete().addOnSuccessListener(aVoid1 -> {
                        // File deleted successfully
                        Log.d("delete", "deleted photo");
                        Intent bookRemovedintent = new Intent();
                        bookRemovedintent.putExtra("book_removed", new BookWrapper(bookToDelete));
                        setResult(BOOK_EFFECTIVELY_REMOVED,bookRemovedintent);
                        finish();
                    });
                }
            });
    }

    public void deleteIndexToAlgolia(Book b){
        String algoliaID = "P15KSBYCLA";
        String algoliaKey = "18740ed4b222f99d8f8dcd7c17002b84";
        Client client = new Client(algoliaID, algoliaKey);


        /* Titolo */
        Index title_index = client.getIndex("book_title");
        title_index.deleteObjectAsync(b.getBook_id(),null);

        /* Autore */
        Index author_index = client.getIndex("book_author");
        author_index.deleteObjectAsync(b.getBook_id(),null);


        /* ISBN */
        Index ISBN_index = client.getIndex("book_ISBN");
        ISBN_index.deleteObjectAsync(b.getBook_id(),null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(bookToShowInfoOf.getUser_id().equals(user.getUid())) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.info_book_menu, menu); //.xml file name
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            case R.id.info_book_delete_book:
                checkAlreadyRequested("delete");
                return true;
            case R.id.info_book_modify_book:
                //Toast.makeText(this, "Features in progress, soon available", Toast.LENGTH_SHORT).show();
                checkAlreadyRequested("modify");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAlreadyRequested(String check){
        FirebaseFirestore.getInstance().collection("requests")
                .whereEqualTo("bookId",bookToShowInfoOf.getId())
                .whereEqualTo("applicantId",FirebaseAuth.getInstance().getUid())
                //.whereEqualTo("ownerId",bookOwner.getUsr_id())
                .whereEqualTo("endLoanApplicant",null)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getDocuments().size()>0){
                        Snackbar.make(findViewById(R.id.info_book_owner),
                                R.string.info_book_request_already_done, Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    else{
                        switch (check){
                            case "request":
                                Intent toRequestBook = new Intent(InfoBook.this, RequestBook.class);
                                toRequestBook.putExtra("requested_book", bookToShowInfoOf);
                                toRequestBook.putExtra("otherUser",new UserWrapper(bookOwner));
                                startActivityForResult(toRequestBook,REQUEST_BOOK);
                                break;
                            case "delete":
                                AskOption();
                                break;
                            case "modify":
                                Intent toEditBook = new Intent(InfoBook.this, EditBook.class);
                                toEditBook.putExtra("book_to_modify", bookToShowInfoOf);
                                startActivityForResult(toEditBook, MODIFY_BOOK);
                        }
                    }
                }
                else{
                    Snackbar.make(findViewById(R.id.request_book_send),
                            R.string.connecting, Snackbar.LENGTH_LONG).show();
                    checkAlreadyRequested(check);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_BOOK:
                if(resultCode == RESULT_OK){
                    Snackbar.make(findViewById(R.id.info_book_ISBN),
                            R.string.request_book_sent, Snackbar.LENGTH_LONG).show();
                }
                break;
            case MODIFY_BOOK:
                if(resultCode == RESULT_OK){
                    Snackbar.make(findViewById(R.id.info_book_ISBN),
                            R.string.request_book_modified, Snackbar.LENGTH_LONG).show();
                    if(data!=null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            BookWrapper bw = extras.getParcelable("bookToShowModified");
//                            bookToShowInfoOf = extras.getParcelable("bookToShowModified");
                            if (bookToShowInfoOf != null) {
//                                myBook = new Book(bookToShowInfoOf);
//                                fillInfoBookViews();
                                Intent bookModifiedIntent = new Intent();
                                bookModifiedIntent.putExtra("book_modified", bw);
                                setResult(BOOK_EFFECTIVELY_MODIFIED, bookModifiedIntent);
                                finish();
                            }
                        }
                    }
                }
                else{
                    finish();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //dopo che l'utente ci ha fornito la risposta alla richesta di permessi
        switch (requestCode){
            case FINE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(myActivity, DisplaySearchOnMap.class);
                    Bundle bundle = new Bundle();
                    ArrayList<BookWrapper> arrayList = new ArrayList<>();
                    arrayList.add(new BookWrapper(myBook));
                    bundle.putParcelableArrayList("bookToShow",arrayList);
                    bundle.putBoolean("isFromInfoBook",true);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.search_book_algolia_request_permission, Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
                    }
                }
                break;
        }
        return;
    }

}
