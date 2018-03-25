package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private int editTextBoxesIds[] = new int[]{R.id.Username,R.id.City,
            R.id.profile_about,R.id.profile_mail};
    private Menu menu;
    private boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        editMode =false;
        if(savedInstanceState == null){
            fillUserData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu=menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inbox, menu); //.xml file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                if(this.editMode ==true){
                    storeUserEditData();
                    onBackPressed();
                }
                else{
                    goToEditMode();
                }
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume(){
        super.onResume();
    }

    private void fillUserData(){
        EditText text;
        for(int i:editTextBoxesIds){
            text = findViewById(i);
            text.setText(sharedPref.getString(Integer.toString(i),""));
        }
    }

    private void storeUserEditData(){
        EditText text;
        SharedPreferences.Editor editor = sharedPref.edit();
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            editor.putString(Integer.toString(i),text.getText().toString());
        }
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        EditText text;
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            outState.putString(Integer.toString(i),text.getText().toString());
        }
        outState.putString("editMode",Boolean.toString(this.editMode));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EditText text;
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            text.setText(savedInstanceState.getString(Integer.toString(i),""));
        }
        if(savedInstanceState.getString("editMode","false").equals("true")==true){
            goToEditMode();
        }

    }

    public void changeIcon(int iconID){
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
        fillUserData();
        this.editMode = false;

    }

    private void goToEditMode(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        changeIcon(R.drawable.ic_done_white_48dp);
        for(int i: editTextBoxesIds){

            findViewById(i).setEnabled(true);

        }
        this.editMode =true;
    }

    @Override
    public void onBackPressed() {
        if(this.editMode ==true){
            goToViewMode();
        }
        else{
            super.onBackPressed();
        }
    }
    /***** To Do */
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            if (requestCode == PICK_IMAGE) {
//                Uri selectedMediaUri = data.getData();
//                if (selectedMediaUri.toString().contains("image")) {
//                    //handle image  -- To Do salvare immagine e settarla come immagine
//                }
//            }
//        }
//    }
//
//    private void selectUserImg(){
//        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        getIntent.setType("image/*");
//        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        pickIntent.setType("image/*");
//        Intent chooserIntent = Intent.createChooser(getIntent,"Select Image");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
//        startActivityForResult(chooserIntent, PICK_IMAGE);
//    }

}
