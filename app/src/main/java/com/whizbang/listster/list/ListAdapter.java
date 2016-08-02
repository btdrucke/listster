package com.whizbang.listster.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whizbang.listster.R;

import java.util.ArrayList;
import java.util.List;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    List<Item> mDataSet;


    public ListAdapter() {
        mDataSet = new ArrayList<>();
    }


    public void addToDataSet(Item item) {
        mDataSet.add(0, item);
        notifyDataSetChanged();
    }


    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = mDataSet.get(position);
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


