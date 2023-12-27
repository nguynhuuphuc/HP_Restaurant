package com.example.hprestaurant.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hprestaurant.Interfaces.MyWebSocketListener;
import com.example.hprestaurant.MyApplication;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MyWebsocketClient {
    private OkHttpClient client;
    private WebSocket webSocket;
    private MyWebSocketListener myWebSocketListener;
    private boolean isClosed;
    private OnConnectionListener onConnectionListener;


    public interface OnConnectionListener{
        void onConnected();
        void onDisconnected();
    }

    public void setOnConnectionListener(OnConnectionListener onConnectionListener){
        this.onConnectionListener = onConnectionListener;
    }


    public MyWebsocketClient() {
        client = new OkHttpClient();
        connectWebSocket();
    }

    public void setMyWebSocketListener(MyWebSocketListener myWebSocketListener) {
        this.myWebSocketListener = myWebSocketListener;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    private void connectWebSocket() {
        Request request = new Request.Builder().url(Util.WEBSOCKET_URL).build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
                if(myWebSocketListener != null){
                    myWebSocketListener.onClosed();
                }
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                if(onConnectionListener != null){
                    onConnectionListener.onConnected();
                }
                reconnectWebSocket();
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
                if(myWebSocketListener != null){
                    myWebSocketListener.onMessage(text);
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
                if(myWebSocketListener != null){
                    myWebSocketListener.onMessage(bytes);
                }
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);
                if(onConnectionListener != null){
                    onConnectionListener.onConnected();
                }
                if(myWebSocketListener != null){
                    myWebSocketListener.onOpen();
                }
            }
        };

        webSocket = client.newWebSocket(request, listener);
    }
    public void reconnectWebSocket() {
        // Tái kết nối sau một khoảng thời gian
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                connectWebSocket();
            }
        }, 5000); // Thời gian đợi trước khi tái kết nối (ví dụ: 5 giây)
    }

    public void sendMessage(String message) {
        webSocket.send(message);
    }

    public void closeWebSocket() {
        webSocket.close(1000, "Goodbye, WebSocket!");
    }
}
