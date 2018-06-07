package com.mad18.nullpointerexception.takeabook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.ScanBarcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditBook extends AppCompatActivity {

    private static final int ZXING_CAMERA_PERMISSION = 4;
    private final int REQUEST_PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2, REQUEST_SCANNER=3,
            REQUEST_PICK_IMAGE_COVER =4, REQUEST_IMAGE_CAPTURE_COVER=5;
    private final int REQUEST_PERMISSION_CAMERA = 2, REQUEST_PERMISSION_GALLERY=1;
    private final String TAG = "editBook";
    private Menu menu;

    LinearLayout horizontal_photo_list;
    View horizontal_photo_list_element;
    List<String> for_me;
    private Bitmap bookImg;
    private HashMap<Integer,Bitmap> bookImgMap = new HashMap<>();
    private ImageView globalViewImgElement;
    private int globalImgPos = 0 ;
   // private int tofillBookImgMap;
    private Boolean modified[] = {false, false, false, false};
    private List<String> newPhotosPos;
    private Spinner staticSpinner;
    private AppCompatActivity myActivity;
    private Book bookInMod ;
    private Bitmap bookCover;
    private boolean isCoverChanged;
    private StorageReference coverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);
        myActivity = this;
        Toolbar toolbar = findViewById(R.id.edit_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_edit_book);
        toolbar.setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle bundle = getIntent().getExtras();
        BookWrapper bookWrapper = (BookWrapper) bundle.getParcelable("book_to_modify");
        bookInMod = new Book(bookWrapper);
        newPhotosPos = new LinkedList<>(bookInMod.getBook_photo_list().keySet());
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        Button read_barcode = findViewById(R.id.edit_book_read_barcode);
        read_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toRetrieveBarcode = new Intent(EditBook.this, ScanBarcode.class);
                toRetrieveBarcode.putExtra("justScan", "");
                startActivityForResult(toRetrieveBarcode,REQUEST_SCANNER);
            }
        });

        staticSpinner = findViewById(R.id.edit_book_spinner_book_cond);
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.book_conditions,
                        android.R.layout.simple_dropdown_item_1line);
        staticSpinner.setAdapter(staticAdapter);
        isCoverChanged = false;
        if(bookInMod.getBook_thumbnail_url().length()>0){
            try {
                FirebaseStorage.getInstance()
                        .getReferenceFromUrl(bookInMod.getBook_thumbnail_url())
                        .getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                ImageView imageView = myActivity.findViewById(R.id.edit_book_thumbnail);
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        selectBookImg(true);
                                    }
                                });
                            }
                        });
            }catch (Exception e){
                Log.d(TAG,"Probably is not a storageReference \\_(^_^)_/");
                e.printStackTrace();
            }
        }
        fillBookViews(bookInMod);
        fillPhotoList(bookInMod);
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
                EditText title = findViewById(R.id.edit_book_title);
                EditText authors = findViewById(R.id.edit_book_authors);
                if((title.getText().toString().length() ==0) || (authors.getText().toString().length() == 0)){
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.edit_book_layout_scroll_view),"title and/or author(s) missing", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return false;
                }
                storeBookEditData();
                Intent intentToInfoBook = new Intent();
                intentToInfoBook.putExtra("bookToShowModified", new BookWrapper(bookInMod));
                setResult(RESULT_OK, intentToInfoBook);
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    private void fillBookViews(Book book){
        TextView tv ;
        String tmp;
        ImageView iw;

        staticSpinner.setSelection(book.getBook_condition());

        tv = findViewById(R.id.edit_book_description);
        tv.setText(book.getBook_description());

        tv = findViewById(R.id.edit_book_ISBN);
        tv.setText(book.getBook_ISBN());

        tv = findViewById(R.id.edit_book_authors);
        tmp = book.getBook_authors().keySet().toString();
        if(tmp.length()>2){
            tv.setText(tmp.substring(1,tmp.length()-1));
        }
        tv = findViewById(R.id.edit_book_edition_year);
        if(book.getBook_editionYear()!=-1){
            tv.setText(String.valueOf(book.getBook_editionYear()));
        }
        tv = findViewById(R.id.edit_book_title);
        if(book.getBook_title().length()>1){
            tv.setText(book.getBook_title());
        }
//        else{
//            Toast.makeText(this, R.string.add_book_title_not_found, Toast.LENGTH_SHORT).show();
//        }

        tv = findViewById(R.id.edit_book_publisher);
        if(book.getBook_publisher().length()>2){
            tv.setText(book.getBook_publisher());
        }

        tv = findViewById(R.id.edit_book_categories);
        tmp = book.getBook_categories().keySet().toString();
        if(tmp.length()>2){
            tv.setText(tmp.substring(1,tmp.length()-1));
        }

        tv = findViewById(R.id.edit_book_number_of_pages);
        if(book.getBook_pages()!=0){
            tv.setText(String.valueOf(book.getBook_pages()));
        }

        if(book.getBook_thumbnail_url().length()>0){
            iw = findViewById(R.id.edit_book_thumbnail);
            GlideApp.with(this).load(book.getBook_thumbnail_url())
                    .placeholder(R.drawable.ic_thumbnail_cover_book).into(iw);
        }

        //fillPhotoList(book);
    }

    private void fillPhotoList(Book book){

        horizontal_photo_list = (LinearLayout) findViewById(R.id.edit_book_horizontal_photo_layout);
        for_me = new LinkedList<>(book.getBook_photo_list().keySet());

        int i;
        try {
            for (i = 0; i < book.getBook_photo_list().size(); i++) {
                horizontal_photo_list_element = getLayoutInflater().inflate(R.layout.cell_in_image_list, null);
                ImageView imageView = (ImageView) horizontal_photo_list_element.findViewById(R.id.image_in_horizontal_list_cell);
                StorageReference mImageRef = FirebaseStorage.getInstance().getReference(for_me.get(i));
                GlideApp.with(this).load(mImageRef).placeholder(R.drawable.ic_thumbnail_cover_book).into(imageView);
                imageView.setOnClickListener(v -> {
                    globalImgPos = Integer.parseInt(imageView.getTag().toString());
                    globalViewImgElement = imageView;
                    selectBookImg(false);
                });
                imageView.setTag(i);
                horizontal_photo_list.addView(horizontal_photo_list_element);
            }
            for (; i < 4; i++) {
                horizontal_photo_list_element = getLayoutInflater().inflate(R.layout.cell_in_image_list, null);
                ImageView imageView = (ImageView) horizontal_photo_list_element.findViewById(R.id.image_in_horizontal_list_cell);
                imageView.setImageResource(R.drawable.ic_insert_photo);
                imageView.setOnClickListener(v -> {
                    globalImgPos = Integer.parseInt(imageView.getTag().toString());
                    globalViewImgElement = imageView;
                    selectBookImg(false);
                });

                imageView.setTag(i);
                horizontal_photo_list.addView(horizontal_photo_list_element);
            }
        }catch(Exception e){
            Log.d(TAG,"Probably activity destroyed before setup \\_(^_^)_/");
            e.printStackTrace();
        }
    }

    private void storeBookEditData(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference mImageRef;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference books = db.collection("books");
        Map<String,Boolean> authors = new HashMap<>();
        EditText et;
        Map<String,Boolean>photourllist = new HashMap<>();
        DocumentReference bookRef = books.document(bookInMod.getBook_id());
        //Aggiunta libro a elenco libri
        if(bookInMod==null){
            finish();
            Log.d(TAG,"ERROR store");
        }
        et = findViewById(R.id.edit_book_ISBN);
        bookInMod.setBook_ISBN(et.getText().toString());
        et = findViewById(R.id.edit_book_description);
        bookInMod.setBook_description(et.getText().toString());
        et = findViewById(R.id.edit_book_authors);
        String tmp[] = et.getText().toString().split(","); //è la virgola???
        for(int i=0;i<tmp.length;i++){
            authors.put(tmp[i],true);
        }
        bookInMod.setBook_first_author(tmp[0]);
        bookInMod.setBook_authors(authors);

        bookInMod.setBook_condition(staticSpinner.getSelectedItemPosition());

        et = findViewById(R.id.edit_book_edition_year);
        bookInMod.setBook_editionYear(Integer.parseInt(et.getText().toString()));

        et = findViewById(R.id.edit_book_title);
        bookInMod.setBook_title(et.getText().toString());

        et = findViewById(R.id.edit_book_publisher);
        bookInMod.setBook_publisher(et.getText().toString());

        //aggiunta foto allo storage
        for(int j= 0; j<4;j++ ) {
            if(bookImgMap.containsKey(j) && modified[j]){
                if (bookImgMap.get(j) != null) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Bitmap bookImgForName = bookImgMap.get(j);
                    bookImgForName.compress(Bitmap.CompressFormat.JPEG,100,out);
                    mImageRef = FirebaseStorage.getInstance().getReference().child("users/"+user.getUid()+ "/books/" + bookInMod.getBook_id() + "/" + UUID.nameUUIDFromBytes(out.toByteArray()));
                    photourllist.put("users/"+user.getUid()+ "/books/" + bookInMod.getBook_id()+ "/"+ UUID.nameUUIDFromBytes(out.toByteArray()), true);
                    mImageRef.putBytes(out.toByteArray());
                }
                else{
                    mImageRef = FirebaseStorage.getInstance().getReference().child(for_me.get(j));
                    mImageRef.delete();
                }
            }
            else if(modified[j]==false && bookImgMap.containsKey(j)&& bookImgMap.get(j)!=null){
                photourllist.put(for_me.get(j),true);
            }
        }
        bookInMod.setBook_photo_list(photourllist);
        bookRef.set(bookInMod, SetOptions.merge());
        if(isCoverChanged){
            FirebaseStorage.getInstance()
                    .getReferenceFromUrl(bookInMod.getBook_thumbnail_url())
                    .delete();
            if(bookCover != null){
                ByteArrayOutputStream coverOut = new ByteArrayOutputStream();
                bookCover.compress(Bitmap.CompressFormat.JPEG,100,coverOut);
                coverRef = FirebaseStorage.getInstance()
                        .getReference()
                        .child(
                        "users/"+bookInMod.getBook_userid()+ "/books/"+bookInMod.getBook_id()+"/"+UUID.nameUUIDFromBytes(coverOut.toByteArray())
                );
                coverRef.putBytes(coverOut.toByteArray());
                coverRef.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                bookInMod.setBook_thumbnail_url(uri.toString());
                                db.collection("books")
                                        .document(bookInMod.getBook_id())
                                        .update("book_thumbnail_url",uri.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                editIndexToAlgolia(bookInMod);
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                bookInMod.setBook_thumbnail_url("");
                                coverRef.delete();
                                editIndexToAlgolia(bookInMod);
                            }
                        });
            }
            else{
                bookInMod.setBook_thumbnail_url("");
                db.collection("books")
                        .document(bookInMod.getBook_id())
                        .update("book_thumbnail_url","");
                editIndexToAlgolia(bookInMod);
            }
        }
        else{
            editIndexToAlgolia(bookInMod);
        }
    }

    public void editIndexToAlgolia(Book b){
        String algoliaID = "P15KSBYCLA";
        String algoliaKey = "18740ed4b222f99d8f8dcd7c17002b84";
        Client client = new Client(algoliaID, algoliaKey);
        String tmp = b.getBook_authors().keySet().toString();
        String authors = "";
        if(tmp.length()>2){
            authors = tmp.substring(1,tmp.length()-1);
        }


        /* Titolo */
        Index title_index = client.getIndex("book_title");
        List<JSONObject> array_title = new ArrayList<JSONObject>();
        try {
            array_title.add(new JSONObject()
                    .put("Title", b.getBook_title())
                    .put("ISBN", b.getBook_ISBN())
                    .put("UserID", b.getBook_userid())
                    .put("Author",authors)
                    .put("ThumbnailURL", b.getBook_thumbnail_url())
                    .put("objectID",b.getBook_id()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        title_index.saveObjectsAsync(new JSONArray(array_title), null);

        /* Autore */
        Index author_index = client.getIndex("book_author");
        List<JSONObject> array_author = new ArrayList<JSONObject>();
        try {
            array_author.add(new JSONObject()
                    .put("Title", b.getBook_title())
                    .put("ISBN", b.getBook_ISBN())
                    .put("UserID", b.getBook_userid())
                    .put("Author",authors)
                    .put("ThumbnailURL", b.getBook_thumbnail_url())
                    .put("objectID",b.getBook_id()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        author_index.saveObjectsAsync(new JSONArray(array_author), null);

        /* ISBN */
        Index ISBN_index = client.getIndex("book_ISBN");
        List<JSONObject> array_ISBN= new ArrayList<JSONObject>();
        try {
            array_ISBN.add(new JSONObject()
                    .put("Title", b.getBook_title())
                    .put("ISBN", b.getBook_ISBN())
                    .put("UserID", b.getBook_userid())
                    .put("Author",authors)
                    .put("ThumbnailURL", b.getBook_thumbnail_url())
                    .put("objectID",b.getBook_id()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ISBN_index.saveObjectsAsync(new JSONArray(array_ISBN), null);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView iw;
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case REQUEST_SCANNER:
                    if(data!=null) {
                        String isbn = data.getStringExtra("isbn");
                        if(isbn!=null){
                            EditText et = findViewById(R.id.edit_book_ISBN);
                            et.setText(isbn);
                        }
                    }
                    break;

                case REQUEST_IMAGE_CAPTURE:
                    if (data != null) {
                        bookImg = (Bitmap) data.getExtras().get("data");
                        iw = globalViewImgElement;
                        if(iw!=null && bookImg != null) {
                            bookImg = Bitmap.createScaledBitmap(bookImg,768, 1024, true);
                            iw.setImageBitmap(bookImg);
                            bookImgMap.put(globalImgPos,bookImg);
                            modified[globalImgPos] = true;
                        }
                    }
                    break;

                case REQUEST_PICK_IMAGE:
                    if (data != null) {
                        Uri selectedMediaUri = data.getData();
                        try {
                            bookImg = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedMediaUri);
                            iw = globalViewImgElement;
                            if(iw!=null && !bookImg.equals("")) {
                                bookImg = Bitmap.createScaledBitmap(bookImg,768, 1024, true);
                                iw.setImageBitmap(bookImg);
                                bookImgMap.put(globalImgPos,bookImg);
                                modified[globalImgPos] = true;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE_COVER:
                    if (data != null) {
                        try{
                            bookCover = Bitmap.createScaledBitmap(
                                    (Bitmap) data.getExtras().get("data"),128,206,true);
                            iw = findViewById(R.id.add_book_picture);
                            iw.setImageBitmap(bookCover);
                            isCoverChanged = true;
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    break;
                case REQUEST_PICK_IMAGE_COVER:
                    if (data != null) {
                        Uri selectedMediaUri = data.getData();
                        try {
                            bookCover = Bitmap.createScaledBitmap(
                                    (Bitmap) MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedMediaUri),
                                    128,206,true);
                            iw = findViewById(R.id.add_book_picture);
                            if(iw!=null && !bookCover.equals("")) {
                                iw.setImageBitmap(bookCover);
                                isCoverChanged = true;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    private void selectBookImg(boolean isCover){
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
                            choosePhotoFromGallery(isCover);
                            break;
                        case 1:
                            choosePhotoFromCamera(isCover);
                            break;
                        case 2:
                            removeBookImg(isCover);
                            break;
                    }
                });
        pictureDialog.show();
    }

    /**
     * Crea l'intent per permettere la scelta di una foto dalla galleria, controllando i permessi necessari.
     */

    public void choosePhotoFromGallery(boolean isCover) {
        if(ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(myActivity,new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE }
                    ,REQUEST_PERMISSION_GALLERY);
            return;
        }
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(isCover){
            startActivityForResult(galleryIntent, REQUEST_PICK_IMAGE_COVER);
        }
        else{
            startActivityForResult(galleryIntent, REQUEST_PICK_IMAGE);
        }
    }

    /**
     * Crea l'intent per lo scatto di un'immagine dalla fotocamera, controllando i permessi necessari
     */
    private void choosePhotoFromCamera(boolean isCover) {
        if(ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(myActivity,new String[]{
                            Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE }
                    ,REQUEST_PERMISSION_CAMERA);
            return;
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(isCover){
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE_COVER);
        }
        else{
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Si occupa della rimozione dell'immagine dalla ImageView in cui è contenuta.
     */
    private void removeBookImg(boolean isCover){
        ImageView iw;
        if(isCover){
            if(bookCover!=null){
                iw = findViewById(R.id.add_book_picture);
                bookCover.recycle();
                iw.setImageResource(R.drawable.ic_addbook);
            }
            isCoverChanged = true;
            bookCover = null;
        }
        else if(bookImgMap.isEmpty()==false){
            iw = globalViewImgElement;
            //bookImgMap.remove(globalImgPos);
            modified[globalImgPos] = true;
            bookImgMap.put(globalImgPos,null);
            iw.setImageResource(R.drawable.ic_insert_photo);
        }
    }
}
