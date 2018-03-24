package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class EditActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        sharedPref = this.
                getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile, menu); //.xml file name
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            case R.id.action_save:
                storeUserEditData();
                finish();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillUserEditData();
    }

    private void storeUserEditData(){
        EditText text;
        SharedPreferences.Editor editor = sharedPref.edit();
        text = (EditText) findViewById(R.id.profile_username);
        if(text.getText().toString().isEmpty()==false) {
            String x = text.getText().toString();
            editor.putString("user_name", text.getText().toString());
        }
        text = (EditText) findViewById(R.id.profile_city);
        if(text.getText().toString().isEmpty()==false) {
            editor.putString("user_city", text.getText().toString());
        }
        text = (EditText) findViewById(R.id.profile_mail);
        if(text.getText().toString().isEmpty()==false) {
            editor.putString("user_mail", text.getText().toString());
        }
        text = (EditText) findViewById(R.id.profile_about);
        if(text.getText().toString().isEmpty()==false){
                //&& t.getText().toString().length()<600) {
            editor.putString("user_about", text.getText().toString());
        }
        editor.apply();
    }

    private void fillUserEditData(){
        EditText inputText;
        inputText = (EditText) findViewById(R.id.profile_username);
        inputText.setText(sharedPref.getString("user_name", ""));
        inputText = (EditText) findViewById(R.id.profile_city);
        inputText.setText(sharedPref.getString("user_city", ""));
        inputText = (EditText) findViewById(R.id.profile_mail);
        inputText.setText(sharedPref.getString("user_mail", ""));
        inputText = (EditText) findViewById(R.id.profile_about);
        inputText.setText(sharedPref.getString("user_about",""));
    }

}
