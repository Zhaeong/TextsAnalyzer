package com.example.owen.textsanalyzer.helperConstructs;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.owen.textsanalyzer.MainActivity;
import com.example.owen.textsanalyzer.R;

/**
 * Created by Owen on 2017-03-23.
 * content adapter
 */

public class contactsAdapter extends CursorAdapter{


    Context context;

    public contactsAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.contacts_row, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template

        TextView contactName = (TextView)view.findViewById(R.id.contactName);

        // Extract properties from cursor
        String taskName = cursor.getString(cursor.getColumnIndexOrThrow(customDBHelper.CONTACTS_COL_NAME));

        contactName.setText(taskName);
    }


}