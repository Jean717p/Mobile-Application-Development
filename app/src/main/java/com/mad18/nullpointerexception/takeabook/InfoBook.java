package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.addBook.BookWrapper;

import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import java.util.Objects;
public class InfoBook extends AppCompatActivity {

    private int ibTextViewIds[] = new int[]{R.id.info_book_title,R.id.info_book_author, R.id.info_book_ISBN,
        R.id.info_book_editionYear,R.id.info_book_publisher, R.id.info_book_categories, R.id.info_book_description};

//        /**
//         * valeria deve inserire questo nel suo codice
//         * Intent intent = new Intent(getBaseContext(), NextActivity.class);
//         * Foo foo = new Foo();
//         * intent.putExtra("foo ", foo);
//         * startActivity(intent);
//         */
//
//        /**
//         * io invece:
//         * Foo foo = getIntent().getExtras().getParcelable("foo");
//         */

    BookWrapper bookToShowInfoOf = Objects.requireNonNull(getIntent().getExtras()).getParcelable("bookToShow");
    private String usr_name;
    private String usr_city;
    private String usr_about;
    private FirebaseUser user;
    private Menu menu;

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

        fillInfoBookViews();



        //        ....To do later......
//        LinearLayout layout = (LinearLayout) view.findViewById(R.id.image_container)
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//
//// Add 4 images
//
//        for (int i = 0; i < 4; i++) {
//            layoutParams.setMargins(20, 20, 20, 20);
//            layoutParams.gravity = Gravity.CENTER;
//            ImageView imageView = new ImageView(getActivity());
//            imageView.setImageResource(R.drawable.image);
//            imageView.setOnClickListener(documentImageListener);
//            imageView.setLayoutParams(layoutParams);
//
//            layout.addView(imageView);
//
//        }

    }

    private void fillInfoBookViews() {

        TextView tv;
        tv = findViewById(R.id.info_book_title);
        tv.setText(bookToShowInfoOf.getTitle());
        tv = findViewById(R.id.info_book_author);
        String tmp;
        tmp = bookToShowInfoOf.getAuthors().toString();
        if(tmp.length()>2){
            tv.setText(tmp.substring(1,tmp.length()-1));
        }
        tv = findViewById(R.id.info_book_ISBN);
        tv.setText(bookToShowInfoOf.getISBN());
        tv = findViewById(R.id.info_book_editionYear);
        tv.setText(Integer.toString(bookToShowInfoOf.getEditionYear()));
        tv = findViewById(R.id.info_book_publisher);
        tv.setText(bookToShowInfoOf.getPublisher());
        tv = findViewById(R.id.info_book_categories);
        tmp = bookToShowInfoOf.getCategories().toString();
        if(tmp.length()>2){
            tv.setText(tmp.substring(1,tmp.length()-1));
        }
       tv = findViewById(R.id.info_book_description);
       tv.setText(bookToShowInfoOf.getDescription());
       ImageView iw = findViewById(R.id.info_book_main_image);
       Book book = new Book(bookToShowInfoOf);
       Glide.with(this).load(book.getBook_thumbnail_url()).into(iw);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference user_doc;

        user_doc = db.collection("users").document(bookToShowInfoOf.getUser_id());
        user_doc.get().addOnCompleteListener(task -> {
            DocumentSnapshot doc = task.getResult();
            //thisUser = doc.toObject(User.class);
            usr_name = doc.getString("usr_name");
            usr_city = doc.getString("usr_city");
            usr_about = doc.getString("usr_about");
            TextView tv2 = findViewById(R.id.info_book_owner);
            tv2.setText(usr_name);
            tv2.setTextColor(Color.BLUE);
            tv2.setClickable(true);
            tv2.setOnClickListener(view -> {
                Intent toInfoUser = new Intent(getApplicationContext() , InfoUser.class);
                toInfoUser.putExtra("usr_id", bookToShowInfoOf.getUser_id());
                toInfoUser.putExtra("usr_name",usr_name);
                toInfoUser.putExtra("usr_city", usr_city);
                toInfoUser.putExtra("usr_bio", usr_about);
                //toInfoUser.putExtra("img_uri",downloadOwnerUri);
                //qui l'immagine

                startActivity(toInfoUser);
            });

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fillInfoBookViews();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView tv;
        for(int i:ibTextViewIds){
            tv = findViewById(i);
            outState.putString(Integer.toString(i),tv.getText().toString());
        }
//        /**
//         * da mettere l'immagine thumbnail + le immagini inserite dal proprietario
//         */
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TextView tv;
        for(int i:ibTextViewIds){
            tv = findViewById(i);
            tv.setText(savedInstanceState.getString(Integer.toString(i),""));
        }
//        /**
//         * da implementare per le immagini
//         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
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
}
