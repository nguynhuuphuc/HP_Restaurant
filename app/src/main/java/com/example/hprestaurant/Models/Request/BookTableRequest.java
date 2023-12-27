package com.example.hprestaurant.Models.Request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class BookTableRequest {
    private int table_id;
    private int customer_id;
    private String date;
    private int quantity_people;
    private int reservation_id;
    private Date created_time;


    public Date getCreated_time() {
        return created_time;
    }

    public void setCreated_time(Date created_time) {
        this.created_time = created_time;
    }

    public int getReservation_id() {
        return reservation_id;
    }

    public void setReservation_id(int reservation_id) {
        this.reservation_id = reservation_id;
    }

    public BookTableRequest(int table_id, int customer_id, LocalDate day, LocalTime time) {
        this.table_id = table_id;
        this.customer_id = customer_id;
        setDate(day.toString() + " " + time);
    }
    public BookTableRequest(LocalDate day, LocalTime time) {
        setDate(day.toString() + " " + time);
    }
    public BookTableRequest(int table_id, int customer_id, LocalDate day, LocalTime time,int quantity_people) {
        this.table_id = table_id;
        this.customer_id = customer_id;
        this.quantity_people = quantity_people;
        setDate(day.toString() + " " + time);
    }

    public BookTableRequest(int table_id, int customer_id) {
        this.table_id = table_id;
        this.customer_id = customer_id;
    }

    public BookTableRequest(int table_id, int customer_id, String date) {
        this.table_id = table_id;
        this.customer_id = customer_id;
        this.date = date;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
