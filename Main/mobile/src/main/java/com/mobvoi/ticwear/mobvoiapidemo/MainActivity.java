/**
 * mobvoi_api_demo
 *
 * @author jiancui
 * @date 2015-12-8
 */
package com.mobvoi.ticwear.mobvoiapidemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {
//public class MainActivity extends Activity {
    /**
     * The collection of all samples in the app. This gets instantiated in {@link
     * #onCreate(android.os.Bundle)} because the {@link Sample} constructor needs access to {@link
     * android.content.res.Resources}.
     */

    private static Sample[] mSamples;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.front_screen);

        final Button help_button = findViewById(R.id.help_button);
        help_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(MainActivity.this, HelpScreen.class));
            }
        });

        final Button add_button = findViewById(R.id.add_contact_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(MainActivity.this, AddContact.class));
            }
        });

        final Button edit_button = findViewById(R.id.edit_contact_button);
        edit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(MainActivity.this, EditContact.class));
            }
        });

        final Button settings_button = findViewById(R.id.settings_button);
        settings_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(new Intent(MainActivity.this, SettingsScreen.class));
            }
        });


//        ListView listView = (ListView) findViewById(android.R.id.list);
//        listView.setAdapter(new ArrayAdapter<Sample>(this,
//                android.R.layout.simple_list_item_1,
//                android.R.id.text1,
//                mSamples));
//        listView.setOnItemClickListener(this);
        drawBackground();
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
                    "/data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts"));
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
        //Log.d("[debug]", "returning from getSelectedContacts");
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
        //Log.d("[debug]", "returning from readContacts");
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

    public void sendSMSander(View view) {
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(MainActivity.this, mSamples[position].activityClass));
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
