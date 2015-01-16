package com.android.settings.parentcontrol;
import com.android.settings.parentcontrol.data.*;
import com.android.settings.parentcontrol.utils.*;
import com.android.settings.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.parentcontrol.ParentControl;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.widget.LockPatternView;

import java.util.List;

public class LockPatternActivity extends Activity {

    private LockPatternView mLockPatternView = null;
    private Stage mUiStage = Stage.Introduction;
    private String mFirstPassword = null;
    private String mSecondPassword = null;
    private ParentControl mParentControl = null;
    private TextView mHeaderText;
    private TextView mTextViewLockList = null;
    private String mLockListValue = null;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_view);
        mLockPatternView = (LockPatternView)findViewById(R.id.lockPatternView);
        mHeaderText = (TextView)findViewById(R.id.headerText);
        mTextViewLockList = (TextView)findViewById(R.id.textview_lock_list);
        Intent intent = getIntent();
        mLockListValue = intent.getStringExtra("LOCK_LIST");
        mTextViewLockList.setText("(" +
            ParentControlUtil.getLockListTextByLockList(this, mLockListValue) + ")");
        mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
        mLockPatternView.setTactileFeedbackEnabled(true);
        mParentControl = new ParentControl(this);
    }

    private enum Stage {
        Introduction(R.string.lockpattern_recording_intro_header),
        ChoiceTooShort(R.string.lockpattern_recording_incorrect_too_short),
        FirstChoiceValid(R.string.lockpattern_pattern_entered_header),
        NeedToConfirm(R.string.lockpattern_need_to_confirm),
        ConfirmWrong(R.string.lockpattern_need_to_unlock_wrong),
        ConfirmDifferent(R.string.lockpattern_pattern_different),
        ChoiceConfirmed(R.string.lockpattern_pattern_confirmed_header),
        ConfirmSuccess(R.string.lockpattern_pattern_success);

        final int headerMessage;
        Stage(int headerMessage) {
            this.headerMessage = headerMessage;
        }
    }

    private LockPatternView.OnPatternListener mChooseNewLockPatternListener =
        new LockPatternView.OnPatternListener() {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            patternInProgress();
        }

        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            StringBuffer password = new StringBuffer();
            for (LockPatternView.Cell cell : pattern) {
                password.append(cell.getPositionNumberByRowColumn(
                    cell.getRow(), cell.getColumn()));
            }
            switch (mUiStage) {
            case ConfirmWrong:
                mFirstPassword = password.toString();
                checkPassword(mFirstPassword);
                break;
            case NeedToConfirm:
                mSecondPassword = password.toString();
                if (mFirstPassword.equals(mSecondPassword)) {
                    updateStage(Stage.FirstChoiceValid);
                    mParentControl.savePassword(LockPatternActivity.this,
                        LockType.PATTERN.toString(),
                        LockList.LOCK_FACTORY_DATA_RESET.toString(),
                        password.toString());
                    Toast.makeText(LockPatternActivity.this, getResources()
                        .getString(R.string.password_save_success), 1)
                        .show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    updateStage(Stage.ConfirmDifferent);
                }
                break;
            case ConfirmDifferent:
            case Introduction:
                mFirstPassword = password.toString();
                checkPassword(mFirstPassword);
                break;
            }
        }

        private void checkPassword(String password) {
            String mDBPassword = mParentControl.getPassword(
                LockPatternActivity.this,
                LockType.PATTERN.toString(),
                LockList.LOCK_FACTORY_DATA_RESET.toString());
            if (mDBPassword == null) {
                updateStage(Stage.NeedToConfirm);
            } else if (mDBPassword.isEmpty()) {
                updateStage(Stage.NeedToConfirm);
            } else if (mDBPassword.equals(password)) {
                updateStage(Stage.ConfirmSuccess);
                setResult(RESULT_OK);
                finish();
            } else {
                updateStage(Stage.ConfirmWrong);
            }
            return;
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
        }

        private void patternInProgress() {
            mHeaderText.setText(R.string.lockpattern_recording_inprogress);
        }
    };

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    protected void updateStage(Stage stage) {
        mUiStage = stage;
        mHeaderText.setText(stage.headerMessage);
        mLockPatternView.clearPattern();
    }
}
