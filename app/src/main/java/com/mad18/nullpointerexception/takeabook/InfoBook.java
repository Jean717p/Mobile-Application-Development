package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mad18.nullpointerexception.takeabook.addBook.BookWrapper;

import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

public class InfoBook extends AppCompatActivity {
    private final String TAG = "InfoBook";
    private SharedPreferences infoBookSP;
    private int ibTextViewIds[] = new int[]{R.id.info_book_title,R.id.info_book_author, R.id.info_book_ISBN,
        R.id.info_book_editionYear,R.id.info_book_publisher, R.id.info_book_categories, R.id.info_book_description};
    private FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoBookSP = getSharedPreferences("Info Book", Context.MODE_PRIVATE);
        setContentView(R.layout.info_book);
        Toolbar toolbar = findViewById(R.id.info_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Info Book");
        toolbar.setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        user = FirebaseAuth.getInstance().getCurrentUser();

        /**
         * valeria deve inserire questo nel suo codice
         * Intent intent = new Intent(getBaseContext(), NextActivity.class);
         * Foo foo = new Foo();
         * intent.putExtra("foo ", foo);
         * startActivity(intent);
         */

        /**
         * io invece:
         * Foo foo = getIntent().getExtras().getParcelable("foo");
         */
        Book bookToShowInfoOf =  getIntent().getExtras().getParcelable("bookToShow");
        fillInfoBookViews(bookToShowInfoOf);

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

    private void fillInfoBookViews(Book bookToShowInfoOf) {
        /**
         * da fare con book e non bookwrapper
         */

        TextView tv;
        tv = findViewById(R.id.info_book_title);
        tv.setText(bookToShowInfoOf.getBook_title().toString());
        tv = findViewById(R.id.info_book_author);
        String tmp;
        tmp = bookToShowInfoOf.getBook_authors().keySet().toString();
        if(tmp.length()>2){
            tv.setText(tmp.substring(1,tmp.length()-1));
        }
        tv.findViewById(R.id.info_book_ISBN);
        tv.setText(bookToShowInfoOf.getBook_ISBN().toString());
        tv.findViewById(R.id.info_book_editionYear);
        tv.setText(bookToShowInfoOf.getBook_editionYear());
        tv.findViewById(R.id.info_book_publisher);
        tv.setText(bookToShowInfoOf.getBook_publisher().toString());
        tv.findViewById(R.id.info_book_categories);
        tmp = bookToShowInfoOf.getBook_categories().keySet().toString();
        if(tmp.length()>2){
            tv.setText(tmp.substring(1,tmp.length()-1));
        }
        tv = findViewById(R.id.info_book_description);
        tv.setText(bookToShowInfoOf.getBook_description().toString());

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView tv;
        for(int i:ibTextViewIds){
            tv = findViewById(i);
            outState.putString(Integer.toString(i),tv.getText().toString());
        }
        /**
         * da mettere l'immagine thumbnail + le immagini inserite dal proprietario
         */
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TextView tv;
        for(int i:ibTextViewIds){
            tv = findViewById(i);
            tv.setText(savedInstanceState.getString(Integer.toString(i),""));
        }
        /**
         * da implementare per le immagini
         */
    }
}