package com.mad18.nullpointerexception.takeabook;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.view.WindowManager;
import android.widget.ImageView;
import android.content.DialogInterface;
import android.widget.TextView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class editProfile extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private int editTextBoxesIds[] = new int[]{R.id.edit_profile_Username,R.id.edit_profile_City,
            R.id.edit_profile_mail,R.id.edit_profile_about};
    private Menu menu;
    private final int REQUEST_PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2;
    private final int REQUEST_PERMISSION_CAMERA = 2, REQUEST_PERMISSION_GALLERY=1;
    private String profileImgName = "profile.jpg";
    private Bitmap profileImg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        setContentView(R.layout.edit_profile);
        Toolbar toolbar = findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.app_name);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(savedInstanceState == null){
            fillUserData();
        }
        ImageView iw = findViewById(R.id.edit_profile_personalPhoto);
        iw.setClickable(true);
        iw.setOnClickListener(view ->
            {
                if(ActivityCompat.checkSelfPermission(editProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(editProfile.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PICK_IMAGE);
                }
                else {
                    selectUserImg();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView iw = findViewById(R.id.edit_profile_personalPhoto);
        iw.setClickable(true);
        iw.setOnClickListener(view -> editProfile.this.selectUserImg());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile_toolbar, menu);
        return true;
}

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.edit_profile_action_save:
                storeUserEditData();
                finish();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeUserEditData(){
        EditText text;
        SharedPreferences.Editor editor = sharedPref.edit();
        int i=0;
        for(String x: showProfile.sharedUserDataKeys){
            text = findViewById(editTextBoxesIds[i++]);
            editor.putString(x,text.getText().toString());
        }
        if(profileImg!=null){
            editor.putString(profileImgName,saveToInternalStorage(profileImg,profileImgName));
        }
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        EditText text;
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            outState.putString(Integer.toString(i),text.getText().toString());
        }
        if(profileImg!=null){
            outState.putString("profileImgPath",saveToInternalStorage(profileImg,"temp_"+profileImgName));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EditText text;
        String path = savedInstanceState.getString("profileImgPath");
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            text.setText(savedInstanceState.getString(Integer.toString(i),""));
        }
        if(path!=null) {
            File file = new File(path);
            if (file.exists()) {
                profileImg = loadImageFromStorage(file.getAbsolutePath(), R.id.edit_profile_personalPhoto);
                file.delete();
            }
        }
    }

    private void fillUserData(){
        TextView text;
        String y;
        int i=0;
        for(String x:showProfile.sharedUserDataKeys){
            text = (EditText) findViewById(editTextBoxesIds[i++]);
            y=sharedPref.getString(x,"");
            if(y.length()>0){
                text.setText(y);
            }
        }
        y=sharedPref.getString(profileImgName,"");
        if(y.length()>0){
            profileImg = loadImageFromStorage(sharedPref.getString(profileImgName,""),R.id.edit_profile_personalPhoto);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        ImageView iw;
        if (resultCode == Activity.RESULT_OK) {
            if(getPickImageResultUri(data) != null){
                Uri selectedMediaUri = getPickImageResultUri(data);
                try {
                    profileImg = MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(),selectedMediaUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                iw = findViewById(R.id.edit_profile_personalPhoto);
                if(iw!=null && profileImg != null) {
                    iw.setImageBitmap(profileImg);
                }
            }
            else {
                if (data != null) {
                    profileImg = (Bitmap) data.getExtras().get("data");
                    iw = findViewById(R.id.edit_profile_personalPhoto);
                    if(iw!=null && profileImg != null) {
                        iw.setImageBitmap(profileImg);
                    }
                }
            }
        }
    }
    private void selectUserImg(){
        startActivityForResult(getPickImageChooserIntent(), REQUEST_PICK_IMAGE);

    }

    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }


    /**
     * Get URI to image received from capture by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }


        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }


    private String saveToInternalStorage(Bitmap bitmapImage,String filename){
        if(bitmapImage==null){
            return null;
        }
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/appname/app_data/imageDir internal
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                //Environment.getExternalStorageDirectory(); sd
        // Create imageDir
        File file = new File(directory,filename);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            if(bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, out)==false){
                //decrementare secondo parametro per compressare
                out.close();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getAbsolutePath();
    }

    private Bitmap loadImageFromStorage(String path,int id) {
        if(path==null){
            return null;
        }
        Bitmap b = null;
        File file = new File(path);
        ImageView img = (ImageView) findViewById(id);
        if(file.exists() == false||img==null){
            return null;
        }
        try {
            b = BitmapFactory.decodeStream(new FileInputStream(file));
            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_GALLERY:
                if(grantResults.length>0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getPickImageChooserIntent();
                    }
                }
                break;
            case REQUEST_PERMISSION_CAMERA:
                if(grantResults.length>0){
                    if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                        getPickImageChooserIntent();
                    }
                }
                break;
        }
    }

    //    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
//        ExifInterface ei = new ExifInterface(image_absolute_path);
//        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//
//        switch (orientation) {
//            case ExifInterface.ORIENTATION_ROTATE_90:
//                return rotate(bitmap, 90);
//
//            case ExifInterface.ORIENTATION_ROTATE_180:
//                return rotate(bitmap, 180);
//
//            case ExifInterface.ORIENTATION_ROTATE_270:
//                return rotate(bitmap, 270);
//
//            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
//                return flip(bitmap, true, false);
//
//            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
//                return flip(bitmap, false, true);
//
//            default:
//                return bitmap;
//        }
//    }
//
//    public static Bitmap rotate(Bitmap bitmap, float degrees) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(degrees);
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//    }
//
//    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
//        Matrix matrix = new Matrix();
//        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//    }

}


