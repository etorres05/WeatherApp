package com.example.weatherapp;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.content.pm.PackageManager;
import android.hardware.lights.Light;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.Manifest;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private WeatherData weather;
    final int HOURS_IN_FORECAST = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Get last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String weatherLocation = latitude + "," + longitude;
                            Log.d("Longitude and Latitude", weatherLocation);

                            getCurrentWeather(weatherLocation);
                        }
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting the location
                Log.d("Success", "Location access granted");
            } else {
                // Permission denied, handle accordingly
                Log.d("Failure", "location request denied");
            }
        }
    }


    private void getCurrentWeather(String location) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String api_key = "8241b250f78f45f4904234442241609";
        String url = String.format("https://api.weatherapi.com/v1/forecast.json?key=%s&q=%s&days=10&aqi=yes&alerts=yes", api_key, location);
        Log.d("API URL", url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//                    @Override
                public void onResponse(JSONObject response) {
                    Log.d("Weather Retrieved", response.toString());
                    try {
                        weather = new WeatherData(response);
                        addUI(weather);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                }, new Response.ErrorListener() {
//                    @Override
                public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        Log.d("Failed to get weather!", "Could not make call: " + error.toString());
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void addUI(WeatherData weather) throws JSONException {
        TextView location = (TextView) findViewById(R.id.location);
        TextView currentTemp = (TextView) findViewById(R.id.currentTemp);
        TextView currentCondition = (TextView) findViewById(R.id.currentCondition);

        location.setText(weather.location);
        currentTemp.setText(String.valueOf(weather.currentTemp));
        currentCondition.setText(weather.condition);

        //set hourly widget
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setHourlyWidget();
        }
        
        setTenDayForecastWidget();
    }

    private void setTenDayForecastWidget() {
        // Find the LinearLayout inside the HorizontalScrollView
        LinearLayout linearLayout = findViewById(R.id.tenDayWidget);

        // Inflate and add items dynamically
        LayoutInflater inflater = LayoutInflater.from(this);

        DailyWeather[] forecastArray = weather.forecast.forecastArray;

        for (int i =0; i < forecastArray.length; i++) {
            DailyWeather dailyWeather = forecastArray[i];
            View itemView = inflater.inflate(R.layout.ten_day_widget, linearLayout, false);

            TextView day = itemView.findViewById(R.id.day);
            ImageView condition = itemView.findViewById(R.id.forecast_condition_image);
            TextView temp_low = itemView.findViewById(R.id.daily_low);
            TextView temp_high = itemView.findViewById(R.id.daily_high);

            if(i == 0){
                day.setText(String.format("%s", "Today"));
            } else {
                day.setText(dailyWeather.day);
            }

            temp_low.setText(String.valueOf(dailyWeather.minTemp));
            temp_high.setText(String.valueOf(dailyWeather.maxTemp));

            // Set the image based on the weather condition
            int imageResource = getImageResourceForCondition(dailyWeather.condition);
            condition.setImageResource(imageResource);


            linearLayout.addView(itemView);
        }
    }

    private int getImageResourceForCondition(String condition) {
        return switch (condition.toLowerCase()) {
            case "sunny" -> R.drawable.sunny;
            case "partly cloudy" -> R.drawable.partly_cloudy;
            case "cloudy" -> R.drawable.cloudy;
            case "overcast" -> R.drawable.cloudy;
            case "mist" -> R.drawable.mist;
            case "light rain shower", "patchy rain possible", "patchy sleet possible",
                 "patchy light drizzle", "light drizzle", "patchy light rain", "light rain",
                 "light sleet", "moderate or heavy sleet" -> R.drawable.light_rain;
            case "light snow showers", "patchy snow possible", "blowing snow", "patchy light snow",
                 "light snow" -> R.drawable.snowing;
            case "light freezing rain", "moderate or heavy showers of ice pellets",
                 "light showers of ice pellets", "moderate or heavy sleet showers",
                 "light sleet showers", "ice pellets", "patchy freezing drizzle possible",
                 "freezing drizzle", "heavy freezing drizzle", "moderate or heavy freezing rain" ->
                    R.drawable.hail;
            case "patchy moderate snow", "moderate snow", "patchy heavy snow", "heavy snow",
                 "moderate or heavy snow showers", "blizzard" -> R.drawable.heavy_snow;
            case "fog", "freezing fog" -> R.drawable.foggy;
            case "torrential rain shower", "moderate or heavy rain shower",
                 "moderate rain at times", "moderate rain", "heavy rain at times", "heavy rain" ->
                    R.drawable.heavy_rain;
            case "moderate or heavy rain with thunder" -> R.drawable.thunderstorm;
            default -> //used for:  "moderate or heavy snow with thunder", "patchy light snow with thunder", "patchy light rain with thunder", "thundery outbreaks possible"
                    R.drawable.mixed_weather;
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setHourlyWidget() {
        // Find the LinearLayout inside the HorizontalScrollView
        LinearLayout linearLayout = findViewById(R.id.linearLayout);

        //get hourly forecast for today and tomorrow
        List<HourlyWeather> hourlyForecastToday = weather.forecast.forecastArray[0].hours;
        List<HourlyWeather> hourlyForecastTomorrow = weather.forecast.forecastArray[1].hours;

        // Inflate and add items dynamically
        LayoutInflater inflater = LayoutInflater.from(this);

        LocalDateTime current_time = LocalDateTime.now();
        int current_hour = current_time.getHour();

        for (int i = current_hour; i < HOURS_IN_FORECAST + current_hour; i++) {
            View itemView = inflater.inflate(R.layout.hourly_weather, linearLayout, false);

            TextView hourly_temp = itemView.findViewById(R.id.hourly_temp);
            ImageView hourly_condition = itemView.findViewById(R.id.hourly_condition_image);
            TextView hourly_time = itemView.findViewById(R.id.hourly_time);

            //if this is the current hour, the current temp will show and time will say 'now'
            if(i == current_hour){
                hourly_time.setText(String.format("%s", "Now"));
                hourly_temp.setText(String.valueOf(weather.currentTemp));

                // Set the image based on the weather condition
                int imageResource = getImageResourceForCondition(weather.condition);
                hourly_condition.setImageResource(imageResource);
                continue;
            }

            //all other hours will use hourly forecast from api
            if(i < HOURS_IN_FORECAST) {
                hourly_time.setText(hourlyForecastToday.get(i).time);
                hourly_temp.setText(String.valueOf(hourlyForecastToday.get(i).temp_f));

                // Set the image based on the weather condition
                int imageResource = getImageResourceForCondition(hourlyForecastToday.get(i).condition);
                hourly_condition.setImageResource(imageResource);
            }else{
                hourly_time.setText(hourlyForecastTomorrow.get(i%HOURS_IN_FORECAST).time);
                hourly_temp.setText(String.valueOf(hourlyForecastTomorrow.get(i%HOURS_IN_FORECAST).temp_f));
                // Set the image based on the weather condition
                int imageResource = getImageResourceForCondition(hourlyForecastToday.get(i%HOURS_IN_FORECAST).condition);
                hourly_condition.setImageResource(imageResource);
            }

            linearLayout.addView(itemView);
        }
    }
}