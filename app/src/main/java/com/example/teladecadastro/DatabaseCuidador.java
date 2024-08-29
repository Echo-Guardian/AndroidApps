package com.example.teladecadastro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DatabaseCuidador extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chatMessages.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_SENDER = "sender";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_DURATION = "duration";

    public DatabaseCuidador(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TEXT + " TEXT, " +
                COLUMN_SENDER + " TEXT, " +
                COLUMN_TIMESTAMP + " LONG, " +
                COLUMN_DURATION + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COLUMN_DURATION + " INTEGER");
        }
    }

    public void addMessage(String text, String sender, long timestamp, int duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, text);
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_DURATION, duration);
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT));
                String sender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION));

                Message message;
                if (text != null) {
                    if (duration > 0) {
                        message = new Message(text, duration, sender, timestamp);
                    } else {
                        message = new Message(text, sender, timestamp);
                    }
                } else {
                    message = new Message("", sender, timestamp);
                }

                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messages;
    }

    public void deleteOldMessages() {
        long fourteenDaysAgo = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000L;
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, COLUMN_TIMESTAMP + " < ?", new String[]{String.valueOf(fourteenDaysAgo)});
        db.close();
    }

    public void deleteAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, null, null);
        db.close();
    }
}