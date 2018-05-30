package com.mad18.nullpointerexception.takeabook.myProfile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.SettingsActivity;
import com.mad18.nullpointerexception.takeabook.info.InfoUser;
import com.mad18.nullpointerexception.takeabook.info.InfoUserShowBooks;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import org.jetbrains.annotations.NotNull;

import static com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.thisUser;


public class showProfile extends AppCompatActivity {
    //gran parte del codice simile alla editprofile e' commentato nella editProfile
    private final String TAG = "showProfile";
    private SharedPreferences sharedPref;
    private final int textViewIds[] = new int[]{R.id.show_profile_Username, R.id.show_profile_City,
            R.id.show_profile_mail,R.id.show_profile_about};
    private Menu menu;
    public static final String sharedUserDataKeys[] = new String[]{"usr_name","usr_city","usr_mail","usr_about"};
    public static final String profileImgName = "profile.jpg";
    private FirebaseAuth mAuth;
    CardView showBooksProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        setContentView(R.layout.show_profile);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        showBooksProfile = findViewById(R.id.show_profile_library_cv);
        Toolbar toolbar = findViewById(R.id.show_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_show_profile);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

        ArrayList<String> userBooks = new ArrayList<>();
        db.collection("users").document(thisUser.getUsr_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot userdoc = task.getResult();
                User user = userdoc.toObject(User.class);
                for (String x : user.getUsr_books().keySet()) {

                    userBooks.add(x);
                }

                if (showBooksProfile != null) {
                    showBooksProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent showBooksIntent = new Intent(showProfile.this, InfoUserShowBooks.class);
                            Bundle bundle = new Bundle();
                            UserWrapper userWrapper = new UserWrapper(thisUser);
                            bundle.putParcelable("user", userWrapper);
                            bundle.putStringArrayList("UserBooks", userBooks);
                            showBooksIntent.putExtras(bundle);
                            startActivity(showBooksIntent);
                        }
                    });
                }


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillUserData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inbox, menu); //.xml file name
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(this,editProfile.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillUserData(){
        TextView text;
        String y;
        int i=0;
        ImageView iw;

        for(String x:sharedUserDataKeys){
            text = findViewById(textViewIds[i]);
            y=sharedPref.getString(x,"");
            if(y.length()>0){
                text.setText(y);
                if(text == findViewById(R.id.show_profile_about)){

                    CardView cardView = findViewById(R.id.show_profile_about_cv);
                    cardView.setVisibility(View.VISIBLE);
                }
            }else{
                CardView cardView = findViewById(R.id.show_profile_about_cv);
                cardView.setVisibility(View.GONE);
            }

            i++;
        }
        y=sharedPref.getString(profileImgName,"");
        if(y.length()>0){
            editProfile.loadImageFromStorage(y,R.id.show_profile_personalPhoto,this);
        }
        else{
            iw = findViewById(R.id.show_profile_personalPhoto);
            iw.setImageResource(R.drawable.ic_account_circle_white_48px);
        }
    }

    public static void deleteUserData(@NotNull SharedPreferences sharedPrefToDel,
                                      @NonNull Locale locale,
                                      @NonNull Resources resources){
        String profileImgPath = sharedPrefToDel.getString(profileImgName,"");
        sharedPrefToDel.edit().clear().apply();
        if(profileImgPath.length()>0){
            File file = new File(profileImgPath);
            if(file.exists()){
                file.delete();
            }
        }
        switch (locale.getLanguage()){
            case "it":
                SettingsActivity.changeLocale(resources,"it");
                break;
            default:
                SettingsActivity.changeLocale(resources,"en");
                break;
        }
        MainActivity.homeBooks.clear();
        MainActivity.myBooks.clear();
    }
}
