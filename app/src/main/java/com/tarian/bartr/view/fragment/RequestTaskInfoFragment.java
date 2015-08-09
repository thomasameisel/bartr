package com.tarian.bartr.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tarian.bartr.R;
import com.tarian.bartr.presenter.RequestTaskPresenter;
import com.tarian.bartr.view.activity.RequestTaskActivity;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;

@RequiresPresenter(RequestTaskPresenter.class)
public class RequestTaskInfoFragment extends NucleusSupportFragment<RequestTaskPresenter> {

    private static final String FIELDS = "fields";

    private String[] mFields;

    public RequestTaskInfoFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public RequestTaskInfoFragment(String[] fields) {
        mFields = fields;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mFields = savedInstanceState.getStringArray(FIELDS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_task, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RequestTaskActivity.setFields(view, mFields);
        getActivity().findViewById(R.id.button_action_toolbar).setVisibility(View.VISIBLE);
        ((Button)getActivity().findViewById(R.id.button_action_toolbar))
                .setText(getString(R.string.send_receipt));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putStringArray(FIELDS, mFields);
    }
}
