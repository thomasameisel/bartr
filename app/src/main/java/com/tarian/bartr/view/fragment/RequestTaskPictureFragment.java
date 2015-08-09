package com.tarian.bartr.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.tarian.bartr.R;
import com.tarian.bartr.presenter.RequestTaskPresenter;
import com.tarian.bartr.view.activity.RequestTaskActivity;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;

@RequiresPresenter(RequestTaskPresenter.class)
public class RequestTaskPictureFragment extends NucleusSupportFragment<RequestTaskPresenter> {

    public interface OnClickReceipt {
        void openCamera();
        Bitmap loadImageFromStorage(String path);
    }

    private static final String PICTURE_PATH = "picturePath";
    private static final String PRICE = "price";
    private static final String FIELDS = "fields";

    private OnClickReceipt mOnClickReceiptListener;
    private String mPicturePath, mPrice;
    private String[] mFields;

    public RequestTaskPictureFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public RequestTaskPictureFragment(String picturePath, String[] fields) {
        mPicturePath = picturePath;
        mFields = fields;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mPicturePath = getArguments().getString(PICTURE_PATH);
            mPrice = getArguments().getString(PRICE);
            mFields = getArguments().getStringArray(FIELDS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_task_picture, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
        RequestTaskActivity.setFields(view, mFields);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(PICTURE_PATH, mPicturePath);
        savedInstanceState.putString(PRICE, mPrice);
        savedInstanceState.putStringArray(FIELDS, mFields);
    }
}
