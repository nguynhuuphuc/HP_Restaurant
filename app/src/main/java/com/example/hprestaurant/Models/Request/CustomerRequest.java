package com.example.hprestaurant.Models.Request;

public class CustomerRequest {
    private int customer_id;

    public CustomerRequest(int customer_id) {
        this.customer_id = customer_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }
}
