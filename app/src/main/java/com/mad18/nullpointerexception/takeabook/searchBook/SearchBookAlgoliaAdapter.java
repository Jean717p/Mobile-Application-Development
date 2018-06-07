package com.mad18.nullpointerexception.takeabook.searchBook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mad18.nullpointerexception.takeabook.GlideApp;

import com.mad18.nullpointerexception.takeabook.R;

import java.util.ArrayList;

public class SearchBookAlgoliaAdapter extends RecyclerView.Adapter<SearchBookAlgoliaAdapter.ViewHolder> {

    private Context myContext;
    private ArrayList<SearchBookAlgoliaItem> booksToShow;
    private final OnItemClickListener myListener;

    public SearchBookAlgoliaAdapter(ArrayList<SearchBookAlgoliaItem> arrayList, Context context, SearchBookAlgoliaAdapter.OnItemClickListener listener){
        booksToShow = arrayList;
        myContext = context;
        myListener = listener;
    }

    @NonNull
    @Override
    public SearchBookAlgoliaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_search_book_algolia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchBookAlgoliaAdapter.ViewHolder holder, int position) {
        holder.tv_title.setText(booksToShow.get(position).getTitle());
        holder.tv_author.setText(booksToShow.get(position).getAuthor());
        GlideApp.with(myContext).load(booksToShow.get(position).getThumbnailURL())
                .placeholder(R.drawable.ic_thumbnail_cover_book)
                .into(holder.iw_thumbnail);
        holder.bind(booksToShow.get(position), myListener);

    }

    @Override
    public int getItemCount() {
        return booksToShow.size();
    }

    public void setData(ArrayList<SearchBookAlgoliaItem> newData){
        booksToShow = newData;
    }

    public interface OnItemClickListener {
        void onItemClick(SearchBookAlgoliaItem item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_title, tv_author;
        private ImageView iw_thumbnail;
        public ViewHolder(View view) {
            super(view);
            tv_title = (TextView)view.findViewById(R.id.book_found_conditions);
            tv_author = (TextView)view.findViewById(R.id.book_found_status);
            iw_thumbnail = (ImageView) view.findViewById(R.id.book_found_thumbnail);
        }

        public void bind(SearchBookAlgoliaItem searchBookAlgoliaItem, OnItemClickListener myListener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myListener.onItemClick(searchBookAlgoliaItem);
                }
            });
        }
    }
}
