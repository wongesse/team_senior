package com.mobvoi.ticwear.mobvoiapidemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.Wearable;

public class WeatherActivity extends AppCompatActivity implements MobvoiApiClient.ConnectionCallbacks,
        MobvoiApiClient.OnConnectionFailedListener, NodeApi.NodeListener, MessageApi.MessageListener {

    private static final String START_ACTIVITY_PATH = "/start-weather";
    private static final String DEFAULT_NODE = "default_node";
    private final String TAG = "WeatherActivity";
    private Button mStartWeatherBtn;
    private TextView mWeatherTv;
    private MobvoiApiClient mMobvoiApiClient;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        mWeatherTv = (TextView) findViewById(R.id.weather_text);
        mStartWeatherBtn = (Button) findViewById(R.id.start_weather);
        mStartWeatherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button Click");
                sendStartActivityMessage();
            }
        });
        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMobvoiApiClient.connect();
        Log.d(TAG, "onResume...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.MessageApi.removeListener(mMobvoiApiClient, this);
        Wearable.NodeApi.removeListener(mMobvoiApiClient, this);
        mMobvoiApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.MessageApi.addListener(mMobvoiApiClient, this);
        Wearable.NodeApi.addListener(mMobvoiApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        if (event.getPath().equals("/weathers")) {
            Log.d(TAG, "onMessageReceived: " + event);
            byte[] data = event.getData();
            final String datas = new String(data);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWeatherTv.setText(datas);
                }
            });
        }
    }

    @Override
    public void onPeerConnected(Node node) {
        Log.d(TAG, "onPeerConncted:");
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d(TAG, "onPeerDisconnected:");
    }

    private void sendStartActivityMessage() {
        Wearable.MessageApi.sendMessage(
                mMobvoiApiClient, DEFAULT_NODE, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        } else {
                            Log.d(TAG, "Success");
                        }
                    }
                }
        );
    }
}
