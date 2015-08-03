package com.tarian.bartr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class RequestTaskPictureFragment extends Fragment {
    public interface OnClickReceipt {
        void openCamera();
        Bitmap loadImageFromStorage(String path);
    }

    private OnClickReceipt mOnClickReceiptListener;
    private String mPicturePath, mPrice;
    private String[] mTaskInfo;

    public static RequestTaskPictureFragment newInstance(final String picturePath,
                                                         final String price,
                                                         final String[] taskInfo) {
        RequestTaskPictureFragment fragment = new RequestTaskPictureFragment();
        Bundle args = new Bundle();
        args.putString(RequestTaskActivity.PICTURE_PATH, picturePath);
        args.putString(RequestTaskActivity.PRICE, price);
        args.putStringArray(ViewTasksActivity.TASK_INFO, taskInfo);
        fragment.setArguments(args);
        return fragment;
    }

    public RequestTaskPictureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPicturePath = getArguments().getString(RequestTaskActivity.PICTURE_PATH);
            mPrice = getArguments().getString(RequestTaskActivity.PRICE);
            mTaskInfo = getArguments().getStringArray(ViewTasksActivity.TASK_INFO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_request_task_picture, container, false);
        view.findViewById(R.id.image_view_receipt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                mOnClickReceiptListener.openCamera();
            }
        });
        final ImageView receiptImageView = (ImageView)view.findViewById(R.id.image_view_receipt);
        final Bitmap receiptPicture = mOnClickReceiptListener.loadImageFromStorage(mPicturePath);
        if (receiptPicture != null) {
            receiptImageView.setImageBitmap(receiptPicture);
        }
        if (mPrice != null) {
            ((EditText)view.findViewById(R.id.edit_text_price)).setText(mPrice);
        }
        RequestTaskActivity.setFields(view, mTaskInfo);
        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mOnClickReceiptListener = (OnClickReceipt)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnClickReceipt");
        }
    }
}
