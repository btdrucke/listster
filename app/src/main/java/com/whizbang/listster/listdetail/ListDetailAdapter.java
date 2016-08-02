package com.whizbang.listster.listdetail;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whizbang.listster.R;

import java.util.ArrayList;
import java.util.List;


public class ListDetailAdapter extends RecyclerView.Adapter<ListDetailAdapter.ViewHolder> {

    public List<ListDetailItem> dataSet;


    public ListDetailAdapter() {
        dataSet = new ArrayList<>();
    }


    public void addToDataSet(ListDetailItem item) {
        dataSet.add(0, item);
        notifyDataSetChanged();
    }


    public ListDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new ViewHolder(itemView);
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ListDetailItem item = dataSet.get(position);
        TextView title = holder.title;
        title.setText(item.title);
        TextView author = holder.author;
        author.setText(item.author);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView author;


        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            author = (TextView) v.findViewById(R.id.sub_text);
        }
    }
}


