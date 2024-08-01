package com.example.cvd;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HSV extends AppCompatActivity {
    ImageView imageView;
    Uri imageUri;
    EditText editTitle;
    Button saveButton;
    Bitmap changedBitmap;

    private Mat imageMat, originImgMat;
    private TextView hueValueText;
    private TextView saturationValueText;
    private TextView valueValueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hsv);

        imageView = findViewById(R.id.hsv_imageView);
        editTitle = findViewById(R.id.name_HSV);
        saveButton = findViewById(R.id.save_HSV);

        hueValueText = findViewById(R.id.hueValueText);
        saturationValueText = findViewById(R.id.saturationValueText);
        valueValueText = findViewById(R.id.valueValueText);

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
            imageView.setImageURI(imageUri);
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            originImgMat = new Mat();
            imageMat = new Mat();
            Utils.bitmapToMat(bitmap, originImgMat);
            originImgMat.copyTo(imageMat);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupHSVAdjustment();

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String title = editTitle.getText().toString();
                if (!title.isEmpty() && imageUri != null) {
                    PhotoBookDB db = new PhotoBookDB(HSV.this);
                    if (db.isTitleExists(title)) {
                        Toast.makeText(HSV.this, "이미 등록된 제목입니다", Toast.LENGTH_SHORT).show();
                    } else {
                        String filePath = saveBitmapToFile(changedBitmap, title);
                        if (filePath != null) {
                            //PhotoBookDB db = new PhotoBookDB(outline.this);
                            db.addPhoto(title, filePath); // 이미지 파일 경로를 저장
                            finish(); // 저장 후 종료
                        } else {
                            Toast.makeText(HSV.this, "이미지 저장 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void setupHSVAdjustment() {
        SeekBar hueSeekBar = findViewById(R.id.seekBar5);
        SeekBar saturationSeekBar = findViewById(R.id.seekBar6);
        SeekBar valueSeekBar = findViewById(R.id.seekBar7);

        hueSeekBar.setMax(360); // Hue 범위 [-180, 180]을 위해 최대값 360으로 설정
        hueSeekBar.setProgress(180); // 중간값을 0으로 맞추기 위해 초기값을 180으로 설정

        saturationSeekBar.setMax(200); // Saturation 범위 [0, 2]를 위해 최대값 200으로 설정
        saturationSeekBar.setProgress(100); // 중간값을 1로 맞추기 위해 초기값을 100으로 설정

        valueSeekBar.setMax(200); // Value 범위 [0, 2]를 위해 최대값 200으로 설정
        valueSeekBar.setProgress(100); // 중간값을 1로 맞추기 위해 초기값을 100으로 설정

        hueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                adjustHSV(hueSeekBar.getProgress(), saturationSeekBar.getProgress(), valueSeekBar.getProgress());
                hueValueText.setText(String.valueOf(progress - 180)); // 실제 범위 [-180, 180]
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        saturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                adjustHSV(hueSeekBar.getProgress(), saturationSeekBar.getProgress(), valueSeekBar.getProgress());
                saturationValueText.setText(String.valueOf(progress / 100.0)); // 실제 범위 [0, 2]
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        valueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                adjustHSV(hueSeekBar.getProgress(), saturationSeekBar.getProgress(), progress);
                valueValueText.setText(String.valueOf(progress / 100.0)); // 실제 범위 [0, 2]
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void adjustHSV(int hue, int saturation, int value) {
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(originImgMat, hsvMat, Imgproc.COLOR_BGR2HSV);

        double hueScale = (hue - 180); // [-180, 180]
        double saturationScale = saturation / 100.0; // [0, 2]
        double valueScale = value / 100.0; // [0, 2]

        List<Mat> hsvChannels = new ArrayList<>(3);
        Core.split(hsvMat, hsvChannels);

        // Adjust Hue
        Core.add(hsvChannels.get(0), new Scalar(hueScale), hsvChannels.get(0));
        Core.normalize(hsvChannels.get(1), hsvChannels.get(1), 0, 255, Core.NORM_MINMAX);

        // Adjust Saturation
        hsvChannels.get(1).convertTo(hsvChannels.get(1), -1, saturationScale, 0);
        Core.normalize(hsvChannels.get(1), hsvChannels.get(1), 0, 255, Core.NORM_MINMAX);

        // Adjust Value
        hsvChannels.get(2).convertTo(hsvChannels.get(2), -1, valueScale, 0);
        Core.normalize(hsvChannels.get(2), hsvChannels.get(2), 0, 255, Core.NORM_MINMAX);

        Core.merge(hsvChannels, hsvMat);
        Imgproc.cvtColor(hsvMat, imageMat, Imgproc.COLOR_HSV2BGR);

        changedBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageMat, changedBitmap);
        imageView.setImageBitmap(changedBitmap);
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
