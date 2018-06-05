package com.mad18.nullpointerexception.takeabook.displaySearchOnMap;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private ArrayList<Book> mData;
    private Context context;

    private LayoutInflater mLayoutInflater;

    private boolean mIsSpaceVisible = true;

    public interface ItemClickListener {
        void onItemClicked(LatLng position);
    }

    private WeakReference<ItemClickListener> mCallbackRef;

        public HeaderAdapter(Context ctx, ArrayList<Book> data, ItemClickListener listener) {
            context = ctx;
            mLayoutInflater = LayoutInflater.from(ctx);
            mData = data;
            mCallbackRef = new WeakReference<>(listener);
        }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            View v = mLayoutInflater.inflate(R.layout.display_search_on_map_card_view, parent, false);
            return new MyItem(v);
        } else if (viewType == TYPE_HEADER) {
            View v = mLayoutInflater.inflate(R.layout.transparent_header_view, parent, false);
            return new HeaderItem(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyItem) {
            Book dataItem = getItem(position);
            ((MyItem) holder).mTitleView.setText(dataItem.getBook_title());
            ((MyItem) holder).mPosition = position;
            ((MyItem) holder).mAuthor.setText(dataItem.getBook_first_author());
            Location book_position = new Location("Provider");
            Location my_position = new Location("Provider");
            book_position.setLatitude(dataItem.getBook_location().getLongitude());
            book_position.setLongitude(dataItem.getBook_location().getLatitude());
            my_position.setLatitude(DisplaySearchOnMap_map.mLocation.latitude);
            my_position.setLongitude(DisplaySearchOnMap_map.mLocation.longitude);
            ((MyItem) holder).mDistance.setText(String.format("%.2f km",my_position.distanceTo(book_position)/1000));
            Glide.with(context).load(dataItem.getBook_thumbnail_url()).into(((MyItem) holder).mThumbnail);
        } else if (holder instanceof HeaderItem) {
            ((HeaderItem) holder).mSpaceView.setVisibility(mIsSpaceVisible ? View.VISIBLE : View.GONE);
            ((HeaderItem) holder).mPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private Book getItem(int position) {
        return mData.get(position -1);
    }

    class MyItem extends HeaderItem {
        TextView mTitleView,mAuthor, mDistance;
        ImageView mThumbnail;

        public MyItem(View itemView) {
            super(itemView);
            mTitleView = (TextView) itemView.findViewById(R.id.book_found_title);
            mThumbnail = (ImageView) itemView.findViewById(R.id.book_found_thumbnail);
            mAuthor = (TextView) itemView.findViewById(R.id.book_found_author);
            mDistance = (TextView) itemView.findViewById(R.id.book_found_distance);
        }
    }

    class HeaderItem extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mSpaceView;
        int mPosition;

        public HeaderItem(View itemView) {
            super(itemView);
            mSpaceView = itemView.findViewById(R.id.display_search_on_map_space);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ItemClickListener callback = mCallbackRef != null ? mCallbackRef.get() : null;
            if (callback != null) {
                callback.onItemClicked(new LatLng(getItem(mPosition).getBook_location().getLongitude(),
                        getItem(mPosition).getBook_location().getLatitude()));
            }

        }
    }

    public void hideSpace() {
        mIsSpaceVisible = false;
        notifyItemChanged(0);
    }

    public void showSpace() {
        mIsSpaceVisible = true;
        notifyItemChanged(0);
    }
}
