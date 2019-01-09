package com.mobindustry.photoweatherapp.ui.camera_view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobindustry.photoweatherapp.R;
import com.mobindustry.photoweatherapp.core.camera.decorator.Decorator;
import com.mobindustry.photoweatherapp.core.entity.Forecast;
import com.mobindustry.photoweatherapp.core.repositories.location.CurrentLocationRepository;
import com.mobindustry.photoweatherapp.core.repositories.location.LocationRepository;
import com.mobindustry.photoweatherapp.core.repositories.photo.PhotoRepository;
import com.mobindustry.photoweatherapp.core.repositories.photo.PicassoRepository;
import com.mobindustry.photoweatherapp.core.repositories.weather.ForecastRepository;
import com.mobindustry.photoweatherapp.core.repositories.weather.NetworkForecastRepository;
import com.mobindustry.photoweatherapp.core.retrofit.RetrofitCreator;
import com.mobindustry.photoweatherapp.core.retrofit.WeatherApi;
import com.mobindustry.photoweatherapp.core.use_cases.FindWeatherUseCase;
import com.mobindustry.photoweatherapp.core.use_cases.WeatherUseCase;
import com.mobindustry.photoweatherapp.core.utils.FileSaver;
import com.mobindustry.photoweatherapp.core.utils.PhotoUtils;
import com.mobindustry.photoweatherapp.ui.camera_view.presenter.CameraWeatherPresenter;
import com.mobindustry.photoweatherapp.ui.camera_view.presenter.WeatherPresenter;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Camera2Activity extends AppCompatActivity implements CameraView {
    private static final String TAG = "Camera2Activity";
    private static final int REQUEST_CAMERA_PERMISSION = 123;

    private final int PICTURE_WIDTH = 600;
    private final int PICTURE_HEIGHT = 800;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @BindView(R.id.textureView_camera2_cameraPreview)
    TextureView textureView;
    @BindView(R.id.imageView_camera2_photoButton)
    ImageView photoButton;
    @BindView(R.id.imageView_camera2_tempImage)
    ImageView tempImageView;
    @BindView(R.id.textView_camera2_temp)
    TextView tempTextView;
    @BindView(R.id.textView_camera2_cityName)
    TextView cityNameTextView;
    @BindView(R.id.progressBar_camera2)
    ProgressBar progressBar;
    @BindView(R.id.constraintLayout_camera_viewContainer)
    View weatherContainer;

    @BindColor(android.R.color.white)
    int containerBackgroundColor;

    private String cameraId;
    private Size imageDimensions;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Decorator decorator;

    private boolean isDialogOnScreen = false;

    private WeatherPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera2);

        ButterKnife.bind(this);

        hideToolbar();
        initPresenter();

        decorator = new Decorator(getWindowManager().getDefaultDisplay());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);

            return;
        }
        presenter.findWeatherByLocation(this);

        textureView.setSurfaceTextureListener(textureListener);
        photoButton.setOnClickListener(this::takePhotoListener);

    }

    private void takePhotoListener(View view) {
        takePhoto();
    }

    private void initPresenter() {
        LocationRepository locationRepository = new CurrentLocationRepository();
        WeatherApi weatherApi = new RetrofitCreator(RetrofitCreator.createRetrofit()).create(WeatherApi.class);
        ForecastRepository forecastRepository = new NetworkForecastRepository(weatherApi);
        PhotoRepository photoRepository = new PicassoRepository();
        WeatherUseCase weatherUseCase = new FindWeatherUseCase(locationRepository, forecastRepository, photoRepository);
        presenter = new CameraWeatherPresenter(weatherUseCase, this);
    }

    private void hideToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "onRequestPermissionsResult: permissions are granted");
                    presenter.findWeatherByLocation(this);
                    textureView.setSurfaceTextureListener(textureListener);

                    photoButton.setOnClickListener(this::takePhotoListener);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void openCamera2() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "camera is open");
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert configurationMap != null;
            imageDimensions = configurationMap.getOutputSizes(SurfaceTexture.class)[0];

            setPreviewSize();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);

                return;
            }

            cameraManager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    int previewWidth, previewHeight;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setPreviewSize() {
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);

        double percent = imageDimensions.getHeight() * 100 / p.x;
        percent *= 0.01;
        previewWidth = (int) (imageDimensions.getHeight() / percent);
        previewHeight = (int) (imageDimensions.getWidth() / percent);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(previewWidth, previewHeight);
        layoutParams.startToStart = R.id.constraintLayout_camera2_parent;
        layoutParams.endToEnd = R.id.constraintLayout_camera2_parent;
        layoutParams.topToTop = R.id.constraintLayout_camera2_parent;
        layoutParams.bottomToBottom = R.id.constraintLayout_camera2_parent;
        textureView.setLayoutParams(layoutParams);

        Log.d(TAG, "display dimensions" + p);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void takePhoto() {
        if (cameraDevice == null) {
            Log.e(TAG, "takePhoto: camera device is null");
            return;
        }

        try {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
            Size[] jpegSizes = null;
            if (cameraCharacteristics != null) {
                jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

            int width = PICTURE_WIDTH;
            int height = PICTURE_HEIGHT;

            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>();
            //outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            outputSurfaces.add(imageReader.getSurface());

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");
            ImageReader.OnImageAvailableListener readerListener = reader -> {
                try (Image image = reader.acquireLatestImage()) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();

                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    bytes = PhotoUtils.rotatePhoto(bytes);
                    if (isForecastUpdated()) {
                        bytes = decorator.draw(bytes, findViewById(R.id.constraintLayout_camera_viewContainer), previewWidth,previewHeight);
                    }
                    showDialogScreen(file.getPath(), bytes);
                }
            };


            imageReader.setOnImageAvailableListener(readerListener, backgroundHandler);
            final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureCallback, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "onConfigureFailed: ");

                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private boolean isForecastUpdated() {
        return !cityNameTextView.getText().toString().equals("");
    }

    private void showDialogScreen(String path, byte[] bytes) {

        ImageView imageView = (ImageView) getLayoutInflater().inflate(R.layout.dialog_view, null);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap mutableBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        imageView.setImageBitmap(mutableBitmap);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar);
        AlertDialog dialog = builder
                .setTitle(R.string.save_photo_dialog_title)
                .setView(imageView)
                .setPositiveButton(R.string.save, (dialog1, which) -> {
                    FileSaver.saveFile(path, bytes);
                    createCameraPreview();
                    Toast.makeText(Camera2Activity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                    isDialogOnScreen = false;
                })
                .setNegativeButton(R.string.cancel, ((dialog1, which) -> {
                    createCameraPreview();
                    Toast.makeText(Camera2Activity.this, R.string.canceled, Toast.LENGTH_SHORT).show();
                    isDialogOnScreen = false;
                })).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog1) {
                isDialogOnScreen = true;
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        });
        dialog.show();
    }


    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera2();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreview() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
            Surface surface = new Surface(surfaceTexture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    cameraCaptureSession = session;
                    updatePreview();

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(Camera2Activity.this, "Configuration changed", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updatePreview() {
        if (cameraDevice == null) {
            Log.e(TAG, "update preview error");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;

        }
    };

    @Override
    public void onBackPressed() {
        if (isDialogOnScreen) {
            isDialogOnScreen = false;
            createCameraPreview();
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);

            return;
        }
        if (textureView.isAvailable()) {
            openCamera2();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showForecastOnCameraView(Forecast forecast) {
        weatherContainer.setBackgroundColor(getResources().getColor(containerBackgroundColor));
        tempTextView.setText(String.valueOf(forecast.getWeatherList().get(0).getTemp()));
        cityNameTextView.setText(forecast.getCity());
        presenter.loadImage(tempImageView, forecast.getWeatherList().get(0).getIcon());
    }

    @Override
    public void showProgress(boolean onShow) {
        progressBar.setVisibility(onShow ? View.VISIBLE : View.GONE);
    }
}
