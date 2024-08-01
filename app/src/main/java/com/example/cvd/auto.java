package com.example.cvd;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class auto extends AppCompatActivity {
    ImageView imageView;
    Uri imageUri;
    EditText editTitle;
    Button saveButton;
    ImageView autoCorrectButton;
    ImageView resetButton; // 되돌리기 버튼 추가

    private Mat imageMat;
    private Bitmap originalBitmap; // 원본 이미지를 저장하는 변수 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);

        imageView = findViewById(R.id.auto_imageView);
        editTitle = findViewById(R.id.name_auto);
        saveButton = findViewById(R.id.save_auto);
        resetButton = findViewById(R.id.return_auto); // 되돌리기 버튼 초기화

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
            imageView.setImageURI(imageUri);
        }

        try {
            originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imageMat = new Mat();
            Utils.bitmapToMat(originalBitmap, imageMat);
            imageView.setImageBitmap(originalBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupAutoCorrection();
        setupResetButton(); // 되돌리기 버튼 설정

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString();
                if (!title.isEmpty() && imageUri != null) {
                    PhotoBookDB db = new PhotoBookDB(auto.this);
                    db.addPhoto(title, imageUri.toString());
                    finish(); // 저장 후 종료
                }
            }
        });
    }

    @SuppressLint("WrongViewCast")
    private void setupAutoCorrection() {
        autoCorrectButton = findViewById(R.id.change_auto);
        autoCorrectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCorrectImage();
            }
        });
    }

    private void setupResetButton() {
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetImage();
            }
        });
    }

    private void autoCorrectImage() {
        Mat correctedMat = imageMat.clone();
        applyColorBlindCorrection(correctedMat);

        Bitmap bitmap = Bitmap.createBitmap(correctedMat.cols(), correctedMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(correctedMat, bitmap);
        imageView.setImageBitmap(bitmap);
    }

    private void resetImage() {
        imageView.setImageBitmap(originalBitmap); // 원본 이미지를 ImageView에 설정
        Utils.bitmapToMat(originalBitmap, imageMat); // imageMat도 원본으로 복원
    }


    //빨간색, 초록색 색 변환
    private void applyColorBlindCorrection(Mat imageMat) {
        // Convert the image from BGRA to BGR if needed
        boolean isFourChannel = imageMat.channels() == 4;
        Mat imageMatBGR = new Mat();
        if (isFourChannel) {
            Imgproc.cvtColor(imageMat, imageMatBGR, Imgproc.COLOR_BGRA2BGR);
        } else {
            imageMatBGR = imageMat;
        }

        // Noise reduction using Gaussian Blur
        Mat denoisedMat = new Mat();
        Imgproc.GaussianBlur(imageMatBGR, denoisedMat, new Size(5, 5), 0);

        // Convert the image to HSV color space
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(denoisedMat, hsvMat, Imgproc.COLOR_BGR2HSV);

        // Iterate over each pixel and change red and green colors
        for (int row = 0; row < hsvMat.rows(); row++) {
            for (int col = 0; col < hsvMat.cols(); col++) {
                double[] pixel = hsvMat.get(row, col);

                // Skip color adjustment for very light pixels (white)
                if (pixel[1] < 30 && pixel[2] > 200) {
                    continue;
                }

                // HSV ranges for red color (including pink and orange)
                if ((pixel[0] >= 110 && pixel[0] <= 180) && (pixel[1] >= 100 && pixel[1] <= 255) && (pixel[2] >= 100 && pixel[2] <= 255)  ) {
                    // Set to bright red (Hue = 0, Saturation = 255, Value = 255)
                    pixel[0] = 140;
                    pixel[1] = 255;
                    pixel[2] = 255;
                }
                // HSV ranges for green color
                else if (pixel[0] >= 35 && pixel[0] <= 85) {
                    // Set to bright green (Hue = 60, Saturation = 255, Value = 255)
                    pixel[0] = 30;
                    pixel[1] = 255;
                    pixel[2] = 255;
                }
             

                hsvMat.put(row, col, pixel);
            }
        }

        // Convert the HSV image back to BGR color space
        Mat finalMat = new Mat();
        Imgproc.cvtColor(hsvMat, finalMat, Imgproc.COLOR_HSV2BGR);

        // If the original image was BGRA, convert the result back to BGRA
        if (isFourChannel) {
            Imgproc.cvtColor(finalMat, finalMat, Imgproc.COLOR_BGR2BGRA);
        }

        // Copy the modified image back to the original image
        finalMat.copyTo(imageMat);
    }
}