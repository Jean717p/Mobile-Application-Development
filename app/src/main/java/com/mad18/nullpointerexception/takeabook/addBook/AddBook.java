package com.mad18.nullpointerexception.takeabook.addBook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mad18.nullpointerexception.takeabook.R;

import org.json.JSONObject;

import java.io.Serializable;

public class AddBook extends AppCompatActivity {
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private View mClss;
    private final int REQUEST_SCANNER=1;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.add_book);

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
                        startActivityForResult(intent, 1);
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


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_SCANNER){
                if(data!=null) {
                    Bundle bundle = data.getExtras();
                    BookWrapper bookinfo = (BookWrapper) bundle.getParcelable("bookinfo");
                    Book book = bookinfo.getBook();
                    Log.d("info", book.getISBN());
                    //TextView totalItems = (TextView)findViewById(R.id.add_book_title);
                    //totalItems.setText(book.getTitle());
                    Log.d("info", book.getTitle());
                }
            }
        }
    }
}
