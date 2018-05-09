package com.mad18.nullpointerexception.takeabook.searchBook;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.mad18.nullpointerexception.takeabook.R;

public class SearchBook_search extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View myFragmentView;
    private int mParam1;
    private String mParam2;

    public SearchBook_search() {
        // Required empty public constructor
    }

    public static SearchBook_search newInstance(int param1, String param2) {
        SearchBook_search fragment = new SearchBook_search();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragmentView = inflater.inflate(R.layout.fragment_search_book_search, container, false);
        String searchBase = getActivity().getIntent().getStringExtra("action");
        EditText text = myFragmentView.findViewById(R.id.search_book_edit_text);
        Button searchButton = myFragmentView.findViewById(R.id.search_book_button);
        switch (searchBase){
            case "Title":
                text.setHint(getString(R.string.search_book_text_title));
                //LinkedList title = (LinkedList) getBooksFromTitle(text.getText().toString());
                break;
            case "Author":
                text.setHint(getString(R.string.search_book_text_author));

                break;
            case "ISBN":
                text.setHint(getString(R.string.search_book_text_ISBN));
                text.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
        }
        searchButton.setOnClickListener(view -> {
            SearchBook s = (SearchBook) getActivity();
            s.searchForBook(searchBase);});

        return myFragmentView;
    }

}
