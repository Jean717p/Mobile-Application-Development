package com.mad18.nullpointerexception.takeabook;

import android.content.Intent;
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

import com.mad18.nullpointerexception.takeabook.addBook.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


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
                text.setText("Harry Potter");
                //LinkedList title = (LinkedList) getBooksFromTitle(text.getText().toString());
                break;
            case "Author":
                text.setHint(getString(R.string.search_book_text_author));

                break;
            case "ISBN":
                text.setHint(getString(R.string.search_book_text_ISBN));
                text.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
        }
    }

    /**
     * Metodo per ottenere i libri corrispondenti agli autori dal database
     */

    private List<String> getBooksFromAuthors(String text){
        List list = new LinkedList();
        list = getFromGoogleApi("Author", text);
        return list;
    }

    /**
     * Metodo per ottenere i libri corrispondenti al titolo dal database
     */

    private List<String> getBooksFromTitle(String text){
        List list = new LinkedList();
        list = getFromGoogleApi("Title", text);
        return list;
    }


    /**
     * Metodo per ottenere i libri corrispondenti all'ISBN dal database
     */

    private List<String> getBooksFromISBN(String text){
        List list = new LinkedList();
        list = getFromGoogleApi("ISBN", text);
        return list;
    }

    /**
     * Metodo utilizzato per ricavare le liste dalle googleAPI
     */
    private List<String> getFromGoogleApi(String flag, String text){
        List list = new LinkedList();
        switch (flag){
            case "Title":
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int numItems = getNumItemsFromApi(flag);

                            int maxCycle = numItems / 40; //Posso prendere solo 40 item alla volta dal json
                            if((numItems%40) != 0) {
                                maxCycle++;
                            }
                            for(int i = 0; i < maxCycle; i++) {
                                JsonParser jsonParser = new JsonParser();
                                JSONObject jsonObject = jsonParser.makeHttpRequest(
                                        "https://www.googleapis.com/books/v1/volumes?maxResults=40&orderBy=relevance&q=" +
                                                text +
                                                "&fields=items(volumeInfo/title)&startIndex=" + Integer.toString(i*40) ,
                                        "GET", new HashMap<String, String>());
                                JSONArray tmp =  jsonObject.getJSONArray("items");
                                for(int j = 0; j < tmp.length(); j++) {
                                    JSONObject item = tmp.getJSONObject(j);
                                    if(item.has("volumeInfo")){
                                        JSONObject vol = item.getJSONObject("volumeInfo");
                                        if(vol.has("title")){
                                            String info = vol.getString("title");
                                            if(list.contains(info) == false) {
                                                list.add(info);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }).start();

            break;
            case "Author":
            break;
            case "ISBN":
            break;
        }
        return list;

        }

    private int getNumItemsFromApi(String text) {
        int totItems = 0;
        try {
            JsonParser jsonParser = new JsonParser();
            JSONObject jsonObject = jsonParser.makeHttpRequest(
                    "https://www.googleapis.com/books/v1/volumes?q=" + text + "&fields=totalItems",
                    "GET", new HashMap<String, String>());
            if (jsonObject.has("totalItems")) {
                totItems = jsonObject.getInt("totalItems");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return totItems;
    }








}
