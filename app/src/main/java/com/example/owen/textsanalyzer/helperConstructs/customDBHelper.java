package com.example.owen.textsanalyzer.helperConstructs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class customDBHelper extends SQLiteOpenHelper {

    private static customDBHelper sDeviceInternalDB;

    public static final String DATABASE_NAME = "SMSContacts.db";
    public static final int DATABASE_VERSION = 1;


    public static final String CONTACTS_TABLE_NAME = "CONTACTS";

    public static final String CONTACTS_COL_ID = "_id";
    public static final String CONTACTS_COL_NAME = "Name";
    public static final String CONTACTS_COL_NUMBER = "Number";


    public static final String TABLE_CREATE_CONTACTS =
            "CREATE TABLE " +
                    CONTACTS_TABLE_NAME +
                    "( " + CONTACTS_COL_ID + " INTEGER PRIMARY KEY, " +
                    CONTACTS_COL_NAME +
                    " TEXT, " +
                    CONTACTS_COL_NUMBER +
                    " TEXT " +
                    " )";

    public customDBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_CONTACTS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
        onCreate(db);
    }

    public static synchronized customDBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.

        if (sDeviceInternalDB == null) {
            sDeviceInternalDB = new customDBHelper(context.getApplicationContext());
        }
        return sDeviceInternalDB;
    }


    //
    //General Functions
    //

    public Cursor getAllItemsInTable(String tableName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from " + tableName, null );
    }

    //
    //Posts Functions
    //

    public long addContact (String name, String number) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(CONTACTS_COL_NAME, name);
        contentValues.put(CONTACTS_COL_NUMBER, number);


        return db.insert(CONTACTS_TABLE_NAME, null, contentValues);
    }

    public boolean doesContactExist(String number)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "select * from " + CONTACTS_TABLE_NAME + " where " + CONTACTS_COL_NUMBER + " = " + "'" + number + "'";
        Cursor result = db.rawQuery( sqlQuery, null );
        if(result.getCount() > 0) {
            result.close();
            return true;
        }
        return false;
    }

    public ContactObj getContact(Long number)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "select * from " + CONTACTS_TABLE_NAME + " where " + CONTACTS_COL_ID + " = " + number;
        Cursor result = db.rawQuery( sqlQuery, null );
        if(result.getCount() == 1) {
            result.moveToFirst();
            String contactName = result.getString(result.getColumnIndex(CONTACTS_COL_NAME));
            String contactNum = result.getString(result.getColumnIndex(CONTACTS_COL_NUMBER));

            ContactObj conObj = new ContactObj(number, contactName, contactNum);
            result.close();
            return conObj;
        }
        else
        {
            return null;
        }
    }

    public void deleteAllContacts()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ CONTACTS_TABLE_NAME);
    }

    public void printAllItemsInTable(String tableName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + tableName, null );
        try {
            res.moveToFirst();
            String loggerMSG = tableName + "\n";
            while (!res.isAfterLast()) {
                String loggerLine = "";
                for(int i = 0; i < res.getColumnCount(); i++)
                {
                    loggerLine += res.getString(i);
                    loggerLine += " ";
                }

                loggerLine += "\n";
                loggerMSG +=loggerLine;

                res.moveToNext();
            }
            Log.i("DatabaseHelper", loggerMSG);
        }
        finally {
            res.close();
        }

    }

}
