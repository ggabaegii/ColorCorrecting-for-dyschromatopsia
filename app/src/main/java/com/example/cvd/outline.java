package com.example.cvd;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class outline extends AppCompatActivity {
    ImageView imageView;
    EditText editTitle;
    Button saveButton;
    ImageView changeOutlineButton;
    ImageView returnOriginalButton;
    Uri imageUri;
    Bitmap originalBitmap;
    Bitmap processedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outline);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV initialization failed");
        } else {
            Log.d("OpenCV", "OpenCV initialization succeeded");
        }

        imageView = findViewById(R.id.outline_imageView);
        editTitle = findViewById(R.id.name_outline);
        saveButton = findViewById(R.id.save_outline);
        changeOutlineButton = findViewById(R.id.change_outline);
        returnOriginalButton = findViewById(R.id.return_outline);

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
            imageView.setImageURI(imageUri);
            loadImage(imageUri); // 이미지 로드
        }

        changeOutlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processImage(); // 이미지 처리 메서드 호출
            }
        });

        returnOriginalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(originalBitmap); // 원본 이미지로 되돌림
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString();
                if (!title.isEmpty() && imageUri != null) {
                    PhotoBookDB db = new PhotoBookDB(outline.this);
                    if (db.isTitleExists(title)) {
                        Toast.makeText(outline.this, "이미 등록된 제목입니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Bitmap bitmapToSave = (processedBitmap != null) ? processedBitmap : originalBitmap;
                        String filePath = saveBitmapToFile(bitmapToSave, title);
                        if (filePath != null) {
                            db.addPhoto(title, filePath); // 이미지 파일 경로를 저장
                            finish(); // 저장 후 종료
                            Toast.makeText(outline.this, "저장 완료", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(outline.this, "이미지 저장 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void loadImage(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            originalBitmap = BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(originalBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지 파일을 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void processImage() {
        try {
            Mat image = new Mat(originalBitmap.getHeight(), originalBitmap.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(originalBitmap, image);

            // Convert to grayscale
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

            // Apply Sobel operator to get the gradients
            Mat gradX = new Mat();
            Mat gradY = new Mat();
            Imgproc.Sobel(grayImage, gradX, CvType.CV_16S, 1, 0);
            Imgproc.Sobel(grayImage, gradY, CvType.CV_16S, 0, 1);

            // Convert gradients to absolute values
            Core.convertScaleAbs(gradX, gradX);
            Core.convertScaleAbs(gradY, gradY);

            // Combine the gradients
            Mat edges = new Mat();
            Core.addWeighted(gradX, 0.5, gradY, 0.5, 0, edges);

            // Convert edges to color
            Mat edgesColor = new Mat();
            Imgproc.cvtColor(edges, edgesColor, Imgproc.COLOR_GRAY2BGR);

            // Convert the final edges Mat back to Bitmap
            processedBitmap = Bitmap.createBitmap(edgesColor.cols(), edgesColor.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(edgesColor, processedBitmap);

            // Display the processed image
            imageView.setImageBitmap(processedBitmap);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveBitmapToFile(Bitmap bitmap, String title) {
        try {
            File file = new File(getExternalFilesDir(null), title + ".png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
