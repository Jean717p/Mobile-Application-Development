package com.mad18.nullpointerexception.takeabook.searchBook;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.displaySearchOnMap.DisplaySearchOnMap;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.myProfile.editProfile;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
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
    private Context context;
    private View mClss;
    private ZXingScannerView barcodeScanner;
    private static final int ZXING_CAMERA_PERMISSION = 4, REQUEST_SCANNER=3;

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
        ImageView iw = findViewById(R.id.search_book_algolia_ImageView);
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        setTitle("Search " + searchBase);
        if(searchBase.equals("ISBN")){
            iw.setVisibility(View.VISIBLE);
        }
        iw.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(SearchBookAlgolia.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                mClss = view;
                ActivityCompat.requestPermissions(SearchBookAlgolia.this,
                        new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
            } else {
                Intent intent = new Intent(SearchBookAlgolia.this, SearchBook_BarcodeScanner.class);
                startActivityForResult(intent,REQUEST_SCANNER);
            }
        });

        setMyLayout();
    }


    private void setMyLayout(){


        Index algolia_index;
        MaterialSearchBar searchBar = findViewById(R.id.search_book_algolia_searchBar);
        searchBar.setHint(searchBase);
        if((algolia_index = algoliaInit(searchBase)) != null){
            searchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    if (charSequence.length() == 0) {
                        searchBar.clearSuggestions();
                        searchBar.hideSuggestionsList();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() == 0) {
                        searchBar.clearSuggestions();
                        searchBar.hideSuggestionsList();
                    }
                    String filter = "NOT UserID:" + FirebaseAuth.getInstance().getUid();
                    if (!searchBase.equals("ISBN")) {
                        Query query = new Query(editable.toString())
                                .setAttributesToRetrieve(searchBase, "ISBN")
                                .setFilters(filter)
                                .setHitsPerPage(50);

                        algolia_index.searchAsync(query, (jsonObject, e) -> {
                            try {
                                if(jsonObject != null){
                                    if (jsonObject.has("hits")) {
                                        JSONArray hits = jsonObject.getJSONArray("hits");
                                        for (int i = 0; i < hits.length(); i++) {
                                            JSONObject retrieved = hits.getJSONObject(i);
                                            if (retrieved.has(searchBase)) {
                                                String item_name = retrieved.get(searchBase).toString();
                                                String isbn = retrieved.get("ISBN").toString();
                                                booklist.put(item_name, isbn);
                                            }
                                        }
                                        List<String> l = new ArrayList<String>(booklist.keySet());
                                        searchBar.setLastSuggestions(l);
                                        searchBar.showSuggestionsList();
                                    }
                                }

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        });
                    } else {
                        Query query = new Query(editable.toString())
                                .setAttributesToRetrieve(searchBase)
                                //.setFilters(filter)
                                .setHitsPerPage(50);

                        algolia_index.searchAsync(query, (jsonObject, e) -> {
                            try {
                                if (jsonObject.has("hits")) {
                                    JSONArray hits = jsonObject.getJSONArray("hits");
                                    for (int i = 0; i < hits.length(); i++) {
                                        JSONObject retrieved = hits.getJSONObject(i);
                                        if (retrieved.has(searchBase)) {
                                            String item_name = retrieved.get(searchBase).toString();
                                            booklist.put(item_name, item_name);
                                        }
                                    }
                                    List<String> l = new ArrayList<String>(booklist.keySet());
                                    searchBar.setLastSuggestions(l);
                                    searchBar.showSuggestionsList();
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        });
                    }

                }
            });
        }


        searchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                String key = searchBar.getLastSuggestions().get(position).toString();
                String ISBN = booklist.get(key);
                searchBooksOnFireStore(ISBN);
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

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
                Intent intent = new Intent(context, DisplaySearchOnMap.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("bookToShow", booksFound);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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
                    Intent intent = new Intent(context, DisplaySearchOnMap.class);
                    Bundle b = new Bundle();
                    b.putParcelableArrayList("bookToShow", booksFound);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //dopo che l'utente ci ha fornito la risposta alla richesta di permessi
        if(requestCode == ZXING_CAMERA_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(mClss != null) {
                    Intent intent = new Intent(SearchBookAlgolia.this, SearchBook_BarcodeScanner.class);
                    startActivityForResult(intent, REQUEST_SCANNER);
                }
            } else {
                Toast.makeText(this, R.string.add_book_permission_camera, Toast.LENGTH_SHORT).show();
            }
            return;
        }

    }
}
