package com.mobvoi.android.test;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AddContact extends Activity {
    //ListView l1;
    ListView contactsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_add);

        contactsListView = (ListView)findViewById(R.id.listView);
        final Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null,null, null);
        startManagingCursor(cursor);

        String[] from = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone._ID};

        int[] to = {android.R.id.text1, android.R.id.text2};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
                from, to);
        contactsListView.setAdapter(simpleCursorAdapter);
        contactsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.contact_add, null);

        //contactsListView.setAdapter(adapter);

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNumber = parsePhoneNumber(phoneNumber);
                String m = name + " --- " + phoneNumber;
                Log.d("[debug]", m);
                writeContactToFile(m);
            }
        });


        /*
        l1 = (ListView)findViewById(R.id.listView);

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null,null, null);
        startManagingCursor(cursor);

        String[] from = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone._ID};

        int[] to = {android.R.id.text1, android.R.id.text2};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
                from, to);
        l1.setAdapter(simpleCursorAdapter);
        l1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        */
    }

    private void alert(String title, String message, boolean isConfirmation) {
        // Add paramter that is a function to be called if the confirmation dialog is sucessful
        final Context context = this;
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        if (isConfirmation) {
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
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

    public void get(View v) {}

    private boolean contactIsUnique(String data) {
//        File file = new File("/data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts");
        File file = new File(getFilesDir() + "/ga_contacts");        try {
            FileInputStream stream = new FileInputStream(file);

            InputStream is = stream;
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            String line;

            while ( (line = rd.readLine()) != null ){
                if(line.matches(data)){ //--regex of what to search--
                    Log.d("AddContact", "Contact is NOT unique!");
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void writeContactToFile(String data) {
//        File contactsFile = new File("/data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts");
        File contactsFile = new File(getFilesDir() + "/ga_contacts");        if (!contactIsUnique(data) && contactsFile.exists()) {
            /* we don't want to add duplicate contacts */
            String[] delimiterTokens = data.split(" --- ");
            String name = delimiterTokens[0];
            alert("Duplicate contact", name + " is already selected to receive emergency messages.", false);
            return;
        }
        String filename = "ga_contacts";
        String fileContents = data + "\n";
        FileOutputStream outputStream;

        Log.d("[debug]", "HERE");
        if (!contactsFile.exists()) {
            try {
                Log.d("[debug]", "Creating new file!");
                /* writes to /data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts */
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Log.d("[debug]", "Appending to file!");
                /* writes to /data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts */
                outputStream = openFileOutput(filename, Context.MODE_APPEND);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isNumeric(char number) {
        String n = "" + number;
        try {
            double d = Double.parseDouble(n);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private String parsePhoneNumber(String origPhoneNumber) {
        String newPhoneNumber = "";
        for (int i = 0; i < origPhoneNumber.length(); i++) {
            if (isNumeric(origPhoneNumber.charAt(i))) {
                newPhoneNumber += origPhoneNumber.charAt(i);
            }
        }
        return newPhoneNumber;
    }
}