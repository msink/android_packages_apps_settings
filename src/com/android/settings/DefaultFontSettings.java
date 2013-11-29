package com.android.settings;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.internal.app.ShutdownThread;
import android.util.Log;

import java.util.*;

public class DefaultFontSettings extends ListActivity {
    private static final String TAG = "DefaultFontSettings";

    private ArrayList<String> mFonts = null;
    private String mDefaultFont = null;
    private ListView mFontList;
    private ProgressDialog mProgressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.font_list);
        mFonts = new ArrayList();
        new loadFont().execute(mFonts);
        mFontList = getListView();
        mFontList.setChoiceMode(1);
        mFontList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String family = mFonts.get(position);
                Log.d(TAG, "family : " + family);
                new setFont().execute(family);
            }
        });

        mDefaultFont = Settings.System.getString(getContentResolver(), "default_system_font");
    }

    private class loadFont extends AsyncTask<ArrayList<String>,Void,Integer> {

        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(DefaultFontSettings.this,
                "", getResources().getString(R.string.loading));
            super.onPreExecute();
        }

        protected Integer doInBackground(ArrayList<String>... params) {
            FontUnit.fillFamiliesList(mFonts);
            return 1;
        }

        protected void onPostExecute(Integer result) {
            int state = result.intValue();
            switch (state) {
            case 0:
                break;
            case 1:
                mProgressDialog.dismiss();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                     DefaultFontSettings.this,
                     android.R.layout.simple_list_item_single_choice,
                     android.R.id.text1, mFonts);
                setListAdapter(adapter);
                mFontList.setItemChecked(adapter.getPosition(mDefaultFont), true);
                break;
            case -1:
                mProgressDialog.dismiss();
                mProgressDialog = ProgressDialog.show(DefaultFontSettings.this,
                    "", getResources().getString(R.string.loading_error));
                break;
            }
            super.onPostExecute(result);
        }
    }

    private class setFont extends AsyncTask<String,Void,Integer> {

        protected Integer doInBackground(String... params) {
            FontUnit.setDefaultFont(params[0]);
            try {
                Settings.System.putString(getContentResolver(),
                    "default_system_font", params[0]);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist default system font setting", e);
            }
            return 1;
        }

        protected void onPostExecute(Integer result) {
            int state = result.intValue();
            switch (state) {
            case -1:
            case 0:
                break;
            case 1:
                AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(DefaultFontSettings.this);
                alertDialogBuilder.setTitle(R.string.set_font_dialog_tile);
                alertDialogBuilder.setMessage(R.string.reboot_device)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ShutdownThread.reboot(DefaultFontSettings.this, "", false);
                            dialog.dismiss();
                            ProgressDialog rebootProgressDialog =
                                ProgressDialog.show(DefaultFontSettings.this,
                                "", getResources().getString(R.string.rebooting));
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            }
            super.onPostExecute(result);
        }
    }
}
