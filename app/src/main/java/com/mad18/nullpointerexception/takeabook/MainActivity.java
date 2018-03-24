package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_profile);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inbox, menu); //.xml file name
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent(MainActivity.this,EditActivity.class);
        startActivity(intent);
        return true;
    }
    @Override
    protected void onResume(){
        super.onResume();
        fillUserData();
    }

    private void fillUserData(){
        TextView text = findViewById(R.id.Username);
        text.setText(sharedPref.getString("user_name", getString(R.string.name)));
        text = (TextView) findViewById(R.id.City);
        text.setText(sharedPref.getString("user_city", getString(R.string.profile_city)));
        text = (TextView) findViewById(R.id.profile_mail);
        text.setText(sharedPref.getString("user_mail", getString(R.string.profile_mail)));
        text = (TextView) findViewById(R.id.profile_AboutMe);
        text.setText(sharedPref.getString("user_about", getString(R.string.profile_about)));
    }
}
