package com.android.settings.parentcontrol;
import com.android.settings.parentcontrol.utils.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.storage.IMountService;

public class ParentControlSettings extends Activity {
    private String mLockListValue = null;
    private boolean mUnMoundSDCardIfPasswordError = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mLockListValue = intent.getStringExtra("LOCK_LIST");
        mUnMoundSDCardIfPasswordError = intent.getBooleanExtra("UNMOUND_SDCARD_IF_PASSWORD_ERROR", false);
        if (ParentControlUtil.isNeedInputPassword(this, mLockListValue)) {
            startActivityForResult(ParentControlUtil.getLockTypeIntentByLockList(this, mLockListValue), 0);
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
            } else if (resultCode == RESULT_CANCELED) {
                if (mUnMoundSDCardIfPasswordError) {
                    IMountService mountService = getMountService();
                    String extStoragePath = Environment.getSdcardStorageDirectory().toString();
                    try {
                        mountService.unmountVolume(extStoragePath, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                setResult(RESULT_CANCELED);
            }
            this.finish();
        }
    }

    private synchronized IMountService getMountService() {
       IMountService mMountService = null;
       IBinder service = ServiceManager.getService("mount");
       if (service != null) {
           mMountService = IMountService.Stub.asInterface(service);
       }
       return mMountService;
    }
}
