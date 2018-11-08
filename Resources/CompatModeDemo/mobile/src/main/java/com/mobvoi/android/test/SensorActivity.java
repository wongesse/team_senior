package com.mobvoi.android.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.MobvoiApiClient.ConnectionCallbacks;
import com.mobvoi.android.wearable.Wearable;

import java.text.DecimalFormat;

public class SensorActivity extends Activity {

    public static final String TAG = "FunctionTest";

    private TextView ax, ay, az, falldetect;

    private MobvoiApiClient client;

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
        setContentView(R.layout.front_screen);

        //send = (TextView)findViewById(R.id.sendText);
        //ax = (TextView) findViewById(R.id.axText);
        //ay = (TextView) findViewById(R.id.ayText);
        //az = (TextView) findViewById(R.id.azText);
        falldetect = (TextView) findViewById(R.id.fallDetect);

        Button button = (Button) findViewById(R.id.dismissAlert);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                falldetect.setText("Fall Detection: I'm Gucci For Now");
            }
        });

        initClient();
        Log.i(TAG, "init client finished.");

        Intent startIntent = new Intent(this, FunctionTestService.class);
        startService(startIntent);

        String[] xyz;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                //if (bundle.containsKey("receive")) {
                String temp = bundle.getString("receive");
                //final String[] xyz = temp.split(",");
                falldetect.setText("Fall Detection: **" + temp + "** OMG SEND HELP");
                //ax.setText("Ax: " + xyz[0]);
                //ay.setText("Ay: " + xyz[1]);
                //az.setText("Az: " + xyz[2]);

                //}
//                else if (bundle.containsKey("send")) {
//                    //send.setText("S:" + bundle.getString("send"));
//                }
            }
        };

        IntentFilter mFilter = new IntentFilter(Utils.INTENT_TAG);
        registerReceiver(receiver, mFilter);
        Log.i(TAG, "register receiver finished.");
    }
}
