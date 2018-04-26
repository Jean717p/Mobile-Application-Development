package com.mad18.nullpointerexception.takeabook.addBook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mad18.nullpointerexception.takeabook.Book;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.myProfile.editProfile;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AddBook extends AppCompatActivity {
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private View mClss;
    private final int REQUEST_SCANNER=1;
    private final int REQUEST_PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2;
    private final int REQUEST_PERMISSION_CAMERA = 2, REQUEST_PERMISSION_GALLERY=1;
    private Book bookToAdd = new Book();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.add_book);
        ImageView iw = findViewById(R.id.add_book_photo);
        iw.setClickable(true);
        iw.setOnClickListener(view -> selectBookImg());

        Button scan = (Button)findViewById(R.id.read_barcode);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AddBook.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    mClss = v;
                    ActivityCompat.requestPermissions(AddBook.this,
                            new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
                } else {
                    Intent intent = new Intent(AddBook.this, ScanBarcode.class);
                    startActivityForResult(intent,REQUEST_SCANNER);
                }

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(AddBook.this, ScanBarcode.class);
                        startActivityForResult(intent, REQUEST_SCANNER);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("back", "i am back");
        //fillBookData();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_SCANNER){
                if(data!=null) {
                    Bundle bundle = data.getExtras();
                    BookWrapper bookwrap = (BookWrapper) bundle.getParcelable("bookinfo");
                    bookToAdd.setBook_ISBN(bookwrap.getISBN());
                    bookToAdd.setBook_title(bookwrap.getTitle());
                    Map<String,Boolean> Mauthors = new HashMap<>();
                    for (String key: bookwrap.getAuthors()) {
                                Mauthors.put(key,true);
                    }
                    bookToAdd.setBook_authors(Mauthors);
                    bookToAdd.setBook_publisher(bookwrap.getPublisher());
                    bookToAdd.setBook_editionYear(bookwrap.getEditionYear());

//                    TextView isbnView = (TextView)findViewById(R.id.add_book_text_field_ISBN);
//                    isbnView.setText(bookwrap.getISBN());
//                    TextView titleView = (TextView)findViewById(R.id.add_book_text_field_Title);
//                    titleView.setText(bookwrap.getTitle());
//                    TextView authorsView = (TextView)findViewById(R.id.add_book_text_field_Author);
//                    authorsView.setText(bookwrap.getAuthors().toString());
//                    TextView publisherView = (TextView)findViewById(R.id.add_book_text_field_Publisher);
//                    publisherView.setText(bookwrap.getPublisher());
//                    TextView editionYearView = (TextView)findViewById(R.id.add_book_text_field_EditionYear);
//                    editionYearView.setText(Integer.toString(bookwrap.getEditionYear()));


                }
            }
        }
    }
    private void fillBookData(){
//        TextView isbnView = (TextView)findViewById(R.id.add_book_ISBN);
//        isbnView.setText(bookToAdd.getBook_ISBN());
//        TextView titleView = (TextView)findViewById(R.id.add_book_title);
//        titleView.setText(bookToAdd.getBook_title());
//        TextView authorsView = (TextView)findViewById(R.id.add_book_authors);
//        String allAuthors = "";
//        for (String key: bookToAdd.getBook_authors().keySet()) {
//            allAuthors = allAuthors + " " + key;
//        }
//        authorsView.setText(allAuthors);
//        TextView publisherView = (TextView)findViewById(R.id.add_book_publisher);
//        publisherView.setText(bookToAdd.getBook_publisher());
//        TextView editionYearView = (TextView)findViewById(R.id.add_book_editionYear);
//        editionYearView.setText(bookToAdd.getBook_editionYear());
    }
    private void selectBookImg(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        //pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                getString(R.string.photo_from_gallery),
                getString(R.string.photo_from_camera),
                getString(R.string.photo_remove) };
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallery();
                            break;
                        case 1:
                            choosePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        if(ActivityCompat.checkSelfPermission(AddBook.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AddBook.this,new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE }
                    ,REQUEST_PERMISSION_GALLERY);
            return;
        }
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_PICK_IMAGE);
    }

    private void choosePhotoFromCamera() {
        if(ActivityCompat.checkSelfPermission(AddBook.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(AddBook.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AddBook.this,new String[]{
                            Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE }
                    ,REQUEST_PERMISSION_CAMERA);
            return;
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

}
