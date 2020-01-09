package com.example.homeactivity;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.MyViewHolder> {
    private List<Filter> filters;

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public MyViewHolder(ImageView v){
            super(v);
            imageView=v;
        }
    }

    public FilterAdapter(List<Filter> myDataset) {
        filters = myDataset;
    }

    @Override
    public FilterAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.imageView.setImageResource(filters.get(position).getDrawable());
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }
}
