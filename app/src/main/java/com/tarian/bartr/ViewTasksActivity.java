package com.tarian.bartr;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import haibison.android.lockpattern.LockPatternActivity;

public class ViewTasksActivity extends AppCompatActivity
        implements OnMapReadyCallback, View.OnTouchListener {

    static final String TASK_INFO = "taskInfo";
    static final String PATTERN = "pattern";

    static SharedPreferences sSharedPreferences;

    private static final int REQ_CREATE_PATTERN = 1;
    private static final String FRAGMENT_ADD_TASK = "fragment_add_task";
    private static final String MAP_SHOWING = "mapShowing";

    private boolean mIsFirstClick, mIsInfoShowing, mIsMapShowing;
    private int mYDelta;
    private Task mCurTask;
    private Marker mCurMarker;
    private ArrayList<Pair<Marker, Task>> mTasks;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sSharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_view_tasks);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        findViewById(R.id.button_action_toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (((Button)view).getText() == getString(R.string.accept_task)) {
                    acceptTaskClicked();
                } else {
                    saveTaskClicked();
                }
            }
        });
        mIsMapShowing = true;
        final SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_google_map);
        mapFragment.getMapAsync(this);
        findViewById(R.id.frame_layout_view_task_info).setVisibility(View.GONE);
        findViewById(R.id.frame_layout_view_task_info).setOnTouchListener(this);
        if (savedInstanceState != null && !savedInstanceState.getBoolean(MAP_SHOWING, true)) {
            setToolbarAddTask();
        }

        mIsFirstClick = true;
        mIsInfoShowing = false;
        mTasks = new ArrayList<>();
    }

    @Override
    public void onSaveInstanceState(final Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putBoolean(MAP_SHOWING, mIsMapShowing);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CREATE_PATTERN: {
                if (resultCode == RESULT_OK) {
                    final char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
                    final Intent requestIntent =
                            new Intent(ViewTasksActivity.this, RequestTaskActivity.class);
                    requestIntent.putExtra(TASK_INFO, mCurTask.getFieldsWithId());
                    requestIntent.putExtra(PATTERN, pattern);
                    startActivity(requestIntent);
                }

                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        final int id = menuItem.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!mIsMapShowing) {
            mIsMapShowing = true;
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            findViewById(R.id.button_action_toolbar).setVisibility(View.GONE);
            super.onBackPressed();
        }else if (mIsInfoShowing) {
            removeInfoView(findViewById(R.id.frame_layout_view_task_info));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;
        final LocationManager locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        final Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 12));

        initializeTasksFromSharedPref(map, sSharedPreferences.getAll());
        final View infoView = findViewById(R.id.frame_layout_view_task_info);
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                marker.showInfoWindow();
                mCurMarker = marker;
                final Task task = getTaskByMarker(marker);
                if (task != null) {
                    mCurTask = task;
                    changeTaskViewed(task);
                    if (mIsFirstClick) {
                        createInitialBounceAnimation(infoView);
                        mIsFirstClick = false;
                    }
                }
                return true;
            }
        });
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mCurTask != null) {
                    removeInfoView(infoView);
                }
            }
        });
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        final int Y = (int) motionEvent.getRawY();
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                mYDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                if (layoutParams.topMargin < 0) {
                    mIsInfoShowing = true;
                    Animator animator = createTopMarginAnimator(view, 0);
                    animator.setInterpolator(new BounceInterpolator());
                    animator.start();
                } else {
                    mIsInfoShowing = false;
                    removeInfoView(view);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                layoutParams.topMargin = Y - mYDelta;
                view.setLayoutParams(layoutParams);
                break;
        }
        view.invalidate();
        return true;
    }

    public void showAddTask(final View view) {
        mIsMapShowing = false;
        setToolbarAddTask();
        final AddTaskFragment addTaskFragment = new AddTaskFragment();
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frame_layout_add_task, addTaskFragment, FRAGMENT_ADD_TASK);
        fragmentTransaction.commit();
    }

    public void checkSimilarTasks(final View view) {
        View addTaskView = getSupportFragmentManager().findFragmentByTag(FRAGMENT_ADD_TASK)
                .getView();
        final String category = ((EditText)addTaskView.findViewById(R.id.edit_text_category))
                .getText().toString();
        CheckSimilarTasksFragment similarTasksFragment =
                CheckSimilarTasksFragment.newInstance(category);
        similarTasksFragment.show(getSupportFragmentManager(), "dialog");
    }

    public static long dollarsToCents(final double dollars) {
        return (long) (dollars * 100);
    }

    public static double centsToDollars(final long cents) {
        return (double) cents / 100;
    }

    private void setToolbarAddTask() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.button_action_toolbar).setVisibility(View.VISIBLE);
        ((Button)findViewById(R.id.button_action_toolbar)).setText(getString(R.string.save));
    }

    private void removeInfoView(final View infoView) {
        final Animator animator = createTopMarginAnimator(infoView, dpToPx(500));
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mCurMarker.hideInfoWindow();
                mIsInfoShowing = false;
                mIsFirstClick = true;
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                findViewById(R.id.button_action_toolbar).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                infoView.setVisibility(View.GONE);
                FrameLayout.LayoutParams layoutParams =
                        (FrameLayout.LayoutParams) infoView.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                infoView.setLayoutParams(layoutParams);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    private void acceptTaskClicked() {
        BasicDialog dialog = BasicDialog.newInstance(getString(R.string.accept_task_question),
                getString(R.string.accept_task_message), true);
        dialog.setOnClickListener(new BasicDialog.OnBasicDialogClick() {
            @Override
            public void onPositiveClick() {
                final Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null,
                        ViewTasksActivity.this, LockPatternActivity.class);
                startActivityForResult(intent, REQ_CREATE_PATTERN);
            }

            @Override
            public void onNegativeClick() {
                // do nothing
            }
        });
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    private void saveTaskClicked() {
        // save task
        AddTaskFragment addTaskFragment = (AddTaskFragment)
                getSupportFragmentManager().findFragmentByTag(FRAGMENT_ADD_TASK);
        if (addTaskFragment.allFieldsSet()) {
            // create task
            final LocationManager locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
            final Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            final LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            final String[] taskFields = addTaskFragment.getFields();
            final Task newTask = new Task(taskFields[0], dollarsToCents(Double.parseDouble(taskFields[2])),
                    dollarsToCents(Double.parseDouble(taskFields[3])), taskFields[4], curLatLng);
            newTask.generateNewUUID();
            sSharedPreferences.edit().putString(newTask.mId.toString(),
                    getJSONStringFromArray(newTask.getFields()).toString()).apply();
            final Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(newTask.mLocation)
                    .title(newTask.mItem)
                    .snippet(String.format("$%.2f", centsToDollars(newTask.mBounty))));
            mTasks.add(new Pair<>(marker, newTask));
            addTaskFragment.clearFields();
            onBackPressed();
        } else {
            BasicDialog dialog =
                    BasicDialog.newInstance(getString(R.string.enter_all_fields), null, false);
            dialog.show(getSupportFragmentManager(), null);
        }
    }

    private void changeTaskViewed(final Task task) {
        ((TextView)findViewById(R.id.text_view_item_needed)).setText(task.mItem);
        ((TextView)findViewById(R.id.text_view_max_price))
                .setText(String.format("$%.2f", centsToDollars(task.mMaxPrice)));
        ((TextView)findViewById(R.id.text_view_bounty))
                .setText(String.format("$%.2f", centsToDollars(task.mBounty)));
        if (task.mNotes.matches("")) {
            findViewById(R.id.text_view_notes_label).setVisibility(View.GONE);
            findViewById(R.id.text_view_notes).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.text_view_notes)).setText(task.mNotes);
        }
    }

    private void createInitialBounceAnimation(final View infoView) {
        final Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        final Animator downAnimator =
                createTranslationYAnimator(infoView, size.y - dpToPx(300), size.y - dpToPx(250));
        downAnimator.setInterpolator(new BounceInterpolator());
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(
                createTranslationYAnimator(infoView, size.y, size.y - dpToPx(300)),
                downAnimator);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsInfoShowing = true;
                infoView.setVisibility(View.VISIBLE);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                final Button actionButton = (Button) findViewById(R.id.button_action_toolbar);
                actionButton.setText(getString(R.string.accept_task));
                actionButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }

    private int dpToPx(final int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return (int)px;
    }

    private Task getTaskByMarker(final Marker marker) {
        for (Pair<Marker, Task> pairTask : mTasks) {
            if (pairTask.first.equals(marker)) {
                return pairTask.second;
            }
        }
        return null;
    }

    private void initializeTasksFromSharedPref(final GoogleMap map, final Map<String, ?> allKeys) {
        if (allKeys != null) {
            try {
                for (Map.Entry<String,?> entry : allKeys.entrySet()) {
                    if (entry.getValue().getClass() == String.class) {
                        final String[] fields = getStringArrayFromJSON(new JSONArray((String)entry.getValue()));
                        final Task task = new Task(fields[0], Long.valueOf(fields[1]),
                                Long.valueOf(fields[2]), fields[3], getLatLngFromString(fields[4]));
                        task.mId = UUID.fromString(entry.getKey());
                        final Marker marker = map.addMarker(new MarkerOptions()
                                .position(task.mLocation)
                                .title(task.mItem)
                                .snippet(String.format("$%.2f", centsToDollars(task.mBounty))));
                        mTasks.add(new Pair<>(marker, task));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private LatLng getLatLngFromString(String location) {
        final String[] latlong =  location.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        final double longitude = Double.parseDouble(latlong[1]);
        return new LatLng(latitude, longitude);
    }

    private double getRandomOffset() {
        return 0.1 * (Math.random()-0.5);
    }

    /**
     * Generates a JSON array from a String array
     * @param values String array to convert to JSON
     */
    private static JSONArray getJSONStringFromArray(final String[] values) {
        final JSONArray array = new JSONArray();
        for (String value : values) {
            array.put(value);
        }
        return array;
    }

    /**
     * Generates a String array from a JSON array
     * @param array JSON array to convert to a String array
     */
    private static String[] getStringArrayFromJSON(final JSONArray array) {
        final String[] stringArray = new String[array.length()];
        try {
            for (int i = 0; i < array.length(); ++i) {
                stringArray[i] = array.getString(i);
            }
        } finally {
            return stringArray;
        }
    }

    private static final String TRANSLATION_X = "translationX";
    private static final String TRANSLATION_Y = "translationY";
    private static final String ALPHA = "alpha";

    private Animator createTranslationXAnimator(final View view, final int start, final int end) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_X, start, end)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createTranslationYAnimator(final View view, final int start, final int end) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, start, end)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createFadeAnimator(final View view, final int from, final int to) {
        return ObjectAnimator.ofFloat(view, ALPHA, from, to)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createTopMarginAnimator(final View view, final int topMargin) {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        final ValueAnimator animator = ValueAnimator.ofInt(params.topMargin, topMargin);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                view.requestLayout();
            }
        });
        animator.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        return animator;
    }
}
