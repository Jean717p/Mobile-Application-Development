package com.mad18.nullpointerexception.takeabook.mainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad18.nullpointerexception.takeabook.LoginActivity;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.User;
import com.mad18.nullpointerexception.takeabook.myProfile.editProfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.deleteUserData;
import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.profileImgName;
import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.sharedUserDataKeys;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private final String TAG = "MainActivity";
    private Toolbar toolbar;
    private SharedPreferences sharedPref;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference user_doc;
    private Context context = this;
    NavigationView navigationView;


    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        context = this;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        // Create an instance of the tab layout from the view.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // Set the text for each tab.
        tabLayout.addTab(tabLayout.newTab().setText("Top"));
        tabLayout.addTab(tabLayout.newTab().setText("My Library"));
        tabLayout.addTab(tabLayout.newTab().setText("Lent"));
        tabLayout.addTab(tabLayout.newTab().setText("Borrowed"));



        // Set the tabs to fill the entire layout.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //
        // Using PagerAdapter to manage page views in fragments.
        // Each page is represented by its own fragment.
        // This is another example of the adapter pattern.
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        // Setting a listener for clicks.
        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

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

        // Add the parameters requested by the NavDrawer (Image, email, username)
        View hview = navigationView.getHeaderView(0);
        setNavDrawerParameters(hview);

        FirebaseUser user = mAuth.getCurrentUser();
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(user.getUid());
        user_doc = db.collection("users").document(user.getUid());
        user_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                SharedPreferences.Editor editor = sharedPref.edit();
                for(String tmp:sharedUserDataKeys){
                    editor.putString(tmp,doc.getString(tmp));
                }
                editor.apply();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Add the parameters requested by the NavDrawer (Image, email, username)
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hview = navigationView.getHeaderView(0);
        setNavDrawerParameters(hview);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_addbook:
                break;
            case R.id.nav_showprofile:
                Intent intent = new Intent(this, com.mad18.nullpointerexception.takeabook.myProfile.showProfile.class);
                startActivity(intent);
                break;
            case R.id.nav_mylibrary:
                break;
            case R.id.nav_logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> {
                            // user is now signed out
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            deleteUserData(sharedPref);
                            finish();
                        });
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


    private void setNavDrawerParameters(View nview){ //Usare poi il metodo loadImageFromStorage della classe editProfile
        ImageView drawerImg = nview.findViewById(R.id.mainActivity_drawer_profileImg);
        String img = sharedPref.getString(profileImgName,"");
        File file = null;
        Bitmap b = null;
        // Insert the image into the drawer
        if(img.length() > 0){
            file = new File(img);
            if(file.exists() == false||drawerImg==null){
                return;
            }
            try {
                b = BitmapFactory.decodeStream(new FileInputStream(file));
                drawerImg.setImageBitmap(b);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Insert username and email into the drawer
        TextView usr_text = nview.findViewById(R.id.mainActivity_drawer_username);
        TextView mail_text = nview.findViewById(R.id.mainActivity_drawer_email);
        String usr = sharedPref.getString("usr_name", "");
        String mail = sharedPref.getString("usr_mail", "");
        if(usr.length() > 0){
            usr_text.setText(usr);
        }
        if(mail.length() > 0){
            mail_text.setText(mail);
        }

    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;
        public PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = 4;

        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new Main_TopBooks_Fragment();
                case 1:
                    return new Main_MyLibrary_Fragment();
                case 2:
                    return  new Main_LentBooks_Fragment();
                case 3:
                    return new Main_BorrowedBooks_Fragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
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
                    //User u = doc.toObject(User.class);
                    for(String tmp:sharedUserDataKeys){
                        editor.putString(tmp,doc.getString(tmp));
                    }
                    editor.apply();
                    if(sharedPref.getString(profileImgName,"").length()==0){
                        StorageReference mImageRef = FirebaseStorage.getInstance().getReference("users/images/"+user.getUid());
                        Glide.with(context).asBitmap().load(mImageRef).into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                String tmp = editProfile.saveImageToInternalStorage(resource,profileImgName,context);
                                if(tmp.length()>0){
                                    editor.putString(profileImgName,tmp);
                                    editor.apply();
                                    //Update img drawer
                                }
                            }
                        });
                    }
                }
            });
            return "ok";
        }
    }

}