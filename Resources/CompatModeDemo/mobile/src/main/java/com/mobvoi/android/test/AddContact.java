package com.mobvoi.android.test;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.io.FileOutputStream;

public class AddContact extends AppCompatActivity {
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
                String m = name + " - " + phoneNumber;
                Log.d("AddContact", m);
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

    public void get(View v) {


    }

    private void writeContactToFile(String data) {
        String filename = "ga_contacts";
        String fileContents = data;
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
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