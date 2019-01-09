package com.mobindustry.photoweatherapp.core.repositories.photo;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class PicassoRepository implements PhotoRepository {

    @Override
    public void loadImage(ImageView imageView, String url) {
        Picasso.get().load(url).into(imageView);
    }
}
