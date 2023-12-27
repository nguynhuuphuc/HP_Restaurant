package com.example.hprestaurant.Models.Request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class DateTimeRequest {
    private LocalDate localDate;
    private LocalTime localTime;
    private String day;
    private String time;

    public DateTimeRequest(LocalDate localDate) {
        this.localDate = localDate;
        setDay(localDate.toString());
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
