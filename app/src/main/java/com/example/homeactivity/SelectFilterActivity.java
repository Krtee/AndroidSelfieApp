package com.example.homeactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SelectFilterActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    List<Filter> filters;
    public static final int PICK_IMAGE = 1;
    File newdir;
    File textfiles;
    File txt;
    FilterAdapter adapter;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_filter);

        ActionBar actionBar = SelectFilterActivity.this.getSupportActionBar();
        actionBar.setTitle("Selfie-App");


        recyclerView= findViewById(R.id.filterview);

        init();

        layoutManager = new GridLayoutManager(this,3);
        adapter= new FilterAdapter(filters);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(allPermissionsGranted()){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                } else {
                    ActivityCompat.requestPermissions(SelectFilterActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                }

            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemOnClick(SelectFilterActivity.this, recyclerView, new RecyclerItemOnClick.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(SelectFilterActivity.this,MainActivity.class);
                intent.putExtra("filtermask",filters.get(position));
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {
                File todelete= new File(filters.get(position).getPic().getPath());
                String dname=todelete.getName();
                if(todelete.delete()){
                    try{
                        File tempFile = new File(textfiles,"myTempFile.txt");

                        BufferedReader reader = new BufferedReader(new FileReader(txt));
                        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                        String lineToRemove =dname+","+filters.get(position).getScalingfact();
                        String currentLine;

                        while((currentLine = reader.readLine()) != null) {
                            // trim newline when comparing with lineToRemove
                            String trimmedLine = currentLine.trim();
                            if(trimmedLine.equals(lineToRemove)) continue;
                            writer.write(currentLine + System.getProperty("line.separator"));
                        }
                        writer.close();
                        reader.close();
                        boolean successful = tempFile.renameTo(txt);
                        Toast.makeText(SelectFilterActivity.this,"Successfully deleted.",Toast.LENGTH_SHORT);
                    }
                    catch (IOException e){
                        Toast.makeText(SelectFilterActivity.this,"Couldn't delete filter.",Toast.LENGTH_SHORT);
                    }
                }
                filters.remove(position);
                adapter.notifyDataSetChanged();
            }
        }));


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == PICK_IMAGE) {
            try{
                //InputStream inputStream = SelectFilterActivity.this.getContentResolver().openInputStream(data.getData());

                Intent intent= new Intent(SelectFilterActivity.this,CropActivity.class);
                Uri uri = data.getData();
                intent.putExtra("pic",data.getData().toString());
                startActivity(intent);

            }
            catch (NullPointerException e){
                Toast.makeText(SelectFilterActivity.this,"No pic selected",Toast.LENGTH_SHORT).show();
            }

        }
    }

    void init(){
        newdir= SelectFilterActivity.this.getDir("filters", Context.MODE_PRIVATE);
        textfiles = SelectFilterActivity.this.getDir("textfiles",Context.MODE_PRIVATE);
        txt = new File(textfiles,"filters.txt");
        if (newdir.exists()) {
            if(newdir.list().length==0){
                List<Defaultfilter> defaultfilter = Helper.getAllFiltersDefault(SelectFilterActivity.this);
                try {
                    PrintWriter out = new PrintWriter(txt);
                    for (Defaultfilter filter : defaultfilter
                    ) {
                        Bitmap bm = BitmapFactory.decodeResource(SelectFilterActivity.this.getResources(), filter.getDrawable());
                        File file = new File(newdir, filter.toString() + ".png");
                        FileOutputStream outStream = new FileOutputStream(file);
                        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        out.println(file.getName() + "," + filter.getScalingfact());
                    }
                    out.close();
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException a) {
                    a.printStackTrace();
                }

            }
        }
        filters= Helper.getAllFilters(newdir,txt);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        filters=Helper.getAllFilters(newdir,txt);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    

}
