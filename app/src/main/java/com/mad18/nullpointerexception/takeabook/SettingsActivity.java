package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    AlertDialog languagedialog;
    Resources res;
    TextView tv_language;
    TextView tv_choosen_language;
    CardView language_view;
    MotionEvent event;
    Toolbar toolbar;
    private SharedPreferences sharedPref;
    private int checkedItem = 0;
    final CharSequence[] items = {"Italiano","English"};
    public static final String sharedDataLanguage[] = new String[]{"checkedItem","language"};
    public static final String language = "language";
    private Menu menu;
    private boolean langChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        //editor.putInt(sharedDataLanguage[0],checkedItem);
        //editor.putString(sharedDataLanguage[1],items[0].toString());
        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.nav_settings);
        tv_language = findViewById(R.id.settings_language_label);
        tv_choosen_language = findViewById(R.id.settings_language);
        language_view = findViewById(R.id.language_card_view);
        language_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // v.setBackgroundColor(Color.parseColor("#BDBDBD"));
                ShowDialog();
            }
        });
        langChanged = false;
        String y;
        y = sharedPref.getString(sharedDataLanguage[1],"");
        if(y.length()>0){
            tv_choosen_language.setText(y);
            checkedItem = sharedPref.getInt(sharedDataLanguage[0],0);
        }
        else{
            Locale current = getResources().getConfiguration().locale;
            switch (current.getLanguage()){
                case "it":
                    tv_choosen_language.setText(items[0].toString());
                    checkedItem = 0;
                    break;
                case "en":
                    tv_choosen_language.setText(items[1].toString());
                    checkedItem = 1;
                    break;
                default:
                    tv_choosen_language.setText(items[1].toString());
                    checkedItem = 1;
                    break;
            }
        }
    }

    public void ShowDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_lang);
        SharedPreferences.Editor editor = sharedPref.edit();
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String x = sharedPref.getString(language,"");
                res = getResources();
                switch(item)
                {
                    case 0:
                        if(x.length()==0 || x.equals(items[0].toString())==false){
                            changeLocale(res,"it");
                            checkedItem = 0;
                            tv_choosen_language.setText(items[0]);
                            editor.putInt(sharedDataLanguage[0],checkedItem);
                            editor.putString(sharedDataLanguage[1],items[0].toString());
                            tv_language.setText(R.string.select_lang);
                            setTitle(R.string.nav_settings);
                            langChanged = true;
                            editor.apply();
                        }
                        break;
                    case 1:
                        if(x.length()==0 || x.equals(items[1].toString())==false) {
                            changeLocale(res, "eng");
                            checkedItem = 1;
                            tv_choosen_language.setText(items[1]);
                            editor.putInt(sharedDataLanguage[0], checkedItem);
                            editor.putString(sharedDataLanguage[1], items[1].toString());
                            tv_language.setText(R.string.select_lang);
                            setTitle(R.string.nav_settings);
                            editor.apply();
                            langChanged = true;
                        }
                        break;
                }
                languagedialog.dismiss();
            }
        });
        languagedialog = builder.create();
        languagedialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent();
        switch (item.getItemId()){
            case android.R.id.home:
                if(langChanged){
                    intent.putExtra("langChanged",true);
                    setResult(RESULT_OK,intent);
                }
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void changeLocale(Resources res, String locale){
        Configuration config;
        config = new Configuration(res.getConfiguration());
        switch (locale){
            case "it":
                config.locale = Locale.ITALIAN;
                break;

            case "eng":
                config.locale = Locale.ENGLISH;
                break;

            default:
                config.locale = Locale.ENGLISH;
                break;
        }
        res.updateConfiguration(config,res.getDisplayMetrics());
    }

}
