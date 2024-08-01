package com.example.cvd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cvd.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "opencv";
    static Uri currentPhotoUri;
    private static final int REQUEST_CODE = 1;
    private ImageView imageView;
    private boolean permissionChecked = false; // 권한 확인 여부를 저장하는 변수
    PhotoBookDB db;
    ArrayList<PhotoBook> photoList = new ArrayList<>();
    RecyclerView recyclerView;
    PhotoBookAdapter adapter;
    TextView noDataText;

    private ActivityMainBinding binding;

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /* 카메라, 갤러리 연결 코드 추가*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int itemId = item.getItemId();
        if (itemId == R.id.btn_camera) {
            try {
                camera();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        } else if (itemId == R.id.btn_gallery) {
            gallery();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    //메뉴에서 갤러리 버튼 선택 시 수행.
    private void gallery() {
        /*
        Intent galleryintent = new Intent(Intent.ACTION_PICK);
        galleryintent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activityResultPicture.launch(galleryintent);

         */

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        Intent chooser = Intent.createChooser(galleryIntent, "Select Picture");
        activityResultPicture.launch(chooser);
    }
    /*
    //메뉴에서 카메라 버튼 선택 시 수행.
    private void camera() throws IOException {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity((getPackageManager())) != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                activityResultCamera.launch(cameraIntent);
            }
        }
    */


    private void camera() throws IOException {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                activityResultCamera.launch(cameraIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("images");
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoUri = FileProvider.getUriForFile(this,
                "com.example.cvd.fileprovider",
                image);
        return image;
    }


    ActivityResultLauncher<Intent> activityResultPicture = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK /*&& result.getData() != null*/) {

                        Intent intent = result.getData();
                        Uri uri = intent.getData();

                        //로깅
                        Log.i("checking", String.valueOf(uri));


                        // edit 화면으로 넘어가는 코드
                        if (uri != null) {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Intent editIntent = new Intent(getApplicationContext(), edit.class);
                            editIntent.putExtra("imageUri", uri.toString());
                            startActivity(editIntent);
                        }
                    }
                }
            });


    ActivityResultLauncher<Intent> activityResultCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK /*&& result.getData() != null*/) {

                    /*    Intent intent = result.getData();
                        Uri uri = intent.getData();

                        //로깅
                        Log.i("checking", String.valueOf(uri));


                        //add 화면으로 넘어가는 코드
                        Intent addIntent = new Intent(getApplicationContext(), CameraFile.class);
                        assert uri != null;
                        addIntent.putExtra("imageUri", uri.toString()); //imageUri.toString
                        startActivity(addIntent);

                     */
                        Log.i("checking", String.valueOf(currentPhotoUri));
                        Intent addIntent = new Intent(getApplicationContext(), edit.class);
                        addIntent.putExtra("imageUri", currentPhotoUri.toString());
                        startActivity(addIntent);
                    }
                }
            });


    @SuppressLint({"ResourceType", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.menu.menu);
        setContentView(R.layout.activity_main);
        // 이전에 권한을 확인했는지 여부를 확인하고, 확인하지 않았다면 권한을 요청
        if (!permissionChecked) {
            // 권한 확인 및 요청
            getPermission();
        }
        //데이터 유무 텍스트
        noDataText = findViewById(R.id.noData_text);
        //리스트 보여줄 화면
        recyclerView = findViewById(R.id.recyclerView);
        //어뎁터
        adapter = new PhotoBookAdapter(MainActivity.this);
        //어뎁터 등록
        recyclerView.setAdapter(adapter);
        //레이아웃 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        //DB 생성
        db = new PhotoBookDB(MainActivity.this);

        //라디오 버튼
        RadioGroup sortOptions = findViewById(R.id.sortOptions);
        sortOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.sortByTime) {
                    storeDataInArrays("time");
                } else if (checkedId == R.id.sortByTitle) {
                    storeDataInArrays("title");
                }
            }
        });

        //storeDataInArrays("time");
        //데이터 가져오기
        storeDataInArrays();

        //슬라이드로 사진 삭제하기 기능
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                switch(direction){
                    case ItemTouchHelper.LEFT:
                        String deleteId = photoList.get(position).getTitle();
                        //삭제 기능
                        photoList.remove(position);
                        adapter.removeItem(position);
                        adapter.notifyItemRemoved(position);
                        db.deleteData(deleteId);
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder,
                        dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(Color.RED)
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftLabel("삭제")
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }

    /**
     * 데이터 가져오기
     */
    void storeDataInArrays() {
        storeDataInArrays("time");
    }

    void storeDataInArrays(String sortOrder) {
        Cursor cursor;
        if (sortOrder.equals("title")) {
            cursor = db.readAllDataTitle();
        } else {
            cursor = db.readAllData();
        }

        if (cursor.getCount() == 0) {
            noDataText.setVisibility(View.VISIBLE);
        } else {
            noDataText.setVisibility(View.GONE);
            photoList.clear();
            adapter.clearItems();

            while (cursor.moveToNext()) {
                PhotoBook photo = new PhotoBook(cursor.getString(1), Uri.parse(cursor.getString(2)));
                photoList.add(photo);
                adapter.addItem(photo);
            }
            //적용
            adapter.notifyDataSetChanged();
        }
    }



    @SuppressLint("NewApi")
    void getPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1000);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            // 권한 확인 여부를 업데이트하고, 거부된 경우에만 사용자에게 알림
            permissionChecked = true;
            SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean permissionRequestedBefore = sharedPreferences.getBoolean("permission_requested_before", false);

            if (!allPermissionsGranted && !permissionRequestedBefore) {
                new AlertDialog.Builder(this)
                        .setTitle("권한 필요")
                        .setMessage("이 앱을 사용하려면 필요한 권한을 부여해야 합니다.")
                        .setPositiveButton("설정", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("취소", (dialog, which) -> {
                            Toast.makeText(this, "권한이 거부되었습니다. 일부 기능이 제한됩니다.", Toast.LENGTH_SHORT).show();
                        })
                        .show();

                // 권한 요청 알림이 표시된 상태를 저장
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("permission_requested_before", true);
                editor.apply();
            }
        }
    }
}
