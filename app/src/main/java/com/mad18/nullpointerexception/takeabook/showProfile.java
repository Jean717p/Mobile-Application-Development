package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import android.widget.TextView;

public class showProfile extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private int textViewIds[] = new int[]{R.id.show_profile_Username,R.id.show_profile_City,
            R.id.show_profile_mail,R.id.show_profile_about};
    public static final String sharedUserDataKeys[] = new String[]{"usr_name","usr_city","usr_mail","usr_about"};
    private Menu menu;
    private String profileImgName = "profile.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        setContentView(R.layout.show_profile);
        Toolbar toolbar = findViewById(R.id.show_profile_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillUserData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inbox, menu); //.xml file name
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(this,editProfile.class);
                startActivity(intent);
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillUserData(){
        TextView text;
        int i=0;
        for(String x:sharedUserDataKeys){
            text = findViewById(textViewIds[i++]);
            if(sharedPref.contains(x)){
                text.setText(sharedPref.getString(x,""));
            }
        }
        if(sharedPref.contains(profileImgName)){
            loadImageFromStorage(sharedPref.getString(profileImgName,""),R.id.show_profile_personalPhoto);
        }
    }

    private Bitmap loadImageFromStorage(String path,int id) {
        if(path==null){
            return null;
        }
        Bitmap b = null;
        File file = new File(path);
        ImageView img = (ImageView) findViewById(id);
        if(file.exists() == false||img==null){
            return null;
        }
        try {
            b = BitmapFactory.decodeStream(new FileInputStream(file));
            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }

}
