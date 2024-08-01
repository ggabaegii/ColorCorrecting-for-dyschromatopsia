package com.example.cvd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class edit extends AppCompatActivity {

    ImageView btn_hsv;
    ImageView btn_auto;
    ImageView btn_outline;

    ImageView imageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        String imageUriString = getIntent().getStringExtra("imageUri");
        Uri image = Uri.parse(imageUriString);

        imageInput = findViewById(R.id.imageView);
        imageInput.setImageURI(image);

        Log.i("image",String.valueOf(image));


        btn_hsv=findViewById(R.id.btn_hsv);
        btn_hsv.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent=new Intent(getApplicationContext(),HSV.class);
                intent.putExtra("imageUri", image.toString());
                startActivity(intent);

            }
        });

        btn_auto=findViewById(R.id.btn_auto);
        btn_auto.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent=new Intent(getApplicationContext(),auto.class);
                intent.putExtra("imageUri", image.toString());
                startActivity(intent);
            }
        });

        btn_outline=findViewById(R.id.btn_outline);
        btn_outline.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent=new Intent(getApplicationContext(),outline.class);
                intent.putExtra("imageUri", image.toString());
                startActivity(intent);
            }
        });
    }


}