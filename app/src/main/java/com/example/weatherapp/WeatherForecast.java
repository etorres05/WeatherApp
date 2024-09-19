package com.example.weatherapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

public class WeatherForecast {
    final int DAYS = 10;
    DailyWeather[] forecastArray = new DailyWeather[DAYS];

    public WeatherForecast(JSONArray dayArray) throws JSONException {
        for(int i = 0; i < 10; i++){
            forecastArray[i] = new DailyWeather(dayArray.getJSONObject(i));
        }
    }
}
