package com.example.hprestaurant.Models;

import com.google.gson.Gson;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeFilterModel {
    private LocalTime time_available;


    public TimeFilterModel(LocalTime time_available) {
        this.time_available = time_available;
    }

    public LocalTime getTime_available() {
        return time_available;
    }

    public void setTime_available(LocalTime time_available) {
        this.time_available = time_available;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
