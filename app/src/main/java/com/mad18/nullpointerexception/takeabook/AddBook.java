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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.ScanBarcode;
import com.mad18.nullpointerexception.takeabook.util.User;

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

import studio.carbonylgroup.textfieldboxes.ExtendedEditText;


public class AddBook extends AppCompatActivity {
    private final String TAG = "AddBook";
    private final int BOOK_EFFECTIVELY_ADDED = 31;
    private static final int ZXING_CAMERA_PERMISSION = 4;
    private final int REQUEST_PERMISSION_CAMERA = 2, REQUEST_PERMISSION_GALLERY=1;
    private final int REQUEST_PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2, REQUEST_SCANNER=3,
                        REQUEST_PICK_IMAGE_COVER =4, REQUEST_IMAGE_CAPTURE_COVER=5;
    private final int addBookTextViewIds[] = {R.id.add_book_text_field_Title,R.id.add_book_text_field_Author,
            R.id.add_book_text_field_EditionYear,R.id.add_book_text_field_Publisher,
            R.id.add_book_text_field_ISBN,R.id.add_book_text_field_Description,R.id.add_book_text_field_Category,
            R.id.add_book_text_field_Pages};
    private final int addBookEditTextViewIds[] = {
            R.id.add_book_extended_edit_text_ISBN,
            R.id.add_book_extended_edit_Title,
            R.id.add_book_extended_edit_Author,
            R.id.add_book_extended_edit_text_EditionYear,
            R.id.add_book_extended_edit_text_Publisher,
            R.id.add_book_extended_edit_text_Description,
            R.id.add_book_extended_edit_Category,
            R.id.add_book_extended_edit_text_Pages
    };
    private Book bookToAdd;
    private View mClss;
    private Toolbar toolbar;
    private Menu menu;
    private Spinner staticSpinner;
    private FirebaseUser user;
    private StorageReference coverRef;
    //simo inizio
    private Bitmap bookImg,bookCover;
    private HashMap<Integer,Bitmap> bookImgMap = new HashMap<>();
    // horizontal_photo_list is the child of the HorizontalScrollView ...
    private LinearLayout horizontal_photo_list;
    private View horizontal_photo_list_element;
    private TextView text;

