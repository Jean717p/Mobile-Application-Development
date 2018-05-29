package com.mad18.nullpointerexception.takeabook.searchBook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.R;

import java.util.List;

class SearchBookRecyclerViewAdapter extends RecyclerView.Adapter<SearchBookRecyclerViewAdapter.MyViewHolder> {
    private Context myContext;
    private List<Book> mData;
    private final SearchBookRecyclerViewAdapter.OnItemClickListener listener;
    private SearchBookRecyclerViewAdapter.OnItemClickInfoListener infoListener;

    public SearchBookRecyclerViewAdapter(Context myContext, List<Book> mData,
                                         SearchBookRecyclerViewAdapter.OnItemClickListener listener,
                                         SearchBookRecyclerViewAdapter.OnItemClickInfoListener infoListener) {
        this.myContext = myContext;
        this.mData = mData;
        this.listener = listener;
        this.infoListener = infoListener;
    }

    public interface OnItemClickListener {
        void onItemClick(Book item);
    }
    interface OnItemClickInfoListener{
        void onItemInfoClick(Book item);
    }

    public void setData(List<Book> mData){
        this.mData = mData;
    }

    @NonNull
    @Override
    public SearchBookRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater minflater = LayoutInflater.from(myContext);
        view = minflater.inflate(R.layout.found_book_view, parent , false);
        return new SearchBookRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchBookRecyclerViewAdapter.MyViewHolder holder, int position) {
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
        holder.bindInfo(mData.get(position),infoListener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_book_title;
        ImageView iv_book_thumbnail;
        CardView cardView;
        Button infoButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_book_title = (TextView) itemView.findViewById(R.id.found_book_title);
            iv_book_thumbnail = (ImageView) itemView.findViewById(R.id.found_book_picture);
            cardView = (CardView) itemView.findViewById(R.id.found_book_card_view);
            infoButton = (Button) itemView.findViewById(R.id.book_found_button_info);
        }

        public void bind(final Book item, final SearchBookRecyclerViewAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
        void bindInfo(Book item, SearchBookRecyclerViewAdapter.OnItemClickInfoListener listener){
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemInfoClick(item);
                }
            });
        }
    }
}
