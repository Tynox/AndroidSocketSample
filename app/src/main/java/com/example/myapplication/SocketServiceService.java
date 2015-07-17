package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by pocktynox on 2015/7/16.
 */
public class SocketServiceService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            serverSocket = new ServerSocket(10086);
            runForever();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("server", "try stop");
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendThread.interrupt();
        receiveThread.interrupt();
        socketThread.interrupt();
        Log.d("server", "stop");
    }

    private void runForever() {
        socketThread.start();
    }

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Thread socketThread = new Thread(new SocketRunnable());
    private Thread sendThread;
    private Thread receiveThread;

    private int count = 0;

    private class SocketRunnable implements Runnable {
        @Override
        public void run() {
            sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        out.println("I got it: " + Integer.toString(++count));
                        Log.d("server", "send feedback");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));

                        String info;
                        while ((info = bufferedReader.readLine()) != null) {
                            Log.d("server", "info: " + info);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            while (!Thread.currentThread().isInterrupted() && !serverSocket.isClosed()) {
                try {
                    clientSocket = serverSocket.accept();
                    clientSocket.setKeepAlive(true);
                    Log.d("server", "accept");

                    receiveMessage();
                    sendMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendMessage() {
            sendThread.start();
        }

        private void receiveMessage() {
            receiveThread.start();
        }
    }
}
