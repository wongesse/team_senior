package com.mobvoi.android.test;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import static android.app.PendingIntent.getActivity;

public class SettingsScreen extends Activity {
    EditText et2;
    int response_time;

    ToggleButton toggleButtonEnabled;
    boolean enabled;

    ToggleButton toggleButtonHaptic;
    boolean hapticEnabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_screen);

        drawBackground();
        read_preferences();

        // Response time
        et2=(EditText)findViewById(R.id.editText2);
        et2.setText(Integer.toString(response_time),TextView.BufferType.EDITABLE);

        // Enabled
        toggleButtonEnabled = (ToggleButton) findViewById(R.id.toggleButtonEnabled);
        toggleButtonEnabled.setChecked(enabled);

        // Haptic
        toggleButtonHaptic = (ToggleButton) findViewById(R.id.toggleButtonHaptic);
        toggleButtonHaptic.setChecked(hapticEnabled);

        setTitle("Settings");
    }

    public void drawBackground() {
        RelativeLayout relativeLayout; // for gradient background
        relativeLayout = (RelativeLayout) findViewById(R.id.background);
        int[] colors = new int[]{Color.parseColor("#494391"), Color.parseColor("#7c8cf4")};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        relativeLayout.setBackgroundDrawable(gradientDrawable);
    }

    public void save_preferences(View v) {
        // MY_PREFS_NAME - a static String variable like:
        //public static final String MY_PREFS_NAME = "MyPrefsFile";
        SharedPreferences.Editor editor = getSharedPreferences("ga_preferences", MODE_PRIVATE).edit();
        //editor.putString("name", "Elena");
        EditText et2 = (EditText) findViewById(R.id.editText2);
        editor.putInt("response_time", Integer.parseInt(et2.getText().toString()));
        editor.putBoolean("enabled", toggleButtonEnabled.isChecked());
        editor.putBoolean("hapticEnabled", toggleButtonHaptic.isChecked());
        editor.apply();
    }
    //https://stackoverflow.com/questions/4396376/how-to-get-edittext-value-and-display-it-on-screen-through-textview/4396400
    //https://stackoverflow.com/questions/23024831/android-shared-preferences-example
    //https://developer.android.com/training/data-storage/shared-preferences
    public void read_preferences () {
        SharedPreferences prefs = getSharedPreferences("ga_preferences", MODE_PRIVATE);

        response_time = prefs.getInt("response_time", 30);
        enabled = prefs.getBoolean("enabled", true);
        hapticEnabled = prefs.getBoolean("hapticEnabled", true);
    }

}
