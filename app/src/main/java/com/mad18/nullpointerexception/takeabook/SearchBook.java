package com.mad18.nullpointerexception.takeabook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class SearchBook extends AppCompatActivity {
    private Toolbar toolbar;
    private String searchBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_book);
        setTitle(R.string.title_activity_search_book);
        searchBase = getIntent().getStringExtra("action");
        EditText text = findViewById(R.id.search_book_edit_text);
        Toolbar toolbar = findViewById(R.id.search_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        switch (searchBase){

            case "Title":
                text.setHint(getString(R.string.search_book_text_title));
                break;
            case "Author":
                text.setHint(getString(R.string.search_book_text_author));
                break;
            case "Category":
                text.setHint(getString(R.string.search_book_text_category));
                break;

            case "ISBN":
                text.setHint(getString(R.string.search_book_text_ISBN));
                text.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;

        }
    }
}
