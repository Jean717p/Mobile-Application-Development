package com.mad18.nullpointerexception.takeabook.mainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.auth.AuthUI;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.AddBook;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.LoginActivity;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.SettingsActivity;
import com.mad18.nullpointerexception.takeabook.SplashScreenActivity;
import com.mad18.nullpointerexception.takeabook.myProfile.editProfile;
import com.mad18.nullpointerexception.takeabook.myProfile.showProfile;
import com.mad18.nullpointerexception.takeabook.requestBook.RequestList;
import com.mad18.nullpointerexception.takeabook.searchBook.SearchBookAlgolia;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.deleteUserData;
import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.profileImgName;
import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.sharedUserDataKeys;
import static java.util.stream.Collectors.toList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static User thisUser;
    public static List<Book> myBooks;
    private Map<String,Book> homeBooks;
    private final String TAG = "MainActivity";
    private final int REQUEST_ADDBOOK = 3, REQUEST_SETTINGS = 4;
    private Toolbar toolbar;
    private SharedPreferences sharedPref;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference user_doc;
    private Context context = this;
    private FloatingActionButton fab_my_lib;
    private boolean isMyBooksSorted;
    private ViewPager viewPager;
    private MyPagerAdapter myPagerAdapter;
    private TabLayout tabLayout;
    private ListenerRegistration userListener;
    private Main_MyLibrary_Fragment fragment_myLibrary;
    private Main_HomeBooks_Fragment fragment_home;
    private Main_BorrowedBooks_Fragment fragment_borrowed;
    private Main_LentBooks_Fragment fragment_lent;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        isMyBooksSorted = true;
        myBooks = new LinkedList<>();
        homeBooks = new HashMap<>();
        setContentView(R.layout.activity_main);
        myOnCreateLayout();
        if(savedInstanceState==null){
            //Download userData & books
            new updateUserData().doInBackground();
            Intent intent = new Intent(this,SplashScreenActivity.class);
            startActivity(intent);
        }
        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
