package com.mobvoi.android.test;


import android.app.Activity;
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
import android.widget.ListAdapter;
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

public class EditContact extends Activity {

    ListView contactsListView;
    String tappedContactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_contact);
        contactsListView = (ListView)findViewById(R.id.listView);
        Log.d("[debug]", "------------------ getting contacts ------------------");
        final List<Map<String, String>> data = getContacts();
        Log.d("[debug]", "------------------ drawing list ------------------");
        final SimpleAdapter adapter = draw(contactsListView, data);
        Log.d("[debug]", "------------------ done basic ------------------");


        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("[debug]", "------------------ updating list ------------------");
                HashMap<String, String> temp = (HashMap<String, String>)adapterView.getItemAtPosition(i);
                tappedContactName = temp.get("name"); // for confirmation dialog
                alert("Remove contact", "Are you sure you do NOT want " + tappedContactName + " to be notified when you fall?", true, data, adapter, i);
                Log.d("[debug]", "------------------ update done ------------------");

            }
        });

        setTitle("Tap on any contact to remove");
    }

    private SimpleAdapter draw(ListView lv, List<Map<String, String>> d) {
        Log.d("[debug]:", "drawing --------------------------------------------------");
        String[] from = {"name", "number"};
        SimpleAdapter adapter = new SimpleAdapter(this, d,
                android.R.layout.simple_list_item_2,
                from,
                new int[] {android.R.id.text1, android.R.id.text2 });
        lv.setAdapter(adapter);
        return adapter;
    }

    private void update(ListView lv, List<Map<String, String>> d, SimpleAdapter adapt, int removeIndex) {

        ListAdapter adapter = lv.getAdapter();
        String message = "";

        // iterate through the adapter to get a list of maps that are contact info
        for (int i= 0; i < adapter.getCount(); i++) {
            Log.d("[debug]", "adapter count: " + adapter.getCount());
            Map<String, String> temp = (Map<String, String>)adapter.getItem(i);
            // if the person we are trying to remove ISN'T equal to the current line we are at in the list of maps
            // append them to the new list we are creating
            if (!(d.get(removeIndex).toString()).equals(temp.toString())) {
                // if its the last line don't append the new line
                if (removeIndex == i) {
                    message += temp.get("name") + "---" + temp.get("number");
                }
                else {
                    message += temp.get("name") + "---" + temp.get("number") + "\n";
                }
            }

        }

        Log.d("[debug]", message);
        writeContactToFile(message);
        d.remove(removeIndex);
        adapt.notifyDataSetChanged();

    }

    private void alert(String title, String message, boolean isConfirmation, final List<Map<String, String>> data, final SimpleAdapter adapter, final int removeIndex) {
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
                            update(contactsListView, data, adapter, removeIndex);
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

    private List<Map<String, String>> getContacts() {

        File file = new File(getFilesDir() + "/ga_contacts");
        List<Map<String, String>> contacts = new ArrayList<Map<String, String>>();

        if (file.exists()) {
            try {
                FileInputStream stream = new FileInputStream(file);

                InputStream is = stream;
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;

                while ( (line = rd.readLine()) != null) {
                    if ("".equals(line)) {
                        Log.d("[debug]", "empty line");
                        continue;
                    }
                    Log.d("[debug]", "line-------------" + line + "-------------");
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
        } else {
            Log.d("[debug]", "can't find file");
        }

        return contacts;
    }

    private void writeContactToFile(String data) {
        File contactsFile = new File(getFilesDir() + "/ga_contacts");
        String filename = "ga_contacts";
        String fileContents = data + "\n";
        try{
            FileOutputStream outputStream = new FileOutputStream(contactsFile);
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

//        if (!contactsFile.exists()) {
//            try {
//                Log.d("[debug]", "Creating new file!");
//                /* writes to /data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts */
//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                outputStream.write(fileContents.getBytes());
//                outputStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            try {
//                Log.d("[debug]", "Appending to file!");
//                /* writes to /data/data/com.mobvoi.ticwear.mobvoiapidemo/files/ga_contacts */
//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                outputStream.write(fileContents.getBytes());
//                outputStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
}
