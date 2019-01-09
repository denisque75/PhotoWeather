package com.mobindustry.photoweatherapp.ui.custom_layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

public class PhotoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    private SurfaceHolder surfaceHolder;

    public PhotoSurfaceView(Context context) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public PhotoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
    }
}
