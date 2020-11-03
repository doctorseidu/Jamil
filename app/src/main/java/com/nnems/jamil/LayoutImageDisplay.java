package com.nnems.jamil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class LayoutImageDisplay extends AppCompatActivity {

    Bitmap mBitmap;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_image_display);

        Intent intent = getIntent();
        mBitmap = intent.getParcelableExtra("bitmapImage");

        mImageView = findViewById(R.id.layout_image_display);

        mImageView.setImageBitmap(mBitmap);





    }
}