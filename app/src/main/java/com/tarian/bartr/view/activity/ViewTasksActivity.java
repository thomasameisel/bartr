package com.tarian.bartr.view.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.albinmathew.transitions.ActivityTransitionLauncher;
import com.tarian.bartr.R;
import com.tarian.bartr.model.Task;
import com.tarian.bartr.presenter.ViewTasksPresenter;
import com.tarian.bartr.view.dialog.BasicDialog;
import com.tarian.bartr.view.fragment.AddTaskFragment;
import com.tarian.bartr.view.fragment.CheckSimilarTasksFragment;
import com.tarian.bartr.view.fragment.FragmentStack;
import com.tarian.bartr.view.fragment.ViewTasksMapFragment;

import haibison.android.lockpattern.LockPatternActivity;
import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusAppCompatActivity;
import nucleus.view.ViewWithPresenter;

@RequiresPresenter(ViewTasksPresenter.class)
public class ViewTasksActivity extends NucleusAppCompatActivity<ViewTasksPresenter> {

    static final String TASK_INFO = "taskInfo";
    static final String PATTERN = "pattern";

    private static final int REQ_CREATE_PATTERN = 1;

    private FragmentStack mFragmentStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_view_tasks);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        findViewById(R.id.button_action_toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (((Button) view).getText() == getString(R.string.accept_task)) {
                    acceptTaskClicked();
                } else {
                    saveTaskClicked();
                }
            }
        });

        mFragmentStack = new FragmentStack(this, getSupportFragmentManager(),
                R.id.frame_layout_fragment_container,
                new FragmentStack.OnFragmentRemovedListener() {
                    @Override
                    public void onFragmentRemoved(Fragment fragment) {
                        if (fragment instanceof ViewWithPresenter) {
                            ((ViewWithPresenter) fragment).getPresenter().destroy();
                        }
                    }
        });
        if (savedInstanceState == null) {
            mFragmentStack.push(new ViewTasksMapFragment());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CREATE_PATTERN: {
                if (resultCode == RESULT_OK) {
                    if (mFragmentStack.peek() instanceof ViewTasksMapFragment) {
                        final char[] pattern = data
                                .getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
                        Task curTask = ((ViewTasksMapFragment)mFragmentStack.peek()).getCurTask();
                        final Intent requestIntent = RequestTaskActivity
                                .getCallingIntent(this, curTask.mId.toString(), curTask.getFields(),
                                pattern);
                        ActivityTransitionLauncher.with(this)
                                .from(findViewById(R.id.card_view_task_info)).launch(requestIntent);

                    }
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
        if (mFragmentStack.peek() instanceof ViewTasksMapFragment) {
            ((ViewTasksMapFragment)mFragmentStack.peek()).onBackPressed();
        } else {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(findViewById(R.id.frame_layout_fragment_container)
                    .getWindowToken(), 0);
            mFragmentStack.pop();
        }
    }

    public void showAddTask(final View view) {
        mFragmentStack.push(new AddTaskFragment());
    }

    public void checkSimilarTasks(final View view) {
        if (mFragmentStack.peek() instanceof AddTaskFragment) {
            final View addTaskView = mFragmentStack.peek().getView();
            final String category = ((EditText)addTaskView.findViewById(R.id.edit_text_category))
                    .getText().toString();
            CheckSimilarTasksFragment similarTasksFragment =
                    CheckSimilarTasksFragment.newInstance(category);
            similarTasksFragment.show(getSupportFragmentManager(), null);
        }
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
        dialog.show(getSupportFragmentManager(), null);
    }

    private void saveTaskClicked() {
        if (mFragmentStack.peek() instanceof AddTaskFragment) {
            if (((AddTaskFragment)mFragmentStack.peek()).allFieldsSet()) {
                Task newTask = ((AddTaskFragment)mFragmentStack.peek()).addTask();
                mFragmentStack.pop();
                if (mFragmentStack.peek() instanceof ViewTasksMapFragment) {
                    ((ViewTasksMapFragment)mFragmentStack.peek()).addTaskToMap(newTask);
                    getPresenter().saveTask(this, newTask);
                }
            } else {
                BasicDialog dialog =
                        BasicDialog.newInstance(getString(R.string.enter_all_fields), null,
                        false);
                dialog.show(getSupportFragmentManager(), null);
            }
        }
    }
}
