package com.tarian.bartr.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.albinmathew.transitions.ActivityTransition;
import com.tarian.bartr.R;
import com.tarian.bartr.presenter.RequestTaskPresenter;
import com.tarian.bartr.view.dialog.BasicDialog;
import com.tarian.bartr.view.fragment.FragmentStack;
import com.tarian.bartr.view.fragment.RequestTaskInfoFragment;
import com.tarian.bartr.view.fragment.RequestTaskPictureFragment;
import com.tarian.bartr.view.fragment.ViewTasksMapFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusAppCompatActivity;
import nucleus.view.ViewWithPresenter;

@RequiresPresenter(RequestTaskPresenter.class)
public class RequestTaskActivity extends NucleusAppCompatActivity<RequestTaskPresenter>
        implements RequestTaskPictureFragment.OnClickReceipt {

    private static final String ID = "id";
    private static final String FIELDS = "fields";
    private static final String PATTERN = "pattern";

    public static Intent getCallingIntent(Context context, String id, String[] fields,
                                          char[] pattern) {
        final Intent intent = new Intent(context, RequestTaskActivity.class);
        intent.putExtra(ID, id);
        intent.putExtra(FIELDS, fields);
        intent.putExtra(PATTERN, pattern);
        return intent;
    }

    private FragmentStack mFragmentStack;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_task);
        ActivityTransition.with(getIntent())
                .to(findViewById(R.id.frame_layout_fragment_container)).start(savedInstanceState);
        findViewById(R.id.button_toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((Button)view).getText().toString().matches(getString(R.string.send_receipt))) {
                    clickSendReceipt();
                } else {
                    clickDropOff();
                }
            }
        });
        mFragmentStack = new FragmentStack(this, getSupportFragmentManager(),
                R.id.frame_layout_fragment_container,
                new FragmentStack.OnFragmentRemovedListener() {
                    @Override
                    public void onFragmentRemoved(Fragment fragment) {
                        if (fragment instanceof ViewWithPresenter) {
                            ((ViewWithPresenter) fragment).getPresenter().destroy();
                        }
                    }
                });
        if (savedInstanceState == null) {
            mFragmentStack.push(new RequestTaskInfoFragment(getIntent()
                    .getStringArrayExtra(FIELDS)));
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && mFragmentStack.peek() instanceof RequestTaskInfoFragment) {
            ((Button)findViewById(R.id.button_toolbar)).setText(getString(R.string.drop_off));
            final Bitmap receiptPicture = (Bitmap) data.getExtras().get("data");
            String picturePath = getPresenter().saveToInternalStorage(this, receiptPicture);
            RequestTaskPictureFragment pictureFragment = new RequestTaskPictureFragment(picturePath,
                    getIntent().getStringArrayExtra(FIELDS));
            mFragmentStack.push(pictureFragment);
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

    public void openCamera() {
        final Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
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
        ((TextView)view.findViewById(R.id.text_view_item_needed)).setText(taskInfo[0]);
        ((TextView)view.findViewById(R.id.text_view_max_price))
                .setText(String.format("$%.2f",
                        ViewTasksMapFragment.centsToDollars(Long.parseLong(taskInfo[1]))));
        ((TextView)view.findViewById(R.id.text_view_bounty))
                .setText(String.format("$%.2f",
                        ViewTasksMapFragment.centsToDollars(Long.parseLong(taskInfo[2]))));
        if (taskInfo[4].matches("")) {
            view.findViewById(R.id.text_view_notes_label).setVisibility(View.GONE);
            view.findViewById(R.id.text_view_notes).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.text_view_notes)).setText(taskInfo[3]);
        }
    }

    private void clickSendReceipt() {
        openCamera();
    }

    private void clickDropOff() {
        final EditText priceEditText = ((EditText)findViewById(R.id.edit_text_price));
        if (priceEditText.getText().toString().matches("")) {
            BasicDialog dialog =
                    BasicDialog.newInstance(getString(R.string.must_enter_price), null, false);
            dialog.show(getSupportFragmentManager(), "dialog");
        } else {
            final Intent confirmIntent = ConfirmActivity.getCallingIntent(this,
                    priceEditText.getText().toString(), getIntent().getStringExtra(ID),
                    getIntent().getStringArrayExtra(FIELDS),
                    getIntent().getCharArrayExtra(PATTERN));
            startActivity(confirmIntent);
        }
    }
}
