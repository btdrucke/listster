package com.whizbang.listster.list;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whizbang.listster.R;
import com.whizbang.listster.databinding.ListItemRowBinding;
import com.whizbang.listster.list.UserListItemAdapter.ListItemViewHolder;
import com.whizbang.listster.listdetail.ListDetailActivity;

import java.util.ArrayList;
import java.util.List;


public class UserListItemAdapter extends RecyclerView.Adapter<ListItemViewHolder> {


    public ArrayList<UserList> dataSet;
    private OnClickListener mClickListener;


    public UserListItemAdapter(ArrayList<UserList> data, OnClickListener listener) {
        dataSet = data;
        mClickListener = listener;
    }


    public static class ListItemViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTextView;
        public TextView lastModifiedTextView;
        public CardView cardView;


        public ListItemViewHolder(ListItemRowBinding binding) {
            super(binding.getRoot());
            cardView = binding.cardView;
            titleTextView = binding.title;
            lastModifiedTextView = binding.date;
        }
    }


    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemRowBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.list_item_row, parent, false);
//        binding.cardView.setOnClickListener(v -> {
//            Context context = v.getContext();
//            context.startActivity(ListDetailActivity.getStartIntent(context, null));
//        });
        return new ListItemViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        UserList userList = dataSet.get(position);
        holder.cardView.setOnClickListener(mClickListener);
        holder.titleTextView.setText(userList.title);
        holder.lastModifiedTextView.setText(
                DateUtils.formatDateTime(holder.lastModifiedTextView.getContext(),
                        userList.lastModifedUtcMillis, DateUtils.FORMAT_ABBREV_RELATIVE |
                                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                                DateUtils.FORMAT_SHOW_WEEKDAY));
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    public void addItem(UserList listItem) {
        dataSet.add(0, listItem);
        notifyDataSetChanged();
    }


    public void setItems(List<UserList> items) {
        dataSet.clear();
        dataSet.addAll(items);
        notifyDataSetChanged();
    }
}
