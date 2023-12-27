package com.example.hprestaurant.Models;

import java.io.Serializable;
import java.util.Date;

public class TableModel implements Serializable {

    private int table_id;
    private int capacity;
    private int location_id;
    private String table_name;
    private int reservation_id;
    private Date reservation_time;
    private boolean in_progress;

    public TableModel() {
    }

    public boolean isIn_progress() {
        return in_progress;
    }

    public void setIn_progress(boolean in_progress) {
        this.in_progress = in_progress;
    }

    public TableModel(int table_id, int capacity, int location_id, String table_name, int reservation_id, Date reservation_time) {
        this.table_id = table_id;
        this.capacity = capacity;
        this.location_id = location_id;
        this.table_name = table_name;
        this.reservation_id = reservation_id;
        this.reservation_time = reservation_time;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getLocation_id() {
        return location_id;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
    }

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public int getReservation_id() {
        return reservation_id;
    }

    public void setReservation_id(int reservation_id) {
        this.reservation_id = reservation_id;
    }

    public Date getReservation_time() {
        return reservation_time;
    }

    public void setReservation_time(Date reservation_time) {
        this.reservation_time = reservation_time;
    }
}
