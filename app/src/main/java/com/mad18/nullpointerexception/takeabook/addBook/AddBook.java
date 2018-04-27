package com.mad18.nullpointerexception.takeabook.addBook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad18.nullpointerexception.takeabook.Book;
import com.mad18.nullpointerexception.takeabook.R;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.mad18.nullpointerexception.takeabook.User;
import com.mad18.nullpointerexception.takeabook.myProfile.editProfile;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

/** DA aggiungere
 * la possibilità di inserire isbn manualmete e tasto ok per fetchare i dati da json
 * La thumbnail da scaricare come img di default se l'utente non la sceglie
 * Usare:
 * Glide.with(context).load(url).into(findViewById(R.id.add_book_photo)) per scaricare la thumbnail nella view
 *
 * Limitare caratteri manuali isbn a 13, aggiungere tasto conferma per l'isbn manuale
 * Campo testo commento del libro ed opinioni
 * layout da migliorare
 */

public class AddBook extends AppCompatActivity {
    private final String TAG = "AddBook";
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private final int REQUEST_PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2, REQUEST_SCANNER=3;;
    private final int REQUEST_PERMISSION_CAMERA = 2, REQUEST_PERMISSION_GALLERY=1;
    private final int addBookTextViewIds[] = {R.id.add_book_text_field_Title,R.id.add_book_text_field_Author,
            R.id.add_book_text_field_EditionYear,R.id.add_book_text_field_Publisher,
            R.id.add_book_text_field_ISBN};
    private Book bookToAdd;
    private View mClss;
    private Toolbar toolbar;
    private Menu menu;
    private Bitmap bookImg;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.add_book);
        toolbar = (Toolbar) findViewById(R.id.add_book_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Add a book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button scan = (Button)findViewById(R.id.read_barcode);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
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
        if(state != null){
            for(int i:addBookTextViewIds){
                findViewById(i).setVisibility(View.INVISIBLE);
            }
            findViewById(R.id.add_book_text_field_ISBN).setVisibility(View.VISIBLE);
            findViewById(R.id.add_book_picture).setVisibility(View.INVISIBLE);
            bookImg = null;
        }
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
        switch (item.getItemId()){
            case R.id.add_book_done:
                storeBookEditData();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ExtendedEditText eet;
        for(int i:addBookTextViewIds){
            eet = findViewById(i);
            outState.putString(Integer.toString(i),eet.getText().toString());
        }
        if(bookImg!=null){
            outState.putString("bookEditImgPath",editProfile.saveImageToInternalStorage(bookImg,"temp_"+"bookEditImage",this));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ExtendedEditText text;
        for(int i:addBookTextViewIds){
            text = findViewById(i);
            text.setText(savedInstanceState.getString(Integer.toString(i),""));
        }
        String bookImgPath = savedInstanceState.getString("bookEditImgPath");
        if(bookImgPath!=null){
            File file = new File(bookImgPath);
            if(file.exists()){
                bookImg = editProfile.loadImageFromStorage(bookImgPath,R.id.add_book_picture,this);
                file.delete();
            }
        }
    }

    private void storeBookEditData(){
        ExtendedEditText text;
        String s; File file;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference mImageRef;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference books = db.collection("books");
        CollectionReference users = db.collection("users");
        String bookImgPath;
        Map<String,Boolean> authors = new HashMap<>();
        ExtendedEditText eet;
        //Aggiunta libro a elenco libri
        eet = findViewById(R.id.add_book_extended_edit_text_ISBN);
        bookToAdd.setBook_ISBN(eet.getText().toString());
        eet = findViewById(R.id.add_book_extended_edit_Author);
        String tmp[] = eet.getText().toString().split(","); //è la virgola???
        for(int i=0;i<tmp.length;i++){
            authors.put(tmp[i],true);
        }
        bookToAdd.setBook_authors(authors);
        eet = findViewById(R.id.add_book_extended_edit_text_EditionYear);
        bookToAdd.setBook_editionYear(Integer.parseInt(eet.getText().toString()));
        eet = findViewById(R.id.add_book_extended_edit_Title);
        bookToAdd.setBook_title(eet.getText().toString());
        mImageRef = FirebaseStorage.getInstance().getReference().child("images/books/"+bookToAdd.getBook_ISBN()+user.getUid());
        if(bookImg!=null){
            bookImgPath = editProfile.saveImageToInternalStorage(bookImg,"temp_"+"bookEditImage",this);
            if(bookImgPath!=null) {
                file = new File(bookImgPath);
                Uri profileImgUri = Uri.fromFile(file);
                mImageRef.putFile(profileImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        file.delete();
                    }
                });
            }
        }
        books.document(user.getUid()).set(bookToAdd);
        //Aggiunta libro all'elenco dell'utente
        users.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User u = documentSnapshot.toObject(User.class);
                u.getUsr_books().put(bookToAdd.getBook_ISBN()+user.getUid(),true);
                users.document(user.getUid()).set(u,SetOptions.merge());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView iw;
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case REQUEST_SCANNER:
                    if(data!=null) {
                        Bundle bundle = data.getExtras();
                        BookWrapper bookwrap = (BookWrapper) bundle.getParcelable("bookinfo");
                        //TextView titleView = (TextView)findViewById(R.id.add_book_title);
                        // titleView.setText(bookwrap.getTitle());
                        for(int i:addBookTextViewIds){
                            findViewById(i).setVisibility(View.VISIBLE);
                        }
                        findViewById(R.id.add_book_picture).setVisibility(View.VISIBLE);
                        Map<String,Boolean> Mauthors = new HashMap<>();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        Mauthors.putAll(bookwrap.getAuthors().stream().collect(Collectors.toMap((String s)->s,s-> true)));
//                    }
                        for (String key: bookwrap.getAuthors()) {
                            Mauthors.put(key,true);
                        }
                        bookToAdd = new Book(bookwrap.getISBN(),bookwrap.getTitle(),
                                bookwrap.getPublisher(),bookwrap.getEditionYear(),0,Mauthors);
                        /**Rendere view cliccabile (imamgine) anche con inserimento isbn manuale)*/
                        fillAddBookViews(bookToAdd);
                        findViewById(R.id.add_book_picture).setVisibility(View.VISIBLE);
                        iw = findViewById(R.id.add_book_picture);
                        iw.setClickable(true);
                        iw.setOnClickListener(view -> selectBookImg());
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null) {
                        bookImg = (Bitmap) data.getExtras().get("data");
                        iw = findViewById(R.id.add_book_picture);
                        if(iw!=null && bookImg != null) {
                            iw.setImageBitmap(bookImg);
                        }
                    }
                    break;
                case REQUEST_PICK_IMAGE:
                    if (data != null) {
                        Uri selectedMediaUri = data.getData();
                        try {
                            bookImg = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),selectedMediaUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        iw = findViewById(R.id.add_book_picture);
                        if(iw!=null && bookImg != null) {
                            iw.setImageBitmap(bookImg);
                        }
                    }
                    break;
            }
        }
    }

    private void fillAddBookViews(Book book){
        ExtendedEditText eet;
        eet = findViewById(R.id.add_book_extended_edit_text_ISBN);
        eet.setText(book.getBook_ISBN());
        eet = findViewById(R.id.add_book_extended_edit_Author);
        eet.setText(book.getBook_authors().keySet().toString());
        eet = findViewById(R.id.add_book_extended_edit_text_EditionYear);
        if(book.getBook_editionYear()!=-1){
            eet.setText(String.valueOf(book.getBook_editionYear()));
        }
        eet = findViewById(R.id.add_book_extended_edit_Title);
        eet.setText(book.getBook_title());
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
                        case 2:
                            removeBookImg();
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

    private void removeBookImg(){
        ImageView iw;
        if(bookImg!=null){
            iw = findViewById(R.id.add_book_picture);
            bookImg = null;
            iw.setImageResource(R.drawable.ic_if_internt_web_technology_05_274892);
        }
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
