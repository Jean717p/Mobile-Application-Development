package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class EditActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private int editTextIds[] = new int[]{R.id.Username,R.id.City,R.id.profile_mail,R.id.profile_about};

    private static final int PICK_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        sharedPref = this.
                getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        if(savedInstanceState == null){
            fillUserEditData();
        }
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
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                Uri selectedMediaUri = data.getData();
                if (selectedMediaUri.toString().contains("image")) {
                    //handle image  -- To Do salvare immagine e settarla come immagine
                 }
            }
        }
    }

    private void selectUserImg(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent,"Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    private void storeUserEditData(){
        EditText text;
        SharedPreferences.Editor editor = sharedPref.edit();
        text = (EditText) findViewById(R.id.Username);
        if(text.getText().toString().isEmpty()==false) {
            String x = text.getText().toString();
            editor.putString("user_name", text.getText().toString());
        }
        text = (EditText) findViewById(R.id.City);
        if(text.getText().toString().isEmpty()==false) {
            editor.putString("user_city", text.getText().toString());
        }
        text = (EditText) findViewById(R.id.profile_mail);
        if(text.getText().toString().isEmpty()==false) {
            editor.putString("user_mail", text.getText().toString());
        }
        text = (EditText) findViewById(R.id.profile_about);
        if(text.getText().toString().isEmpty()==false){
            editor.putString("user_about", text.getText().toString());
        }
        editor.apply();
    }

    private void fillUserEditData(){
        EditText inputText;
        inputText = (EditText) findViewById(R.id.Username);
        inputText.setText(sharedPref.getString("user_name", ""));
        inputText = (EditText) findViewById(R.id.City);
        inputText.setText(sharedPref.getString("user_city", ""));
        inputText = (EditText) findViewById(R.id.profile_mail);
        inputText.setText(sharedPref.getString("user_mail", ""));
        inputText = (EditText) findViewById(R.id.profile_about);
        inputText.setText(sharedPref.getString("user_about",""));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        EditText text;
        for(int i: editTextIds){
            text = findViewById(i);
            outState.putString(Integer.toString(i),text.getText().toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EditText text;
        for(int i: editTextIds){
            text = findViewById(i);
            text.setText(savedInstanceState.getString(Integer.toString(i),""));
        }
    }
}
