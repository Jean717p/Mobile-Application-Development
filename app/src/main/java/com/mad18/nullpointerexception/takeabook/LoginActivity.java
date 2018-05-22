package com.mad18.nullpointerexception.takeabook;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.mad18.nullpointerexception.takeabook.myProfile.editProfile;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity  {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 123;
    private static final int REQUEST_PERMISSION_INTERNET=3;
    private static final int PLACE_PICKER_REQUEST = 1;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView progressBarTextView;
    private boolean firstAttempt;
    private boolean newUser;
    public static final String usr_lat = "usr_lat";
    public static final String usr_long = "usr_lat";
    private Menu menu;
    GeoPoint gp;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = (ProgressBar) findViewById(R.id.login_progress_bar);
        progressBarTextView = findViewById(R.id.login_progress_bar_text);
//        mAuth = FirebaseAuth.getInstance();
//        if(mAuth.getCurrentUser()!=null){
//            Intent intent = new Intent(this, com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
        toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        String x = sharedPref.getString(SettingsActivity.language,"");
        if(x.length()>0){
            LanguageHelper.changeLocale(getResources(),x);
        }
        toolbar.setTitle(R.string.insert_location);
        toolbar.setVisibility(View.INVISIBLE);
        firstAttempt = true;
        newUser = false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_book_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        String s;

        switch (item.getItemId()){
            case R.id.add_book_done:
                TextView tw = findViewById(R.id.login_Address);
                if(tw.getText().toString().length() > 0) {

                    /* Inserimento dati su Firebase */

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference users = db.collection("users");
                    FirebaseUser user = mAuth.getCurrentUser();
                    User u = new User(user.getEmail(), user.getDisplayName(), "", "", new HashMap<String, Boolean>(),gp, user.getUid());
                    users.document(user.getUid()).set(u);
                    //Nel caso serva inserire tasto ok su toolbar
                    Intent intent = new Intent(this, com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                    finish();
                    return true;
                }else{

                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.relativeLayout),getText(R.string.Error_position), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return false;
                }


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
                if (!newUser) {
                    Intent intent = new Intent(this, com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.class);
                    startActivity(intent);
                    finish();
                }
        }
        else {
            if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{
                                Manifest.permission.INTERNET}
                        , REQUEST_PERMISSION_INTERNET);
            } else if (firstAttempt) {
                sign_in();
                firstAttempt = !firstAttempt;
            }
        }
    }

    private void sign_in(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build()
                                ,new AuthUI.IdpConfig.GoogleBuilder().build()
                                ,new AuthUI.IdpConfig.FacebookBuilder().build()
                        ))
                        //.setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */)
                        .build(),
                RC_SIGN_IN);
        return;
        }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    String placeName = String.format("Place: %s", place.getName());
                    gp = new GeoPoint(place.getLatLng().latitude,place.getLatLng().longitude);
                    TextView addr_text = findViewById(R.id.login_Address);
                    addr_text.setVisibility(View.VISIBLE);
                    addr_text.setText(place.getName().toString());



                } else {
                    //Place picking fallito
                }
                break;

            case RC_SIGN_IN:
                // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
                if (requestCode == RC_SIGN_IN) {
                    IdpResponse response = IdpResponse.fromResultIntent(data);

                    // Successfully signed in
                    if (resultCode == RESULT_OK) {
                        IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
                        FirebaseUserMetadata metadata = mAuth.getCurrentUser().getMetadata();
                        newUser = metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp();
                        if (newUser) {
                            getUserPosition(this);
                        }
                        else{
                            Intent intent = new Intent(this, com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Sign in failed
                        if (response == null) {
                            // User pressed back button
                            Log.d("Debug", "cancelled");
                            finish();
                        } else {
                            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                                Log.e("Error", "Sign-in error: ", response.getError());
                                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                            }
                            new CountDownTimer(5000, 5000) {

                                public void onTick(long millisUntilFinished) {
                                }

                                public void onFinish() {
                                    sign_in();
                                }
                            }.start();
                        }
                    }
                }
        }
    }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode){
                case REQUEST_PERMISSION_INTERNET:
                    if(grantResults.length>0) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            sign_in();
                        }
                        else{
                            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{
                                            Manifest.permission.INTERNET}
                                    , REQUEST_PERMISSION_INTERNET);
                        }
                    }
                    break;
            }
        }


    /**
     * Questo metodo rende visibili gli elementi del layout necessari all'utente per inserire la propria posizione.
     * Lancia un Intent di tipo PlacePicker, collegato a GoogleMaps e restituisce la latitudine e la longitudine che
     * andranno poi inseriti su FireBase (da aggiungere all'oggetto user).
     * @param thisActivity
     */
    private void getUserPosition(Activity thisActivity){
        FirebaseUser user = mAuth.getCurrentUser();

        /* Rende visibili i vari campi*/
        progressBar.setVisibility(View.INVISIBLE);
        findViewById(R.id.login_progress_bar_text).setVisibility(View.INVISIBLE);
        TextView addr_text = findViewById(R.id.login_Address);
        Button getPos_button = findViewById(R.id.login_getPos_button);
        addr_text.setVisibility(View.INVISIBLE);
        getPos_button.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);


        getPos_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Lanciare l'intent per ottenere la posizione*/
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(thisActivity), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });



    }
}
