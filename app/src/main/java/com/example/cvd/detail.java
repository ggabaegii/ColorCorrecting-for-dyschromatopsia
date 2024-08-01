package com.example.cvd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class detail extends AppCompatActivity {
    ImageView imageView;
    Uri imageUri;
    EditText editTitle;
    Button updateBtn;
    String oldTitle;
    ImageView downloadBtn;
    ImageView shareImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imageView = findViewById(R.id.detail_imageView);
        editTitle = findViewById(R.id.name_detail);
        updateBtn = findViewById(R.id.update_detail);
        downloadBtn = findViewById(R.id.download_imageView);
        shareImageView = findViewById(R.id.share_imageView);

        getAndSetIntentData();

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTitle = editTitle.getText().toString();
                PhotoBookDB db = new PhotoBookDB(detail.this);
                db.updateData(oldTitle, newTitle);
            }
        });
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImageToGallery();
            }
        });
        shareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage();
            }
        });
    }
        private void saveImageToGallery() {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            OutputStream fos;

            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "Image_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/MyImages");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                Toast.makeText(this, "다운로드 완료", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "다운로드 실패", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    private void shareImage() {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        File cachePath = new File(getExternalFilesDir(null), "images");
        cachePath.mkdirs();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(cachePath, "shared_image.png"));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File imagePath = new File(getExternalFilesDir(null), "images");
        File newFile = new File(imagePath, "shared_image.png");
        Uri contentUri = FileProvider.getUriForFile(this, "com.example.cvd.fileprovider", newFile);

        if (contentUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        }
    }

    /**
     * 데이터 가져와서 화면에 보여주기
     */
    private void getAndSetIntentData() {
        if (getIntent().hasExtra("title") && getIntent().hasExtra("imageUri")) {
            // 데이터 가져오기
            oldTitle = getIntent().getStringExtra("title");
            String imageUriString = getIntent().getStringExtra("imageUri");
            imageUri = Uri.parse(imageUriString);

            // 데이터 넣기
            editTitle.setText(oldTitle);
            imageView.setImageURI(imageUri);
        }
    }
}