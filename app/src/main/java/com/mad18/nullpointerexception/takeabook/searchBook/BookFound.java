package com.mad18.nullpointerexception.takeabook.searchBook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.mad18.nullpointerexception.takeabook.R;

public class BookFound extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_search_on_map_card_view);
        RecyclerView rv = (RecyclerView)findViewById(R.id.book_found_rv);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
    }
}
