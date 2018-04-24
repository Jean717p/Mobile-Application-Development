package com.mad18.nullpointerexception.takeabook.loginActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mad18.nullpointerexception.takeabook.R;

import java.nio.file.FileStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.mad18.nullpointerexception.takeabook.myProfile.showProfile.sharedUserDataKeys;

public class LoginActivity extends AppCompatActivity  {
    private static final String TAG = "Login";
    private static final int RC_SIGN_IN = 123;
    private static final int REQUEST_PERMISSION_INTERNET=3;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_login);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            if(ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{
                                Manifest.permission.INTERNET}
                        , REQUEST_PERMISSION_INTERNET);
            }
            else{
               sign_in();
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
                                //,new AuthUI.IdpConfig.FacebookBuilder().build()
                        ))
                        //.setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */)
                        .build(),
                RC_SIGN_IN);
        return;
        }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
                FirebaseUserMetadata metadata = mAuth.getCurrentUser().getMetadata();
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    // The user is new, show them a fancy intro screen!
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference users = db.collection("users");
                    FirebaseUser user = mAuth.getCurrentUser();
                    Map<String,String> user_data = new HashMap<>();
                    user_data.put("usr_mail",user.getEmail());
                    user_data.put("usr_name",user.getDisplayName());
                    user_data.put("usr_city","");
                    user_data.put("usr_about","");
                    users.document(user.getUid()).set(user_data);
                } else {
                    // This is an existing user, show them a welcome back screen.
                }
                Intent intent = new Intent(this, com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.d("Debug","cancelled");
                    finish();
                }
                else{
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        Log.e("Error", "Sign-in error: ", response.getError());
                    }
                    sign_in();
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
}
