package com.example.owen.textsanalyzer;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.owen.textsanalyzer.helperConstructs.ContactObj;
import com.example.owen.textsanalyzer.helperConstructs.customDBHelper;

public class ContactActivity extends AppCompatActivity {

    public customDBHelper myDeviceDatabase;

    private String ContactName;
    private String ContactNumber;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        myDeviceDatabase = customDBHelper.getInstance(this);

        Intent intent = getIntent();
        Long postID_internal = intent.getLongExtra(MainActivity.ContactId, -1);
        ContactObj curContact = myDeviceDatabase.getContact(postID_internal);
        ContactName = curContact.name;
        ContactNumber = curContact.number;

        TextView contactNameTextView = (TextView)findViewById(R.id.contactName);
        contactNameTextView.setText(curContact.name);


        final Button button = (Button) findViewById(R.id.Analyze);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getInfo();
            }
        });


    }


    protected void getInfo()
    {
        setUpDialog();

    }

    protected void setUpDialog()
    {
        dialog = new Dialog(ContactActivity.this);
        dialog.setContentView(R.layout.info_display_dialog);
        dialog.setTitle("Info");
        dialog.setCancelable(true);

        //set up anim
        ImageView heartImg = (ImageView) dialog.findViewById(R.id.heartImg);
        heartImg.setBackgroundResource(R.drawable.animation_loading);
        AnimationDrawable heartAnim = (AnimationDrawable) heartImg.getBackground();

        heartAnim.start();

        TextView diagMsg = (TextView) dialog.findViewById(R.id.dialogMsg);
        diagMsg.setText(ContactName);

        TextView SMSReceived = (TextView) dialog.findViewById(R.id.messagesRec);
        SMSReceived.setText(Integer.toString(getSMSReceived()));

        //set up button
        Button button = (Button) dialog.findViewById(R.id.okButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dialog.show();
    }

    protected int getSMSReceived()
    {
        final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
        Cursor c = getContentResolver().query(SMS_INBOX, null, "address = " + "'" + ContactNumber + "'", null, null);
        int numMsgs = c.getCount();
        c.close();
        return numMsgs;
    }
}
