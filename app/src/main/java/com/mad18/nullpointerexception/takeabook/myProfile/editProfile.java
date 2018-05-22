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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.User;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//    SharedPreferences is an object point to a file of key-value pairs, it works for small amount of data or settings
//    that need to be shared inside activities in a project, it is managed by the framework and can be private or shared
//    in this specific case private means that you can use it just in this activity
//    Menu are used to create a set of options available to user
//    these static constants are use to launch and retrieve code in startonactivityresult method when you expects certain behaviours on
//    a called activity and wait for results
//    initially and when user delete user profile photo, we have to provide a default image and value in  order to avoid crash
public class editProfile extends AppCompatActivity {
    private final String TAG = "editProfile";
    private static final int JPEG_COMPRESSION_QUALITY = 90;
    private SharedPreferences sharedPref;
    private int editTextBoxesIds[] = new int[]{R.id.edit_profile_Username,R.id.edit_profile_City,
            R.id.edit_profile_mail,R.id.edit_profile_about};
    private Menu menu;
    private final int REQUEST_PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2;
    private final int REQUEST_PERMISSION_CAMERA = 2, REQUEST_PERMISSION_GALLERY=1;
    private String profileImgName = "profile.jpg";
    private Bitmap profileImg = null;
    private Boolean profileImgChanged;

    //        onCreate function is called when the activity is launched and before it appears
//        You can create a new shared preference file or access an existing one by calling:
//        getSharedPreferences() — Use this if you need multiple shared preference files identified by name,
//        which you specify with the first parameter. You can call this from any Context in your app.
//        this method set the layout with which this activity will appear
//       setSupportActionBar(toolbar); Retrieve a reference to this activity's ActionBar.
//        this method allows user to go back to its logical parent activity
//        ora stiamo settando il nome da visualizzare nella toolbar
//        la toolbar diventa visibile a seguito della definizione del layout
//        all'avvio dell'activity la tastiera del cellulare rimane nascosta
//        se non sono ancora state salvate gli inserimenti dell'utente  vai a recuperare le info tramite la
//        funzione fill userdata
//        vedi sotto cosa fa la funzione chiamata
//        recupera l'elemento view con id = a "edit_profile_personalPhoto" e assegnalo all'oggetto ImageView come variabile
//        java iw
//        settiamo l'elemento foto profilo presente nella view e appena recuperato come cliccabile
//        per interagire con gli elementi dell'xml dobbiamo portarli in java come variabili oggetto e operare su di esse
//        mettiamo una sentinella pronta a ricevere i click sulla foto profilo e una volta cliccata la foto profilo,
//        con questa lambda expression vogliamo dire di avviare la funzione selectUserImg() più sotto nel codice
//        vengono qui inizializzate le animazioni per le transizioni da queste activity ad un'altra a seguito della chiamata startactivity
//        o finish.  R.anim.slide_in_right è il primo campo e quindi l'animazione di ingresso in questa activity
//        R.anim.slide_out_left è il secondo campo e indica che animazione utilizzare quando ce ne andiamo da questa activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        setContentView(R.layout.edit_profile);
        Toolbar toolbar = findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_edit);
        toolbar.setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(savedInstanceState == null){
            fillUserData();
        }
        ImageView iw = findViewById(R.id.edit_profile_personalPhoto);
        iw.setClickable(true);
        iw.setOnClickListener(view -> selectUserImg());
        profileImgChanged = false;
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

//        la prima volta che il menu viene creato viene invocato questo metodo, per successive modifiche chiama onprepareotionsmenu
//        assegnamo a menu il valore richiesto
//        creiamo l'oggetto inflater per poter gonfiare e dare vita alle cose
//        gonfia il menu e rendilo vivo e utilizzabile dall'utente sfruttando l'inflater appena creato
//        ritornando true indichiamo che il menu deve essere mostrato
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile_toolbar, menu);
        return true;
    }

    //        quando viene selezionato un elemento del menu viene chiamato questo metodo: onOptionsItemSelected()
//                se viene selezionato il pulsante save
//                chimata al metodo UserEditData che vedi sotto
//                chiudi questa activity
//                animazioni vedi sopra, le ho già commentate prima
//                se vogliamo tornare indietro non salviamo i dati inseriti
//                chiudi questa activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.edit_profile_action_save:
                storeUserEditData();
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

    //        con l'editor non andiamo ad agire sulle shared preferences selezionate fino a che non verrà chiamato apply
