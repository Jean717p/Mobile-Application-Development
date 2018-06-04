package com.mad18.nullpointerexception.takeabook.info;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.Book;

import java.util.List;




public class ShowBooksRecyclerViewAdapter extends RecyclerView.Adapter<ShowBooksRecyclerViewAdapter.MyViewHolder>{
    private Context myContext;
    private List<Book> mData;
    private final OnItemClickListener listener;

    public ShowBooksRecyclerViewAdapter(Context myContext, List<Book> mData, ShowBooksRecyclerViewAdapter.OnItemClickListener listener ) {
        this.myContext = myContext;
        this.mData = mData;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Book item);
    }

    public void setData(List<Book> mData){
        this.mData = mData;
    }
    public List<Book> getData(){
        return this.mData;
    }

    @NonNull
    @Override
    public ShowBooksRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater minflater = LayoutInflater.from(myContext);
        view = minflater.inflate(R.layout.cardview_item_show_books, parent , false);
        return new ShowBooksRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String title;
        String[] parts;
        holder.tv_book_title.setText(mData.get(position).getBook_title());
        title = (mData.get(position).getBook_title());
        if(title.contains(".")) {
            parts = title.split("[.]",2);
            holder.tv_book_title.setText(parts[0]);
        }
        //holder.iv_book_thumbnail.setImageResource(mData.get(position));
        if(mData.get(position).getBook_thumbnail_url().length()>0){
            GlideApp.with(myContext).load(mData.get(position).getBook_thumbnail_url())
                    .placeholder(R.drawable.ic_thumbnail_cover_book)
                    .into(holder.iv_book_thumbnail);
        }
        else{
            holder.iv_book_thumbnail.setImageResource(R.drawable.ic_thumbnail_cover_book);
        }
        holder.bind(mData.get(position), listener);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_book_title;
        ImageView iv_book_thumbnail;
        CardView cardView;
        MyViewHolder(View itemView) {
            super(itemView);
            tv_book_title = (TextView) itemView.findViewById(R.id.info_user_show_books_book_title);
            iv_book_thumbnail = (ImageView) itemView.findViewById(R.id.info_user_show_books_book_picture);
            cardView = (CardView) itemView.findViewById(R.id.info_user_show_books_card_view);
        }

        void bind(final Book item, final ShowBooksRecyclerViewAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }

    }
}
