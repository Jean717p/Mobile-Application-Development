package com.mad18.nullpointerexception.takeabook.myProfile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import android.widget.TextView;
import com.mad18.nullpointerexception.takeabook.R;


public class showProfile extends AppCompatActivity {
    private final String TAG = "showProfile";
    private SharedPreferences sharedPref;
    private final int textViewIds[] = new int[]{R.id.show_profile_Username, R.id.show_profile_City,
            R.id.show_profile_mail,R.id.show_profile_about};
    private Menu menu;

    public static final String sharedUserDataKeys[] = new String[]{"usr_name","usr_city","usr_mail","usr_about"};
    public static final String profileImgName = "profile.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        setContentView(R.layout.show_profile);
        Toolbar toolbar = findViewById(R.id.show_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.app_name);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
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
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillUserData(){
        TextView text;
        String y;
        int i=0;
        ImageView iw;

        for(String x:sharedUserDataKeys){
            text = findViewById(textViewIds[i]);
            y=sharedPref.getString(x,"");
            if(y.length()>0 || textViewIds[i] == R.id.show_profile_about){
                text.setText(y);
            }
            i++;
        }
        y=sharedPref.getString(profileImgName,"");
        if(y.length()>0){
            editProfile.loadImageFromStorage(y,R.id.show_profile_personalPhoto,this);
        }
        else{
            iw = findViewById(R.id.show_profile_personalPhoto);
            iw.setImageResource(R.drawable.ic_account_circle_white_48px);
        }
    }

    public static void deleteUserData(SharedPreferences sharedPrefToDel){
        String profileImgPath = sharedPrefToDel.getString(profileImgName,"");
        sharedPrefToDel.edit().clear().apply();
        if(profileImgPath.length()>0){
            File file = new File(profileImgPath);
            if(file.exists()){
                file.delete();
            }
        }
    }
}
