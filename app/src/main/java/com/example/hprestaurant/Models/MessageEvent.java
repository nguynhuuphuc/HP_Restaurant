package com.example.hprestaurant.Models;

import java.util.Date;
import java.util.List;

public class MessageEvent {
    private List<ReservationModel> reservationModels;
    private int table_id;
    private boolean in_progress;
    private Date dateUpdate;

    public Date getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(Date dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }

    public boolean isIn_progress() {
        return in_progress;
    }

    public void setIn_progress(boolean in_progress) {
        this.in_progress = in_progress;
    }

    public MessageEvent() {
    }

    public MessageEvent(int table_id, boolean in_progress,Date date) {
        this.table_id = table_id;
        this.in_progress = in_progress;
        this.dateUpdate = date;
    }

    public MessageEvent(List<ReservationModel> reservationModels) {
        this.reservationModels = reservationModels;
    }

    public List<ReservationModel> getReservationModels() {
        return reservationModels;
    }

    public void setReservationModels(List<ReservationModel> reservationModels) {
        this.reservationModels = reservationModels;
    }
}
