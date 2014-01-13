package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.Window;

public class MasterDialog extends Activity {
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.master_dialog);
        Button btn_ok = (Button) findViewById(R.id.master_dialog_btn_ok);
        Button btn_cancel = (Button) findViewById(R.id.master_dialog_btn_cancel);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(MasterDialog.this,
                                           MasterDialogClear.class);
                startActivity(intent);
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
