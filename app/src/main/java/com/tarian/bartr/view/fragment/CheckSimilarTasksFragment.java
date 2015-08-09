package com.tarian.bartr.view.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tarian.bartr.R;

/**
 * Popup dialog fragment for either adding or editing a memorea<br>
 * Requires an Extra String dialog_title and boolean is_editing<br>
 * If is_editing is true, also requires String array of the memorea's information to pre-fill in the form
 */
public class CheckSimilarTasksFragment extends DialogFragment {
    private static final String CATEGORY = "category";

    private String mCategory;

    public static CheckSimilarTasksFragment newInstance(final String category) {
        CheckSimilarTasksFragment fragment = new CheckSimilarTasksFragment();
        Bundle args = new Bundle();
        args.putString(CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        mCategory = getArguments().getString(CATEGORY);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_check_similar_tasks, null);
        final View itemView = inflater.inflate(R.layout.similar_task_popup_item, null);
        ((TextView)itemView.findViewById(R.id.text_view_item)).setText("Milk");
        ((ViewGroup)view.findViewById(R.id.grid_layout_similar_tasks))
                .addView(itemView);
        final View itemView2 = inflater.inflate(R.layout.similar_task_popup_item, null);
        ((TextView)itemView2.findViewById(R.id.text_view_item)).setText("$5.00");
        ((ViewGroup)view.findViewById(R.id.grid_layout_similar_tasks))
                .addView(itemView2);

        return builder.setView(view)
                .setTitle(mCategory)
                .create();
    }
}
