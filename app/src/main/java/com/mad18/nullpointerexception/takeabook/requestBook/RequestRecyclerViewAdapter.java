package com.mad18.nullpointerexception.takeabook.requestBook;

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
import com.mad18.nullpointerexception.takeabook.util.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

class RequestRecyclerViewAdapter extends RecyclerView.Adapter<RequestRecyclerViewAdapter.MyViewHolder> {

    private Context myContext;
    private List<Loan> mData;
    private final OnItemClickListener listener;
    private final User thisUser;

    public RequestRecyclerViewAdapter(Context myContext, List<Loan> mData, User thisUser, OnItemClickListener listener ) {
        this.myContext = myContext;
        this.mData = mData;
        this.listener = listener;
        this.thisUser = thisUser;
    }

    interface OnItemClickListener {
        void onItemClick(Loan item);
    }

    void setData(List<Loan> mData){
        this.mData = mData;
    }
    List<Loan> getData(){
        return this.mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater minflater = LayoutInflater.from(myContext);
        view = minflater.inflate(R.layout.card_view_item_loan, parent , false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if(thisUser.getUsr_id().equals(mData.get(position).getOwnerId())){
            holder.tv_loan_applicant_name.setText(mData.get(position).getApplicantName());
            if(mData.get(position).getEndLoanOwner()!=null){
                holder.tv_loan_request_end_date.setText(formatter.format(mData.get(position).getEndLoanOwner()));
            }
        }
        else{
            holder.tv_loan_applicant_name.setText(mData.get(position).getOwnerName());
        }
        holder.tv_loan_request_start_date.setText(formatter.format(mData.get(position).getStartDate()));
        holder.tv_loan_book_title.setText(mData.get(position).getBookTitle());
        if(mData.get(position).getBookThumbnail().length()>0){
            GlideApp.with(myContext).load(mData.get(position).getBookThumbnail())
                    .placeholder(R.drawable.ic_thumbnail_cover_book)
                    .into(holder.iw_loan_book_thumbnail);
        }
        holder.bind(mData.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_loan_applicant_name;
        TextView tv_loan_request_start_date;
        TextView tv_loan_book_title;
        TextView tv_loan_request_end_date;
        ImageView iw_loan_book_thumbnail;
        CardView cardView;

        MyViewHolder(View itemView) {
            super(itemView);
            tv_loan_applicant_name = itemView.findViewById(R.id.request_loan_cv_applicant);
            tv_loan_request_start_date = itemView.findViewById(R.id.request_loan_cv_start_date);
            tv_loan_book_title = itemView.findViewById(R.id.request_loan_cv_book_title);
            tv_loan_request_end_date = itemView.findViewById(R.id.request_loan_cv_end_date);
            iw_loan_book_thumbnail = itemView.findViewById(R.id.request_loan_cv_book_thumbnail);
            cardView = (CardView) itemView.findViewById(R.id.request_loan_card_view);
        }

        void bind(final Loan item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);

                }
            });


        }

    }
}
