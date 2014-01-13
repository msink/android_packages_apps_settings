package com.android.settings;

import com.android.internal.app.ShutdownThread;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import java.lang.Runnable;

public class ShutDownActivity extends Activity {
    Handler handlerUpdate = new Handler();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ShutdownThread.beginShutdownSequence(getBaseContext());
    }
    Runnable UpdateThread = new Runnable() {
        public void run() {
            ShutdownThread.shutdown(ShutDownActivity.this, true);
            handlerUpdate.removeCallbacks(UpdateThread);
        }
    };
}
