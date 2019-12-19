package com.example.homeactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class PreviewActivity extends AppCompatActivity {
    ImageView pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        pic = findViewById(R.id.takenpic);

        Uri imageUri = getIntent().getData();

        System.out.println(imageUri.toString());

        if (imageUri == null) {
            Toast.makeText(this,"No pic." , Toast.LENGTH_LONG).show(); }
        else {
            pic.setImageURI(imageUri);
        }

    }
}
