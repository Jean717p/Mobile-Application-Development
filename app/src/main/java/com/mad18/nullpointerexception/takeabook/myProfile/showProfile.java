package com.mad18.nullpointerexception.takeabook.myProfile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.info.InfoUserShowBooks;
import com.mad18.nullpointerexception.takeabook.info.ShowReviews;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.util.Review;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;

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
    private Context context;
    CardView showBooksProfile;
    CardView showReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        setContentView(R.layout.show_profile);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        showBooksProfile = findViewById(R.id.show_profile_library_cv);
        showReviews = findViewById(R.id.show_profile_reviews_cv);
        Toolbar toolbar = findViewById(R.id.show_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_show_profile);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        showReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShowReviews.class);
                intent.putExtra("type","user");
                intent.putExtra("thisUser",new UserWrapper(MainActivity.thisUser));
                startActivity(intent);
            }
        });
        if (showBooksProfile != null) {
            showBooksProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent showBooksIntent = new Intent(showProfile.this, InfoUserShowBooks.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("owner", new UserWrapper(thisUser));
                    showBooksIntent.putExtras(bundle);
                    startActivity(showBooksIntent);
                }
            });
        }
        db.collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("reviews")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                Float mean;
                RatingBar ratingBar = findViewById(R.id.show_profile_rating_bar);
                if(querySnapshot.getDocuments().size()==0){
                    findViewById(R.id.show_profile_reviews_cv).setVisibility(View.GONE);
                    return;
                }
                ratingBar.setVisibility(View.VISIBLE);
                mean = 0f;
                for(DocumentSnapshot doc:querySnapshot.getDocuments()){
                    Review review = doc.toObject(Review.class);
                    mean += review.getRating();
                }
                mean/=querySnapshot.size();
                ratingBar.setRating(mean);
                TextView textView = findViewById(R.id.show_profile_rating_count);
                String s = "("+querySnapshot.getDocuments().size()+")";
                textView.setText(s);
                textView.setVisibility(View.VISIBLE);
                textView = findViewById(R.id.show_profile_rating_average);
                textView.setText(String.format(Locale.US,"%.1f",mean));
                textView.setVisibility(View.VISIBLE);
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
//        switch (locale.getLanguage()){
//            case "it":
//                SettingsActivity.changeLocale(resources,"it");
//                break;
//            default:
//                SettingsActivity.changeLocale(resources,"en");
//                break;
//        }
        MainActivity.myBooks.clear();
    }
}
