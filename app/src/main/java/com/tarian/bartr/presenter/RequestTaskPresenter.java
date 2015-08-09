package com.tarian.bartr.presenter;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.tarian.bartr.model.Task;
import com.tarian.bartr.view.fragment.ViewTasksMapFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import nucleus.presenter.RxPresenter;

/**
 * Created by Tommy on 8/8/2015.
 */
public class RequestTaskPresenter extends RxPresenter<ViewTasksMapFragment> {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    public String saveToInternalStorage(final Context context, final Bitmap bitmapImage) {
        final ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        final File path = new File(directory, "profile.jpg");

        try {
            FileOutputStream fos = new FileOutputStream(path);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }
}
