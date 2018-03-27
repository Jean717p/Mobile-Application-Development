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

public class showProfile extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private int editTextBoxesIds[] = new int[]{R.id.Username,R.id.City,
            R.id.profile_about,R.id.profile_mail};
    private Menu menu;
    private String profileImgName = "profile.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        goToViewMode();
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
        EditText text;
        for(int i:editTextBoxesIds){
            text = findViewById(i);
            text.setText(sharedPref.getString(Integer.toString(i),""));
        }
        if(!sharedPref.getString(profileImgName,"").isEmpty()){
            loadImageFromStorage(sharedPref.getString(profileImgName,""),R.id.personalPhoto);
        }
    }

    private void changeIcon(int iconID){
        runOnUiThread(() -> {
            if (menu != null) {
                MenuItem item = menu.findItem(R.id.action_settings);
                if (item != null) {
                    item.setIcon(iconID);
                }
            }
        });
    }

    private void goToViewMode(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        changeIcon(R.drawable.ic_mode_edit_white_24dp);
        for(int i:editTextBoxesIds){
            findViewById(i).setEnabled(false);
        }
        ImageView iw = findViewById(R.id.personalPhoto);
        iw.setClickable(false);
    }

    private Bitmap loadImageFromStorage(String path, int id) {
        Bitmap b = null;
        File file = new File(path);
        if(file.exists() == false){
            return null;
        }
        try {
            b = BitmapFactory.decodeStream(new FileInputStream(file));
            ImageView img = (ImageView) findViewById(id);
            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }

}
