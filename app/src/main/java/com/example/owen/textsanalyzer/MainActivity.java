package com.example.owen.textsanalyzer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.getMessages);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getMessage();
            }
        });



    }

    protected void getMessage()
    {
        List<String> sms = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            Uri uriSMSURI = Uri.parse("content://sms/inbox");

            Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);

            while (cur.moveToNext()) {
                String address = cur.getString(cur.getColumnIndex("address"));
                String body = cur.getString(cur.getColumnIndexOrThrow("body"));
                sms.add("Number: " + address + " .Message: " + body);

            }
        }
        else
        {
            final int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_SMS},
                    REQUEST_CODE_ASK_PERMISSIONS);

        }
    }
}
