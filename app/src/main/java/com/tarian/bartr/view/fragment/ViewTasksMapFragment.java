package com.tarian.bartr.view.fragment;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tarian.bartr.R;
import com.tarian.bartr.model.Task;
import com.tarian.bartr.presenter.ViewTasksPresenter;

import java.util.ArrayList;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusSupportFragment;

@RequiresPresenter(ViewTasksPresenter.class)
public class ViewTasksMapFragment extends NucleusSupportFragment<ViewTasksPresenter>
        implements OnMapReadyCallback, View.OnTouchListener {

    private static View sView;
    private GoogleMap mMap;
    private int mYDelta;
    private boolean mIsFirstClick, mIsInfoShowing;
    private Pair<Marker, Task> mCurMarkerTask;
    private ArrayList<Pair<Marker, Task>> mMarkersTasks;

    public ViewTasksMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (sView != null) {
            ViewGroup parent = (ViewGroup) sView.getParent();
            if (parent != null) {
                parent.removeView(sView);
            }
        }
        try {
            sView = inflater.inflate(R.layout.fragment_view_tasks_map, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        return sView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager()
                .findFragmentById(R.id.fragment_google_map);
        mapFragment.getMapAsync(this);
        mIsInfoShowing = false;
        view.findViewById(R.id.card_view_task_info).setVisibility(View.GONE);
        view.findViewById(R.id.card_view_task_info).setOnTouchListener(this);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getActivity().findViewById(R.id.button_action_toolbar).setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mIsFirstClick = true;
        mIsInfoShowing = false;

        final LocationManager locationManager = (LocationManager)getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        final Location location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 12));
        mMap.clear();

        ArrayList<Task> tasks = getPresenter().getAllTasks(getActivity());
        mMarkersTasks = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            addTaskToMap(task);
        }
        final View infoView = getView().findViewById(R.id.card_view_task_info);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                ViewTasksMapFragment.this.onMarkerClick(marker, infoView);
                return true;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mCurMarkerTask != null) {
                    removeInfoView(infoView);
                }
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        final int Y = (int) motionEvent.getRawY();
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                view.getLayoutParams();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams)
                        view.getLayoutParams();
                mYDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                if (layoutParams.topMargin < 0) {
                    Animator animator = createTopMarginAnimator(getActivity(), view, 0);
                    animator.setInterpolator(new BounceInterpolator());
                    animator.start();
                } else {
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

    public void addTaskToMap(Task task) {
        final Marker marker = setMarkerFromTask(mMap, task);
        mMarkersTasks.add(new Pair<>(marker, task));
    }

    public Task getCurTask() {
        return mCurMarkerTask.second;
    }

    public void onBackPressed() {
        if (mIsInfoShowing) {
            removeInfoView(getView().findViewById(R.id.card_view_task_info));
        }
    }

    public static double centsToDollars(final long cents) {
        return (double) cents / 100;
    }

    private void onMarkerClick(Marker marker, View infoView) {
        marker.showInfoWindow();
        final Task task = getTaskByMarker(marker);
        if (task != null) {
            mIsInfoShowing = true;
            mCurMarkerTask = new Pair<>(marker, task);
            changeTaskViewed(task);
            if (mIsFirstClick) {
                createInitialBounceAnimation(infoView);
                mIsFirstClick = false;
            }
        }
    }

    private Task getTaskByMarker(final Marker marker) {
        for (Pair<Marker, Task> pairTask : mMarkersTasks) {
            if (pairTask.first.equals(marker)) {
                return pairTask.second;
            }
        }
        return null;
    }

    private Marker setMarkerFromTask(final GoogleMap map, final Task task) {
        return map.addMarker(new MarkerOptions()
                .position(task.mLocation)
                .title(task.mItem)
                .snippet(String.format("$%.2f", centsToDollars(task.mBounty))));
    }

    private void createInitialBounceAnimation(final View infoView) {
        final Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        final Animator downAnimator = createTranslationYAnimator(getActivity(), infoView,
                size.y - dpToPx(300), size.y - dpToPx(250));
        downAnimator.setInterpolator(new BounceInterpolator());
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(
                createTranslationYAnimator(getActivity(), infoView, size.y, size.y - dpToPx(300)),
                downAnimator);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                infoView.setVisibility(View.VISIBLE);
                final Button actionButton = (Button)getActivity()
                        .findViewById(R.id.button_action_toolbar);
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

    private void removeInfoView(final View infoView) {
        final Animator animator = createTopMarginAnimator(getActivity(), infoView, dpToPx(500));
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mCurMarkerTask.first.hideInfoWindow();
                mIsFirstClick = true;
                mIsInfoShowing = false;
                getActivity().findViewById(R.id.button_action_toolbar).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                infoView.setVisibility(View.GONE);
                FrameLayout.LayoutParams layoutParams =
                        (FrameLayout.LayoutParams) infoView.getLayoutParams();
                layoutParams.setMargins(dpToPx(10), 0, dpToPx(10), 0);
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

    private int dpToPx(final int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources()
                .getDisplayMetrics());
        return (int)px;
    }

    private void changeTaskViewed(final Task task) {
        ((TextView)getView().findViewById(R.id.text_view_item_needed)).setText(task.mItem);
        ((TextView)getView().findViewById(R.id.text_view_max_price))
                .setText(String.format("$%.2f", centsToDollars(task.mMaxPrice)));
        ((TextView)getView().findViewById(R.id.text_view_bounty))
                .setText(String.format("$%.2f", centsToDollars(task.mBounty)));
        if (task.mNotes.matches("")) {
            getView().findViewById(R.id.text_view_notes_label).setVisibility(View.GONE);
            getView().findViewById(R.id.text_view_notes).setVisibility(View.GONE);
        } else {
            ((TextView)getView().findViewById(R.id.text_view_notes)).setText(task.mNotes);
        }
    }

    private static final String TRANSLATION_Y = "translationY";

    public static Animator createTranslationYAnimator(final Context context, final View view,
                                                      final int start, final int end) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, start, end)
                .setDuration(context.getResources()
                        .getInteger(android.R.integer.config_mediumAnimTime));
    }

    public static Animator createTopMarginAnimator(final Context context, final View view,
                                                   final int topMargin) {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        final ValueAnimator animator = ValueAnimator.ofInt(params.topMargin, topMargin);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                view.requestLayout();
            }
        });
        animator.setDuration(context.getResources()
                .getInteger(android.R.integer.config_mediumAnimTime));
        return animator;
    }
}
