package com.example.cvd;

import android.net.Uri;

public class PhotoBook {
    private String title;
    private Uri uri;

    public PhotoBook(String title, Uri uri) {
        this.title = title;
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
