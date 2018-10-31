package com.mobvoi.android.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.MobvoiApiClient.ConnectionCallbacks;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.Wearable;

public class FunctionTestActivity extends Activity implements SensorEventListener {

    public static final String TAG = "FunctionTest";

    private int type = 0;

    private TextView send, receive;

    private View button;

    private MobvoiApiClient client;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private boolean connected = false;

    private BroadcastReceiver receiver;

    private void initClient() {
        client = new MobvoiApiClient.Builder(this).addApi(Wearable.API)
                .addConnectionCallbacks(new ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "Mobile Service connected.");
                        connected = true;
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.e(TAG, "Mobile Service connection suspended. cause " + cause);
                    }
                })
                .addOnConnectionFailedListener(new MobvoiApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.e(TAG, "Mobile Service connection failed. result " + connectionResult);
                    }
                }).build();
        client.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_test);

        send = (TextView)findViewById(R.id.sendText);
        receive = (TextView)findViewById(R.id.receiveText);
        button = findViewById(R.id.startButton);

        initClient();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
        );
        Log.i(TAG, "init client finished.");

        Intent startIntent = new Intent(this, FunctionTestService.class);
        startService(startIntent);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle.containsKey("send")) {
                    send.setText("S:" + bundle.getString("send"));
                } else if (bundle.containsKey("receive")) {
                    receive.setText("R:" + bundle.getString("receive"));
                }
            }
        };

        IntentFilter mFilter = new IntentFilter(Utils.INTENT_TAG);
        registerReceiver(receiver, mFilter);
        Log.i(TAG, "register receiver finished.");

        Log.i(TAG, "set radio button listener finished.");
    }
    @Override
    protected void onResume() {
        super.onResume();
        client.connect();
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //SENSOR DATA
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        final String message = x + "," + y + "," + z;
        Log.d(TAG, message);
        byte[] data = message.getBytes();

        if (!connected) {
            Log.i(TAG, "discard a request, connect : " + connected);
            return;
        }

        final byte[] sendData = data;

        Utils.setText(FunctionTestActivity.this, "send", message);

        Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                Log.d(TAG, "send message with nodes (" + result.getNodes().size() + ") " + result.getNodes());
                for (Node node : result.getNodes()) {
                    Wearable.MessageApi.sendMessage(client, node.getId(), "/accelerometer", sendData).setResultCallback(
                            new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult result) {
                                    if (result.getStatus().isSuccess()) {
                                        Utils.setText(FunctionTestActivity.this, "send", message);
                                    }
                                }
                            });
                }
            }
        });
    }

}
