package com.mad18.nullpointerexception.takeabook.info;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.util.Review;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


public class ShowReviewsRecyclerViewAdapter extends RecyclerView.Adapter<ShowReviewsRecyclerViewAdapter.MyViewHolder> {

    private Context myContext;
    private List<Review> mData;

    public ShowReviewsRecyclerViewAdapter(Context myContext, List<Review> mData) {
        this.myContext = myContext;
        this.mData = mData;
    }

    public void setData(List<Review> mData){
        this.mData = mData;
    }

    public List<Review> getData(){
        return this.mData;
    }


    @NonNull
    @Override
    public ShowReviewsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater minflater = LayoutInflater.from(myContext);
        view = minflater.inflate(R.layout.cardiview_review, parent , false);
        return new ShowReviewsRecyclerViewAdapter.MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ShowReviewsRecyclerViewAdapter.MyViewHolder holder, int position) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Review review = mData.get(position);
        holder.tv_review_username.setText(review.getUsername());
        holder.tv_review_comment.setText(review.getText());
        holder.tv_review_date.setText(formatter.format(review.getReviewDate()));
        holder.ratingBar_review.setRating(review.getRating());
        if(review.getUserPic().length()>0){
            GlideApp.with(myContext)
                    .load(FirebaseStorage.getInstance().getReference(review.getUserPic()))
                    .placeholder(R.drawable.ic_account_circle_white_48px)
                    .into(holder.iv_user_image);
        }
        else{
            holder.iv_user_image.setImageResource(R.drawable.ic_account_circle_white_48px);
        }
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_review_comment;
        ImageView iv_user_image;
        CardView cardView;
        TextView tv_review_username;
        TextView tv_review_date;
        RatingBar ratingBar_review;

        MyViewHolder(View itemView) {
            super(itemView);
            tv_review_comment = (TextView) itemView.findViewById(R.id.review_text_comment);
            iv_user_image = (ImageView) itemView.findViewById(R.id.review_user_Img);
            cardView = (CardView) itemView.findViewById(R.id.review_cv);
            tv_review_date = (TextView)itemView.findViewById(R.id.review_date);
            tv_review_username = (TextView)itemView.findViewById(R.id.review_username);
            ratingBar_review = (RatingBar)itemView.findViewById(R.id.review_rating_bar);
        }

    }

}
