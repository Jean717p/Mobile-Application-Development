package com.mad18.nullpointerexception.takeabook.searchBook;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Book_generic_info extends AppCompatActivity {
    private Book book;
    private AppCompatActivity myActivity;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_generic_info);
        Toolbar toolbar = findViewById(R.id.generic_info_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myActivity = this;
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            BookWrapper bw = extras.getParcelable("bookToShow");
            if(bw!=null){
                book = new Book(bw);
            }
            else{
                finish();
            }
        }
        else{
            finish();
        }
        //toolbar.setTitle(book.getBook_title());
        this.setTitle(book.getBook_title());
        ImageView iw = findViewById(R.id.generic_info_book_image);
        if(book.getBook_thumbnail_url().length()>0){
            GlideApp.with(this).load(book.getBook_thumbnail_url())
                    .placeholder(R.drawable.ic_thumbnail_cover_book)
                    .into(iw);
        }
        TextView title = findViewById(R.id.generic_info_book_title);
        title.setText(book.getBook_title());
        TextView author = findViewById(R.id.generic_info_book_author);
        TextView numpage = findViewById(R.id.generic_info_book_num_pages);
        if(book.getBook_pages()>0){
            numpage.setText(Integer.toString(book.getBook_pages()));
        }
        else{
            numpage.setText(R.string.add_book_info_not_available);
        }
        String tmp = book.getBook_authors().keySet().toString();
        if(tmp.length()>2){
            author.setText(tmp.substring(1,tmp.length()-1));
        }
        //numpage.setText(Integer.toString(book.getBook_pages));
        //progress bar on plot visible
        //new DownloadDescription().doInBackground();
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DownloadDescription extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            JsonParser jsonParser = new JsonParser();
            try {
                String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + book.getBook_ISBN()+
                        "&fields=items(volumeInfo/description)";
                JSONObject jsonObject = jsonParser.makeHttpRequest(
                        url,
                        "GET", new HashMap<String, String>());
                String description;
                if(jsonObject.has("items")){
                    JSONObject tmp =  jsonObject.getJSONArray("items").getJSONObject(0);
                    if(tmp.has("volumeInfo")){
                        tmp = tmp.getJSONObject("volumeInfo");
                        if(tmp.has("description")){
                            description = tmp.getString("title");
                            TextView textView = myActivity.findViewById(R.id.generic_info_book_plot);
                            textView.setText(description);
                            //progress bar on plot invisible
                        }
                    }
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return "ok";
        }
    }
}
