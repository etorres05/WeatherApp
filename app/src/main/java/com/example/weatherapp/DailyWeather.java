package com.example.weatherapp;

import android.icu.text.SimpleDateFormat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyWeather {
    int maxTemp;
    int minTemp;
    String condition;
    String day;
    List<HourlyWeather> hours = new ArrayList<>();


    public DailyWeather(JSONObject day) throws JSONException {
        this.maxTemp = (int) Math.ceil(day.getJSONObject("day").getDouble("maxtemp_f"));
        this.minTemp = (int) Math.ceil(day.getJSONObject("day").getDouble("mintemp_f"));
        this.condition = day.getJSONObject("day").getJSONObject("condition").getString("text");
        this.day = getDayName(day.getString("date"));
        setHourlyForecast(day.getJSONArray("hour"));
    }

    private void setHourlyForecast(JSONArray hourly) throws JSONException {
        for(int i = 0; i < hourly.length(); i++){
            hours.add(new HourlyWeather(hourly.getJSONObject(i)));
        }
    }

    private String getDayName(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = dateFormat.parse(dateString);
            Log.d("Date", date.toString());

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            String dayName = dayFormat.format(date);
            Log.d("Date", dayName);

            return dayName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
