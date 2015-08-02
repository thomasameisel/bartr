package com.tarian.bartr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RequestTaskInfoFragment extends Fragment {
    public RequestTaskInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_request_task, container, false);
        setFields(view, getArguments().getStringArray(ViewTasksActivity.TASK_INFO));
        return view;
    }

    private void setFields(final View view, final String[] taskInfo) {
        ((TextView)view.findViewById(R.id.text_view_item_needed))
                .setText(taskInfo[1]);
        ((TextView)view.findViewById(R.id.text_view_max_price))
                .setText(String.format("$%.2f",
                        ViewTasksActivity.centsToDollars(Long.parseLong(taskInfo[2]))));
        ((TextView)view.findViewById(R.id.text_view_bounty))
                .setText(String.format("$%.2f",
                        ViewTasksActivity.centsToDollars(Long.parseLong(taskInfo[3]))));
        ((TextView)view.findViewById(R.id.text_view_notes))
                .setText(taskInfo[4]);
    }
}
