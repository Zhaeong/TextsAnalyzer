package com.example.owen.textsanalyzer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.owen.textsanalyzer.helperConstructs.contactsAdapter;
import com.example.owen.textsanalyzer.helperConstructs.customDBHelper;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog pDialog;

    final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    //Device database variables
    public customDBHelper myDeviceDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDeviceDatabase = customDBHelper.getInstance(this);

        // Set up progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        populateList();

        final Button button = (Button) findViewById(R.id.getMessages);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getContacts();
            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
                &&
            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

            getContacts();
        }
        else {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    getContacts();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    protected void populateList()
    {
        contactsAdapter cAdapter = new contactsAdapter(this, myDeviceDatabase.getAllItemsInTable(customDBHelper.CONTACTS_TABLE_NAME));
        ListView contactsListView = (ListView) findViewById(R.id.contactsList);
        contactsListView.setAdapter(cAdapter);

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Intent intent = new Intent(MainActivity.this, AddTaskView.class);
                //intent.putExtra(TASK_ID, id);

                //startActivityForResult(intent, 1);

            }
        });
    }

    protected void getContacts()
    {
        pDialog.setMessage("Getting Contacts");
        showDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri uriSMSURI = Uri.parse("content://sms/inbox");
                Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
                while (cur.moveToNext()) {
                    String address = cur.getString(cur.getColumnIndex("address"));

                    String ContactName = getContactName(MainActivity.this, address);

                    if(!myDeviceDatabase.doesContactExist(address)) {
                        myDeviceDatabase.addContact(ContactName, address);
                    }
                }
                cur.close();
                hideDialog();
            }
        }).start();

        populateList();

    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return phoneNumber;
        }
        String contactName = phoneNumber;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        cursor.close();

        return contactName;
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
