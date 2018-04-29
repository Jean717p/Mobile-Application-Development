package com.mad18.nullpointerexception.takeabook.addBook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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


public class AddBook extends AppCompatActivity {
    private final String TAG = "AddBook";
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private final int REQUEST_PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2, REQUEST_SCANNER=3;;
    private final int REQUEST_PERMISSION_CAMERA = 2, REQUEST_PERMISSION_GALLERY=1;
    private final int addBookTextViewIds[] = {R.id.add_book_text_field_Title,R.id.add_book_text_field_Author,
            R.id.add_book_text_field_EditionYear,R.id.add_book_text_field_Publisher,
            R.id.add_book_text_field_ISBN,R.id.add_book_text_field_Description};
    private Book bookToAdd;
    private View mClss;
    private Toolbar toolbar;
    private Menu menu;
    private Bitmap bookImg;
    private Spinner staticSpinner;

    private android.support.design.widget.FloatingActionButton img_fab;
    private FirebaseUser user;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.add_book);
        toolbar = (Toolbar) findViewById(R.id.add_book_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Add a book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button scan = (Button)findViewById(R.id.read_barcode);
        Button search = (Button)findViewById(R.id.add_book_read_ISBN); //new search
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        ImageView iw =  findViewById(R.id.add_book_picture);
        iw.setClickable(true);
        iw.setOnClickListener(view -> selectBookImg());
        //get the spinner from the xml.
        staticSpinner = findViewById(R.id.add_book_spinner_book_cond);
        //create a list of items for the spinner.
        String[] items = new String[]{"condizioni del libro", "Ottime", "Buone", "Scarse"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.book_conditions,
                        android.R.layout.simple_dropdown_item_1line);
        staticSpinner.setAdapter(staticAdapter);
        user = FirebaseAuth.getInstance().getCurrentUser();
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
       if(state == null){
            for(int i:addBookTextViewIds){
                //(i).setVisibility(View.INVISIBLE);
                findViewById(i).setEnabled(false);
            }
//            findViewById(R.id.add_book_text_field_ISBN).setVisibility(View.VISIBLE);
//            findViewById(R.id.add_book_picture).setVisibility(View.INVISIBLE);
            findViewById(R.id.add_book_text_field_ISBN).setEnabled(true);
            bookImg = null;
        }

        search.setOnClickListener(view -> { //new search
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
                Toast toast = Toast.makeText(getApplicationContext(), "Exactly 13 characters for ISBN", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }


    /**
     * Crea il menu (toolbar) partendo dall'xml add_book_menu.xml
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_book_menu, menu);
        return true;
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
        /*
        ExtendedEditText eet;
        for(int i:addBookTextViewIds){
            eet = findViewById(i);
            outState.putString(Integer.toString(i),eet.getText().toString());
        }
        if(bookImg!=null){
            outState.putString("bookEditImgPath",editProfile.saveImageToInternalStorage(bookImg,"temp_"+"bookEditImage",this));
        }
        */
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

    /**
     * Metodo che si occupa di inserire nel database Firebase i dati relativi al libro aggiunto dall'utente.
     * Setta tutti i parametri ricevuti dalle edit text in un oggetto bookToAdd della classe book, che verrà poi inserito
     * su Firebase.
     * Viene gestito anche il caricamento dell'immagine su Firebase, inserita nella cartella images/books utilizzando un nome
     * dato dalla chiave univoca ISBN + userID.
     * Viene infine aggiornato il campo books dell'oggetto user del database.
     */

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
        if(bookToAdd==null){
            bookToAdd = new Book();
        }
        eet = findViewById(R.id.add_book_extended_edit_text_ISBN);
        bookToAdd.setBook_ISBN(eet.getText().toString());
        eet = findViewById(R.id.add_book_extended_edit_text_Description);
        bookToAdd.setDescription(eet.getText().toString());
        eet = findViewById(R.id.add_book_extended_edit_Author);
        String tmp[] = eet.getText().toString().split(","); //è la virgola???
        for(int i=0;i<tmp.length;i++){
            authors.put(tmp[i],true);
        }
        bookToAdd.setBook_condition(staticSpinner.getSelectedItemPosition());
        bookToAdd.setBook_authors(authors);
        eet = findViewById(R.id.add_book_extended_edit_text_EditionYear);
        bookToAdd.setBook_editionYear(Integer.parseInt(eet.getText().toString()));
        eet = findViewById(R.id.add_book_extended_edit_Title);
        bookToAdd.setBook_title(eet.getText().toString());
        if(bookToAdd.getBook_title().length()==0){
            return;
        }
        eet = findViewById(R.id.add_book_extended_edit_text_Publisher);
        bookToAdd.setBook_publisher(eet.getText().toString());
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
        books.document(bookToAdd.getBook_ISBN() + user.getUid()).set(bookToAdd);
        //Aggiunta libro all'elenco dell'utente
        users.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User u = documentSnapshot.toObject(User.class);
                u.getUsr_books().put(bookToAdd.getBook_ISBN() + user.getUid(),true);
                users.document(user.getUid()).set(u);
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
                                bookwrap.getPublisher(),bookwrap.getEditionYear(),0,user.getUid(),Mauthors,"");
//                        for(int i:addBookTextViewIds){
//                            findViewById(i).setVisibility(View.VISIBLE);
//                        }
                        fillAddBookViews(bookToAdd);
                        findViewById(R.id.add_book_text_field_Description).setEnabled(true);
                        //findViewById(R.id.add_book_picture).setVisibility(View.VISIBLE);

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

    /**
     * Questo metodo si occupa di riempire le ExtendedTextView con i dati del libro.
     * Viene effettuato un controllo sul campo autors per rimuovere le parentesi quadre restituite dal JSON.
     * @param book
     */

    private void fillAddBookViews(Book book){
        ExtendedEditText eet;
        String tmp;
        eet = findViewById(R.id.add_book_extended_edit_text_ISBN);
        eet.setText(book.getBook_ISBN());
        eet = findViewById(R.id.add_book_extended_edit_Author);
        tmp = book.getBook_authors().keySet().toString();
        if(tmp.length()>2){
            eet.setText(tmp.substring(1,tmp.length()-1));
        }
        eet = findViewById(R.id.add_book_extended_edit_text_EditionYear);
        if(book.getBook_editionYear()!=-1){
            eet.setText(String.valueOf(book.getBook_editionYear()));
        }
        eet = findViewById(R.id.add_book_extended_edit_Title);
        eet.setText(book.getBook_title());
        eet = findViewById(R.id.add_book_extended_edit_text_Publisher);
        eet.setText(book.getBook_publisher());
    }

    /**
     * Metodo che si occupa di generare il menu per poter scegliere tra lo scatto di una foto, la scelta
     * di un'immagine dalla galleria o la rimozione di un'immagine
     */
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

    /**
     * Crea l'intent per permettere la scelta di una foto dalla galleria, controllando i permessi necessari.
     */

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

    /**
     * Crea l'intent per lo scatto di un'immagine dalla fotocamera, controllando i permessi necessari
     */
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

    /**
     * Si occupa della rimozione dell'immagine dalla ImageView in cui è contenuta.
     */

    private void removeBookImg(){
        ImageView iw;
        if(bookImg!=null){
            iw = findViewById(R.id.add_book_picture);
            bookImg = null;
            iw.setImageResource(R.drawable.ic_if_internt_web_technology_05_274892);
        }
    }

    /**
     * Si occupa di gestire il risultato della richiesta dei permessi.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

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
