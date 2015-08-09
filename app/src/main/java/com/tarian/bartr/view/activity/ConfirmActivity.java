package com.tarian.bartr.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.tarian.bartr.R;
import com.tarian.bartr.view.fragment.AddTaskFragment;
import com.tarian.bartr.view.fragment.ViewTasksMapFragment;

import haibison.android.lockpattern.LockPatternActivity;

public class ConfirmActivity extends AppCompatActivity {

    private static final String PRICE = "price";
    private static final String ID = "id";
    private static final String FIELDS = "fields";
    private static final String PATTERN = "pattern";

    public static Intent getCallingIntent(Context context, String price, String id, String[] fields,
                                          char[] pattern) {
        final Intent intent = new Intent(context, ConfirmActivity.class);
        intent.putExtra(PRICE, price);
        intent.putExtra(ID, id);
        intent.putExtra(FIELDS, fields);
        intent.putExtra(PATTERN, pattern);
        return intent;
    }

    private static final int REQ_ENTER_PATTERN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        final String[] taskInfo = getIntent().getStringArrayExtra(FIELDS);

        final long bountyCents = Long.parseLong(taskInfo[2]);
        final double bountyDollars = ViewTasksMapFragment.centsToDollars(bountyCents);
        final String priceString = getIntent().getStringExtra(PRICE);
        final double priceDollars = Double.parseDouble(priceString);
        final long priceCents = AddTaskFragment.dollarsToCents(priceDollars);

        final long totalCents = bountyCents + priceCents;
        final double totalDollars = ViewTasksMapFragment.centsToDollars(totalCents);

        ((TextView)findViewById(R.id.text_view_confirm_item_needed))
                .setText(taskInfo[0]);
        ((TextView)findViewById(R.id.text_view_confirm_price))
                .setText(String.format("$%.2f", priceDollars));
        ((TextView)findViewById(R.id.text_view_confirm_bounty))
                .setText(String.format("$%.2f", bountyDollars));
        ((TextView)findViewById(R.id.text_view_confirm_total))
                .setText(String.format("$%.2f", totalDollars));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_ENTER_PATTERN: {
                switch (resultCode) {
                    case RESULT_OK:
                        // The user passed
                        final Intent intent = new Intent(this, ViewTasksActivity.class);
                        startActivity(intent);
                        break;
                    case RESULT_CANCELED:
                        // The user cancelled the task
                        break;
                    case LockPatternActivity.RESULT_FAILED:
                        // The user failed to enter the pattern
                        break;
                    case LockPatternActivity.RESULT_FORGOT_PATTERN:
                        // The user forgot the pattern and invoked your recovery Activity.
                        break;
                }
                break;
            }
        }
    }

    public void confirmPattern(final View view) {
        char[] savedPattern = getIntent().getCharArrayExtra(PATTERN);
        Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                this, LockPatternActivity.class);
        intent.putExtra(LockPatternActivity.EXTRA_PATTERN, savedPattern);
        startActivityForResult(intent, REQ_ENTER_PATTERN);
    }
}
