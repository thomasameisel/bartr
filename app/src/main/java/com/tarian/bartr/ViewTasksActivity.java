package com.tarian.bartr;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
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

public class ViewTasksActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnTouchListener {
    public static SharedPreferences sSharedPreferences;

    private boolean mIsFirstClick, mIsInfoFullScreen;
    private int mYDelta, mInitialY;
    private ArrayList<Pair<Marker, Task>> mTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sSharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tasks);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_google_map);
        mapFragment.getMapAsync(this);
        findViewById(R.id.scroll_view_view_task_info).setOnTouchListener(this);

        mIsFirstClick = true;
        mIsInfoFullScreen = false;
        mTasks = new ArrayList<>();
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
        if (mIsInfoFullScreen) {
            Animator animator = createTopMarginAnimator(findViewById(R.id.scroll_view_view_task_info), mInitialY);
            animator.setInterpolator(new BounceInterpolator());
            animator.start();
            mIsInfoFullScreen = false;
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            findViewById(R.id.button_accept_task_toolbar).setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        final LocationManager locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        final Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 12));

        initializeTasksFromSharedPref(map, sSharedPreferences.getAll());
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                marker.showInfoWindow();
                final Task task = getTaskByMarker(marker);
                if (task != null) {
                    changeTaskViewed(task);
                    if (mIsFirstClick) {
                        createInitialBounceAnimation();
                        mIsFirstClick = false;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        final int Y = (int) motionEvent.getRawY();
        if (!mIsInfoFullScreen) {
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                    mYDelta = Y - lParams.topMargin;
                    break;
                case MotionEvent.ACTION_UP:
                    if (layoutParams.topMargin < 0-dpToPx(150)) {
                        Animator animator = createTopMarginAnimator(view, 0-dpToPx(150));
                        animator.setInterpolator(new BounceInterpolator());
                        animator.start();
                    } else {
                        Animator animator = createTopMarginAnimator(view, mInitialY);
                        animator.setInterpolator(new BounceInterpolator());
                        animator.start();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    layoutParams.topMargin = Y - mYDelta;
                    view.setLayoutParams(layoutParams);
                    if (layoutParams.topMargin < 0-dpToPx(370)) {
                        makeInfoFullScreen(view, 0-dpToPx(386));
                    }
                    break;
            }
            view.invalidate();
        }
        return true;
    }

    private void makeInfoFullScreen(final View infoView, final int fullScreen) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.button_accept_task_toolbar).setVisibility(View.VISIBLE);
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) infoView.getLayoutParams();
        layoutParams.topMargin = fullScreen;
        mIsInfoFullScreen = true;
    }

    private void changeTaskViewed(final Task task) {
        ((TextView)findViewById(R.id.text_view_item_needed)).setText(task.mItem);
        ((TextView)findViewById(R.id.text_view_max_price)).setText(String.format("$%.2f", centsToDollars(task.mMaxPrice)));
        ((TextView)findViewById(R.id.text_view_bounty)).setText(String.format("$%.2f", centsToDollars(task.mBounty)));
        ((TextView)findViewById(R.id.text_view_notes)).setText(task.mNotes);
    }

    private void createInitialBounceAnimation() {
        final View infoView = findViewById(R.id.scroll_view_view_task_info);
        final Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        final Animator downAnimator = createTranslationYAnimator(infoView, size.y-dpToPx(300), size.y-dpToPx(150));
        downAnimator.setInterpolator(new BounceInterpolator());
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(createTranslationYAnimator(infoView, size.y, size.y - dpToPx(300)),
                downAnimator);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                infoView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                        findViewById(R.id.scroll_view_view_task_info).getLayoutParams();
                mInitialY = layoutParams.topMargin;
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
                        final Task task = new Task(fields[0],
                                Long.valueOf(fields[1]), Long.valueOf(fields[2]), fields[3], getLatLngFromString(fields[4]));
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
        //double latitude = Double.parseDouble(latlong[0]);
        final double latitude = 43.102379109211185;
        //final double longitude = Double.parseDouble(latlong[1]);
        final double longitude = -89.37296024417279;
        return new LatLng(latitude, longitude);
    }

    private long dollarsToCents(final double dollars) {
        return (long) (dollars * 100);
    }

    private double centsToDollars(final long cents) {
        return (double) cents / 100;
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
