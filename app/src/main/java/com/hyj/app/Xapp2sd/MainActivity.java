package com.hyj.app.Xapp2sd;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends Activity {
    int allapp = 0;
    int sysapp = 1;
    int userapp = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.sysapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadAppList(sysapp);
            }
        });

        findViewById(R.id.userapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadAppList(userapp);
            }
        });

    }

    private void loadAppList(int appType) {
        ListView mListView = (ListView) findViewById(R.id.lvApps);
        AppInfoAdapter adapter = new AppInfoAdapter(this, Utils.getInstalledApp(this, appType), getPackageManager());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ApplicationInfo info = (ApplicationInfo) parent.getItemAtPosition(position);
                Utils.launchApp(parent.getContext(), getPackageManager(), info.packageName);
            }
        });
    }
}
