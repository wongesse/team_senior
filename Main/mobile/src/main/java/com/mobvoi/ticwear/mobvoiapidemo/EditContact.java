package com.mobvoi.ticwear.mobvoiapidemo;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditContact extends AppCompatActivity {

    ListView contactsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_contact);
        contactsListView = (ListView)findViewById(R.id.listView);

        List<Map<String, String>> data = getContacts();
        String[] from = {"name", "number"};


        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                from,
                new int[] {android.R.id.text1, android.R.id.text2 });

        contactsListView.setAdapter(adapter);



//        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
////            @Override
////            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
////                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
////                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
////                //phoneNumber = parsePhoneNumber(phoneNumber);
////                String m = name + " --- " + phoneNumber;
////                Log.d("[debug]", m);
////                writeContactToFile(m);
////            }
////        });

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> temp = (HashMap<String, String>)adapterView.getItemAtPosition(i);
                Log.d("[debug]", temp.get("name"));
                Log.d("[debug]", temp.get("number"));
            }
        });

    }

    private List<Map<String, String>> getContacts() {
        Log.d("[debug]:","===============================================");
        File file = new File("/data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts");
        List<Map<String, String>> contacts = new ArrayList<Map<String, String>>();;
//        Map<String, String> contacts = new HashMap<>();
        try {
            FileInputStream stream = new FileInputStream(file);

            InputStream is = stream;
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;

            while ( (line = rd.readLine()) != null) {
                String[] data = line.split("---");
                Map<String, String> contact = new HashMap<String, String>(2);
                contact.put("number", data[1]);
                contact.put("name", data[0]);
                contacts.add(contact);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("[debug]:","++++++++++++++++++++++++++++++++++++++++");
        return contacts;
    }

    private boolean contactIsUnique(String data) {
        File file = new File("/data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts");
        try {
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
        File contactsFile = new File("/data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts");
        if (!contactIsUnique(data) && contactsFile.exists()) {
            /* we don't want to add duplicate contacts */
            String[] delimiterTokens = data.split(" --- ");
            String name = delimiterTokens[0];
//            alert("Duplicate contact", name + " is already selected to receive emergency messages.", false);
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


}
