package com.example.cvd;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class PhotoBookAdapter extends RecyclerView.Adapter<PhotoBookAdapter.MyViewHolder> {

    Context context;
    ArrayList<PhotoBook> photoList = new ArrayList<>();

    PhotoBookAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.photo_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PhotoBook photo = photoList.get(position);
        holder.name_text.setText(photo.getTitle()); // 사진 제목 설정
        holder.imageView.setImageURI(photo.getUri()); // URI를 통해 이미지 설정


        // 상세 화면으로 이동
        holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(context, detail.class);
                 intent.putExtra("title", photo.getTitle());
                 intent.putExtra("imageUri", photo.getUri().toString());
                 context.startActivity(intent);
             }
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    /**
     * 아이템 삭제
     * @param position 위치
     */
    public void removeItem(int position) {
        photoList.remove(position);
    }

    public void addItem(PhotoBook item) {
        photoList.add(item);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name_text;
        ImageView imageView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name_text = itemView.findViewById(R.id.name_text);
            imageView = itemView.findViewById(R.id.photo_image);
        }
    }
    public void clearItems() {
        photoList.clear();
    }
}
