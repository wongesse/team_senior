package com.mobvoi.android.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.MobvoiApiClient.ConnectionCallbacks;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.Asset;
import com.mobvoi.android.wearable.DataApi.DataItemResult;
import com.mobvoi.android.wearable.DataApi.GetFdForAssetResult;
import com.mobvoi.android.wearable.DataItemAsset;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.PutDataRequest;
import com.mobvoi.android.wearable.Wearable;

import java.io.InputStream;

public class FunctionTestActivity extends Activity {

    public static final String TAG = "FunctionTest";

    private int type = 0;

    private TextView send, receive;

    private View button;

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
        setContentView(R.layout.activity_function_test);

        send = (TextView)findViewById(R.id.sendText);
        receive = (TextView)findViewById(R.id.receiveText);
        button = findViewById(R.id.startButton);

        initClient();
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

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onclick, type = " + type);
                if (!connected) {
                    Log.i(TAG, "discard a request, connect : " + connected);
                    return;
                }
                byte[] data = null;
                String hashCode = "";
                data = Utils.getData(1000);
                final byte[] sendData = data;
                hashCode = "" + Utils.getHashCode(data);
                Utils.setText(FunctionTestActivity.this, "send", hashCode);
                final String fh = hashCode;
                Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult result) {
                        Log.d(TAG, "send message with nodes (" + result.getNodes().size() + ") " + result.getNodes());
                        for (Node node : result.getNodes()) {
                            Wearable.MessageApi.sendMessage(client, node.getId(), "/function/message", sendData).setResultCallback(
                                    new ResultCallback<MessageApi.SendMessageResult>() {
                                        @Override
                                        public void onResult(MessageApi.SendMessageResult result) {
                                            if (result.getStatus().isSuccess()) {
                                                Utils.setText(FunctionTestActivity.this, "send", fh);
                                            }
                                        }
                                    });
                        }
                    }
                });
                Log.i(TAG, "send a message.");
                Log.i(TAG, "hashcode = " + hashCode);
            }
        });
        Log.i(TAG, "set button listener finished.");
    }

}