//                fino ad allora le modifiche restano in un file di batch tempoaneo e non vanno a modificare le sharedPref
//                originali
//        prima interazione con firebase per recuperare dal database l'utente che sta usando l'app
//        StorageReference è un riferimento a una risorsa in cloud di google che in questo caso recuperiamo da firebase nel
//                sottoalbero users/images/
//        recuperiamo il database da firestore dentro firebase
//        firestore è un nuovo servizio cloud che opera come firebase ma è più prestante e di facile utilizzo
//        stiamo chiedendo a firestore di ritornarci la collezione di utenti
//            la shareduserdatakeys è inizializzata nella showprofile e contiene alcuni campi di testo che si trovano
//            nelle sharedPreferences //            è un vettore di stringhe
//            qui stiamo salvando nelle shared preferences temporanea i campi inseriti dall'utente negli edittext
//                se non è ancora presente la mail dell'utente
//                  allora inserisci nella mappa ciò che è stato inserito in questo form
//            se non è ancora stata salvata un'immagine profilo
//            prendi l'immagine di default
//                se il percorso per fare lo storage dell'immagine non è vuoto allora assegna il nostro path
    private void storeUserEditData(){
        EditText text;
        String s; File file;
        SharedPreferences.Editor editor = sharedPref.edit();
        int i=0;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference().child("users/images/"+user.getUid());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        User u = MainActivity.thisUser;
        Map<String,String> user_data = new HashMap<>();
        for(String x: showProfile.sharedUserDataKeys){
            text = findViewById(editTextBoxesIds[i++]);
            editor.putString(x,text.getText().toString());
            if(x.equals(showProfile.sharedUserDataKeys[2])==false){ //Not email
                user_data.put(x,text.getText().toString());
            }
        }
        if(profileImgChanged==true){
            if(profileImg!=null){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                profileImg.compress(Bitmap.CompressFormat.JPEG,JPEG_COMPRESSION_QUALITY,out);
                mImageRef = FirebaseStorage.getInstance().getReference().child("users/"+user.getUid()+"/profileImage/"+ UUID.nameUUIDFromBytes(out.toByteArray()));
                editor.putString(profileImgName,saveImageToInternalStorage(profileImg,profileImgName,this));
                mImageRef.putBytes(out.toByteArray());
                String newImgPath = mImageRef.getPath();
                if(u.getProfileImgStoragePath().length()>0) {
                    StorageReference oldImgRef = FirebaseStorage.getInstance().getReference().child(u.getProfileImgStoragePath());
                    oldImgRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Map<String,String> x = new HashMap<>();
                            x.put("profileImgStoragePath",newImgPath);
                            users.document(user.getUid()).set(x, SetOptions.merge());
                        }
                    });
                }
                else{
                    Map<String,String> x = new HashMap<>();
                    x.put("profileImgStoragePath",newImgPath);
                    users.document(user.getUid()).set(x,SetOptions.merge());
                }
            }
            else{
                s = sharedPref.getString(profileImgName,"");
                if(s.length()>0){
                    file = new File(s);
                    if(file.exists()){
                        file.delete();
                        StorageReference oldImg = FirebaseStorage.getInstance().getReference().child(u.getProfileImgStoragePath());
                        oldImg.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Map<String,String> x = new HashMap<>();
                                x.put("profileImgStoragePath","");
                                users.document(user.getUid()).set(x, SetOptions.merge());
                            }
                        });
                    }
                }
                editor.putString(profileImgName,"");
            }
        }
        editor.apply();
        users.document(user.getUid()).set(user_data, SetOptions.merge());
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    // call superclass to save any view hierarchy
    //salva tutti i campi inseriti dall'utente più il path della immagine profilo
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        EditText text;
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            outState.putString(Integer.toString(i),text.getText().toString());
        }
        if(profileImg!=null){
            outState.putString("profileImgPath", saveImageToInternalStorage(profileImg,"temp_"+profileImgName,this));
        }
    }

    // This callback is called only when there is a saved instance that is previously saved by using
// onSaveInstanceState(). We restore some state in onCreate(), while we can optionally restore
// other state here, possibly usable after onStart() has commpleted.
// The savedInstanceState Bundle is same as the one used in onCreate().
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
                profileImg = loadImageFromStorage(file.getAbsolutePath(), R.id.edit_profile_personalPhoto,this);
                file.delete();
            }
        }
    }

    //come si può dedurre dal titolo della funzione. stiamo recuperando le info dell'utente dalle shared pred
    // e le stiamo riposizionando nei field della view edit_profile.xml
    // bisogna sempre fare i check se i campi esistono
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
            profileImg = loadImageFromStorage(sharedPref.getString(profileImgName,""),R.id.edit_profile_personalPhoto,this);
        }
        else{
            ImageView iw = findViewById(R.id.edit_profile_personalPhoto);
            iw.setImageResource(R.drawable.ic_account_circle_white_48px);
        }
    }

    //in risposta ad una activity che è stata invocata con startactivityforresult
    // dobbiamo prima capire da quale activity stiamo tornando e con quale codice
    //l'immagine è stata presa dallo storage del telefono -> galleria
    //immagine recuperata tramite fotocampera
    //notare come siano state messe nel bundle "data" dall'altra activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        ImageView iw;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case REQUEST_PICK_IMAGE:

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
                            profileImgChanged = true;
                        }
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null) {
                        profileImg = (Bitmap) data.getExtras().get("data");
                        iw = findViewById(R.id.edit_profile_personalPhoto);
                        if(iw!=null && profileImg != null) {
                            iw.setImageBitmap(profileImg);
                            profileImgChanged = true;
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
            profileImgChanged = true;
        }
    }

    //salvare immagine profilo nello storage del telefono per non doverla recuperare da firebase ogni volta
    // path to /data/data/appname/app_data/imageDir internal
    //Environment.getExternalStorageDirectory(); sd
    // Create imageDir
    public static String saveImageToInternalStorage(Bitmap bitmapImage, String filename, Context context){
        if(bitmapImage==null){
            return null;
        }
        ContextWrapper cw = new ContextWrapper(context);

        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File file = new File(directory,filename);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            if(bitmapImage.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESSION_QUALITY, out)==false){
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


