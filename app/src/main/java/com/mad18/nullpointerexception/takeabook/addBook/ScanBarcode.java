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
import java.util.LinkedList;
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
                String title="";
                List<String> authors=new LinkedList<>();
                String publisher="";
                int editionYear=-1;
                if(jsonObject.has("items")){
                    JSONObject tmp =  jsonObject.getJSONArray("items").getJSONObject(0);
                    if(tmp.has("volumeInfo")){
                        tmp = tmp.getJSONObject("volumeInfo");
                        if(tmp.has("title")){
                            title = tmp.getString("title");
                        }
                        if(tmp.has("authors")){
                            JSONArray Jauthors = tmp.getJSONArray("authors");
                            authors = new LinkedList<>();
                            for(int i=0; i<Jauthors.length();i++) {
                                authors.add(Jauthors.getString(i));
                            }

                        }
                        if(tmp.has("publisher")){
                            publisher =tmp.getString("publisher");
                        }
                        if(tmp.has("publishedDate")){
                            String SeditionYear = tmp.getString("publishedDate");
                            editionYear = Integer.parseInt(SeditionYear);
                        }
                    }
                }
                BookWrapper bookWrapper = new BookWrapper(ISBN,title,authors, publisher,editionYear);
                intent.putExtra("bookinfo", bookWrapper);
                setResult(RESULT_OK,intent);
                finish();
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }).start();

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

}

