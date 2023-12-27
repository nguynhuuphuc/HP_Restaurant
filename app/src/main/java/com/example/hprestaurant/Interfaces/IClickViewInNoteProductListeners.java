package com.example.hprestaurant.Interfaces;


import com.example.hprestaurant.Models.ProductInReservationModel;

public interface IClickViewInNoteProductListeners {
    public final static String REMOVE_PRODUCT = "remove";
    public final static String SAVED_NOTE = "saved";
    void onClickListener(ProductInReservationModel model, String command);
}
