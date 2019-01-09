package com.mobindustry.photoweatherapp.core.camera.decorator;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;

import android.view.Display;
import android.view.View;

import java.io.ByteArrayOutputStream;

public class Decorator {
    private final Display display;

    public Decorator(Display display) {
        this.display = display;
    }

    public byte[] draw(byte[] bytes, View container, Camera.Size imageDimensions) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        Bitmap mutableBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        Canvas canvas = new Canvas(mutableBitmap);


        double widthPercent = calculateWidthScale(container);
        float heightPercent = calculateHeight(container);

        Bitmap bm = getBitmapFromView(container);
        Bitmap bitmapWeather = Bitmap.createScaledBitmap(bm, (int) (imageDimensions.height * widthPercent), (int) (imageDimensions.width * widthPercent * heightPercent), true);
        canvas.drawBitmap(bitmapWeather, 0, 0 ,null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        return baos.toByteArray();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public byte[] draw(final byte[] bytes, View container, int widthPreview, int heightPreview) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        //Bitmap mutableBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(canvas.getWidth()/(float)widthPreview, canvas.getHeight()/(float)heightPreview);
        container.draw(canvas);


//        double widthPercent = calculateWidthScale(container);
//        float heightPercent = calculateHeight(container);
//        double height = imageDimensions.getWidth() * widthPercent;
//
//        Bitmap bm = getBitmapFromView(container);
//        Bitmap bitmapWeather = Bitmap.createScaledBitmap(bm, (int) (imageDimensions.getHeight() * widthPercent), (int) (heightPercent * imageDimensions.getWidth()), true);
//        canvas.drawBitmap(bitmapWeather, 0, 0 ,null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        return baos.toByteArray();
    }

    private float calculateHeight(View container) {
        Point size = new Point();
        display.getSize(size);

        int containerHeight = container.getHeight();
        int displayHeight = size.y;

        float heightPercent = containerHeight * 100 / displayHeight;

        return heightPercent * 0.01f;

    }

    private double calculateWidthScale(View container) {
        Point size = new Point();
        display.getSize(size);

        int viewWidth = container.getWidth();
        int screenWidth = size.x;

        double widthPercent = viewWidth * 100 / screenWidth;

        return widthPercent * 0.01f;
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
}
