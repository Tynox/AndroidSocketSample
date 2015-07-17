package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SocketClientService.ISocketClientFeedback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, SocketServiceService.class));
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean bound = bindService(new Intent(MainActivity.this, SocketClientService.class),
                        socketClientServiceConnection, BIND_AUTO_CREATE);
                Log.d("MainActivity", "bound: " + Boolean.toString(bound));
            }
        }).start();

        listView = (ListView) findViewById(R.id.list);
        input = (EditText) findViewById(R.id.input);
        buttonSend = (Button) findViewById(R.id.send_button);

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return data.size();
            }

            @Override
            public Object getItem(int i) {
                return data.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                TextView container = (TextView) view;
                if (container == null) {
                    container = new TextView(MainActivity.this);
                }
                container.setText(data.get(i));
                return container;
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = input.getText().toString();
                if (content.isEmpty()) {
                    return;
                }

                sendMessage(content);
                input.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SocketServiceService.class));
        if (isBound) {
            unbindService(socketClientServiceConnection);
            isBound = false;
        }
    }

    @Override
    public void onMessageReceived(String message) {
        data.add("SERVER: " + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onMessageSent(String content) {
        data.add("ME: " + content);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private void sendMessage(final String content) {
        if (content == null || content.isEmpty() || !isBound) {
            Log.d("client", "content is empty");
            return;
        }

        socketClientService.sendMessage(content);
    }

    private ListView listView;
    private EditText input;
    private Button buttonSend;

    private ArrayList<String> data = new ArrayList<>();

    private SocketClientService socketClientService;
    private boolean isBound = false;
    private ServiceConnection socketClientServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("MainActivity", "socket client connection");
            SocketClientService.LocalBinder binder = (SocketClientService.LocalBinder) iBinder;
            socketClientService = binder.getService();
            socketClientService.setMessageListener(MainActivity.this);
            isBound = true;
            Log.d("MainActivity", "on service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            socketClientService = null;
            isBound = false;
        }
    };
}
