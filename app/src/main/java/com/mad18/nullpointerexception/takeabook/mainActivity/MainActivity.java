package com.mad18.nullpointerexception.takeabook.mainActivity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad18.nullpointerexception.takeabook.Book;
import com.mad18.nullpointerexception.takeabook.LoginActivity;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.SplashScreenActivity;
import com.mad18.nullpointerexception.takeabook.User;
import com.mad18.nullpointerexception.takeabook.addBook.BookWrapper;
import com.mad18.nullpointerexception.takeabook.myProfile.editProfile;
import  com.github.clans.fab.FloatingActionMenu;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.deleteUserData;
import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.profileImgName;
import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.sharedUserDataKeys;
import static java.util.stream.Collectors.toList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private final String TAG = "MainActivity";
    private final int REQUEST_ADDBOOK = 3;
    private Toolbar toolbar;
    private SharedPreferences sharedPref;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference user_doc;
    private Context context = this;
    public static User thisUser;
    private FloatingActionButton fab_my_lib;
    public static List<Book> myBooks;
    private boolean isMyBooksSorted;
    private ViewPager viewPager;
    private MyPagerAdapter myPagerAdapter;
    private TabLayout tabLayout;
    Main_MyLibrary_Fragment f;

    NavigationView navigationView;
    //Called when a fragment is attached as a child of this fragment.

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }
    //has not yet had a previous call to onCreate.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        isMyBooksSorted = true;
        myBooks = new LinkedList<>();
        setContentView(R.layout.activity_main);
        myOnCreateLayout();
        if(savedInstanceState==null){
            //Download userData & books
            new updateUserData().doInBackground();
            Intent intent = new Intent(this,SplashScreenActivity.class);
            startActivity(intent);
        }
        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        // Add the parameters requested by the NavDrawer (Image, email, username)
        View hview = navigationView.getHeaderView(0);
        setNavDrawerParameters(hview);
        DocumentReference userDocRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
        userDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Book Circle");
        // Create an instance of the tab layout from the view.
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // Set the text for each tab.
        tabLayout.addTab(tabLayout.newTab().setText(R.string.top_books));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_library));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.lent_books));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.borrowed_books));

        // Set the tabs to fill the entire layout.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //
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
                        break;
                    case 1:
                        fabSearch.setVisibility(View.GONE);
                        floatingActionButton.setVisibility(View.VISIBLE);
                        f = (Main_MyLibrary_Fragment) adapter.getRegisteredFragment(viewPager.getCurrentItem());
                        if(f!=null){
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
                                f.updateView(myBooks);
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
                intent = new Intent(context,com.mad18.nullpointerexception.takeabook.addBook.AddBook.class);
                //startActivityForResult(intent,REQUEST_ADDBOOK);
                startActivity(intent);
                break;
            case R.id.nav_showprofile:
                intent = new Intent(this, com.mad18.nullpointerexception.takeabook.myProfile.showProfile.class);
                startActivity(intent);
                break;
            case R.id.nav_logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> {
                            // user is now signed out
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            deleteUserData(sharedPref);
                            myBooks.clear();
                            finish();
                        });
                break;
            case R.id.nav_mychat:
                intent = new Intent(context,com.mad18.nullpointerexception.takeabook.chatActivity.listOfChatActivity.class);
                //startActivityForResult(intent,REQUEST_ADDBOOK);
                startActivity(intent);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_main, menu); //.xml file name
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_refresh:

                f.updateView(myBooks);
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.main_library_coordinator_layout),getText(R.string.info_book_snackbar), Snackbar.LENGTH_LONG);
                snackbar.show();
                break;

        }

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
        }
        else{
            drawerImg.setImageBitmap(b);
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
                    thisUser = doc.toObject(User.class);
                    for(String tmp:sharedUserDataKeys){
                        editor.putString(tmp,doc.getString(tmp));
                    }
                    editor.apply();
                    View hview = navigationView.getHeaderView(0);
                    setNavDrawerParameters(hview);
                    if(sharedPref.getString(profileImgName,"").length()==0
                            && thisUser.getProfileImgStoragePath().length() > 0){
                        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(thisUser.getProfileImgStoragePath().substring(1,thisUser.getProfileImgStoragePath().length()-1));
                        Glide.with(context).asBitmap().load(mImageRef).into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL) {
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
                        db.collection("books").document(x).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot bookDoc = task.getResult();
                                Book book = bookDoc.toObject(Book.class);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    if(myBooks.stream().filter(b->b.getBook_ISBN().equals(book.getBook_ISBN())).count()==0){
                                        myBooks.add(bookDoc.toObject(Book.class));
                                        isMyBooksSorted = false;
                                    }
                                }
                                else{
                                    boolean bookNotPresent = true;
                                    for(Book b:myBooks){
                                        if(b.getBook_ISBN().equals(book.getBook_ISBN())){
                                            bookNotPresent = false;
                                            break;
                                        }
                                    }
                                    if(bookNotPresent){
                                        myBooks.add(book);
                                        isMyBooksSorted = false;
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

    public List<Book> getMyBooks() {
        return myBooks;
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode){
//            case REQUEST_ADDBOOK:
//                if(resultCode == RESULT_OK){
//                    isMyBooksSorted = false;
//                    TabLayout.Tab t = tabLayout.getTabAt(1);
//                    if(data!=null){
//                        Bundle extras = data.getExtras();
//                        if(extras!=null){
//                            BookWrapper bookWrapper = extras.getParcelable("newbook");
//                            if(bookWrapper!=null){
//                                myBooks.add(new Book(bookWrapper));
//                            }
//                        }
//                    }
//                    if(t!=null){
//                        if(t.isSelected()){
//                            MyPagerAdapter adapter = (MyPagerAdapter) viewPager.getAdapter();
//                            Main_MyLibrary_Fragment fragment = (Main_MyLibrary_Fragment) adapter.getItem(viewPager.getCurrentItem());
//                            if(fragment!=null){
//                                if(isMyBooksSorted==false){
////                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
////                                        Comparator<Book> byTitle = Comparator.comparing(b->b.getBook_title());
////                                        Comparator<Book> byAuthor = Comparator.comparing(b->b.getBook_first_author());
////                                        myBooks = myBooks.stream().sorted(byAuthor.thenComparing(byTitle)).collect(toList());
////                                    }
////                                    else{
////                                        Collections.sort(myBooks, (a, b) -> {
////                                            if(a.getBook_first_author().equals(b.getBook_first_author())){
////                                                return a.getBook_title().compareTo(b.getBook_title());
////                                            }
////                                            else{
////                                                return a.getBook_first_author().compareTo(b.getBook_first_author());
////                                            }
////                                        });
////                                    }
//                                    fragment.updateView(myBooks);
//                                }
//                            }
//                        }
//                        else{
//                            t.select();
//                        }
//                    }
//                }
//                break;
//        }
//    }

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
                    return Main_TopBooks_Fragment.newInstance(0, "Page #"+position);
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