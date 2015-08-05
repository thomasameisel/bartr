package com.tarian.bartr;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.albinmathew.transitions.ActivityTransition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RequestTaskActivity extends AppCompatActivity
        implements RequestTaskPictureFragment.OnClickReceipt {
    static final String PICTURE_PATH = "picturePath";
    static final String PRICE = "price";

    private String mPicturePath, mPrice;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_task);
        ActivityTransition.with(getIntent())
                .to(findViewById(R.id.frame_layout_request_task)).start(savedInstanceState);
        if (savedInstanceState == null || savedInstanceState.getString(PICTURE_PATH) == null) {
            ((TextView)findViewById(R.id.button_toolbar)).setText(getString(R.string.send_receipt));
            findViewById(R.id.button_toolbar).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    openCamera();
                }
            });
            RequestTaskInfoFragment requestTaskFragment = new RequestTaskInfoFragment();
            requestTaskFragment.setArguments(getIntent().getExtras());
            final FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.frame_layout_request_task,
                    requestTaskFragment, "fragment_request_task_info");
            fragmentTransaction.commit();
        } else {
            mPicturePath = savedInstanceState.getString(PICTURE_PATH);
            mPrice = savedInstanceState.getString(PRICE);
            startPictureFragment(mPicturePath, mPrice);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            final Bitmap receiptPicture = (Bitmap) data.getExtras().get("data");
            mPicturePath = saveToInternalStorage(this, receiptPicture);
            startPictureFragment(mPicturePath, mPrice);
        }
    }

    @Override
    public void onBackPressed() {
        BasicDialog dialog = BasicDialog.newInstance(getString(R.string.cancel_task_question),
                null, true);
        dialog.setOnClickListener(new BasicDialog.OnBasicDialogClick() {
            @Override
            public void onPositiveClick() {
                RequestTaskActivity.this.finish();
            }
            @Override
            public void onNegativeClick() {
                // do nothing
            }
        });
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(PICTURE_PATH, mPicturePath);
        final EditText priceEditText = (EditText) findViewById(R.id.edit_text_price);
        if (priceEditText != null) {
            savedInstanceState.putString(PRICE, priceEditText.getText().toString());
        }
    }

    public void openCamera() {
        final Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    public String saveToInternalStorage(final Context context, final Bitmap bitmapImage) {
        final ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        final File path = new File(directory, "profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    public Bitmap loadImageFromStorage(final String path) {
        try {
            final File file = new File(path, "profile.jpg");
            return BitmapFactory.decodeStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFields(final View view, final String[] taskInfo) {
        ((TextView)view.findViewById(R.id.text_view_item_needed)).setText(taskInfo[1]);
        ((TextView)view.findViewById(R.id.text_view_max_price))
                .setText(String.format("$%.2f",
                        ViewTasksActivity.centsToDollars(Long.parseLong(taskInfo[2]))));
        ((TextView)view.findViewById(R.id.text_view_bounty))
                .setText(String.format("$%.2f",
                        ViewTasksActivity.centsToDollars(Long.parseLong(taskInfo[3]))));
        if (taskInfo[4].matches("")) {
            view.findViewById(R.id.text_view_notes_label).setVisibility(View.GONE);
            view.findViewById(R.id.text_view_notes).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.text_view_notes)).setText(taskInfo[4]);
        }
    }

    private void startPictureFragment(final String picturePath, final String price) {
        ((TextView)findViewById(R.id.button_toolbar)).setText(getString(R.string.drop_off));
        findViewById(R.id.button_toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final EditText priceEditText = ((EditText)findViewById(R.id.edit_text_price));
                if (priceEditText.getText().toString().matches("")) {
                    BasicDialog dialog =
                            BasicDialog.newInstance(getString(R.string.must_enter_price), null, false);
                    dialog.show(getSupportFragmentManager(), "dialog");
                } else {
                    final Intent confirmIntent = new Intent(RequestTaskActivity.this,
                            ConfirmActivity.class);
                    confirmIntent.putExtra(ViewTasksActivity.TASK_INFO,
                            getIntent().getExtras().getStringArray(ViewTasksActivity.TASK_INFO));
                    confirmIntent.putExtra(ViewTasksActivity.PATTERN,
                            getIntent().getExtras().getCharArray(ViewTasksActivity.PATTERN));
                    confirmIntent.putExtra(PRICE, priceEditText.getText().toString());
                    startActivity(confirmIntent);
                }
            }
        });
        final RequestTaskPictureFragment requestTaskFragment =
                RequestTaskPictureFragment.newInstance(picturePath, price,
                        getIntent().getStringArrayExtra(ViewTasksActivity.TASK_INFO));
        final FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_request_task,
                requestTaskFragment, "fragment_request_task_picture");
        fragmentTransaction.commit();
    }
}
