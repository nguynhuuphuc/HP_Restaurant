package com.example.hprestaurant.Models.Response;

import com.example.hprestaurant.Models.MessageModel;
import com.example.hprestaurant.Models.ReservationModel;

import java.util.Date;

public class WebsocketResponse {
    private String activity;
    private int toCustomer;
    private ReservationModel reservation;
    private boolean tableInProgress;
    private int tableId;
    private String from;
    private Date dateBooking;
    private MessageModel conversationMessage;
    public MessageModel getConversationMessage() {
        return conversationMessage;
    }

    public void setConversationMessage(MessageModel conversationMessage) {
        this.conversationMessage = conversationMessage;
    }



    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isTableInProgress() {
        return tableInProgress;
    }

    public void setTableInProgress(boolean tableInProgress) {
        this.tableInProgress = tableInProgress;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public Date getDateBooking() {
        return dateBooking;
    }

    public void setDateBooking(Date dateBooking) {
        this.dateBooking = dateBooking;
    }



    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public int getToCustomer() {
        return toCustomer;
    }

    public void setToCustomer(int toCustomer) {
        this.toCustomer = toCustomer;
    }

    public ReservationModel getReservation() {
        return reservation;
    }

    public void setReservation(ReservationModel reservation) {
        this.reservation = reservation;
    }
}
