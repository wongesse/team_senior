package com.mobvoi.android.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
    int responseTime;

    ToggleButton toggleButtonEnabled;
    boolean enabled;

    ToggleButton toggleButtonHaptic;
    boolean hapticEnabled;

    ToggleButton toggleButtonMessage;
    boolean messageWithCall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_screen);

        drawBackground();
        readPreferences();

        // Response time
        et2=(EditText)findViewById(R.id.editText2);
        et2.setText(Integer.toString(responseTime),TextView.BufferType.EDITABLE);

        // Enabled
        toggleButtonEnabled = (ToggleButton) findViewById(R.id.toggleButtonEnabled);
        toggleButtonEnabled.setChecked(enabled);

        // Haptic
        toggleButtonHaptic = (ToggleButton) findViewById(R.id.toggleButtonHaptic);
        toggleButtonHaptic.setChecked(hapticEnabled);

        // Call+Message
        toggleButtonMessage = (ToggleButton) findViewById(R.id.toggleButtonMessage);
        toggleButtonMessage.setChecked(messageWithCall);

        setTitle("Settings");
    }

    public void drawBackground() {
        RelativeLayout relativeLayout; // for gradient background
        relativeLayout = (RelativeLayout) findViewById(R.id.background);
        int[] colors = new int[]{Color.parseColor("#494391"), Color.parseColor("#7c8cf4")};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        relativeLayout.setBackgroundDrawable(gradientDrawable);
    }

    public void savePreferences(View v) {
        // MY_PREFS_NAME - a static String variable like:
        //public static final String MY_PREFS_NAME = "MyPrefsFile";
        SharedPreferences.Editor editor = getSharedPreferences("ga_preferences", MODE_PRIVATE).edit();
        //editor.putString("name", "Elena");
        EditText et2 = (EditText) findViewById(R.id.editText2);
        editor.putInt("responseTime", Integer.parseInt(et2.getText().toString()));
        editor.putBoolean("enabled", toggleButtonEnabled.isChecked());
        editor.putBoolean("hapticEnabled", toggleButtonHaptic.isChecked());
        editor.putBoolean("messageWithCall", toggleButtonMessage.isChecked());
        editor.apply();
    }
    //https://stackoverflow.com/questions/4396376/how-to-get-edittext-value-and-display-it-on-screen-through-textview/4396400
    //https://stackoverflow.com/questions/23024831/android-shared-preferences-example
    //https://developer.android.com/training/data-storage/shared-preferences
    public void readPreferences () {
        SharedPreferences prefs = getSharedPreferences("ga_preferences", MODE_PRIVATE);

        responseTime = prefs.getInt("responseTime", 30);
        enabled = prefs.getBoolean("enabled", true);
        hapticEnabled = prefs.getBoolean("hapticEnabled", true);
        messageWithCall = prefs.getBoolean("messageWithCall", true);
    }

    private boolean settingsDidChange() {
        SharedPreferences prefs = getSharedPreferences("ga_preferences", MODE_PRIVATE);
        if (prefs.getInt("responseTime", 30) != Integer.parseInt(et2.getText().toString()) ||
                prefs.getBoolean("enabled", true) != toggleButtonEnabled.isChecked() ||
                prefs.getBoolean("hapticEnabled", true) != toggleButtonHaptic.isChecked() ||
                prefs.getBoolean("messageWithCall", true) != toggleButtonMessage.isChecked()) {
            return true;
        } else {
            return  false;
        }
    }

    @Override
    public void onBackPressed() {
        if (settingsDidChange()) {
            createDialog();
        } else {
            SettingsScreen.super.onBackPressed();
        }

    }

    private void createDialog() {

        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);

        alertDlg.setMessage("Are you sure you leave settings without saving your changes?");
        alertDlg.setCancelable(false); // We avoid that the dialog can be cancelled, forcing the user to choose one of the options

        alertDlg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SettingsScreen.super.onBackPressed();
                    }
                }
        );

        alertDlg.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // We do nothing
            }
        });

        alertDlg.create().show();
    }

}
