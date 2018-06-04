package com.mad18.nullpointerexception.takeabook.searchBook;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.displaySearchOnMap.DisplaySearchOnMap;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.ScanBarcode;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SearchBookAlgolia extends AppCompatActivity {

    private String searchBase;
    private Map<String, String> booklist = new HashMap<>();
    private ArrayList<BookWrapper> booksFound = new ArrayList<>();
    private ArrayList<SearchBookAlgoliaItem> booksToShow = new ArrayList<>();
    private Context context;
    private View mClss;
    private Index algolia_index;
    private ZXingScannerView barcodeScanner;
    private static final int ZXING_CAMERA_PERMISSION = 4, REQUEST_SCANNER=3, FINE_LOCATION_PERMISSION=7, FINE_LOCATION_PERMISSION_BARCODE = 6;
    private RecyclerView mRecyclerView;
    private SearchBookAlgoliaAdapter myAdapter;
    private ImageView iw_scanbarcode;


    @Override
    protected void onResume() {
        super.onResume();
        booksFound.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_book_algolia);
        context = this;
        searchBase = getIntent().getStringExtra("action");
        iw_scanbarcode = findViewById(R.id.search_book_algolia_ImageView);
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.search_toolbar);
        mRecyclerView = findViewById(R.id.search_book_algolia_recycler_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Search " + searchBase);
        if(searchBase.equals("ISBN")){
            iw_scanbarcode.setVisibility(View.VISIBLE);
        }
        iw_scanbarcode.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(SearchBookAlgolia.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                mClss = view;
                ActivityCompat.requestPermissions(SearchBookAlgolia.this,
                        new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
            } else {
                Intent intent = new Intent(SearchBookAlgolia.this, ScanBarcode.class);
                intent.putExtra("justScan","ok");
                startActivityForResult(intent,REQUEST_SCANNER);
            }
        });

        myAdapter = new SearchBookAlgoliaAdapter(new ArrayList<SearchBookAlgoliaItem>(), this, new SearchBookAlgoliaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SearchBookAlgoliaItem item) {
                //TODO: Far partire l'activity della mappa con tutto ciò che serve nel BUNDLE
                searchBooksOnFireStore(item.getISBN());
                if(booksFound.size() != 0){
                    Intent intent = new Intent(context, DisplaySearchOnMap.class);
                    Bundle b = new Bundle();
                    b.putParcelableArrayList("bookToShow", booksFound);
                    intent.putExtras(b);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(context, R.string.search_book_algolia_no_found, Toast.LENGTH_SHORT).show();
                }

            }
        });
        mRecyclerView.setAdapter(myAdapter);
        algolia_index = algoliaInit(searchBase);
        initViews();
    }

    private Index algoliaInit(String flag){
        String algoliaID = "P15KSBYCLA";
        String algoliaKey = "d2bde37905872a9f4658d85ad5d7776d";
        Client client = new Client(algoliaID, algoliaKey);
        Index index = null;
        switch(flag){
            case "Title":
                index = client.getIndex("book_title");
                break;
            case "Author":
                index = client.getIndex("book_author");
                break;

            case "ISBN":
                index = client.getIndex("book_ISBN");
            default:
                return index;
        }
        return index;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_book_algolia_menu, menu);
        MenuItem search = menu.findItem(R.id.search_book_algolia_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchOnAlgolia(searchView);
        return true;
    }

    private void initViews(){
        mRecyclerView = (RecyclerView) findViewById(R.id.search_book_algolia_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void searchOnAlgolia(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length() > 0){
                    String filter = "NOT UserID:" + FirebaseAuth.getInstance().getUid();
                    booksToShow.clear();
                    Query query = new Query(newText)
                            .setAttributesToRetrieve("Title", "ISBN", "Author", "ThumbnailURL")
                            .setFilters(filter)
                            .setHitsPerPage(50);

                    algolia_index.searchAsync(query, (jsonObject, e) -> {
                        try {
                            if (jsonObject != null) {
                                if (jsonObject.has("hits")) {
                                    JSONArray hits = jsonObject.getJSONArray("hits");
                                    for (int i = 0; i < hits.length(); i++) {
                                        JSONObject retrieved = hits.getJSONObject(i);
                                        String title, isbn, author, thumbnailURL;
                                        if (retrieved.has("Title")) {
                                            title = retrieved.get("Title").toString();
                                        } else {
                                            title = "";
                                        }
                                        if (retrieved.has("ISBN")) {
                                            isbn = retrieved.get("ISBN").toString();
                                        } else {
                                            isbn = "";
                                        }
                                        if (retrieved.has("Author")) {
                                            author = retrieved.get("Author").toString();
                                        } else {
                                            author = "";
                                        }
                                        if (retrieved.has("ThumbnailURL")) {
                                            thumbnailURL = retrieved.get("ThumbnailURL").toString();
                                        } else {
                                            thumbnailURL = "";
                                        }
                                        booksToShow.add(new SearchBookAlgoliaItem(title, author, thumbnailURL, isbn));
                                    }
                                    //Update recycler view
                                    updateView(booksToShow);
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    });
                }
                else{
                    if(searchBase.equals("ISBN")){
                        iw_scanbarcode.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                booksToShow.clear();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateView(ArrayList<SearchBookAlgoliaItem> books){
        if(myAdapter==null){
            return;
        }
        myAdapter.setData(books);
        myAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView iw;
        if(resultCode==RESULT_OK){
            if (requestCode==REQUEST_SCANNER){
                if(data != null){
                    Bundle bundle = data.getExtras();
                    String ISBN = bundle.getString("ISBN");
                    searchBooksOnFireStore(ISBN);
                    if(booksFound.size() != 0){
                        Intent intent = new Intent(context, DisplaySearchOnMap.class);
                        Bundle b = new Bundle();
                        b.putParcelableArrayList("bookToShow", booksFound);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(this, R.string.search_book_algolia_no_found, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //dopo che l'utente ci ha fornito la risposta alla richesta di permessi
        switch (requestCode){
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(SearchBookAlgolia.this, ScanBarcode.class);
                        intent.putExtra("justScan","justScan");
                        startActivityForResult(intent, REQUEST_SCANNER);
                    }
                } else {
                    Toast.makeText(this, R.string.add_book_permission_camera, Toast.LENGTH_SHORT).show();
                }
                break;
            case FINE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, R.string.search_book_algolia_request_permission, Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
                    }
                }
                break;
            case FINE_LOCATION_PERMISSION_BARCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(context, DisplaySearchOnMap.class);
                    Bundle b = new Bundle();
                    b.putParcelableArrayList("bookToShow", booksFound);
                    intent.putExtras(b);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.search_book_algolia_request_permission, Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
                    }
                }
                break;
        }

        return;
    }

    private void searchBooksOnFireStore(String ISBN){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference booksRef = db.collection("books");
        com.google.firebase.firestore.Query query_ISBN = booksRef.whereEqualTo("book_ISBN", ISBN);
        Task<QuerySnapshot> bookToShow = query_ISBN.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                for (DocumentSnapshot documentSnapshot : documents) {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            Book tmp = documentSnapshot.toObject(Book.class);
                            booksFound.add(new BookWrapper(tmp));
                        }
                    }
                }
                if(booksFound.size() != 0){
                    Intent intent = new Intent(context, DisplaySearchOnMap.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("bookToShow", booksFound);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(context, R.string.search_book_algolia_no_found, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }



}
