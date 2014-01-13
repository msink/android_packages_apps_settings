package com.android.settings;

import com.android.internal.os.storage.ExternalStorageFormatter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

public class MasterDialogClear extends Activity {
    private String mPath;
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        mPath = extras.getString("path");
        setContentView(R.layout.master_dialog_clearall);
        Button btn_ok = (Button) findViewById(R.id.master_dialog_btn_ok);
        Button btn_cancel = (Button) findViewById(R.id.master_dialog_btn_cancel);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (Utils.isMonkeyRunning())
                    return;
                Intent intent = new Intent(ExternalStorageFormatter.FORMAT_ONLY);
                intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                intent.putExtra("path", mPath);
                startService(intent);
                finish();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                finish();
            }
        });
    }
}
