package com.mad18.nullpointerexception.takeabook.myProfile;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class editProfile extends AppCompatActivity {
    private final String TAG = "editProfile";
    private SharedPreferences sharedPref;
    //    SharedPreferences is an object point to a file of key-value pairs, it works for small amount of data or settings
//    that need to be shared inside activities in a project, it is managed by the framework and can be private or shared
//    in this specific case private means that you can use it just in this activity
    private int editTextBoxesIds[] = new int[]{R.id.edit_profile_Username,R.id.edit_profile_City,
            R.id.edit_profile_mail,R.id.edit_profile_about};
    //    this vector of R.id is later used to scan alll editable field of the layout inside a for loop to avoid code redundancy
    private Menu menu;
    //    Menu are used to create a set of options available to user
    private final int REQUEST_PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2;
    private final int REQUEST_PERMISSION_CAMERA = 2, REQUEST_PERMISSION_GALLERY=1;
    //    these static constants are use to launch and retrieve code in startonactivityresult method when you expects certain behaviours on
//    a called activity and wait for results
    private String profileImgName = "profile.jpg";
    private Bitmap profileImg = null;
    //initially and when user delete user profile photo, we have to provide a default image and value in  order to avoid crash

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        onCreate function is called when the activity is launched and before it appears
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
//        You can create a new shared preference file or access an existing one by calling:
//        getSharedPreferences() — Use this if you need multiple shared preference files identified by name,
//        which you specify with the first parameter. You can call this from any Context in your app.
        setContentView(R.layout.edit_profile);
//        this method set the layout with which this activity will appear
        Toolbar toolbar = findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
//      setSupportActionBar(toolbar); Retrieve a reference to this activity's ActionBar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        this method allows user to go back to its logical parent activity
        setTitle(R.string.title_activity_edit);
//ora stiamo settando il nome da visualizzare nella toolbar
        toolbar.setVisibility(View.VISIBLE);
//        la toolbar diventa visibile a seguito della definizione del layout
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        all'avvio dell'activity la tastiera del cellulare rimane nascosta
        if(savedInstanceState == null){
//            se non sono ancora state salvate gli inserimenti dell'utente  vai a recuperare le info tramite la
//                    funzione fill userdata
            fillUserData();
//            vedi sotto cosa fa la funzione chiamata
        }
        ImageView iw = findViewById(R.id.edit_profile_personalPhoto);
//        recupera l'elemento view con id = a "edit_profile_personalPhoto" e assegnalo all'oggetto ImageView come variabile
//        java iw
        iw.setClickable(true);
//        settiamo l'elemento foto profilo presente nella view e appena recuperato come cliccabile
//        per interagire con gli elementi dell'xml dobbiamo portarli in java come variabili oggetto e operare su di esse
        iw.setOnClickListener(view -> selectUserImg());
//        mettiamo una sentinella pronta a ricevere i click sulla foto profilo e una volta cliccata la foto profilo,
//        con questa lambda expression vogliamo dire di avviare la funzione selectUserImg() più sotto nel codice
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
//        vengono qui inizializzate le animazioni per le transizioni da queste activity ad un'altra a seguito della chiamata startactivity
//                o finish.  R.anim.slide_in_right è il primo campo e quindi l'animazione di ingresso in questa activity
//        R.anim.slide_out_left è il secondo campo e indica che animazione utilizzare quando ce ne andiamo da questa activity
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        la prima volta che il menu viene creato viene invocato questo metodo, per successive modifiche chiama onprepareotionsmenu
        this.menu = menu;
//        assegnamo a menu il valore richiesto
        MenuInflater inflater = getMenuInflater();
//        creiamo l'oggetto inflater per poter gonfiare e dare vita alle cose
        inflater.inflate(R.menu.edit_profile_toolbar, menu);
//        gonfia il menu e rendilo vivo e utilizzabile dall'utente sfruttando l'inflater appena creato
        return true;
//        ritornando true indichiamo che il menu deve essere mostrato
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
//        quando viene selezionato un elemento del menu viene chiamato questo metodo: onOptionsItemSelected()
        switch (item.getItemId()){
            case R.id.edit_profile_action_save:
//                se viene selezionato il pulsante save
                storeUserEditData();
//                chimata al metodo UserEditData che vedi sotto
                finish();
//                chiudi questa activity
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
//                animazioni vedi sopra, le ho già commentate prima
                return true;
            case android.R.id.home:
//                se vogliamo tornare indietro non salviamo i dati inseriti
                finish();
//                chiudi questa activity
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
//                come sopra
                return true;
        }
        return super.onOptionsItemSelected(item);
//        questo sarebbe il default case nel momento in cui l'utente non selezione nessun item
    }

    private void storeUserEditData(){
        EditText text;
        String s; File file;
        SharedPreferences.Editor editor = sharedPref.edit();
//        con l'editor non andiamo ad agire sulle shared preferences selezionate fino a che non verrà chiamato apply
//                fino ad allora le modifiche restano in un file di batch tempoaneo e non vanno a modificare le sharedPref
//                originali
        int i=0;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        prima interazione con firebase per recuperare dal database l'utente che sta usando l'app
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference().child("users/images/"+user.getUid());
//        StorageReference è un riferimento a una risorsa in cloud di google che in questo caso recuperiamo da firebase nel
//                sottoalbero users/images/
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        recuperiamo il database da firestore dentro firebase
//        firestore è un nuovo servizio cloud che opera come firebase ma è più prestante e di facile utilizzo
        CollectionReference users = db.collection("users");
//        stiamo chiedendo a firestore di ritornarci la collezione di utenti
        String profileImgPath;
        Map<String,String> user_data = new HashMap<>();

        for(String x: showProfile.sharedUserDataKeys){
//            la shareduserdatakeys è inizializzata nella showprofile e contiene alcuni campi di testo che si trovano
//            nelle sharedPreferences //            è un vettore di stringhe
            text = findViewById(editTextBoxesIds[i++]);
            editor.putString(x,text.getText().toString());
//            qui stiamo salvando nelle shared preferences temporanea i campi inseriti dall'utente negli edittext
            if(x.equals(showProfile.sharedUserDataKeys[2])==false){ //Not email
//                se non è ancora presente la mail dell'utente
                user_data.put(x,text.getText().toString());
//                  allora inserisci nella mappa ciò che è stato inserito in questo form
            }
        }
        if(profileImg!=null){
//            se non è ancora stata salvata un'immagine profilo
            profileImgPath = saveImageToInternalStorage(profileImg,profileImgName,this);
//            prendi l'immagine di default
            if(profileImgPath!=null){
//                se il percorso per fare lo storage dell'immagine non è vuoto allora assegna il nostro path
                editor.putString(profileImgName,profileImgPath);
                Uri profileImgUri = Uri.fromFile(new File(profileImgPath));
                mImageRef.putFile(profileImgUri);
//                e restitusci l'uri dello storage dell'imagine nel mImageRef
            }
        }
        else{
            s = sharedPref.getString(profileImgName,"");
            if(s.length()>0){
//                altrimenti controllo se il path esiste
                file = new File(s);
                if(file.exists()){
                    mImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //se tutto ha successo cancello il file temporaneo
                            file.delete();
                        }
                    });
                }
            }
            editor.putString(profileImgName,"");
            //aggiorna il pathe dell'immagine
        }
        editor.apply();
        // aggiorna le shared preferences perchè tutto è andato a buon fine
        // quind prendi dal file batch e scrivi su sharedpreferences
        users.document(user.getUid()).set(user_data, SetOptions.merge());
        //fai l'insert on update delle info inserite dall'utente sul db firebase
        //questa istruzione
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        // invoked when the activity may be temporarily destroyed, save the instance state here
        super.onSaveInstanceState(outState);
        // call superclass to save any view hierarchy
        EditText text;
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            outState.putString(Integer.toString(i),text.getText().toString());
        }
        //salva tutti i campi inseriti dall'utente più il path della immagine profilo
        if(profileImg!=null){
            outState.putString("profileImgPath", saveImageToInternalStorage(profileImg,"temp_"+profileImgName,this));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // This callback is called only when there is a saved instance that is previously saved by using
// onSaveInstanceState(). We restore some state in onCreate(), while we can optionally restore
// other state here, possibly usable after onStart() has completed.
// The savedInstanceState Bundle is same as the one used in onCreate().
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
                profileImg = loadImageFromStorage(file.getAbsolutePath(), R.id.edit_profile_personalPhoto,this);
                file.delete();
            }
        }
    }

    private void fillUserData(){
        //come si può dedurre dal titolo della funzione. stiamo recuperando le info dell'utente dalle shared pred
        // e le stiamo riposizionando nei field della view edit_profile.xml
        // bisogna sempre fare i check se i campi esistono
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
            profileImg = loadImageFromStorage(sharedPref.getString(profileImgName,""),R.id.edit_profile_personalPhoto,this);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //in risposta ad una activity che è stata invocata con startactivityforresult
        super.onActivityResult(requestCode,resultCode,data);
        ImageView iw;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // dobbiamo prima capire da quale activity stiamo tornando e con quale codice
                case REQUEST_PICK_IMAGE:
                    //l'immagine è stata presa dallo storage del telefono -> galleria
                    if (data != null) {
                        Uri selectedMediaUri = data.getData();
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
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    //immagine recuperata tramite fotocampera
                    //notare come siano state messe nel bundle "data" dall'altra activity
                    if (data != null) {
                        profileImg = (Bitmap) data.getExtras().get("data");
                        iw = findViewById(R.id.edit_profile_personalPhoto);
                        if(iw!=null && profileImg != null) {
                            iw.setImageBitmap(profileImg);
                        }
                    }
                    break;
            }
        }
    }

    private void selectUserImg(){
        //dialog box che compare quando vogliamo inserire l'immagine profilo
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        //pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                getString(R.string.photo_from_gallery),
                getString(R.string.photo_from_camera),
                getString(R.string.photo_remove) };
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        //a seconda della scelta utente, invochiamo la funzione corrispondente
                        case 0:
                            choosePhotoFromGallery();
                            break;
                        case 1:
                            choosePhotoFromCamera();
                            break;
                        case 2:
                            removeUserImg();
                            break;
                    }
                });
        pictureDialog.show();
    }


    public void choosePhotoFromGallery() {
        //bisogna fare un check sui permessi forniti dall'utente alla nostra app
        if(ActivityCompat.checkSelfPermission(editProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(editProfile.this,new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE }
                    ,REQUEST_PERMISSION_GALLERY);
            return;
        }
        //ora si fa partire l'intent per recuperare i file dalla galleria
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_PICK_IMAGE);
    }

    private void choosePhotoFromCamera() {
        //come sopra ma per usare la fotocamera
        if(ActivityCompat.checkSelfPermission(editProfile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(editProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(editProfile.this,new String[]{
                            Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE }
                    ,REQUEST_PERMISSION_CAMERA);
            return;
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //invochiamo la fotocamera
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void removeUserImg(){
        //se l'utente volesse togliere la propria immagine profilo, ripristiniamo quella di default
        ImageView iw = findViewById(R.id.edit_profile_personalPhoto);
        if(profileImg!=null){
            iw.setImageResource(R.drawable.ic_account_circle_white_48px);
            profileImg = null;
        }
    }

    public static String saveImageToInternalStorage(Bitmap bitmapImage, String filename, Context context){
        //salvare immagine profilo nello storage del telefono per non doverla recuperare da firebase ogni volta
        if(bitmapImage==null){
            return null;
        }
        ContextWrapper cw = new ContextWrapper(context);
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

    public static Bitmap loadImageFromStorage(String path,int id,AppCompatActivity activity) {
        //come sopra ma per salvare immagine nello storage
        if(path==null){
            return null;
        }
        Bitmap b = null;
        File file = new File(path);
        ImageView img = (ImageView) activity.findViewById(id);
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
        //dopo che l'utente ci ha fornito la risposta alla richesta di permessi
        switch (requestCode){
            case REQUEST_PERMISSION_GALLERY:
                if(grantResults.length>0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        choosePhotoFromGallery();
                    }
                }
                break;
            case REQUEST_PERMISSION_CAMERA:
                if(grantResults.length>0){
                    if(grantResults[0]==PackageManager.PERMISSION_GRANTED
                            && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                        choosePhotoFromCamera();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //dove andare quando l'utente preme indietro
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}


