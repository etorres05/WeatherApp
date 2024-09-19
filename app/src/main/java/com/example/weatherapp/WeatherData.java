package com.example.weatherapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherData {

    String location;
    String condition; //overcast, sunny, cloudy, etc..
    int currentTemp;
    int maxTemp;
    int minTemp;
    WeatherForecast forecast;


    public WeatherData(JSONObject response) throws JSONException {
        this.location = response.getJSONObject("location").getString("name");
        this.condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
        this.currentTemp = response.getJSONObject("current").getInt("temp_f");

        Log.d("New WeatherData Obj:", "location: " + location + "Condition: " + condition + "Current Temp: " + currentTemp);

        JSONArray dayArray = response.getJSONObject("forecast").getJSONArray("forecastday");
        this.maxTemp = (int) Math.ceil(dayArray.getJSONObject(0).getJSONObject("day").getDouble("maxtemp_f"));
        this.minTemp = (int) Math.ceil(dayArray.getJSONObject(0).getJSONObject("day").getDouble("mintemp_f"));
        forecast = new WeatherForecast(dayArray);

    }
}
