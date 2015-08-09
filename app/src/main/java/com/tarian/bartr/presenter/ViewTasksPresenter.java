package com.tarian.bartr.presenter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.tarian.bartr.model.Task;
import com.tarian.bartr.model.TasksContentProvider;
import com.tarian.bartr.model.TasksDatabase;
import com.tarian.bartr.view.fragment.ViewTasksMapFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nucleus.presenter.RxPresenter;

/**
 * Created by Tommy on 8/8/2015.
 */
public class ViewTasksPresenter extends RxPresenter<ViewTasksMapFragment> {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    public ArrayList<Task> getAllTasks(Context context) {
        final ArrayList<Task> tasks = new ArrayList<>();
        final Cursor c = context.getContentResolver().query(
                TasksContentProvider.TASKS_URI, null, null, null, null);
        while (c.moveToNext()) {
            final UUID id = UUID.fromString(c.getString(c
                    .getColumnIndexOrThrow(TasksDatabase.MenuColumns.ID)));
            final String item = c.getString(c
                    .getColumnIndexOrThrow(TasksDatabase.MenuColumns.ITEM));
            final String category = c.getString(c
                    .getColumnIndexOrThrow(TasksDatabase.MenuColumns.CATEGORY));
            final long maxPrice = c.getLong(c
                    .getColumnIndexOrThrow(TasksDatabase.MenuColumns.MAXPRICE));
            final long bounty = c.getLong(c
                    .getColumnIndexOrThrow(TasksDatabase.MenuColumns.BOUNTY));
            final String notes = c.getString(c
                    .getColumnIndexOrThrow(TasksDatabase.MenuColumns.NOTES));
            final LatLng latLng = getLatLngFromString(c.getString(c
                    .getColumnIndexOrThrow(TasksDatabase.MenuColumns.LATLNG)));
            final Task task = new Task(item, maxPrice, bounty, notes, latLng);
            task.mId = id;
            tasks.add(task);
        }
        c.close();
        return tasks;
    }

    public void saveTask(Context context, Task newTask) {
        ContentValues values = new ContentValues();
        values.put(TasksDatabase.MenuColumns.ID, newTask.mId.toString());
        values.put(TasksDatabase.MenuColumns.ITEM, newTask.mItem);
        values.put(TasksDatabase.MenuColumns.CATEGORY, "");
        values.put(TasksDatabase.MenuColumns.MAXPRICE, newTask.mMaxPrice);
        values.put(TasksDatabase.MenuColumns.BOUNTY, newTask.mBounty);
        values.put(TasksDatabase.MenuColumns.NOTES, newTask.mNotes);
        values.put(TasksDatabase.MenuColumns.LATLNG, newTask.latLngToString(newTask.mLocation));

        context.getContentResolver().insert(TasksContentProvider.TASKS_URI, values);
    }

    private LatLng getLatLngFromString(String location) {
        final String[] latlong =  location.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        final double longitude = Double.parseDouble(latlong[1]);
        return new LatLng(latitude, longitude);
    }
}
