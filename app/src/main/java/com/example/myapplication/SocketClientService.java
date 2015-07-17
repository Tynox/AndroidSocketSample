package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by pocktynox on 2015/7/17.
 */
public class SocketClientService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket("localhost", 10086);
                    receiveMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(final String content) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println(content);
                    messageListener.onMessageSent(content);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void receiveMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));
                        String received = bufferedReader.readLine();
                        messageListener.onMessageReceived(received);
                        Log.d("client", "receive message");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void setMessageListener(ISocketClientFeedback listener) {
        messageListener = listener;
    }

    private Socket clientSocket;

    private ISocketClientFeedback messageListener;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SocketClientService getService() {
            return SocketClientService.this;
        }
    }

    public interface ISocketClientFeedback {
        void onMessageSent(String content);
        void onMessageReceived(String message);
    }
}
