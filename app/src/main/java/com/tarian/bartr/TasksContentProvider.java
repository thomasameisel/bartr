package com.tarian.bartr;

import android.app.ActivityManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class TasksContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.tarian.bartr.provider";
    private static final int TASKS = 1;
    private static final int TASKS_ID = 2;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, "tasks", TASKS);
        sURIMatcher.addURI(AUTHORITY, "tasks/#", TASKS_ID);
    }

    public static final Uri TASKS_URI = Uri.parse("content://" + AUTHORITY + "/" + "tasks");
    private TasksDatabase mDb;

    @Override
    public boolean onCreate() {
        mDb = new TasksDatabase(getContext());
        try {
            mDb.createDataBase();
        } catch (IOException e) {
            throw new Error("Cannot create database");
        }
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case TASKS:
                return "vnd.Android.cursor.dir/vnd.com.tarian.bartr.provider.tasks";
            case TASKS_ID:
                return "vnd.Android.cursor.dir/vnd.com.tarian.bartr.provider.tasks";
            default:
                throw new RuntimeException("getType No URI Match: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();
        long id;
        switch (sURIMatcher.match(uri)) {
            case TASKS:
                id = sqlDB.insertOrThrow(TasksDatabase.TABLES.TASKS, null, values);
                Log.d("insert", Long.toString(id));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(TasksDatabase.TABLES.TASKS + "/" + id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        switch (sURIMatcher.match(uri)) {
            case TASKS:
                return mDb.getTaskItems();
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}