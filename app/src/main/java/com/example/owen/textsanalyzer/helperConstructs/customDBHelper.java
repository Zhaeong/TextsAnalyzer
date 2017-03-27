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
    public static final int DATABASE_VERSION = 2;


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


    public static final String TEXTS_TABLE_NAME = "TEXTS";

    public static final String TEXTS_COL_ID = "_id";
    public static final String TEXTS_COL_TEXTID = "TextID";
    public static final String TEXTS_COL_ADDRESS = "Address";
    public static final String TEXTS_COL_BODY = "Body";
    public static final String TEXTS_COL_READ = "Read";
    public static final String TEXTS_COL_DATE = "Date";
    public static final String TEXTS_COL_TYPE = "Type";

    public static final String TABLE_CREATE_TEXTS =
            "CREATE TABLE " +
                    TEXTS_TABLE_NAME +
                    "( " + TEXTS_COL_ID + " INTEGER PRIMARY KEY, " +
                    TEXTS_COL_TEXTID +
                    " TEXT, " +
                    TEXTS_COL_ADDRESS +
                    " TEXT, " +
                    TEXTS_COL_BODY +
                    " TEXT, " +
                    TEXTS_COL_READ +
                    " TEXT, " +
                    TEXTS_COL_DATE +
                    " TEXT, " +
                    TEXTS_COL_TYPE +
                    " TEXT " +
                    " )";

    public customDBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_CONTACTS);
        db.execSQL(TABLE_CREATE_TEXTS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TEXTS_TABLE_NAME);
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
    //Contacts Functions
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

    public Integer deleteContact (String number) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CONTACTS_TABLE_NAME,
                CONTACTS_COL_NUMBER + " = ? ",
                new String[] { number });
    }

    public void deleteAllItemsInTable(String table)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ table);
    }



    //
    //Texts Functions
    //

    public long addText (String TextId, String address, String body,String read,String date,String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(TEXTS_COL_TEXTID, TextId);
        contentValues.put(TEXTS_COL_ADDRESS, address);
        contentValues.put(TEXTS_COL_BODY, body);
        contentValues.put(TEXTS_COL_READ, read);
        contentValues.put(TEXTS_COL_DATE, date);
        contentValues.put(TEXTS_COL_TYPE, type);


        return db.insert(TEXTS_TABLE_NAME, null, contentValues);
    }

    public int getSMSReceivedfromNumber(String number)
    {

        SQLiteDatabase db = this.getReadableDatabase();

        String NumOnly = getNumericOnly(number);

        String sqlQuery = "select * from " + TEXTS_TABLE_NAME + " where " + TEXTS_COL_ADDRESS + " LIKE " + "'%" + NumOnly + "%'"
                + " AND " + TEXTS_COL_TYPE + " = 'inbox'";
        Cursor result = db.rawQuery( sqlQuery, null );

        int numSMS = result.getCount();
        result.close();
        return numSMS;
    }

    public int getSMSSentfromNumber(String number)
    {

        SQLiteDatabase db = this.getReadableDatabase();

        String NumOnly = getNumericOnly(number);

        String sqlQuery = "select * from " + TEXTS_TABLE_NAME + " where " + TEXTS_COL_ADDRESS + " LIKE " + "'%" + NumOnly + "%'"
                + " AND " + TEXTS_COL_TYPE + " = 'sent'";
        Cursor result = db.rawQuery( sqlQuery, null );
        int numSMS = result.getCount();
        result.close();
        return numSMS;
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

    public String getNumericOnly(String inputString)
    {
        if (inputString == null) {
            return null;
        }

        StringBuilder strBuff = new StringBuilder();
        char c;

        for (int i = 0; i < inputString.length() ; i++) {
            c = inputString.charAt(i);

            if (Character.isDigit(c)) {
                strBuff.append(c);
            }
        }
        return strBuff.toString();
    }

}
