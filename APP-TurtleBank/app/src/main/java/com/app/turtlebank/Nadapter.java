package com.app.turtlebank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Nadapter extends RecyclerView.Adapter<Nadapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<NoticeListRecords> noticeRecords;
    private OnItemClickListener mListener;

    public Nadapter(Context context, List<NoticeListRecords> noticeRecords) {
        this.inflater = LayoutInflater.from(context);
        this.noticeRecords = noticeRecords;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_notice_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoticeListRecords currentRecord = noticeRecords.get(position);
        holder.nId.setText(currentRecord.getId());
        holder.nTitle.setText(currentRecord.getTitle());
        holder.nUserId.setText(currentRecord.getUserId());
        holder.nUpdatedAt.setText(currentRecord.getUpdatedAtFormatted());
    }

    @Override
    public int getItemCount() {
        return noticeRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nId, nTitle, nUserId, nUpdatedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nId = itemView.findViewById(R.id.nl_id);
            nTitle = itemView.findViewById(R.id.nl_title);
            nUserId = itemView.findViewById(R.id.nl_userId);
            nUpdatedAt = itemView.findViewById(R.id.nl_updatedAt);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public void filterList(List<NoticeListRecords> filteredList) {
        noticeRecords = filteredList;
        notifyDataSetChanged();
    }


}