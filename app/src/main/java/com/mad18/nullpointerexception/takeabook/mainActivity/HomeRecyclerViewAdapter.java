package com.mad18.nullpointerexception.takeabook.mainActivity;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.GeoPoint;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.Book;

import java.util.List;


public class HomeRecyclerViewAdapter extends Adapter<HomeRecyclerViewAdapter.MyViewHolder> {

    private Context myContext;
    private List<Book> mData;
    private final HomeRecyclerViewAdapter.OnItemClickListener listener;
    private Location user_loc;

    public HomeRecyclerViewAdapter(Context myContext, List<Book> mData, OnItemClickListener listener ) {
        this.myContext = myContext;
        this.mData = mData;
        this.listener = listener;
        user_loc = new Location("");
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
    public void setUser_loc(GeoPoint user_geo){
        if(user_geo!=null){
            this.user_loc.setLongitude(user_geo.getLongitude());
            this.user_loc.setLatitude(user_geo.getLatitude());
        }
    }

    @NonNull
    @Override
    public HomeRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        LayoutInflater minflater = LayoutInflater.from(myContext);
        view = minflater.inflate(R.layout.card_view_item_book_home, parent , false);
        return new MyViewHolder(view);
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
        if(mData.get(position).getBook_thumbnail_url().length()>0){
            GlideApp.with(myContext).load(mData.get(position).getBook_thumbnail_url())
                    .placeholder(R.drawable.ic_thumbnail_cover_book)
                    .into(holder.iv_book_thumbnail);
        }
        else{
            holder.iv_book_thumbnail.setImageResource(R.drawable.ic_thumbnail_cover_book);
        }
        holder.bind(mData.get(position), listener);
        GeoPoint book_geo = mData.get(position).getBook_location();
        Location book_loc = new Location("");
        book_loc.setLatitude(book_geo.getLatitude());
        book_loc.setLongitude(book_geo.getLongitude());
        holder.tv_book_distance.setText(String.format("%.2f km",user_loc.distanceTo(book_loc)/1000));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_book_title;
        TextView tv_book_distance;
        ImageView iv_book_thumbnail;
        CardView cardView;



        public MyViewHolder(View itemView) {
            super(itemView);
            tv_book_title = (TextView) itemView.findViewById(R.id.home_book_title);
            tv_book_distance = (TextView) itemView.findViewById(R.id.home_book_distance);
            iv_book_thumbnail = (ImageView) itemView.findViewById(R.id.home_book_picture);
            cardView = (CardView) itemView.findViewById(R.id.home_book_card_view);
        }

        public void bind(final Book item, final HomeRecyclerViewAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);

                }
            });


        }

    }


}

