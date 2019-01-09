package com.mobindustry.photoweatherapp.core.repositories.photo;

import android.widget.ImageView;

public interface PhotoRepository {

    void loadImage(ImageView imageView, String url);
}
