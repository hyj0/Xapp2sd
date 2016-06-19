package com.hyj.app.Xapp2sd;
//http://blog.csdn.net/dzq_feixiang/article/details/50827662
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dell-pc on 2016/6/19.
 */
public class AppInfoAdapter extends BaseAdapter {

    private static final String TAG = "AppInfoAdapter";
    private Context mContext;
    private List mListApp;
    private LayoutInflater mLayoutInflater;
    private PackageManager mPackageManager;

    List<Boolean> mChecked;

    public AppInfoAdapter(Context mContext, List mListApp, PackageManager mPackageManager) {
        this.mContext = mContext;
        this.mListApp = mListApp;
        this.mPackageManager = mPackageManager;
        this.mLayoutInflater = LayoutInflater.from(mContext);

        ArrayList<ApplicationInfo> hasApp = new ArrayList<ApplicationInfo>();
        ArrayList<ApplicationInfo> nothasApp = new ArrayList<ApplicationInfo>();
        ArrayList<String> doappLst = Utils.getDoAppList();
        mChecked = new ArrayList<Boolean>();
        for(int i=0;i<mListApp.size();i++){
            ApplicationInfo appInfo = (ApplicationInfo) mListApp.get(i);
            mChecked.add(false);
            for (String doapp :
                    doappLst) {
                if (doapp.equals(appInfo.packageName)) {
                    mChecked.add(i, true);
                }
            }
        }
        for(int i = 0; i < mListApp.size(); i++) {
            if (mChecked.get(i)) {
                hasApp.add((ApplicationInfo) mListApp.get(i));
            } else {
                nothasApp.add((ApplicationInfo) mListApp.get(i));
            }
        }
        ArrayList<ApplicationInfo> newappList = new ArrayList<ApplicationInfo>();
        for(int i = 0; i < hasApp.size(); i++) {
            newappList.add(hasApp.get(i));
            mChecked.set(i, true);
        }
        for(int i = 0; i < nothasApp.size(); i++) {
            int j = i + hasApp.size();
            newappList.add(nothasApp.get(i));
            mChecked.set(j, false);
        }
        this.mListApp = newappList;
    }

    @Override
    public int getCount() {
        return mListApp.size();
    }

    @Override
    public Object getItem(int position) {
        return mListApp.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ApplicationInfo appInfo = (ApplicationInfo) mListApp.get(position);
        ViewHolder holder = null;
        if(convertView == null){

            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.list_item, null);
            holder.ivAppIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            holder.tvAppName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvPackageName = (TextView) convertView.findViewById(R.id.tvPack);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.check_isSelect);

            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ivAppIcon.setImageDrawable(appInfo.loadIcon(mPackageManager));
        holder.tvAppName.setText(appInfo.loadLabel(mPackageManager));
        holder.tvPackageName.setText(appInfo.packageName);
        holder.checkBox.setChecked(mChecked.get(position));

        holder.ivAppIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ApplicationInfo appInfo = (ApplicationInfo) mListApp.get(position);
                Utils.launchApp(mContext, mPackageManager, appInfo.packageName);
            }
        });


        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox cb = (CheckBox)view;
                final ApplicationInfo appInfo = (ApplicationInfo) mListApp.get(position);
                mChecked.set(position, cb.isChecked());
                Log.d(TAG, "onClick posit:" + position + " check:" + cb.isChecked());
                if (cb.isChecked()) {
                    Log.d(TAG, "onClick add app:" + appInfo.packageName);
                    Utils.AddAndWriteToDoAppListFile(appInfo.packageName);
                } else {
                    Log.d(TAG, "onClick del app:" + appInfo.packageName);
                    Utils.DeleteAndWriteToDoAppListFile(appInfo.packageName);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder{
        ImageView ivAppIcon;
        TextView tvAppName;
        TextView tvPackageName;
        CheckBox checkBox;
    }
}
