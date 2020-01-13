package com.example.homeactivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.MyViewHolder> {
    private List<Filter> filters;
    private ItemClickListener mClickListener;


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageView;
        public MyViewHolder(View v){
            super(v);
            imageView=v.findViewById(R.id.imageView4);
            v.setOnClickListener(this);

        }
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public FilterAdapter(List<Filter> myDataset) {
        filters = myDataset;
    }

    @Override
    public FilterAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.savedimage_view, parent, false);

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

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