//        Resources res;
//        res = getResources();
//        String y;
//        y = sharedPref.getString("language", "");
//        if(y.length()>0){
//            switch (y){
//                case "Italiano":
//                    SettingsActivity.changeLocale(res,"it");
//                    break;
//                case "English":
//                    SettingsActivity.changeLocale(res,"eng");
//                    break;
//            }
//        }
        // Add the parameters requested by the NavDrawer (Image, email, username)
        View hview = navigationView.getHeaderView(0);
        setNavDrawerParameters(hview);
        DocumentReference userDocRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
        userListener = userDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.e(TAG,"Error listen failed userDocRef@MainActivity");
                    return;
                }
                if(documentSnapshot!=null){
                    if(documentSnapshot.exists()){
                        new updateUserData().doInBackground();
                    }
                }
            }
        });
    }

    private void myOnCreateLayout(){
        fab_my_lib = findViewById(R.id.fab_add);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.autour_one);
        CollapsingToolbarLayout collapsingToolbar =(CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Book Circle");
        collapsingToolbar.setExpandedTitleTypeface(typeface);
        collapsingToolbar.setCollapsedTitleTypeface(typeface);
        AppBarLayout appBarLayout = findViewById(R.id.main_app_bar);
        appBarLayout.setExpanded(false);
        // Create an instance of the tab layout from the view.
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // Set the text for each tab.
        tabLayout.addTab(tabLayout.newTab().setText(R.string.top_books));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_library));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.lent_books));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.borrowed_books));
        // Set the tabs to fill the entire layout.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        // Using PagerAdapter to manage page views in fragments.
        // Each page is represented by its own fragment.
        // This is another example of the MyPagerAdapter pattern.
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        myPagerAdapter = new MyPagerAdapter(
                getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        // Setting a listener for clicks.
        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                MyPagerAdapter adapter = (MyPagerAdapter) viewPager.getAdapter();
                com.github.clans.fab.FloatingActionMenu fabSearch= (FloatingActionMenu) findViewById(R.id.top_floating_action_menu);
                FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_add);
                switch (tab.getPosition()){
                    case 0:
                        fabSearch.setVisibility(View.VISIBLE);
                        floatingActionButton.setVisibility(View.GONE);
                        fragment_home = (Main_HomeBooks_Fragment) adapter.getRegisteredFragment(viewPager.getCurrentItem());
                        if(homeBooks.size()>0 && thisUser!=null){
                            fragment_home.updateView(new LinkedList<>(homeBooks.values()),thisUser.getUsr_geoPoint());
                        }
                        break;
                    case 1:
                        fabSearch.setVisibility(View.GONE);
                        floatingActionButton.setVisibility(View.VISIBLE);
                        fragment_myLibrary = (Main_MyLibrary_Fragment) adapter.getRegisteredFragment(viewPager.getCurrentItem());
                        if(fragment_myLibrary !=null){
                            if(isMyBooksSorted==false){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Comparator<Book> byTitle = Comparator.comparing(b->b.getBook_title());
                                    Comparator<Book> byAuthor = Comparator.comparing(b->b.getBook_first_author());
                                    myBooks = myBooks.stream().sorted(byAuthor.thenComparing(byTitle)).collect(toList());
                                }
                                else{
                                    Collections.sort(myBooks, (a, b) -> {
                                        if(a.getBook_first_author().equals(b.getBook_first_author())){
                                            return a.getBook_title().compareTo(b.getBook_title());
                                        }
                                        else{
                                            return a.getBook_first_author().compareTo(b.getBook_first_author());
                                        }
                                    });
                                }
                                fragment_myLibrary.updateView(myBooks);
                            }
                        }
                        break;
                    case 2:
                        floatingActionButton.setVisibility(View.GONE);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.nav_addbook:
                intent = new Intent(context,AddBook.class);
                //startActivityForResult(intent,REQUEST_ADDBOOK);
                startActivity(intent);
                break;
            case R.id.nav_searchBook:
                Intent search = new Intent(this, SearchBookAlgolia.class);
                search.putExtra("action", "Title");
                startActivity(search);
                break;
            case R.id.nav_mychat:
                intent = new Intent(context,com.mad18.nullpointerexception.takeabook.chatActivity.ListOfChatActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_request_sent:
                intent = new Intent(context, RequestList.class);
                intent.putExtra("requestType","sent");
                intent.putExtra("thisUser",new UserWrapper(thisUser));
                startActivity(intent);
                break;
            case R.id.nav_request_received:
                intent = new Intent(context, RequestList.class);
                intent.putExtra("requestType","received");
                intent.putExtra("thisUser",new UserWrapper(thisUser));
                startActivity(intent);
                break;
            case R.id.nav_request_archive:
                intent = new Intent(context, RequestList.class);
                intent.putExtra("requestType","archived");
                intent.putExtra("thisUser",new UserWrapper(thisUser));
                startActivity(intent);
                break;
            case R.id.nav_showprofile:
                intent = new Intent(this, com.mad18.nullpointerexception.takeabook.myProfile.showProfile.class);
                startActivity(intent);
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent,REQUEST_SETTINGS);
                break;
            case R.id.nav_logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> {
                            // user is now signed out
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            deleteUserData(sharedPref, Locale.getDefault(),getResources());
                            finish();
                        });
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setNavDrawerParameters(View nview){ //Usare poi il metodo loadImageFromStorage della classe editProfile
        ImageView drawerImg = nview.findViewById(R.id.mainActivity_drawer_profileImg);
        String imgPath = sharedPref.getString(profileImgName,"");
        File file = null;
        Bitmap b = null;
        // Insert the image into the drawer
        if(imgPath.length() > 0){
            file = new File(imgPath);
            if(file.exists() == true && drawerImg!=null){
                try {
                    b = BitmapFactory.decodeStream(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        if(b==null){
            drawerImg.setImageResource(R.drawable.ic_account_circle_white_48px);
            drawerImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,showProfile.class);
                    startActivity(intent);
                }
            });
        }
        else{
            drawerImg.setImageBitmap(b);
            drawerImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context ,showProfile.class);
                    startActivity(intent);
                }
            });
        }
        // Insert username and email into the drawer
        TextView usr_text = nview.findViewById(R.id.mainActivity_drawer_username);
        TextView mail_text = nview.findViewById(R.id.mainActivity_drawer_email);
        String usr = sharedPref.getString("usr_name", "");
        String mail = sharedPref.getString("usr_mail", "");
        if(usr.length() > 0){
            usr_text.setText(usr);
        }
        else{
            usr_text.setText(R.string.Username);
        }
        if(mail.length() > 0){
            mail_text.setText(mail);
        }
        else {
            mail_text.setText(R.string.Email);
        }
    }

    private class updateUserData extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }


        @Override
        protected String doInBackground(String... strings) {
            FirebaseUser user = mAuth.getCurrentUser();

            user_doc = db.collection("users").document(user.getUid());
            user_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot doc = task.getResult();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    navigationView = (NavigationView) findViewById(R.id.nav_view);
                    User user1 = thisUser;
                    thisUser = doc.toObject(User.class);
                    if(user1 == null || user1.getUsr_geoPoint().equals(thisUser.getUsr_geoPoint())==false
                            || homeBooks.size()==0){
                        new UpdateHomeData().doInBackground();
                    }
                    for(String tmp:sharedUserDataKeys){
                        editor.putString(tmp,doc.getString(tmp));
                        Log.d(TAG,tmp+" - "+doc.getString(tmp));
                    }
                    editor.apply();
                    View hview = navigationView.getHeaderView(0);
                    setNavDrawerParameters(hview);
                    if(sharedPref.getString(profileImgName,"").length()==0
                            && thisUser.getProfileImgStoragePath().length() > 0){
                        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(thisUser.getProfileImgStoragePath());
                        GlideApp.with(context).asBitmap().load(mImageRef).into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                String tmp = editProfile.saveImageToInternalStorage(resource,profileImgName,context);
                                if(tmp.length()>0){
                                    editor.putString(profileImgName,tmp);
                                    editor.apply();
                                    //Update img drawer
                                    setNavDrawerParameters(hview);
                                }
                            }
                        });
                    }
                    for (String x : thisUser.getUsr_books().keySet()) {
                        db.collection("books")
                                .document(x).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot bookDoc) {
                                        Book book = bookDoc.toObject(Book.class);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            if(myBooks.stream().filter(b->b.getBook_id().equals(book.getBook_id())).count()==0){
                                                myBooks.add(bookDoc.toObject(Book.class));
                                                isMyBooksSorted = false;
                                            }
                                        }
                                        else{
                                            boolean bookNotPresent = true;
                                            for(Book b:myBooks){
                                                if(b.getBook_id().equals(book.getBook_id())){
                                                    bookNotPresent = false;
                                                    break;
                                                }
                                            }
                                            if(bookNotPresent){
                                                if(book!=null) {
                                                    myBooks.add(book);
                                                    isMyBooksSorted = false;
                                                }
                                            }
                                        }
                                    }
                                });
                    }
                }
            });
            return "ok";
        }
    }

    private class UpdateHomeData extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        protected String doInBackground(String... strings) {
            CollectionReference booksRef = db.collection("books");
            double lat = 0.0144927536231884;
            double lon = 0.0181818181818182;
            double distance = 186.411; //circa 300Km
            double lowerLat = thisUser.getUsr_geoPoint().getLatitude() - (lat * distance);
            double lowerLon = thisUser.getUsr_geoPoint().getLongitude() - (lon * distance);
            double greaterLat = thisUser.getUsr_geoPoint().getLatitude() + (lat * distance);
            double greaterLon = thisUser.getUsr_geoPoint().getLongitude() + (lon * distance);
            GeoPoint lowerBoundGeo = new GeoPoint(lowerLat,lowerLon);
            GeoPoint upperBoundGeo = new GeoPoint(greaterLat,greaterLon);
            Location user_loc;
            double user_lat=0,user_long=0;
            user_lat = thisUser.getUsr_geoPoint().getLatitude();
            user_long = thisUser.getUsr_geoPoint().getLongitude();
            user_loc = new Location("Provider");
            user_loc.setLatitude(user_lat);
            user_loc.setLongitude(user_long);
            Query a = booksRef.whereGreaterThanOrEqualTo("book_location",lowerBoundGeo)
                    .whereLessThanOrEqualTo("book_location",upperBoundGeo);
            a.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for(DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()){
                        if(doc!=null && doc.exists()){
                            Book book = doc.toObject(Book.class);
                            if(homeBooks.containsKey(book.getBook_ISBN()) &&
                                    homeBooks.get(book.getBook_ISBN()).getBook_id().equals(book.getBook_id()) &&
                                    book.getBook_status()){
                                homeBooks.remove(book.getBook_ISBN());
                            }
                        }
                    }
                    for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                        if (d != null && d.exists()) {
                            Book book = d.toObject(Book.class);
                            if(book.getBook_userid().equals(thisUser.getUsr_id())==false
                                    && book.getBook_status()==false){
                                if(homeBooks.containsKey(book.getBook_ISBN())){
                                    Book a = homeBooks.get(book.getBook_ISBN());
                                    Location book_loc_a = new Location("Provider");
                                    Location book_loc_b = new Location("Provider");
                                    book_loc_a.setLatitude(a.getBook_location().getLatitude());
                                    book_loc_a.setLongitude(a.getBook_location().getLongitude());
                                    book_loc_b.setLatitude(book.getBook_location().getLatitude());
                                    book_loc_b.setLongitude(book.getBook_location().getLongitude());
                                    if(book_loc_a.distanceTo(user_loc)>book_loc_b.distanceTo(user_loc)){
                                        homeBooks.put(book.getBook_ISBN(),book);
                                    }
                                }
                                else{
                                    homeBooks.put(book.getBook_ISBN(),book);
                                }
                            }
                        }
                    }
                    if(viewPager!=null && viewPager.getAdapter()!=null){
                        MyPagerAdapter adapter = (MyPagerAdapter) viewPager.getAdapter();
                        Fragment fragment = adapter.getRegisteredFragment(viewPager.getCurrentItem());
                        if(fragment instanceof Main_HomeBooks_Fragment) {
                            fragment_home = (Main_HomeBooks_Fragment) fragment;
                        }
                        if(fragment_home!=null){
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                homeBooks = homeBooks.values().stream().sorted((a,b)->{
//                                    Location book_loc_a = new Location("Provider");
//                                    Location book_loc_b = new Location("Provider");
//                                    book_loc_b.setLatitude(b.getBook_location().getLatitude());
//                                    book_loc_b.setLongitude(b.getBook_location().getLongitude());
//                                    book_loc_a.setLatitude(a.getBook_location().getLatitude());
//                                    book_loc_a.setLongitude(a.getBook_location().getLongitude());
//                                    return Float.compare(book_loc_a.distanceTo(user_loc),book_loc_b.distanceTo(user_loc));
//                                }).collect(Collectors.toList());
//                            }
//                            else{

//                            Collections.sort(homeBooks.entrySet(), (a, b) -> {
//                                Location book_loc_b = new Location("Provider");
//                                Location book_loc_a = new Location("Provider");
//                                book_loc_b.setLatitude(b.getBook_location().getLatitude());
//                                book_loc_b.setLongitude(b.getBook_location().getLongitude());
//                                book_loc_a.setLatitude(a.getBook_location().getLatitude());
//                                book_loc_a.setLongitude(a.getBook_location().getLongitude());
//                                return Float.compare(book_loc_a.distanceTo(user_loc),book_loc_b.distanceTo(user_loc));
//                            });
//                            List<Book> x = new LinkedList<>(homeBooks.values());
//                            Collections.sort(x, (a, b) -> {
//                                Location book_loc_a = new Location("Provider");
//                                Location book_loc_b = new Location("Provider");
//                                book_loc_a.setLatitude(a.getBook_location().getLatitude());
//                                book_loc_a.setLongitude(a.getBook_location().getLongitude());
//                                book_loc_b.setLatitude(b.getBook_location().getLatitude());
//                                book_loc_b.setLongitude(b.getBook_location().getLongitude());
//                                return Float.compare(book_loc_a.distanceTo(user_loc),book_loc_b.distanceTo(user_loc));
//                            });
//                            Map<String,Book> xSorted = new HashMap<>();
//                            for(int i=0; i<x.size();i++){
//                                Book e = x.get(i);
//                                xSorted.put((String)e.getBook_ISBN(),(Book)e);
//                            }
//                            homeBooks = xSorted;
                            fragment_home.updateView(new LinkedList<>(homeBooks.values()),thisUser.getUsr_geoPoint());
                        }
                    }
                }
            });
            return "ok";
        }
    }

    @Override
    protected void onDestroy() {
        userListener.remove();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
//            case REQUEST_SETTINGS:
//                if(resultCode==RESULT_OK && data !=null){
//                    Bundle extras = data.getExtras();
//                    if(extras!=null){
//                        Boolean langChanged = extras.getBoolean("langChanged");
//                        if(langChanged){
//                            this.recreate();
//                        }
//                    }
//                }
        }
    }

    public static class MyPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();
        private int NUM_ITEMS;

        public MyPagerAdapter(FragmentManager fragmentManager,int pageCount) {
            super(fragmentManager);
            NUM_ITEMS = pageCount > 0 ? pageCount : 1;
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
                case 0: // Fragment # 0 - This will show FirstFragment
                    return Main_HomeBooks_Fragment.newInstance(0, "Page #"+position);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return Main_MyLibrary_Fragment.newInstance(1, "Page #"+position);
                case 2: // Fragment # 1 - This will show SecondFragment
                    return Main_LentBooks_Fragment.newInstance(2, "Page #"+position);
                case 3:
                    return Main_BorrowedBooks_Fragment.newInstance(3,"Page #"+position);
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