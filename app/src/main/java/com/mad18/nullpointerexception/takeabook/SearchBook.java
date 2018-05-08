package com.mad18.nullpointerexception.takeabook;

import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.Continuation;
import com.mad18.nullpointerexception.takeabook.addBook.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.mad18.nullpointerexception.takeabook.addBook.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class SearchBook extends AppCompatActivity {
    private final String TAG="SearchBook";
    private Toolbar toolbar;
    private String searchBase;
    private Menu menu;

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
                text.setText("Harry Potter e la pietra filosofale");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Metodo per ottenere i libri corrispondenti agli autori dal database
     */

    private LinkedList getBooksFromAuthors(String text){
        LinkedList list = new LinkedList();
        list = (LinkedList) getFromGoogleApi("Author", text);
        return list;
    }

    /* Metodo per ottenere i libri corrispondenti al titolo dal database
     */

    private LinkedList getBooksFromTitle(String text){
        LinkedList list = new LinkedList();
        list = (LinkedList) getFromGoogleApi("Title", text);
        return list;
    }

    private LinkedList getBooksFromISBN(String text){
        LinkedList list = new LinkedList();
        list = (LinkedList) getFromGoogleApi("ISBN", text);
        return list;
    }

    private List<Book> searchBooksOnFireStore(String flag,List<String> inputList){
        List<Book> booksFound = new LinkedList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        double user_lat=0,user_long=0;
        Location user_loc;
        if(inputList.size()==0){
            return booksFound;
        }
        CollectionReference booksRef = db.collection("books");
        for(String x : inputList){
            switch(flag){
                case "Title":
                    Query query = booksRef.whereEqualTo("book_title",x);
                    Task<QuerySnapshot> task = query.get().continueWithTask(new Continuation<QuerySnapshot, Task<QuerySnapshot>>() {
                        @Override
                        public Task<QuerySnapshot> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                            QuerySnapshot snapshots = task.getResult();
                            List<DocumentSnapshot> documents = snapshots.getDocuments();
                            for(DocumentSnapshot documentSnapshot:documents){
                                if(documentSnapshot!=null){
                                    if(documentSnapshot.exists()){
                                        booksFound.add(documentSnapshot.toObject(Book.class));
                                        break;
                                    }
                                }
                            }
                            return null;
                        }
                    });

                    break;
                case "Author":
                    query = booksRef.whereEqualTo("book_authors."+x,true);
                    break;
                case "ISBN":
                    query = booksRef.whereEqualTo("book_ISBN",x);
                    break;
                default:
                    return booksFound;
            }
        }
        /*
        Bundle intentExtras = getIntent().getExtras();
        if(intentExtras!=null){
            user_lat = intentExtras.getDouble("user_lat");
            user_long = intentExtras.getDouble("user_long");
        }
        user_loc = new Location("Provider");
        user_loc.setLatitude(user_lat);
        user_loc.setLongitude(user_long);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            booksFound = booksFound.stream().sorted((a,b)->{
                Location book_loc_a = new Location("Provider");
                Location book_loc_b = new Location("Provider");
                book_loc_b.setLatitude(b.getBook_location().getLatitude());
                book_loc_b.setLongitude(b.getBook_location().getLongitude());
                book_loc_a.setLatitude(a.getBook_location().getLatitude());
                book_loc_a.setLongitude(a.getBook_location().getLongitude());
                return Float.compare(book_loc_a.distanceTo(user_loc),book_loc_b.distanceTo(user_loc));
            }).collect(Collectors.toList());
        }
        else{
            Collections.sort(booksFound, (a, b) -> {
                Location book_loc_b = new Location("Provider");
                Location book_loc_a = new Location("Provider");
                book_loc_b.setLatitude(b.getBook_location().getLatitude());
                book_loc_b.setLongitude(b.getBook_location().getLongitude());
                book_loc_a.setLatitude(a.getBook_location().getLatitude());
                book_loc_a.setLongitude(a.getBook_location().getLongitude());
                return Float.compare(book_loc_a.distanceTo(user_loc),book_loc_b.distanceTo(user_loc));
            });
        }
        */
        return booksFound;
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
                            JsonParser jsonParser = new JsonParser();
                            for(int i = 0; i < maxCycle; i++) {
                                String url = "https://www.googleapis.com/books/v1/volumes?maxResults=40&orderBy=relevance&q=" +
                                        text +
                                        "&fields=items(volumeInfo/title)&startIndex=" + Integer.toString(i*40);
                                JSONObject jsonObject = jsonParser.makeHttpRequest(url,
                                        "GET", new HashMap<String, String>());
                                if(jsonObject.has("items")){
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
                            searchBooksOnFireStore(flag,list);
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case "Author":
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int numItems = getNumItemsFromApi(flag);
                            int maxCycle = numItems / 40; //Posso prendere solo 40 item alla volta dal json
                            if((numItems%40) != 0) {
                                maxCycle++;
                            }
                            JsonParser jsonParser = new JsonParser();
                            for(int i = 0; i < maxCycle; i++) {
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
                            searchBooksOnFireStore(flag,list);
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            break;
            case "ISBN":
                list.add(text);
                searchBooksOnFireStore(flag,list);
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
        } catch (Exception e){
            e.printStackTrace();
        }
        return totItems;
    }

}

