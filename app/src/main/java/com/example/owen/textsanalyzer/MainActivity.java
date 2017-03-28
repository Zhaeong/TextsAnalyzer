package com.example.owen.textsanalyzer;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.owen.textsanalyzer.helperConstructs.ContactObj;
import com.example.owen.textsanalyzer.helperConstructs.contactsAdapter;
import com.example.owen.textsanalyzer.helperConstructs.customDBHelper;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog pDialog;

    private Dialog PopUpDialog;

    final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    public static final String ContactId = "ContactId";
    //Device database variables
    public customDBHelper myDeviceDatabase;

    public boolean onLoadGetInfo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDeviceDatabase = customDBHelper.getInstance(this);

        // Set up progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        populateList();

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
                &&
            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

            if(customDBHelper.FirstPassContactSync)
            {
                getContacts();
                customDBHelper.FirstPassContactSync = false;
            }
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
                    if(customDBHelper.FirstPassContactSync)
                    {
                        getContacts();
                        customDBHelper.FirstPassContactSync = false;
                    }


                } else {

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

                ContactObj curContact = myDeviceDatabase.getContact(id);
                setUpDialog(curContact.name, curContact.number);

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

                myDeviceDatabase.deleteAllItemsInTable(customDBHelper.CONTACTS_TABLE_NAME);

                String[] projection= { "DISTINCT address"};

                Uri uriSMSURI = Uri.parse("content://sms/inbox");
                Cursor cur = getContentResolver().query(uriSMSURI, projection, null, null, null);
                while (cur.moveToNext()) {
                    String address = cur.getString(cur.getColumnIndex("address"));

                    String numOnlyAddress = myDeviceDatabase.getNumericOnly(address);

                    String ContactName = getContactName(MainActivity.this, numOnlyAddress);
                    myDeviceDatabase.addContact(ContactName, numOnlyAddress);

                }
                cur.close();

                myDeviceDatabase.deleteAllItemsInTable(customDBHelper.TEXTS_TABLE_NAME);

                Uri URLmessage = Uri.parse("content://sms/");

                Cursor c = getContentResolver().query(URLmessage, null, null, null, null);

                int totalSMS = c.getCount();

                if (c.moveToFirst()) {
                    for (int i = 0; i < totalSMS; i++) {

                        String SMS_id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String SMS_address = c.getString(c
                                .getColumnIndexOrThrow("address"));

                        //String address_num = myDeviceDatabase.getNumericOnly(SMS_address);
                        String address_num = PhoneNumberUtils.getStrippedReversed (SMS_address);

                        String SMS_body = c.getString(c.getColumnIndexOrThrow("body"));
                        String SMS_read = c.getString(c.getColumnIndex("read"));
                        String SMS_date = c.getString(c.getColumnIndexOrThrow("date"));

                        String SMS_type;
                        if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                            SMS_type = "inbox";
                        } else {
                            SMS_type = "sent";
                        }

                        myDeviceDatabase.addText(SMS_id, address_num, SMS_body, SMS_read, SMS_date, SMS_type);
                        c.moveToNext();
                    }
                }
                // else {
                // throw new RuntimeException("You have no SMS");
                // }
                c.close();

                myDeviceDatabase.printAllItemsInTable(customDBHelper.TEXTS_TABLE_NAME);



                hideDialog();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        populateList();
                    }
                });

            }
        }).start();



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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.refresh_list:
                getContacts();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //
    //Popup Dialog functions
    //

    protected void setUpDialog(String ContactName, String ContactNumber)
    {
        int smsRec;
        int smsSent;
        int smsRecAvg;
        int smsSentAvg;

        PopUpDialog = new Dialog(MainActivity.this);
        PopUpDialog.setContentView(R.layout.info_display_dialog);
        PopUpDialog.setTitle("Info");
        PopUpDialog.setCancelable(true);

        //set up anim
        ImageView heartImg = (ImageView) PopUpDialog.findViewById(R.id.heartImg);
        heartImg.setBackgroundResource(R.drawable.animation_loading);
        final AnimationDrawable heartAnim = (AnimationDrawable) heartImg.getBackground();

        heartAnim.start();

        TextView diagMsg = (TextView) PopUpDialog.findViewById(R.id.dialogMsg);
        diagMsg.setText("Does "+ ContactName + " like you?");

        final TextView SMSReceived = (TextView) PopUpDialog.findViewById(R.id.messagesRec);

        smsRec = myDeviceDatabase.getSMSFromNumber(ContactNumber, "inbox");

        //myDeviceDatabase.testfunction();

        SMSReceived.setText(Integer.toString(smsRec));
        SMSReceived.setVisibility(View.INVISIBLE);

        SMSReceived.postDelayed(new Runnable() {
            public void run() {
                SMSReceived.setVisibility(View.VISIBLE);
            }
        }, 1000);


        final TextView SMSSent = (TextView) PopUpDialog.findViewById(R.id.messagesSent);

        smsSent = myDeviceDatabase.getSMSFromNumber(ContactNumber, "sent");

        SMSSent.setText(Integer.toString(smsSent));
        SMSSent.setVisibility(View.INVISIBLE);

        SMSSent.postDelayed(new Runnable() {
            public void run() {
                SMSSent.setVisibility(View.VISIBLE);
            }
        }, 2000);

        final TextView SMSAvgRec = (TextView) PopUpDialog.findViewById(R.id.avgMsgRec);
        smsRecAvg = getAvgSMSLength("inbox", ContactNumber);
        SMSAvgRec.setText(Integer.toString(smsRecAvg));
        SMSAvgRec.setVisibility(View.INVISIBLE);

        SMSAvgRec.postDelayed(new Runnable() {
            public void run() {
                SMSAvgRec.setVisibility(View.VISIBLE);
            }
        }, 3000);

        final TextView SMSAvgSent = (TextView) PopUpDialog.findViewById(R.id.avgMsgSent);
        smsSentAvg = getAvgSMSLength("sent", ContactNumber);
        SMSAvgSent.setText(Integer.toString(smsSentAvg));
        SMSAvgSent.setVisibility(View.INVISIBLE);

        SMSAvgSent.postDelayed(new Runnable() {
            public void run() {
                SMSAvgSent.setVisibility(View.VISIBLE);
            }
        }, 4000);


        final TextView ChanceofLike = (TextView) PopUpDialog.findViewById(R.id.chanceOfLike);
        final TextView ChanceofLikeMsg = (TextView) PopUpDialog.findViewById(R.id.changceOfLikeMsg);
        String sPercentage = Float.toString(getPercentageLike(smsRec, smsSent, smsRecAvg, smsSentAvg)) + "%";
        ChanceofLike.setText(sPercentage);

        ChanceofLike.setVisibility(View.INVISIBLE);
        ChanceofLikeMsg.setVisibility(View.INVISIBLE);

        SMSAvgSent.postDelayed(new Runnable() {
            public void run() {
                ChanceofLike.setVisibility(View.VISIBLE);
                ChanceofLikeMsg.setVisibility(View.VISIBLE);
                heartAnim.stop();
            }
        }, 5000);


        //set up button
        Button button = (Button) PopUpDialog.findViewById(R.id.okButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpDialog.hide();
            }
        });

        PopUpDialog.show();
    }

    protected int getSMSNum(String boxType, String ContactNumber)
    {
        final Uri SMS_INBOX = Uri.parse("content://sms/" + boxType);
        Cursor c = getContentResolver().query(SMS_INBOX, null, "address LIKE " + "'%" + ContactNumber + "%'", null, null);
        int numMsgs = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < numMsgs; i++) {


                String sid = c.getString(c.getColumnIndexOrThrow("_id"));
                String saddress = c.getString(c
                        .getColumnIndexOrThrow("address"));
                String sbody = c.getString(c.getColumnIndexOrThrow("body"));
                String sperson = c.getString(c.getColumnIndexOrThrow("person"));
                String sread = c.getString(c.getColumnIndex("read"));
                String sdate = c.getString(c.getColumnIndexOrThrow("date"));



                c.moveToNext();
            }
        }

        c.close();
        return numMsgs;
    }


    protected int getAvgSMSLength(String boxType, String ContactNumber)
    {
        int avgLength = -1;
        String[] projection= { "AVG(Length(" +customDBHelper.TEXTS_COL_BODY + " ))"};

        Uri uriSMSURI = Uri.parse("content://sms/"+boxType);
        Cursor cur = getContentResolver().query(uriSMSURI, projection, "address LIKE " + "'%" + ContactNumber + "%'", null, null);


        while (cur.moveToNext()) {
            //String body = cur.getString(cur.getColumnIndex("body"));
            avgLength = cur.getInt(0);
            Log.i("SMS", Integer.toString(avgLength));
        }
        cur.close();
        return avgLength;
    }

    protected float getPercentageLike(int smsRec,int  smsSent,int  smsRecAvg,int  smsSentAvg)
    {
        Random randomnGen = new Random();
        float Percentage = 0;

        if(smsRec!= -1 && smsSent!= -1) {
            if (smsRec > smsSent) {
                Percentage += 30;
                Percentage += randomnGen.nextFloat() * 5;
            }
            else
            {
                Percentage -= 20;
                Percentage += randomnGen.nextFloat() * 5;
            }
        }

        if(smsRecAvg!= -1 && smsSentAvg!= -1) {
            if (smsRecAvg > smsSentAvg) {
                Percentage += 30;
                Percentage += randomnGen.nextFloat() * 5;
            }
            else
            {
                Percentage -= 20;
                Percentage += randomnGen.nextFloat() * 5;
            }
        }

        if (smsRec > 40)
        {
            Percentage += 30;
            Percentage += randomnGen.nextFloat();
        }
        else if (smsRec > 30)
        {
            Percentage += 20;
            Percentage += randomnGen.nextFloat();
        }
        else if (smsRec > 20)
        {
            Percentage += 10;
            Percentage += randomnGen.nextFloat();
        }
        else if(smsRec > 10)
        {
            Percentage -= 5;
            Percentage += randomnGen.nextFloat();
        }
        else if(smsRec < 5)
        {
            Percentage -= 20;
            Percentage -= randomnGen.nextFloat();
        }

        if(smsSent < 10)
        {
            Percentage -= 10;
            Percentage -= randomnGen.nextFloat();
        }

        Percentage += randomnGen.nextFloat() * 10;
        Percentage += randomnGen.nextFloat();

        if(Percentage < 0)
            return randomnGen.nextFloat();

        if(Percentage > 100)
            return 99 + randomnGen.nextFloat();

        return Percentage;
    }
}

