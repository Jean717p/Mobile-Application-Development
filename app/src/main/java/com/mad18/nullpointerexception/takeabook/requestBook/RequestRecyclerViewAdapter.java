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
import com.mad18.nullpointerexception.takeabook.util.Loan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class RequestRecyclerViewAdapter extends RecyclerView.Adapter<RequestRecyclerViewAdapter.MyViewHolder> {

    private Context myContext;
    private List<Loan> mData;
    private final OnItemClickListener listener;
    private final String thisUserId;
    private boolean showStatusIcon;

    public RequestRecyclerViewAdapter(Context myContext, List<Loan> mData, String thisUserId, Boolean showStatusIcon, OnItemClickListener listener ) {
        this.myContext = myContext;
        this.mData = mData;
        this.listener = listener;
        this.thisUserId = thisUserId;
        this.showStatusIcon = showStatusIcon;
    }

    public interface OnItemClickListener {
        void onItemClick(Loan item, int position);
    }

    public void setData(List<Loan> mData){
        this.mData = mData;
    }
    public List<Loan> getData(){
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
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
        Loan loan = mData.get(position);
        if(thisUserId.equals(loan.getOwnerId())){
            holder.tv_loan_label_username.setText(R.string.request_book_applicant_no_dots);
            holder.tv_loan_applicant_name.setText(loan.getApplicantName());
            if(loan.getEndLoanOwner()!=null){
                holder.tv_loan_request_end_date.setText(formatter.format(loan.getEndLoanOwner()));
            }
            if(showStatusIcon){
                if(loan.getExchangedOwner()){
                    holder.iw_loan_request_status.setImageResource(R.drawable.ic_hand_shake);
                }
                else if(loan.getRequestStatus()) { //A
                    if (loan.getExchangedApplicant()) { //B
                        holder.iw_loan_request_status.setImageResource(R.drawable.ic_notification);
                    } else {
                        holder.iw_loan_request_status.setImageResource(R.drawable.ic_clock_pending);
                    }
                }
                else{
                    holder.iw_loan_request_status.setImageResource(R.drawable.ic_notification);
                }
            }
            else{
                holder.iw_loan_request_status.setVisibility(View.GONE);
            }
        }
        else{
            holder.tv_loan_label_username.setText(R.string.info_book_label_owner_no_dots);
            holder.tv_loan_applicant_name.setText(loan.getOwnerName());
            if(showStatusIcon){
                if(loan.getExchangedOwner()){
                    holder.iw_loan_request_status.setImageResource(R.drawable.ic_hand_shake);
                }
                else if(loan.getRequestStatus()) { //A
                    if (loan.getExchangedApplicant()) { //B
                        holder.iw_loan_request_status.setImageResource(R.drawable.ic_clock_pending);
                    } else {
                        holder.iw_loan_request_status.setImageResource(R.drawable.ic_notification);
                    }
                }
                else{
                    holder.iw_loan_request_status.setImageResource(R.drawable.ic_clock_pending);
                }
            }
            else{
                holder.iw_loan_request_status.setVisibility(View.GONE);
            }
        }
        holder.tv_loan_request_start_date.setText(formatter.format(loan.getStartDate()));
        holder.tv_loan_book_title.setText(loan.getBookTitle());
        if(loan.getBookThumbnail().length()>0){
            GlideApp.with(myContext).load(loan.getBookThumbnail())
                    .placeholder(R.drawable.ic_thumbnail_cover_book)
                    .into(holder.iw_loan_book_thumbnail);
        }
        holder.bind(loan, listener);
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
        TextView tv_loan_label_username;
        ImageView iw_loan_book_thumbnail;
        ImageView iw_loan_request_status;
        CardView cardView;

        MyViewHolder(View itemView) {
            super(itemView);
            tv_loan_applicant_name = itemView.findViewById(R.id.request_loan_cv_applicant);
            tv_loan_request_start_date = itemView.findViewById(R.id.request_loan_cv_start_date);
            tv_loan_book_title = itemView.findViewById(R.id.request_loan_cv_book_title);
            tv_loan_request_end_date = itemView.findViewById(R.id.request_loan_cv_end_date);
            tv_loan_label_username = itemView.findViewById(R.id.request_loan_cv_applicant_label);
            iw_loan_book_thumbnail = itemView.findViewById(R.id.request_loan_cv_book_thumbnail);
            iw_loan_request_status = itemView.findViewById(R.id.request_loan_cv_status_icon);
            cardView = (CardView) itemView.findViewById(R.id.request_loan_card_view);
        }

        void bind(final Loan item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item, getAdapterPosition());

                }
            });
        }

    }
}