    private ImageView globalViewImgElement;
    private int globalImgPos = 0 ;
    private List<String> all_photos_url= new LinkedList<>();
    //simo fine
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.add_book);
        toolbar = (Toolbar) findViewById(R.id.add_book_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Add a book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Button scan = (Button)findViewById(R.id.read_barcode);
        Button search = (Button)findViewById(R.id.add_book_read_ISBN); //new search
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        ExtendedEditText eetS;
        eetS = findViewById(R.id.add_book_extended_edit_text_ISBN);
        eetS.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
               if(actionID == EditorInfo.IME_ACTION_SEARCH){
                   searchAfterISBNInsertion();
               }
               return false;
            }
        });
        horizontal_photo_list = (LinearLayout) findViewById(R.id.horizontal_photo_layout);
        for (int i = 0; i < 4; i++) {
            horizontal_photo_list_element = getLayoutInflater().inflate(R.layout.add_book_cell_in_image_list, null);
            final ImageView imageView = (ImageView) horizontal_photo_list_element.findViewById(R.id.add_book_image_in_horizontal_list_cell);
            imageView.setOnClickListener(v -> {
                globalImgPos = Integer.parseInt(imageView.getTag().toString());
                globalViewImgElement = imageView;
                selectBookImg(false);
            });
            imageView.setTag(i);
            imageView.setImageResource(R.drawable.ic_insert_photo);
            horizontal_photo_list.addView(horizontal_photo_list_element);
        }
        staticSpinner = findViewById(R.id.add_book_spinner_book_cond);
        //create a list of items for the spinner.
        //String[] items = new String[]{"condizioni del libro", "Ottime", "Buone", "Scarse"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.book_conditions,
                        android.R.layout.simple_dropdown_item_1line);
        staticSpinner.setAdapter(staticAdapter);
        user = FirebaseAuth.getInstance().getCurrentUser();
        scan.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(AddBook.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                mClss = v;
                ActivityCompat.requestPermissions(AddBook.this,
                        new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
            } else {
                Intent intent = new Intent(AddBook.this, ScanBarcode.class);
                startActivityForResult(intent,REQUEST_SCANNER);
            }
        });
       if(state == null){
           /*
            for(int i:addBookTextViewIds){
                //(i).setVisibility(View.INVISIBLE);
                findViewById(i).setEnabled(false);
            }*/
//            findViewById(R.id.add_book_text_field_ISBN).setVisibility(View.VISIBLE);
//            findViewById(R.id.add_book_picture).setVisibility(View.INVISIBLE);
           findViewById(R.id.add_book_text_field_ISBN).setEnabled(true);
           findViewById(R.id.add_book_text_field_Description).setEnabled(true);
           findViewById(R.id.add_book_text_field_Publisher).setEnabled(true);
           bookImg = null;
        }
        search.setOnClickListener(view -> { //new search
            searchAfterISBNInsertion();
        });
       ImageView iw = findViewById(R.id.add_book_picture);
       iw.setClickable(true);
       iw.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               selectBookImg(true);
           }
       });
    }

    public void searchAfterISBNInsertion(){
        ExtendedEditText isbneditfield = findViewById(R.id.add_book_extended_edit_text_ISBN);
        boolean valid= true;
        if(isbneditfield.getText().toString().length()== 13){
            for (char x: isbneditfield.getText().toString().toCharArray()) {
                if(!Character.isDigit(x)){
                    valid = false;
                    break;
                }
            }
            if(valid==false){
                Toast toast = Toast.makeText(getApplicationContext(), "Exactly 13 characters for ISBN", Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                //fai la ricerca tramite isbn
                Intent searchbarcodeintent = new Intent(AddBook.this, ScanBarcode.class);
                searchbarcodeintent.putExtra("toSearch", isbneditfield.getText().toString());
                startActivityForResult(searchbarcodeintent, REQUEST_SCANNER);
            }
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.add_book_helper_text_ISBN), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_book_menu, menu);
        return true;
    }


    public void addIndexToAlgolia(Book b){
        String algoliaID = "P15KSBYCLA";
        String algoliaKey = "18740ed4b222f99d8f8dcd7c17002b84";
        Client client = new Client(algoliaID, algoliaKey);


        /* Titolo */
        Index title_index = client.getIndex("book_title");
        List<JSONObject> array_title = new ArrayList<JSONObject>();
        try {
            array_title.add(new JSONObject().put("Title", b.getBook_title()).put("ISBN", b.getBook_ISBN()).put("UserID", b.getBook_userid())
                    .put("Author",b.getBook_first_author())
                    .put("ThumbnailURL", b.getBook_thumbnail_url()).put("objectID",b.getBook_id()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        title_index.addObjectsAsync(new JSONArray(array_title), null);

        /* Autore */
        Index author_index = client.getIndex("book_author");
        List<JSONObject> array_author = new ArrayList<JSONObject>();
        try {
            array_author.add(new JSONObject().put("Title", b.getBook_title()).put("ISBN", b.getBook_ISBN()).put("UserID", b.getBook_userid())
                    .put("Author",b.getBook_first_author())
                    .put("ThumbnailURL", b.getBook_thumbnail_url())
                    .put("objectID",b.getBook_id()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        author_index.addObjectsAsync(new JSONArray(array_author), null);

        /* ISBN */
        Index ISBN_index = client.getIndex("book_ISBN");
        List<JSONObject> array_ISBN= new ArrayList<JSONObject>();
        try {
            array_ISBN.add(new JSONObject().put("Title", b.getBook_title()).put("ISBN", b.getBook_ISBN()).put("UserID", b.getBook_userid())
                    .put("Author",b.getBook_first_author())
                    .put("ThumbnailURL", b.getBook_thumbnail_url())
                    .put("objectID",b.getBook_id()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ISBN_index.addObjectsAsync(new JSONArray(array_ISBN), null);
    }

    /**
     * Gestisce le opzioni della toolbar, in particolare si occupa di gestire le operazioni necessarie al
     * salvataggio di un libro.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.add_book_done:
                ExtendedEditText eet_title, eet_ISBN, eet_author;
                eet_title = findViewById(R.id.add_book_extended_edit_Title);
                eet_ISBN = findViewById(R.id.add_book_extended_edit_text_ISBN);
                eet_author = findViewById(R.id.add_book_extended_edit_Author);
                if(eet_ISBN.getText().toString().length()==13 || eet_author.getText().toString().length() > 0
                        || eet_title.getText().toString().length() > 0) {
                    storeBookEditData();
                    Intent myLibIntent = new Intent();
                    myLibIntent.putExtra("new_book",new BookWrapper(bookToAdd));
                    setResult(BOOK_EFFECTIVELY_ADDED,myLibIntent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    return true;
                }else{
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.add_book_layout),getText(R.string.add_book_error), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return false;
                }

            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Metodo che si occupa di inserire nel database Firebase i dati relativi al libro aggiunto dall'utente.
     * Setta tutti i parametri ricevuti dalle edit text in un oggetto bookToAdd della classe book, che verrà poi inserito
     * su Firebase.
     * Viene gestito anche il caricamento dell'immagine su Firebase, inserita nella cartella photo_conditions_by_user/books utilizzando un nome
     * dato dalla chiave univoca ISBN + userID.
     * Viene infine aggiornato il campo books dell'oggetto user del database.
     */

    private void storeBookEditData(){
        ExtendedEditText eet;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference mImageRef;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference books = db.collection("books");
        CollectionReference users = db.collection("users");
        String bookImgPath;
        Map<String,Boolean> authors = new HashMap<>(), categories = new HashMap<>();
        Map<String,Boolean>photourllist = new HashMap<>();
        DocumentReference bookRef = books.document();
        //Aggiunta libro a elenco libri
        if(bookToAdd==null){
            bookToAdd = new Book("","","",0,0,"",
                    new HashMap<String,Boolean>(),"","",
                    new HashMap<String,Boolean>(),
                    new GeoPoint(0,0),
                    0,false);
        }
        bookToAdd.setBook_userid(user.getUid());
        eet = findViewById(R.id.add_book_extended_edit_text_ISBN);
        if(eet.getText().toString().length() > 0){
            bookToAdd.setBook_ISBN(eet.getText().toString());
        }
        eet = findViewById(R.id.add_book_extended_edit_text_Description);
        if(eet.getText().toString().length() > 0){
            bookToAdd.setBook_description(eet.getText().toString());
        }
        else{
            bookToAdd.setBook_description("");
        }
        eet = findViewById(R.id.add_book_extended_edit_Author);
        if(eet.getText().toString().length() > 0){
            String tmp[] = eet.getText().toString().split(","); //è la virgola???
            for(int i=0;i<tmp.length;i++){
                authors.put(tmp[i],true);
            }
            bookToAdd.setBook_authors(authors);
        }
        bookToAdd.setBook_condition(staticSpinner.getSelectedItemPosition());
        eet = findViewById(R.id.add_book_extended_edit_text_EditionYear);
        if(eet.getText().toString().length() > 0){
            bookToAdd.setBook_editionYear(Integer.parseInt(eet.getText().toString()));
        }
        eet = findViewById(R.id.add_book_extended_edit_Title);
        if(eet.getText().toString().length() > 0){
            bookToAdd.setBook_title(eet.getText().toString());
        }
        eet = findViewById(R.id.add_book_extended_edit_text_Publisher);
        if(eet.getText().toString().length() > 0) {
            bookToAdd.setBook_publisher(eet.getText().toString());
        }
        eet = findViewById(R.id.add_book_extended_edit_Category);
        if(eet.getText().length() > 0){
            String tmp[] = eet.getText().toString().split(",");
            for(int i=0;i<tmp.length;i++){
                categories.put(tmp[i],true);
            }
            bookToAdd.setBook_categories(categories);
        }
        //aggiunta foto allo storage
        for(int j= 0; j<4;j++ ) {
            if(bookImgMap.containsKey(j)){
                if (bookImgMap.get(j) != null) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Bitmap bookImgForName = bookImgMap.get(j);
                    bookImgForName.compress(Bitmap.CompressFormat.JPEG,100,out);
                    String path = "users/"+user.getUid()+ "/books/" + bookRef.getId() + "/" + UUID.nameUUIDFromBytes(out.toByteArray());
                    mImageRef = FirebaseStorage.getInstance().getReference().child(path);
                    photourllist.put(path, true);
                    mImageRef.putBytes(out.toByteArray());
                }
            }
        }
        bookToAdd.setBook_photo_list(photourllist);
        bookToAdd.setBook_id(bookRef.getId());
        if(bookToAdd.getBook_thumbnail_url().length()==0 && bookCover!=null){
            ByteArrayOutputStream coverOut = new ByteArrayOutputStream();
            bookCover.compress(Bitmap.CompressFormat.JPEG,100,coverOut);
            coverRef = FirebaseStorage.getInstance().getReference().child(
                    "users/"+user.getUid()+ "/books/"+bookRef.getId()+"/"+UUID.nameUUIDFromBytes(coverOut.toByteArray())
            );
            coverRef.putBytes(coverOut.toByteArray());
        }
        bookRef.set(bookToAdd).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(coverRef!=null
                        && bookToAdd.getBook_thumbnail_url().length()==0
                        && bookCover!=null){
                    coverRef.getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    FirebaseFirestore.getInstance()
                                            .collection("books")
                                            .document(bookToAdd.getBook_id())
                                            .update("book_thumbnail_url",uri.toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    addIndexToAlgolia(bookToAdd);
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            addIndexToAlgolia(bookToAdd);
                        }
                    });
                }
                else{
                    addIndexToAlgolia(bookToAdd);
                }
            }
        });

        users.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User u = documentSnapshot.toObject(User.class);
                if(u==null){
                    return;
                }
                u.getUsr_books().put(bookRef.getId(),true);
                users.document(u.getUsr_id())
                        .update("usr_books",u.getUsr_books());
            }
        });
    }

    /**
     * Questo metodo si occupa di effettuare le operazioni seguenti al risultato prodotto da un Intent.
     * Caso 1 REQUEST_SCANNER:
     *      Si occupa del recupero dei dati dal Bundle fornito dalla classe ScanBarcode.
     *      All'interno del Bundle è presente un oggetto di tipo BookWrapper, da cui è possibile estrarre le informazioni
     *      necessarie per la creazione dell'oggetto di tipo Book.
     * Caso 2 REQUEST_IMAGE_CAPTURE:
     *      Si occupa di gestire la bitmap dello scatto effettuato tramite fotocamera.
     * Caso 3 REQUEST_PICK_IMAGE:
     *      Si occupa di recuperare dalla memoria la Bitmap, tramite la Uri fornita dal metodo getExtra
     */

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
                        for(int i:addBookTextViewIds){
                            findViewById(i).setVisibility(View.VISIBLE);
                        }
                        findViewById(R.id.add_book_picture).setVisibility(View.VISIBLE);
                        Map<String,Boolean> Mauthors = new HashMap<>();
                        Map<String,Boolean> Mcategories = new HashMap<>();
                        for (String key: bookwrap.getAuthors()) {
                            Mauthors.put(key,true);
                        }
                        for (String key: bookwrap.getCategories()) {
                            Mcategories.put(key,true);
                        }
                        bookToAdd = new Book(bookwrap.getISBN(),bookwrap.getTitle(),
                                bookwrap.getPublisher(),bookwrap.getEditionYear(),0,user.getUid(),Mauthors,"",bookwrap.getThumbnail(),
                                Mcategories, MainActivity.thisUser.getUsr_geoPoint(), bookwrap.getPages(),false);
