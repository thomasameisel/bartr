package com.tarian.bartr;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

/**
 * Popup dialog fragment for a basic dialog fragment with a mTitle, message, and OK button
 */
public class BasicDialog extends DialogFragment {
    public interface OnBasicDialogClick {
        void onPositiveClick();
        void onNegativeClick();
    }

    private OnBasicDialogClick mOnBasicDialogClick;
    private static final String DIALOG_TITLE = "dialogTitle";
    private static final String DIALOG_MESSAGE = "dialogMessage";
    private static final String SHOW_NO_BUTTON = "showNoButton";

    /**
     * Creates a new dialog fragment
     * @param title Title of the dialog fragment
     * @param message Message of the dialog fragment
     */
    public static BasicDialog newInstance(final String title, final String message,
                                          final boolean showNoButton) {
        final BasicDialog frag = new BasicDialog();
        final Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putString(DIALOG_MESSAGE, message);
        args.putBoolean(SHOW_NO_BUTTON, showNoButton);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final String title = getArguments().getString(DIALOG_TITLE);
        final String message = getArguments().getString(DIALOG_MESSAGE);
        final boolean showNoButton = getArguments().getBoolean(SHOW_NO_BUTTON);
        final int okayText = (showNoButton) ? R.string.yes : R.string.ok;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(okayText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnBasicDialogClick != null) {
                            mOnBasicDialogClick.onPositiveClick();
                        }
                    }
                });
        if (showNoButton) {
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mOnBasicDialogClick != null) {
                        mOnBasicDialogClick.onNegativeClick();
                    }
                }
            });
        }
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void setOnClickListener(final OnBasicDialogClick listener) {
        mOnBasicDialogClick = listener;
    }
}