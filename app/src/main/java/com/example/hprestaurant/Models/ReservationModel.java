package com.example.hprestaurant.Models;

import java.util.Date;

public class ReservationModel{
    private int id;
    private int customer_id;
    private Date reservation_time;
    private int table_id;
    private String after_available;
    private String before_available;
    private String table_name;
    private String status;

    public ReservationModel() {
    }

    public ReservationModel(int id, int customer_id, Date reservation_time, int table_id) {
        this.id = id;
        this.customer_id = customer_id;
        this.reservation_time = reservation_time;
        this.table_id = table_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBefore_available() {
        return before_available;
    }

    public void setBefore_available(String before_available) {
        this.before_available = before_available;
    }

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public String getAfter_available() {
        return after_available;
    }

    public void setAfter_available(String after_available) {
        this.after_available = after_available;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public Date getReservation_time() {
        return reservation_time;
    }

    public void setReservation_time(Date reservation_time) {
        this.reservation_time = reservation_time;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }
}
