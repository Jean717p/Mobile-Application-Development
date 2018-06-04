package com.mad18.nullpointerexception.takeabook.searchBook;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.JsonParser;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.MyAtomicCounter;
import com.mad18.nullpointerexception.takeabook.util.OnCounterChangeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


public class SearchBook extends AppCompatActivity {
    private final String TAG="SearchBook";
    private Toolbar toolbar;
    private String searchBase;
    private Menu menu;
    private MyAtomicCounter booksFoundCounter;
    private List<Book> booksFound;
    private ViewPager viewPager;
    private SearchBookPagerAdapter myAdapter;
    private TabLayout tabLayout;
    private Boolean resultFragmentChanged;
    private Boolean Issearching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Issearching = false;
        setContentView(R.layout.search_book);
        setTitle(R.string.title_activity_search_book);
        searchBase = getIntent().getStringExtra("action");
        Toolbar toolbar = findViewById(R.id.search_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        booksFound = new LinkedList<>();
        booksFoundCounter = new MyAtomicCounter(0);
        // Create an instance of the tab layout from the view.
        tabLayout = (TabLayout) findViewById(R.id.search_book_tab_layout);
        // Set the text for each tab.
        tabLayout.addTab(tabLayout.newTab().setText(R.string.search_books));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.found_books));
        // Set the tabs to fill the entire layout.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager = (ViewPager) findViewById(R.id.search_book_view_pager);
        myAdapter = new SearchBookPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(myAdapter);
        resultFragmentChanged = true;
        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                SearchBookPagerAdapter adapter = (SearchBookPagerAdapter) viewPager.getAdapter();
                switch (tab.getPosition()){
                    case 0:
                        if(Issearching==false){
                            SearchBook_search search_f = (SearchBook_search) adapter.getRegisteredFragment(viewPager.getCurrentItem());
                            if(search_f!=null){
                                search_f.ProgressBarVisibility(View.INVISIBLE);
                                search_f.ButtonSearchVisibility(View.VISIBLE);
                            }
                            //chiamiamo un metodo del fragment che mette il bottone visibile e la progress
                            //bar invisibile
                        }
                        break;
                    case 1:
                        if(resultFragmentChanged){
                            Issearching=false;
                            SearchBook_found f = (SearchBook_found) adapter.getRegisteredFragment(viewPager.getCurrentItem());
                            if(f!=null){
                                f.updateView(booksFound);
                            }
                            resultFragmentChanged=false;
                        }
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        booksFoundCounter.setListener(new OnCounterChangeListener() {
            @Override
            public void onCounterReachZero() {
                resultFragmentChanged=true;
                Bundle intentExtras = getIntent().getExtras();
                if(booksFound.size()==0){
                    /** change UI - NO books found **/

                }
                else if(booksFound.size()==1){
                    /** change UI **/
                    tabLayout.getTabAt(1).select();
                    return;
                }
                //Sort alfabelito booksfound
                /** Change UI **/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    booksFound = booksFound.stream()
                            .collect(toMap(Book::getBook_ISBN, b->b))
                            .values()
                            .stream()
                            .sorted((a,b)->a.getBook_title().compareTo(b.getBook_title()))
                            .collect(toList());
                }
                else{
                    for(int i=0;i<booksFound.size();i++){
                        Book b = booksFound.get(i);
                        for(int j=i+1;j<booksFound.size();){
                            if(b.getBook_ISBN().equals(booksFound.get(j).getBook_ISBN())){
                                booksFound.remove(j);
                            }
                            else{
                                j++;
                            }
                        }
                    }
                    Collections.sort(booksFound, (a, b) -> a.getBook_title().compareTo(b.getBook_title()));
                }
                tabLayout.getTabAt(1).select();
            }
        });
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

    public void searchForBook(String flag){
        Issearching = true;
        EditText text = findViewById(R.id.search_book_edit_text);
        String to_find;
        to_find = text.getText().toString();
        booksFound.clear();
        if(to_find.length()>0){
            getFromGoogleApi(flag,to_find);
            //Startare l'intent della nuova activity o riempire fragment con i risultati
        }
        else{
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.search_book_layout),getText(R.string.search_book_nothing_inserted), Snackbar.LENGTH_LONG);
            snackbar.show();
            //Issearching = false;
            return;
        }


    }

    private void searchBooksOnFireStore(String flag, List<String> inputList){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(inputList.size()==0){
            booksFoundCounter.getListener().onCounterReachZero();
            return;
        }
        CollectionReference booksRef = db.collection("books");
        booksFoundCounter.set(1);
        for(String x : inputList){
            if(x.length()==0){
                continue;
            }
            else if(x.contains("*")||x.contains("~")||x.contains("/")||x.contains("[")||x.contains("]")
                ||x.contains("..")){
                continue;
            }
            else if(x.startsWith(".")||x.endsWith(".")){
                continue;
            }
            switch(flag){
                case "Title":
                    Query query_t = booksRef.whereEqualTo("book_title",x);
                    booksFoundCounter.increment();
                    query_t.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for(DocumentSnapshot documentSnapshot:documents){
                                if(documentSnapshot!=null){
                                    if(documentSnapshot.exists()){
                                        booksFound.add(documentSnapshot.toObject(Book.class));
                                        break;
                                    }
                                }
                            }
                            booksFoundCounter.decrement();
                        }
                    });
                    break;
                case "Author":
                    booksFoundCounter.increment();
                    Query query_a = booksRef.whereEqualTo("book_authors."+x,true);
                    query_a.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for(DocumentSnapshot documentSnapshot:documents){
                                if(documentSnapshot!=null){
                                    if(documentSnapshot.exists()){
                                        booksFound.add(documentSnapshot.toObject(Book.class));
                                        break;
                                    }
                                }
                            }
                            booksFoundCounter.decrement();
                        }
                    });
                    break;
                case "ISBN":
                    Query query_i = booksRef.whereEqualTo("book_ISBN",x);
                    booksFoundCounter.increment();
                    query_i.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for(DocumentSnapshot documentSnapshot:documents){
                                if(documentSnapshot!=null){
                                    if(documentSnapshot.exists()){
                                        booksFound.add(documentSnapshot.toObject(Book.class));
                                        break;
                                    }
                                }
                            }
                            booksFoundCounter.decrement();
                        }
                    });
                    break;
                default:
                    return;
            }
        }
        booksFoundCounter.decrement();
        return;
    }
    /**
     * Metodo utilizzato per ricavare le liste dalle googleAPI
     */
    private void getFromGoogleApi(String flag, String text){
        List list = new LinkedList();
        list.add(text);
        switch (flag){
            case "Title":
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int numItems = getNumItemsFromApi(text,flag);
                            int maxCycle = numItems / 40; //Posso prendere solo 40 item alla volta dal json
                            if((numItems%40) != 0) {
                                maxCycle++;
                            }
                            JsonParser jsonParser = new JsonParser();
                            for(int i = 0; i < maxCycle; i++) {
                                String url = "https://www.googleapis.com/books/v1/volumes?maxResults=40&orderBy=relevance&q=intitle:" +
                                        text +
                                        "&fields=items(volumeInfo/title)&startIndex=" + Integer.toString(i*40);
                                JSONObject jsonObject;
                                try{
                                    jsonObject = jsonParser.makeHttpRequest(url,
                                            "GET", new HashMap<String, String>());
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                    continue;
                                }
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
                            list.remove(text);
                            int numItems = getNumItemsFromApi(text,flag);
                            int maxCycle = numItems / 40; //Posso prendere solo 40 item alla volta dal json
                            if((numItems%40) != 0) {
                                maxCycle++;
                            }
                            JsonParser jsonParser = new JsonParser();
                            for(int i = 0; i < maxCycle; i++) {
                                String url = "https://www.googleapis.com/books/v1/volumes?maxResults=40&orderBy=relevance&q=inauthor:" +
                                        text +
                                        "&fields=items(volumeInfo/title)&startIndex=" + Integer.toString(i*40);
                                JSONObject jsonObject;
                                try{
                                    jsonObject = jsonParser.makeHttpRequest(url,
                                            "GET", new HashMap<String, String>());
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                    continue;
                                }
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
//                            for(int i = 0; i < maxCycle; i++) {
//                                String url = "https://www.googleapis.com/books/v1/volumes?maxResults=40&orderBy=relevance&q=inauthor:" +
//                                        text +
//                                        "&fields=items(volumeInfo/industryIdentifiers)&startIndex=" +
//                                        Integer.toString(i*40);
//                                JSONObject jsonObject;
//                                try{
//                                    jsonObject = jsonParser.makeHttpRequest(url,
//                                            "GET", new HashMap<String, String>());
//                                }
//                                catch (Exception e){
//                                    continue;
//                                }
//                                if(jsonObject.has("items")){
//                                    JSONArray tmp =  jsonObject.getJSONArray("items");
//                                    for(int j = 0; j < tmp.length(); j++) {
//                                        JSONObject item = tmp.getJSONObject(j);
//                                        if(item.has("volumeInfo")){
//                                            JSONObject vol = item.getJSONObject("volumeInfo");
//                                            if(vol.has("industryIdentifiers")){
//                                                JSONArray industryIdentifiers = vol.getJSONArray("industryIdentifiers");
//                                                for(int k=0; k< industryIdentifiers.length(); k++){
//                                                    JSONObject id = industryIdentifiers.getJSONObject(k);
//                                                    if(id.has("type") && id.has("identifiers")){
//                                                        if(id.getString("type").equals("ISBN_13")){
//                                                            String isbna = id.getString("identifiers");
//                                                            if(list.contains(isbna)==false){
//                                                                list.add(isbna);
//                                                                break;
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                            searchBooksOnFireStore("Title",list);
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            break;
            case "ISBN":
                searchBooksOnFireStore(flag,list);
                break;
        }
        return;
    }

    private int getNumItemsFromApi(String text, String flag) {
        int totItems = 0;
        String url;
        JsonParser jsonParser = new JsonParser();
        switch (flag){
            case "Title":
                try {
                    url = "https://www.googleapis.com/books/v1/volumes?q=intitle:" + text + "&fields=totalItems";
                    JSONObject jsonObject = jsonParser.makeHttpRequest(
                            url,
                            "GET", new HashMap<String, String>());
                    if (jsonObject.has("totalItems")) {
                        totItems = jsonObject.getInt("totalItems");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }

                break;
            case "Author":
                try {
                    url = "https://www.googleapis.com/books/v1/volumes?q=inauthor:" + text + "&fields=totalItems";
                    JSONObject jsonObject = jsonParser.makeHttpRequest(
                            url,
                            "GET", new HashMap<String, String>());
                    if (jsonObject.has("totalItems")) {
                        totItems = jsonObject.getInt("totalItems");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
        return totItems;

    }

    private static class SearchBookPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();
        private int NUM_ITEMS=2;

        public SearchBookPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SearchBook_search.newInstance(0,"Search a book");
                case 1: // Fragment # 0 - This will show FirstFragment
                    return SearchBook_found.newInstance(1,"Books Found");
                default:
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position,fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position){
            return registeredFragments.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page "+position;
        }
    }

}

