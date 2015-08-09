package com.tarian.bartr.view.fragment;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tarian.bartr.R;
import com.tarian.bartr.model.Task;
import com.tarian.bartr.presenter.ViewTasksPresenter;
import com.tarian.bartr.view.dialog.BasicDialog;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;

@RequiresPresenter(ViewTasksPresenter.class)
public class AddTaskFragment extends NucleusSupportFragment<ViewTasksPresenter> {
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
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fields = new EditText[5];
        fields[0] = (EditText)view.findViewById(R.id.edit_text_item);
        fields[1] = (EditText)view.findViewById(R.id.edit_text_category);
        fields[2] = (EditText)view.findViewById(R.id.edit_text_max_price);
        fields[3] = (EditText)view.findViewById(R.id.edit_text_bounty);
        fields[4] = (EditText)view.findViewById(R.id.edit_text_notes);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().findViewById(R.id.button_action_toolbar).setVisibility(View.VISIBLE);
        ((Button)getActivity().findViewById(R.id.button_action_toolbar))
                .setText(getString(R.string.save));
    }

    public boolean allFieldsSet() {
        for (EditText field : fields) {
            if (field.getText().toString().matches("")) {
                return false;
            }
        }
        return true;
    }

    public Task addTask() {
        // create task
        final LocationManager locationManager = (LocationManager)getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        final Location location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        final String[] taskFields = getFields();
        final Task newTask = new Task(taskFields[0],
                dollarsToCents(Double.parseDouble(taskFields[2])),
                dollarsToCents(Double.parseDouble(taskFields[3])), taskFields[4], curLatLng);
        newTask.generateNewUUID();
        clearFields();
        return newTask;
    }

    public static long dollarsToCents(final double dollars) {
        return (long) (dollars * 100);
    }

    private String[] getFields() {
        final String[] stringFields = new String[5];
        for (int i = 0; i < fields.length; ++i) {
            stringFields[i] = fields[i].getText().toString();
        }
        return stringFields;
    }

    private void clearFields() {
        for (EditText field : fields) {
            field.setText(null);
        }
        fields[0].requestFocus();
    }
}
