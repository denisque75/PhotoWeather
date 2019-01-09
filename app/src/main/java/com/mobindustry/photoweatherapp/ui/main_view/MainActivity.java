package com.mobindustry.photoweatherapp.ui.main_view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobindustry.photoweatherapp.ui.camera_view.Camera1Activity;
import com.mobindustry.photoweatherapp.R;
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
import com.mobindustry.photoweatherapp.ui.camera_view.Camera2Activity;
import com.mobindustry.photoweatherapp.ui.main_view.presenter.MainScreenPresenter;
import com.mobindustry.photoweatherapp.ui.main_view.presenter.MainViewPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainView {
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_READ_LOCATION = 2;

    @BindView(R.id.main_activity__cityName)
    TextView cityNameTextView;
    @BindView(R.id.main_activity__temp)
    TextView tempTextView;
    @BindView(R.id.main_activity__temp_image)
    ImageView tempImgView;
    @BindView(R.id.main_activity__photo_button)
    Button photoButton;
    @BindView(R.id.main_activity__progress_bar)
    ProgressBar progressBar;

    private MainViewPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        photoButton.setOnClickListener(this::onPhotoButtonClicked);

        LocationRepository locationRepository = new CurrentLocationRepository();
        WeatherApi weatherApi = new RetrofitCreator(RetrofitCreator.createRetrofit()).create(WeatherApi.class);
        ForecastRepository forecastRepository = new NetworkForecastRepository(weatherApi);
        PhotoRepository photoRepository = new PicassoRepository();
        WeatherUseCase weatherUseCase = new FindWeatherUseCase(locationRepository, forecastRepository, photoRepository);
        presenter = new MainScreenPresenter(weatherUseCase, this);

        requestPermissions();

    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_READ_LOCATION);


        } else {
            presenter.findWeatherByPosition(this);
        }

    }



    private void onPhotoButtonClicked(View view) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            startActivity(new Intent(this, Camera2Activity.class));
        } else {
            startActivity(new Intent(this, Camera1Activity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "onRequestPermissionsResult: permissions are granted");
                    presenter.findWeatherByPosition(this);
                    photoButton.setEnabled(true);
                } else {
                    cityNameTextView.setText("Turn on permissions to use the app.");
                    photoButton.setEnabled(false);
                }
                return;
            }
        }

    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showResult(Forecast forecast) {
        cityNameTextView.setText(forecast.getCity());
        tempTextView.setText(forecast.getWeatherList().get(0).getTemp() + "C");
        presenter.showImage(tempImgView, forecast.getWeatherList().get(0).getIcon());
    }

    @Override
    public void showProgress(boolean onShow) {
        progressBar.setVisibility(onShow ? View.VISIBLE : View.GONE);
    }
}
