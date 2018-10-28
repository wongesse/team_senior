/**
 * mobvoi_api_demo
 *
 * @author jiancui
 * @date 2015-12-8
 */
package com.mobvoi.ticwear.mobvoiapidemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.telephony.SmsManager;
import android.widget.RelativeLayout;
import android.graphics.Color;

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
        relativeLayout = (RelativeLayout)findViewById(R.id.background);
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

    public void sendSMSander(View view) {
        String latitude = "37.02234631";
        String longitude = "32.10154542";
        String message = "I have fallen at (" + latitude + ", " + longitude + ")";
        sendSMS("11234561234", message);
        //Log.d("MainActivity", "ANDER here");
    }

    public void readContacts(View view) {
        Log.d("MainActivity", "ANDER readContacts");
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
