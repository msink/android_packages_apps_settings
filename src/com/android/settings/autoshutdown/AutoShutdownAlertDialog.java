package com.android.settings.autoshutdown;

import com.android.settings.R;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;

public class AutoShutdownAlertDialog extends Activity {
    private static final int SHUTDOWN_TIMER_COUNT = 0;
    private int count = 10;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("AutoShutdown", "AutoShutdownAlertDialog");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
        WindowManager.LayoutParams.ALPHA_CHANGED);
        LayoutInflater inflater = LayoutInflater.from(this);
        setContentView(inflateView(inflater));
        Log.e("AutoShutdown", "AutoShutdownAlertDialog");
        tv = (TextView) findViewById(R.id.shutdown_num);
        ListView listview = (ListView) findViewById(R.id.dialog_list);
        String[] items = new String[]{ getResources().getString(R.string.dialog_cancel) };
        listview.setAdapter(new RestoreDialogAdapter(this, items));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                case 0:
                    mhHandler.removeMessages(SHUTDOWN_TIMER_COUNT);
                    finish();
                    break;
                }
            }
        });
        mhHandler.sendEmptyMessage(SHUTDOWN_TIMER_COUNT);
    }

    protected View inflateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.autoshutdown_layout, null);
    }

    private Handler mhHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SHUTDOWN_TIMER_COUNT:
                if (count > 0) {
                    tv.setText(new StringBuilder().append(count).append("").toString());
                    tv.invalidate();
                    mhHandler.sendEmptyMessageDelayed(SHUTDOWN_TIMER_COUNT, 1000);
                    count--;
                } else {
                    mhHandler.removeMessages(SHUTDOWN_TIMER_COUNT);
                    shutdown();
                }
                break;
            }
            super.handleMessage(msg);
        }
    };

    private void shutdown() {
        Log.e("AutoShutdown", "shutdown()");
        if (ActivityManagerNative.isSystemReady()) {
            Intent in = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
            in.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
        }
    }
}
