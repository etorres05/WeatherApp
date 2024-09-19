package com.example.weatherapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HourlyWeather {
    String time;
    int temp_f;
    String condition;

    public HourlyWeather(JSONObject hour) throws JSONException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //create formatter for time string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dt = LocalDateTime.parse(hour.getString("time"), formatter);
            DateTimeFormatter standardHourFormatter = DateTimeFormatter.ofPattern("hh a");

            //set time

            time = dt.format(standardHourFormatter);
            temp_f = (int) Math.ceil(hour.getDouble("temp_f"));
            condition = hour.getJSONObject("condition").getString("text");


        } else{
            throw new RuntimeException("Android SDK not up to date");
        }
    }
}
