package com.mobvoi.android.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.view.WindowManager;

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
    private TextView mText;
    private MobvoiApiClient client;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private SpeechRecognizer sr;
    private boolean connected = false;

    private BroadcastReceiver receiver;
    private String fall_or_not;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
                    //send.setText("S:" + bundle.getString("send"));
                } else if (bundle.containsKey("receive")) {
                    //receive.setText("R:" + bundle.getString("receive"));
                }
            }
        };

        IntentFilter mFilter = new IntentFilter(Utils.INTENT_TAG);
        registerReceiver(receiver, mFilter);

        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
        mText = (TextView) findViewById(R.id.value);
    }
    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error)
        {
            Log.d(TAG,  "error " +  error);
            //mText.setText("error " + error);
        }
        public void onResults(Bundle results)
        {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
            mText.setText("results: "+String.valueOf(data.size()));
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        client.connect();
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    boolean moIsMin = false;
    boolean moIsMax = false;
    boolean orientation = false;
    long mlPreviousTime;
    long mlFallPoint;
    int i = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        //SENSOR DATA
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        double sx = Math.pow(x, 2);
        double sy = Math.pow(y, 2);
        double sz = Math.pow(z, 2);
        double loAccelerationReader = Math.sqrt(sx + sy + Math.pow(z, 2));
        double pitch = Math.atan(x/(Math.sqrt(sy + sz)));
        double roll = Math.atan(y/(Math.sqrt(sx + sz)));

        //Log.d(TAG, "vector sum: " + loAccelerationReader);
        //Log.d(TAG, "pitch: " + pitch);
        //Log.d(TAG, "roll: " + pitch);

        if (!connected) {
            return;
        }

        if (loAccelerationReader <= 4.0) {
            mlPreviousTime = System.currentTimeMillis();
            moIsMin = true;
        }

        if (moIsMin) {
            i++;
            if (loAccelerationReader >= 50.0) {
                long llCurrentTime = System.currentTimeMillis();
                long llTimeDiff = llCurrentTime - mlPreviousTime;
                if (llTimeDiff <= 1500) {
                    moIsMax = true;
                    mlFallPoint = System.currentTimeMillis();
                }
            }
        }

        if (moIsMax){
            if (pitch >= 1.0 || roll >= 1.0 || pitch <= -1.0 || roll <= -1.0){
                long llCurrentTime = System.currentTimeMillis();
                long llTimeDiff = llCurrentTime - mlFallPoint;
                if (llTimeDiff < 10)
                    orientation = true;
            }
        }

        if (moIsMin && moIsMax && orientation) {
            Log.e(TAG, "FALL DETECTED!");

            //
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] vibrationPattern = {0, 500, 50, 300};
            //-1 - don't repeat
            final int indexInPatternToRepeat = -1;
            vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
            //

            displaySpeechRecognizer();

            //alert("Fall Detected" , "Are you okay?", );
            final String message = "True";
            final byte[] sendData = message.getBytes();


            Utils.setText(FunctionTestActivity.this, "send", message);

            Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult result) {
                    //Log.d(TAG, "send message with nodes (" + result.getNodes().size() + ") " + result.getNodes());
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

            i = 0;
            moIsMin = false;
            moIsMax = false;
            orientation = false;
        }
    }

    private static final int SPEECH_REQUEST_CODE = 0;
    int help = 0;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        sr.startListening(intent);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText
            if (spokenText.contains("help")) {
                help = 1;
            } else if (spokenText.contains("ok")){
                help = 2;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void alert(String title, String message, boolean isConfirmation) {
        // Add paramter that is a function to be called if the confirmation dialog is sucessful
        final Context context = this;
        android.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new android.app.AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new android.app.AlertDialog.Builder(context);
        }

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Help me!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        fall_or_not = "True";
                    }
                })
                .setNegativeButton("I'm Alright!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        fall_or_not = "False";
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


}
