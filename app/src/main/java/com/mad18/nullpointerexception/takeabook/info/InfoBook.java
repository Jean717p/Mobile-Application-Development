package com.mad18.nullpointerexception.takeabook.info;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.ImageViewPopUpHelper;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.requestBook.RequestBook;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class InfoBook extends AppCompatActivity {

    private final int BOOK_EFFECTIVELY_REMOVED = 41;
    private int ibTextViewIds[] = new int[]{R.id.info_book_title, R.id.info_book_author, R.id.info_book_ISBN,
            R.id.info_book_editionYear, R.id.info_book_publisher, R.id.info_book_categories, R.id.info_book_description,
            R.id.info_book_pages};

    BookWrapper bookToShowInfoOf;
    private String usr_name;
    private String usr_city;
    private String usr_about;
    private FirebaseAuth mAuth;
    private Menu menu;
    private LinearLayout horizontal_photo_list;
    private View horizontal_photo_list_element;
    private List<String> for_me;
    private boolean isImageFitToScreen = true;
    private String usr_prof_strg_path;
    private int j=0;
    private User bookOwner;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.info_book);
        Toolbar toolbar = findViewById(R.id.info_book_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_activity_info_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //setTitle(R.string.title_activity_info_book);

        toolbar.setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bookToShowInfoOf = getIntent().getExtras().getParcelable("bookToShow");
        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        fillInfoBookViews();


    }

    private void fillInfoBookViews() {

        TextView tv;
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
        Glide.with(this).load(book.getBook_thumbnail_url()).into(iw);


        //simo inizio

        horizontal_photo_list = (LinearLayout) findViewById(R.id.info_book_list_photo_container);

        for_me = new LinkedList<>(book.getBook_photo_list().keySet());
        for (int i = 0; i < book.getBook_photo_list().size(); i++) {
            horizontal_photo_list_element = getLayoutInflater().inflate(R.layout.cell_in_image_list, null);
            ImageView imageView = (ImageView) horizontal_photo_list_element.findViewById(R.id.image_in_horizontal_list_cell);
            horizontal_photo_list.addView(horizontal_photo_list_element);
            StorageReference mImageRef = FirebaseStorage.getInstance().getReference(for_me.get(i));

            GlideApp.with(this).load(mImageRef).into(imageView);

            ImageViewPopUpHelper.enablePopUpOnClick(InfoBook.this,imageView);

        }
        //simo fine

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference user_doc;

        user_doc = db.collection("users").document(bookToShowInfoOf.getUser_id());
        user_doc.get().addOnCompleteListener(task -> {
            DocumentSnapshot doc = task.getResult();
            //thisUser = doc.toObject(User.class);
            UserWrapper bookOwnerWrapped;
            if(doc==null){
                return;
            }
            bookOwner = doc.toObject(User.class);
            bookOwnerWrapped = new UserWrapper(bookOwner);
            TextView tv2 = findViewById(R.id.info_book_owner);
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if(!bookToShowInfoOf.getUser_id().equals(user.getUid())) {
                final Button request_button = findViewById(R.id.info_book_request_book_button);
                request_button.setClickable(true);
                request_button.setVisibility(View.VISIBLE);
                request_button.setOnClickListener( (View view) -> {
                    //TODO: richiesta libro
                    Intent toRequestBook = new Intent(InfoBook.this, RequestBook.class);
                    toRequestBook.putExtra("requested_book", bookToShowInfoOf);
                    toRequestBook.putExtra("otherUser",bookOwnerWrapped);
                    startActivity(toRequestBook);
                });
                tv2.setText(bookOwner.getUsr_name());
                tv2.setTextColor(Color.BLUE);
                tv2.setClickable(true);
                UserWrapper userWrapper = new UserWrapper(bookOwner);
                tv2.setOnClickListener(view -> {
                    Intent toInfoUser = new Intent(InfoBook.this, InfoUser.class);
                    toInfoUser.putExtra("otherUser", userWrapper);
                    //toInfoUser.putExtra("img_uri",downloadOwnerUri);
                    //qui l'immagine

                    startActivity(toInfoUser);
                });
            }
            else{
                tv2.setVisibility(View.GONE);
                //tv2.setHeight(0);
                tv2 = findViewById(R.id.info_book_label_owner);
                tv2.setVisibility(View.GONE);
                //tv2.setHeight(0);
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
                    DeleteBook();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
        return myQuittingDialogBox;

    }

    private void DeleteBook() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("books").document(bookToShowInfoOf.getISBN() + bookToShowInfoOf.getUser_id())
                .delete().addOnSuccessListener(new OnSuccessListener< Void >() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(InfoBook.this, "Data deleted !",
//                        Toast.LENGTH_SHORT).show();
                Log.d("delete","deleted from books");

                DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(MainActivity.thisUser.getUsr_id());
                Map<String,Object> updates = new HashMap<>();
                String that_specific_book = "usr_books."+bookToShowInfoOf.getISBN()+bookToShowInfoOf.getUser_id();
                updates.put(that_specific_book, FieldValue.delete());
                docRef.update(updates).addOnCompleteListener(task -> {
                    Log.d("delete","deleted from users");

                    for_me = new LinkedList<>(bookToShowInfoOf.getPhoto_list());
                    if(bookToShowInfoOf.getPhoto_list().size() == 0) {
                        Intent bookRemovedintent = new Intent();
                        bookRemovedintent.putExtra("book_removed", bookToShowInfoOf);
                        setResult(BOOK_EFFECTIVELY_REMOVED,bookRemovedintent);
                        finish();
                    }
                    for (int i = 0; i < bookToShowInfoOf.getPhoto_list().size(); i++) {
                        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(for_me.get(i));
                        mImageRef.delete().addOnSuccessListener(aVoid1 -> {
                            // File deleted successfully

                            Log.d("delete", "deleted photo");

                            Intent bookRemovedintent = new Intent();
                            bookRemovedintent.putExtra("book_removed", bookToShowInfoOf);
                            setResult(BOOK_EFFECTIVELY_REMOVED,bookRemovedintent);
                            finish();
                        });
                    }
                });
                }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //fillInfoBookViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView tv;
        for (int i : ibTextViewIds) {
            tv = findViewById(i);
            outState.putString(Integer.toString(i), tv.getText().toString());
        }
//        /**
//         * da mettere l'immagine thumbnail + le immagini inserite dal proprietario
//         */
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TextView tv;
        for (int i : ibTextViewIds) {
            tv = findViewById(i);
            tv.setText(savedInstanceState.getString(Integer.toString(i), ""));
        }

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
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            case R.id.info_book_delete_book:
                //TODO: if isLent == false
                AskOption();
                return true;
            case R.id.info_book_modify_book:
                //TODO: fase di modifica e update del libro
                Toast.makeText(this, "Features in progress, soon available", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
