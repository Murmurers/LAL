package com.test.li182.my_game;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by li182 on 2018/1/20.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private ArrayList<Player> mData;

    public MyAdapter(ArrayList<Player> data) {
        this.mData = data;
    }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 实例化展示的view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rc_item, parent, false);
        // 实例化viewholder
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // 绑定数据
        holder.TvU.setText(mData.get(position).getName());
        holder.TvS.setText(""+mData.get(position).getScore());
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView TvU;
        TextView TvS;
        TextView TvR;
        public ViewHolder(View itemView) {
            super(itemView);
            TvU = (TextView) itemView.findViewById(R.id.item_user);
            TvS = (TextView) itemView.findViewById(R.id.item_score);
            TvS = (TextView) itemView.findViewById(R.id.item_state);
        }
    }
}