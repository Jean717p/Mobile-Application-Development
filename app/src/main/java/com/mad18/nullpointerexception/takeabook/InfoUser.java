package com.mad18.nullpointerexception.takeabook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class InfoUser extends AppCompatActivity {

    private String usr_name;
    private String usr_city;
    private String usr_bio;
    private String usr_prof_stg_path;
    //private Uri usr_img_uri;
    private String usr_id;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_user);
        Toolbar toolbar = findViewById(R.id.info_user_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_info_user);
        toolbar.setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        String user_id = getIntent().getExtras().getString("userID");
        usr_name = getIntent().getExtras().getString("usr_name");
        usr_city = getIntent().getExtras().getString("usr_city");
        usr_bio = getIntent().getExtras().getString("usr_bio");
        //usr_img_uri = (Uri)getIntent().getExtras().get("img_uri");
        usr_id = getIntent().getExtras().getString("usr_id");
        usr_prof_stg_path = getIntent().getExtras().getString("usr_prof_strg_path");
        usr_prof_stg_path = usr_prof_stg_path.substring(1);
        //fillInfoUserViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillInfoUserViews();
    }

    private void fillInfoUserViews(){
        TextView tv = findViewById(R.id.info_user_username);
        tv.setText(usr_name);
        tv = findViewById(R.id.info_user_city);
        tv.setText(usr_city);
        tv = findViewById(R.id.info_user_about_me);
        tv.setText(usr_bio);
        ImageView iv = findViewById(R.id.info_user_photo_profile);

        //iv.setImageResource(R.drawable.ic_book_cover);


        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(usr_prof_stg_path);
        //iv.setImageResource(R.drawable.ic_book_cover);

        GlideApp.with(this).load(mImageRef).placeholder(R.drawable.account_circle).into(iv);
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

