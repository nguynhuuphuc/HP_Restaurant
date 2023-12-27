package com.example.hprestaurant.Utils;

public class Auth {
    public static String userUid;
    public static String phoneNumber;
    public static String positionId;
    public static int customer_id;

    public static int getCustomer_id() {
        return customer_id;
    }

    public static void setCustomer_id(int customer_id) {
        Auth.customer_id = customer_id;
    }

    public static String getUserUid() {
        return userUid;
    }

    public static void setUserUid(String userUid) {
        Auth.userUid = userUid;
    }

    public static String getPhoneNumber() {
        return phoneNumber;
    }

    public static void setPhoneNumber(String phoneNumber) {
        Auth.phoneNumber = phoneNumber;
    }

    public static String getPositionId() {
        return positionId;
    }

    public static void setPositionId(String positionId) {
        Auth.positionId = positionId;
    }
}
