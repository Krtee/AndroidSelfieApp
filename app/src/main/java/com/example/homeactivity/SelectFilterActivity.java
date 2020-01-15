package com.example.homeactivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class SelectFilterActivity extends AppCompatActivity implements FilterAdapter.ItemClickListener {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    List<Filter> filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_filter);

        ActionBar actionBar = SelectFilterActivity.this.getSupportActionBar();
        actionBar.setTitle("Selfie-App");


        recyclerView= findViewById(R.id.filterview);

        filters= Helper.getAllFilters(SelectFilterActivity.this);

        layoutManager = new GridLayoutManager(this,3);
        FilterAdapter adapter= new FilterAdapter(filters);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);



    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(SelectFilterActivity.this,MainActivity.class);
        intent.putExtra("filtermask",filters.get(position));
        startActivity(intent);
    }

}
