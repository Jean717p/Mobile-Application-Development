package com.mad18.nullpointerexception.takeabook.mainActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import static com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity.thisUser;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{
    private Context myContext;
    private List<Book> mData;


    public RecyclerViewAdapter(Context myContext, List<Book> mData) {
        this.myContext = myContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        LayoutInflater minflater = LayoutInflater.from(myContext);
        view = minflater.inflate(R.layout.cardview_item_book_library, parent , false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tv_book_title.setText(mData.get(position).getBook_title());
       //holder.iv_book_thumbnail.setImageResource(mData.get(position));
       Glide.with(myContext).load(mData.get(position).getBook_thumbnail_url()).into(holder.iv_book_thumbnail);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tv_book_title;
        ImageView iv_book_thumbnail;
        CardView cardView ;


        public MyViewHolder(View itemView){
            super(itemView);
            tv_book_title = (TextView)  itemView.findViewById(R.id.my_library_book_title);
            iv_book_thumbnail = (ImageView) itemView.findViewById(R.id.my_library_book_picture);
            cardView = (CardView) itemView.findViewById(R.id.my_library_card_view);
        }


    }


}