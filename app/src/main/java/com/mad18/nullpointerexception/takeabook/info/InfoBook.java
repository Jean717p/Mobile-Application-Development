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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.ImageViewPopUpHelper;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;

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
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.info_book);
        Toolbar toolbar = findViewById(R.id.info_book_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_activity_info_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_info_book);
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
            usr_name = doc.getString("usr_name");
            usr_city = doc.getString("usr_city");
            usr_about = doc.getString("usr_about");
            usr_prof_strg_path = doc.getString("profileImgStoragePath");
            TextView tv2 = findViewById(R.id.info_book_owner);
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if(!bookToShowInfoOf.getUser_id().equals(user.getUid())) {
                tv2.setText(usr_name);
                tv2.setTextColor(Color.BLUE);
                tv2.setClickable(true);
                tv2.setOnClickListener(view -> {
                    Intent toInfoUser = new Intent(InfoBook.this, InfoUser.class);
                    toInfoUser.putExtra("usr_id", bookToShowInfoOf.getUser_id());
                    toInfoUser.putExtra("usr_name", usr_name);
                    toInfoUser.putExtra("usr_city", usr_city);
                    toInfoUser.putExtra("usr_bio", usr_about);
                    toInfoUser.putExtra("usr_prof_strg_path", usr_prof_strg_path);
                    //toInfoUser.putExtra("img_uri",downloadOwnerUri);
                    //qui l'immagine

                    startActivity(toInfoUser);
                });
            }
            else{
                tv2.setVisibility(View.INVISIBLE);
                tv2.setHeight(0);
                tv2 = findViewById(R.id.info_book_label_owner);
                tv2.setVisibility(View.INVISIBLE);
                tv2.setHeight(0);
                //TODO: implementa qui il click sul bottone delete this book
                //if isLent == false
                final Button delete_button = findViewById(R.id.info_book_delete_book_button);
                delete_button.setVisibility(View.VISIBLE);
                delete_button.setClickable(true);
                delete_button.setOnClickListener( (View view) -> {
                    AskOption();
                });


            }

        });
    }

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to Delete")
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




        //
        //finish();
//        if(true){
//            SharedPreferences.Editor editor = sharedPref.edit();
//            editor.putBoolean("book_removed",true);
//            editor.apply();
//            Snackbar snackbar = Snackbar
//                  .make(findViewById(R.id.info_book_layout),getText(R.string.info_book_deleted), Snackbar.LENGTH_LONG);
//            snackbar.show();
//            Book book = new Book(bookToShowInfoOf);
//            MainActivity.myBooks.remove(book);
//        }

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
        }
        return super.onOptionsItemSelected(item);
    }

}