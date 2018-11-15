package com.mobvoi.android.test;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.MobvoiApiClient.ConnectionCallbacks;
import com.mobvoi.android.wearable.Wearable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class SensorActivity extends Activity {

    public static final String TAG = "FunctionTest";

    private TextView ax, ay, az, falldetect;

    private MobvoiApiClient client;

    private boolean connected = false;

    private static Sample[] mSamples;

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

        final Button help_button = findViewById(R.id.help_button);
        help_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(SensorActivity.this, HelpScreen.class));
            }
        });

        final Button add_button = findViewById(R.id.add_contact_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(SensorActivity.this, AddContact.class));
            }
        });

        final Button edit_button = findViewById(R.id.edit_contact_button);
        edit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(SensorActivity.this, EditContact.class));
            }
        });

        final Button settings_button = findViewById(R.id.settings_button);
        settings_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(SensorActivity.this, SettingsScreen.class));
            }
        });

        final Button sms_button = findViewById(R.id.sms_button);
        settings_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                //startActivity(new Intent(SensorActivity.this, SettingsScreen.class));
                //sendSMS("7135154644", "DEAD");
                sendSMSander();
            }
        });


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
                if (temp.equals("True")) {
                    alert("Fall Detected!", "Yo! You gucci?", true);
                }
            }
        };

        IntentFilter mFilter = new IntentFilter(Utils.INTENT_TAG);
        registerReceiver(receiver, mFilter);
        Log.i(TAG, "register receiver finished.");

        drawBackground();
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
        if (isConfirmation) {
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Help me!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            double[] latAndLong = new double[2];
                            latAndLong = getGPSData();
                            String message = "HELP!!! I have fallen at (" + latAndLong[0] + ", " + latAndLong[1] + ")";
                            sendSMS("2707919445", message);
                        }
                    })
                    .setNegativeButton("I'm Alright!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

    public void drawBackground() {
        RelativeLayout relativeLayout; // for gradient background
        relativeLayout = (RelativeLayout) findViewById(R.id.background);
        int[] colors = new int[]{Color.parseColor("#494391"), Color.parseColor("#7c8cf4")};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        relativeLayout.setBackgroundDrawable(gradientDrawable);
    }

    public void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception ex) {
            String errorMessage = "[ERROR] " + ex.toString();
            Log.d("MainActivity", errorMessage);
        }
    }

    public Contact[] getSelectedContacts() {
        Contact[] selectedContacts = new Contact[64];
        //Log.d("[debug]", "starting getSelectedContacts");
        int i = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    getFilesDir() + "/ga_contacts"));
                    //"/data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts"));
            String line = reader.readLine();
            while (line != null) {
                //System.out.println(line);
                String[] delimiterTokens = line.split(" --- ");
                Contact contact = new Contact(delimiterTokens[0], delimiterTokens[1]);
                selectedContacts[i] = contact;
                line = reader.readLine();
                i++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("[debug]", "returning from getSelectedContacts");
        return selectedContacts;
    }

    public void readContacts(View view) {
        Contact[] contacts;
        contacts = getSelectedContacts();

        for (int i = 0; i < contacts.length; i++) {
            if (contacts[i] != null) {
                Log.d("[debug]", "contact #: " + contacts[i].getName());
            }
        }
        Log.d("[debug]", "returning from readContacts");
    }

    public double[] getGPSData() {
        double[] latAndLong = new double[2];
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("[Permissions error]", "User has not allowed the permissions");
            latAndLong[0] = 1;
            latAndLong[1] = 1;
            return latAndLong;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        latAndLong[0] = latitude;
        latAndLong[1] = longitude;
        return latAndLong;
    }

    public void sendSMSander() {
        double[] latAndLong = new double[2];
        latAndLong = getGPSData();
        String message = "I have fallen at (" + latAndLong[0] + ", " + latAndLong[1] + ")";
        Contact[] selectedContacts;
        selectedContacts = getSelectedContacts();
        for (int i = 0; i < selectedContacts.length; i++) {
            if (selectedContacts[i] != null) {
                /* want to figure out how to send group MMS */
                /* think I need to use
                sendMultimediaMessage(Context context, Uri contentUri, String locationUrl, Bundle configOverrides, PendingIntent sentIntent)*/
                /* code below can only send to one person since it is an SMS */
                sendSMS(selectedContacts[i].getPhoneNumber(), message);
            }
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(SensorActivity.this, mSamples[position].activityClass));
    }

    /**
     * This class describes an individual sample (the sample title, and the activity class that
     * demonstrates this sample).
     */
    private class Sample {
        private CharSequence title;
        private Class<? extends Activity> activityClass;

        public Sample(int titleResId, Class<? extends Activity> activityClass) {
            this.activityClass = activityClass;
            this.title = getResources().getString(titleResId);
        }

        @Override
        public String toString() {
            return title.toString();
        }
    }
}
