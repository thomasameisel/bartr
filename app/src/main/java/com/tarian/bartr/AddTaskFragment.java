package com.tarian.bartr;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class AddTaskFragment extends Fragment {
    private EditText[] fields;

    public AddTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_task, container, false);
        fields = new EditText[5];
        fields[0] = (EditText)view.findViewById(R.id.edit_text_item);
        fields[1] = (EditText)view.findViewById(R.id.edit_text_category);
        fields[2] = (EditText)view.findViewById(R.id.edit_text_max_price);
        fields[3] = (EditText)view.findViewById(R.id.edit_text_bounty);
        fields[4] = (EditText)view.findViewById(R.id.edit_text_notes);
        return view;
    }

    public boolean allFieldsSet() {
        for (EditText field : fields) {
            if (field.getText().toString().matches("")) {
                return false;
            }
        }
        return true;
    }

    public String[] getFields() {
        final String[] stringFields = new String[5];
        for (int i = 0; i < fields.length; ++i) {
            stringFields[i] = fields[i].getText().toString();
        }
        return stringFields;
    }

    public void clearFields() {
        for (EditText field : fields) {
            field.setText(null);
        }
        fields[0].requestFocus();
    }
}
