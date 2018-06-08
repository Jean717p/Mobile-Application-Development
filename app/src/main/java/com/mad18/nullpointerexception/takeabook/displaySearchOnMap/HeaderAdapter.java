package com.mad18.nullpointerexception.takeabook.displaySearchOnMap;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;
import com.mad18.nullpointerexception.takeabook.util.Book;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private ArrayList<Book> mData;
    private Context context;
    private FragmentActivity myActivity;

    private LayoutInflater mLayoutInflater;

    private boolean mIsSpaceVisible = true;

    public interface ItemClickListener {
        void onItemClicked(LatLng position);
    }

    private WeakReference<ItemClickListener> mCallbackRef;

        public HeaderAdapter(FragmentActivity myActivity, ArrayList<Book> data, ItemClickListener listener) {
            context = myActivity;
            this.myActivity = myActivity;
            mLayoutInflater = LayoutInflater.from(myActivity);
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
            switch (dataItem.getBook_condition()) {
                case 0:
                    ((MyItem) holder).mConditions.setText(myActivity.getString(R.string.add_book_info_not_available));
                    break;
                case 1:
                    ((MyItem) holder).mConditions.setText(myActivity.getResources().getStringArray(R.array.book_conditions)[1]);
                    break;
                case 2:
                    ((MyItem) holder).mConditions.setText(myActivity.getResources().getStringArray(R.array.book_conditions)[2]);
                    break;
                case 3:
                    ((MyItem) holder).mConditions.setText(myActivity.getResources().getStringArray(R.array.book_conditions)[3]);
                    break;
            }
            ((MyItem) holder).mPosition = position;
            if(dataItem.getBook_status()){
                ((MyItem) holder).mStatus.setText(R.string.request_book_status_on_loan);
                ((MyItem) holder).mStatus.setTextColor(Color.parseColor("#D50000"));
            }
            else{
                ((MyItem) holder).mStatus.setText(R.string.info_book_status_free);
                ((MyItem) holder).mStatus.setTextColor(Color.parseColor("#4CAF50"));

            }
            Location book_position = new Location("Provider");
            Location my_position = new Location("Provider");
            book_position.setLatitude(dataItem.getBook_location().getLatitude());
            book_position.setLongitude(dataItem.getBook_location().getLongitude());
            my_position.setLatitude(DisplaySearchOnMap_map.mLocation != null ? DisplaySearchOnMap_map.mLocation.latitude : MainActivity.thisUser.getUsr_geoPoint().getLatitude());
            my_position.setLongitude(DisplaySearchOnMap_map.mLocation != null ? DisplaySearchOnMap_map.mLocation.longitude : MainActivity.thisUser.getUsr_geoPoint().getLongitude());
            ((MyItem) holder).mDistance.setText(String.format("%.2f km",my_position.distanceTo(book_position)/1000));
            GlideApp.with(context).load(dataItem.getBook_thumbnail_url())
                    .placeholder(R.drawable.ic_thumbnail_cover_book)
                    .into(((MyItem) holder).mThumbnail);
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
        TextView mConditions, mStatus, mDistance;
        ImageView mThumbnail;

        public MyItem(View itemView) {
            super(itemView);
            mConditions = (TextView) itemView.findViewById(R.id.book_found_conditions);
            mThumbnail = (ImageView) itemView.findViewById(R.id.book_found_thumbnail);
            mStatus = (TextView) itemView.findViewById(R.id.book_found_status);
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
                callback.onItemClicked(new LatLng(getItem(mPosition).getBook_location().getLatitude(),
                        getItem(mPosition).getBook_location().getLongitude()));
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