//                        for(int i:addBookTextViewIds){
//                            findViewById(i).setVisibility(View.VISIBLE);
//                        }
                        fillAddBookViews(bookToAdd);
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
                        }
                    } catch (IOException e) {
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
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e){
                            e.printStackTrace();
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
                    }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                }
                break;
            }
        }
    }

    /**
     * Questo metodo si occupa di riempire le ExtendedTextView con i dati del libro.
     * Viene effettuato un controllo sul campo autors per rimuovere le parentesi quadre restituite dal JSON.
     * @param book
     */


    private void fillAddBookViews(Book book){
        ExtendedEditText eet;
        String tmp;
        ImageView iw;
        if(book.getBook_title().length()>0){
            for(int i:addBookEditTextViewIds){
                ExtendedEditText e = findViewById(i);
                e.setText("");
            }
            eet = findViewById(R.id.add_book_extended_edit_Title);
            eet.setText(book.getBook_title());
        }
        else{
            Toast.makeText(this, R.string.add_book_title_not_found, Toast.LENGTH_SHORT).show();
        }
        eet = findViewById(R.id.add_book_extended_edit_text_ISBN);
        eet.setText(book.getBook_ISBN());
        eet = findViewById(R.id.add_book_extended_edit_Author);
        tmp = book.getBook_authors().keySet().toString();
        if(tmp.length()>2){
            eet.setText(tmp.substring(1,tmp.length()-1));
        }
        eet = findViewById(R.id.add_book_extended_edit_text_EditionYear);
        if(book.getBook_editionYear()>0){
            eet.setText(String.valueOf(book.getBook_editionYear()));
        }
        eet = findViewById(R.id.add_book_extended_edit_text_Publisher);
        if(book.getBook_publisher().length()>2){
            eet.setText(book.getBook_publisher());
        }

        eet = findViewById(R.id.add_book_extended_edit_Category);
        tmp = book.getBook_categories().keySet().toString();
        if(tmp.length()>2){
            eet.setText(tmp.substring(1,tmp.length()-1));
        }

        eet = findViewById(R.id.add_book_extended_edit_text_Pages);
        if(book.getBook_pages()!=0){
            eet.setText(String.valueOf(book.getBook_pages()));
        }

        if(book.getBook_thumbnail_url().length()>0){
            iw = findViewById(R.id.add_book_picture);
            GlideApp.with(this).load(book.getBook_thumbnail_url())
                    .placeholder(R.drawable.ic_thumbnail_cover_book).into(iw);
            iw.setClickable(false);
            bookCover = null;
        }
    }

    /**
     * Metodo che si occupa di generare il menu per poter scegliere tra lo scatto di una foto, la scelta
     * di un'immagine dalla galleria o la rimozione di un'immagine
     */
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
        if(ActivityCompat.checkSelfPermission(AddBook.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AddBook.this,new String[]{
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
        if(ActivityCompat.checkSelfPermission(AddBook.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(AddBook.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AddBook.this,new String[]{
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
                bookCover = null;
                iw.setImageResource(R.drawable.ic_insert_photo);
            }
        }
        else if(bookImgMap.isEmpty()==false){
            iw = globalViewImgElement;
            bookImgMap.remove(globalImgPos);
            iw.setImageResource(R.drawable.ic_insert_photo);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //dopo che l'utente ci ha fornito la risposta alla richesta di permessi
        switch (requestCode){
//            case REQUEST_PERMISSION_GALLERY:
//                if(grantResults.length>0) {
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        //choosePhotoFromGallery();
//                    }
//                }
//                break;
//            case REQUEST_PERMISSION_CAMERA:
//                if(grantResults.length>0){
//                    if(grantResults[0]==PackageManager.PERMISSION_GRANTED
//                            && grantResults[1]==PackageManager.PERMISSION_GRANTED){
//                        //choosePhotoFromCamera();
//                    }
//                }
//                break;
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(AddBook.this, ScanBarcode.class);
                        startActivityForResult(intent, REQUEST_SCANNER);
                    }
                } else {
                    Toast.makeText(this, R.string.add_book_permission_camera, Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    //    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        ExtendedEditText eet;
//        for(int i:addBookTextViewIds){
//            eet = findViewById(i);
//            outState.putString(Integer.toString(i),eet.getText().toString());
//        }
////        if(bookImg!=null){
////            outState.putString("bookEditImgPath",editProfile.saveImageToInternalStorage(bookImg,"temp_"+"bookEditImage",this));
////        }
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        ExtendedEditText text;
//        for(int i:addBookTextViewIds){
//            text = findViewById(i);
//            text.setText(savedInstanceState.getString(Integer.toString(i),""));
//        }
////        String bookImgPath = savedInstanceState.getString("bookEditImgPath");
////        if(bookImgPath!=null){
////            File file = new File(bookImgPath);
////            if(file.exists()){
////                bookImg = editProfile.loadImageFromStorage(bookImgPath,R.id.add_book_picture,this);
////                file.delete();
////            }
////        }
//    }

}
