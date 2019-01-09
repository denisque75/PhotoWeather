package com.mobindustry.photoweatherapp.ui.camera_view;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.mobindustry.photoweatherapp.ui.camera_view.presenter.CameraWeatherPresenter;
import com.mobindustry.photoweatherapp.ui.custom_layout.PhotoSurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Camera1Activity extends AppCompatActivity implements CameraView {
    private static final int REQUEST_CAMERA_PERMISSION = 123;
    private static final String TAG = "Camera1Activity";

    private Camera camera;
    private Decorator decorator;
    private CameraWeatherPresenter presenter;

    @BindView(R.id.photoSurfaceView_camera1)
    PhotoSurfaceView surfaceView;
    @BindView(R.id.imageView_camera1_photoButton)
    ImageView photoButton;
    @BindView(R.id.imageView_camera1_temp)
    ImageView tempImageView;
    @BindView(R.id.textView_camera1_temp)
    TextView tempTextView;
    @BindView(R.id.textView_camera1_cityName)
    TextView cityNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ButterKnife.bind(this);

        camera = checkDeviceCamera();
        surfaceView.setCamera(camera);
        decorator = new Decorator(getWindowManager().getDefaultDisplay());

        initPresenter();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);

            return;
        }

        photoButton.setOnClickListener(this::takePhotoListener);

    }

    private void initPresenter() {
        LocationRepository locationRepository = new CurrentLocationRepository();
        WeatherApi weatherApi = new RetrofitCreator(RetrofitCreator.createRetrofit()).create(WeatherApi.class);
        ForecastRepository forecastRepository = new NetworkForecastRepository(weatherApi);
        PhotoRepository photoRepository = new PicassoRepository();
        WeatherUseCase weatherUseCase = new FindWeatherUseCase(locationRepository, forecastRepository, photoRepository);
        presenter = new CameraWeatherPresenter(weatherUseCase, this);
    }

    private Camera checkDeviceCamera() {
        return Camera.open();
    }

    private void takePhotoListener(View view) {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d(TAG, "onPictureTaken: photo was taken");
                data = decorator.draw(data, findViewById(R.id.constraintLayout_camera1_viewContainer), camera.getParameters().getPictureSize());
                save(data);
                Toast.makeText(Camera1Activity.this, "Saved", Toast.LENGTH_SHORT).show();
                camera.startPreview();
            }

            private void save(byte[] bytes){
                final File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");
                try (OutputStream os = new FileOutputStream(file)) {
                    os.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

                    photoButton.setOnClickListener(this::takePhotoListener);
                } else {
                    // TODO: 10/2/18 turn off swipe layout
                }
            }
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showForecastOnCameraView(Forecast forecast) {
        tempTextView.setText(forecast.getWeatherList().get(0).getTemp() + "");
        cityNameTextView.setText(forecast.getCity());
        presenter.loadImage(tempImageView, forecast.getWeatherList().get(0).getIcon());
    }

    @Override
    public void showProgress(boolean onShow) {

    }
}
