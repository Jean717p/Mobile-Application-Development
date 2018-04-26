package com.mad18.nullpointerexception.takeabook.addBook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.zxing.Result;
import com.mad18.nullpointerexception.takeabook.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;



public class ScanBarcode extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }
    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }
    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }
    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        //Log.v(TAG, rawResult.getText()); // Prints scan results
        //Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Log.d("barcode format", rawResult.getBarcodeFormat().toString());
        Log.d("print scan result", rawResult.getText());
        Intent intent = new Intent();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                JsonParser jsonParser = new JsonParser();
                JSONObject jsonObject = jsonParser.makeHttpRequest(
                        "https://www.googleapis.com/books/v1/volumes?q=isbn:" + rawResult.getText(),
                        "GET", new HashMap<String, String>());
                //String totalItems = jsonObject.getString("totalItems");
                //String id = jsonObject.getJSONArray("items").getJSONObject(0).getString("id");
                //Log.d("title",totalItems);
                //Log.d("title2",id);
                String ISBN = rawResult.getText();
                String title = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("title");
                //Log.d("title3",title);
                //String Jauthors = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONArray("authors").getString(0);
                JSONArray Jauthors = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONArray("authors");
               // Log.d("title7", Jauthors);
                Map<String, Boolean> authors = new HashMap<>();
                for(int i=0; i<Jauthors.length();i++) {
                    authors.put(Jauthors.getString(i),true);
                }
                String publisher = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("publisher");
                //Log.d("title4",publisher);
                String SeditionYear = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("publishedDate");
                Integer editionYear = Integer.parseInt(SeditionYear);
                //Log.d("title5",editionYear);
                //intent.putExtra("bookinfo", new Book(totalItems, id));
                Book book = new Book(ISBN,title,authors,publisher,editionYear);
                BookWrapper bookWrapper = new BookWrapper(book);
                intent.putExtra("bookinfo", bookWrapper);
                setResult(RESULT_OK,intent);
                finish();
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }).start();
        //Use JSONParser to download the JSON

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);




    }

}

class BookWrapper implements Parcelable{
    private Book book;

    BookWrapper(Book book){
        this.book=book;
    }

    protected BookWrapper(Parcel in) {
        new BookWrapper(in);
    }

    public static final Creator<BookWrapper> CREATOR = new Creator<BookWrapper>() {
        @Override
        public BookWrapper createFromParcel(Parcel in) {
            return new BookWrapper(in);
        }

        @Override
        public BookWrapper[] newArray(int size) {
            return new BookWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeValue(this.book);
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}