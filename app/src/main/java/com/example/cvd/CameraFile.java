package com.example.cvd;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraFile extends AppCompatActivity {

    Uri uri;

    ImageView imageView;
    static File currentPhotoFile;
    static Uri currentPhotoUri;
    static String currentPhotoPath;
    static String currentPhotoFileName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        imageView = findViewById(R.id.imageView);

        String imageUriString = getIntent().getStringExtra("imageUri");
        currentPhotoUri = Uri.parse(imageUriString);
        imageView.setImageURI(currentPhotoUri);

        // 이미지 파일을 갤러리에 추가
        galleryAddPic(currentPhotoUri, currentPhotoFileName);
    }
    
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//        imageView = findViewById(R.id.imageView);
//
//        String imageUriString = getIntent().getStringExtra("imageUri");
//        Uri image = Uri.parse(imageUriString);
//
//        imageView.setImageURI(image);
//
//
//
//
//
//
//
//
//
//
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (cameraIntent.resolveActivity((getPackageManager())) != null) {
//
//                    File imageFile = null;
//                    try {
//                        imageFile = createImageFile();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                    if (imageFile != null) {
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
//                        activityResultCamera.launch(cameraIntent);
//                    }
//                }
//            }
//
//
//
//    ActivityResultLauncher<Intent> activityResultCamera = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if (result.getResultCode() == Activity.RESULT_OK /*&& result.getData() != null*/) {
//
//                        Intent intent = result.getData();
//                        Uri uri = intent.getData();
//
//                        //로깅
//                        Log.i("checking", String.valueOf(uri));
//
//
//                        //edit 화면으로 넘어가는 코드
//                        Intent editIntent = new Intent(getApplicationContext(), edit.class);
//                        assert uri != null;
//                        editIntent.putExtra("imageUri", uri.toString()); //imageUri.toString
//                        startActivity(editIntent);
//                    }
//                }
//            });
//
//    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if (result.getResultCode() == Activity.RESULT_OK /*&& result.getData() != null*/) {
//
//                        Intent intent = result.getData();
//                        Uri uri = intent.getData();
//
//                        //로깅
//                        Log.i("checking", String.valueOf(uri));
//
//
//                        //add 화면으로 넘어가는 코드
//                        Intent editIntent = new Intent(getApplicationContext(), edit.class);
//                        assert uri != null;
//                        editIntent.putExtra("imageUri", uri.toString()); //imageUri.toString
//                        startActivity(editIntent);
//                    }
//                }
//            });
//
//
//    //이미지파일 생성
//    private File createImageFile() throws IOException {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//
//        File imagePath = getExternalFilesDir("images");
//
//        File newFile = File.createTempFile(imageFileName, ".jpg", imagePath);
//
//        currentPhotoFile = newFile;
//        currentPhotoFileName = newFile.getName();
//        currentPhotoPath = newFile.getAbsolutePath();
//
//        try {
//            currentPhotoUri = FileProvider.getUriForFile(this,
//                    getApplicationContext().getPackageName() + ".fileprovider",
//                    newFile);
//        } catch (Exception ex) {
//            Log.d("FileProvider", ex.getMessage());
//            ex.printStackTrace();
//            throw ex;
//        }
//
//        return newFile;
//    }

    //갤러리에 이미지 파일 생성
    private Uri galleryAddPic(Uri srcImageFileUri ,String srcImageFileName) {
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, srcImageFileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/MyImages"); // 두개의 경로[DCIM/ , Pictures/]만 가능함 , 생략시 Pictures/ 에 생성됨
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 1); //다른앱이 파일에 접근하지 못하도록 함(Android 10 이상)
        Uri newImageFileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        try {
            AssetFileDescriptor afdInput = contentResolver.openAssetFileDescriptor(srcImageFileUri, "r");
            AssetFileDescriptor afdOutput = contentResolver.openAssetFileDescriptor(newImageFileUri, "w");
            FileInputStream fis = afdInput.createInputStream();
            FileOutputStream fos = afdOutput.createOutputStream();

            byte[] readByteBuf = new byte[1024];
            while(true){
                int readLen = fis.read(readByteBuf);
                if (readLen <= 0) {
                    break;
                }
                fos.write(readByteBuf,0,readLen);
            }

            fos.flush();
            fos.close();
            afdOutput.close();

            fis.close();
            afdInput.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        contentValues.clear();
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0); //다른앱이 파일에 접근할수 있도록 함
        contentResolver.update(newImageFileUri, contentValues, null, null);
        return newImageFileUri;
    }

}
