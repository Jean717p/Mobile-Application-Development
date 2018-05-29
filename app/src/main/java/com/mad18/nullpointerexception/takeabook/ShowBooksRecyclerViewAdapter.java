package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.CardView;

import com.bumptech.glide.Glide;
import com.mad18.nullpointerexception.takeabook.Book;
import com.mad18.nullpointerexception.takeabook.R;

import java.util.List;

import com.bumptech.glide.Glide;
import com.mad18.nullpointerexception.takeabook.mainActivity.MyLibraryRecyclerViewAdapter;

import java.util.List;




public class ShowBooksRecyclerViewAdapter extends RecyclerView.Adapter<ShowBooksRecyclerViewAdapter.MyViewHolder>{
    private Context myContext;
    private List<Book> mData;
    private final OnItemClickListener listener;

    public ShowBooksRecyclerViewAdapter(Context myContext, List<Book> mData, com.mad18.nullpointerexception.takeabook.ShowBooksRecyclerViewAdapter.OnItemClickListener listener ) {
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
    public com.mad18.nullpointerexception.takeabook.ShowBooksRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        LayoutInflater minflater = LayoutInflater.from(myContext);
        view = minflater.inflate(R.layout.cardview_item_show_books, parent , false);
        return new com.mad18.nullpointerexception.takeabook.ShowBooksRecyclerViewAdapter.MyViewHolder(view);
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
        Glide.with(myContext).load(mData.get(position).getBook_thumbnail_url()).into(holder.iv_book_thumbnail);
        holder.bind(mData.get(position), listener);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_book_title;
        ImageView iv_book_thumbnail;
        CardView cardView;



        public MyViewHolder(View itemView) {
            super(itemView);
            tv_book_title = (TextView) itemView.findViewById(R.id.info_user_show_books_book_title);
            iv_book_thumbnail = (ImageView) itemView.findViewById(R.id.info_user_show_books_book_picture);
            cardView = (CardView) itemView.findViewById(R.id.info_user_show_books_card_view);
        }

        public void bind(final Book item, final com.mad18.nullpointerexception.takeabook.ShowBooksRecyclerViewAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);

                }
            });


        }

    }
}
