package com.example.hprestaurant.Interfaces;


import okio.ByteString;

public interface MyWebSocketListener {
    void onMessage(String text);
    void onMessage(ByteString bytes);
    void onOpen();
    void onClosed();
}
