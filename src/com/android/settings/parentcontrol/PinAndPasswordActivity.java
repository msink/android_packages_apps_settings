package com.android.settings.parentcontrol;
import com.android.settings.parentcontrol.data.*;
import com.android.settings.parentcontrol.utils.*;
import com.android.settings.R;

import android.app.Activity;
import android.content.Intent;
import android.parentcontrol.ParentControl;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PinAndPasswordActivity extends Activity implements View.OnClickListener {
    private String mLockListValue = null;
    private String mLockTypeValue = null;
    private TextView mTextViewLockList = null;
    private EditText mEditTextPassword = null;
    private Button mButtonCancel = null;
    private Button mButtonOK = null;
    private ParentControl mParentControl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_and_password);
        Intent intent = getIntent();
        mParentControl = new ParentControl(this);
        mLockListValue = intent.getStringExtra("LOCK_LIST");
        mLockTypeValue = intent.getStringExtra("LOCK_TYPE");
        mTextViewLockList = (TextView)findViewById(R.id.textview_lock_list);
        mEditTextPassword = (EditText)findViewById(R.id.edittext_password);
        mTextViewLockList.setText("(" +
            ParentControlUtil.getLockListTextByLockList(this, mLockListValue) + ")");
        mButtonCancel = (Button)findViewById(R.id.button_cancel);
        mButtonOK = (Button)findViewById(R.id.button_ok);
        mButtonCancel.setOnClickListener(this);
        mButtonOK.setOnClickListener(this);
        if (mLockTypeValue.equals(LockType.PIN.toString())) {
            mEditTextPassword.setInputType(InputType.TYPE_CLASS_PHONE);
        } else if (mLockTypeValue.equals(LockType.PASSWORD.toString())) {
            mEditTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_cancel:
            finish();
            break;
        case R.id.button_ok:
            String dbPassword = mParentControl.getPassword(this, mLockTypeValue, mLockListValue);
            String inputPassword = mEditTextPassword.getText().toString();
            if (inputPassword != null &&
                    !inputPassword.isEmpty() &&
                    inputPassword.equals(dbPassword)) {
                setResult(RESULT_OK);
                Toast.makeText(this, getResources()
                    .getString(R.string.lockpattern_pattern_success), 1)
                    .show();
                finish();
            } else {
                Toast.makeText(this, getResources()
                   .getString(R.string.password_error), 1)
                   .show();
                mEditTextPassword.setText("");
            }
            break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
