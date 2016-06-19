package com.hyj.app.Xapp2sd;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell-pc on 2016/6/19.
 */
public class Utils {

    /**
     * 获取已安装的应用
     * @param context
     * @return
     */
    public static List<ApplicationInfo> getInstalledApp(Context context, int appType){
        List<ApplicationInfo> applst = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> retAppLst = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo appinfo :
                applst) {
            if (appType == 0) {
                retAppLst.add(appinfo);
            } else if (appType == 1) {
                if ((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                    retAppLst.add(appinfo);
                }
            } else  if (appType == 2) {
                if ((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                    retAppLst.add(appinfo);
                }
            }

        }
        return retAppLst;
    }

    /**
     * 根据包名,启动应用
     * @param context
     * @param pm
     * @param packageName
     * @return
     */
    public static boolean launchApp(Context context, PackageManager pm, String packageName){
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if(intent != null){
            try {
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Applicatin not found", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        return false;
    }

    public static String applistfile = "/sdcard/xapp2sd.applist.txt";

    public static ArrayList<String> getDoAppList() {
        String listfile = applistfile;
        ArrayList<String> retStrs = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(listfile)));
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    retStrs.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retStrs;
    }

    public static void  DeleteAndWriteToDoAppListFile(String delapp) {
        ArrayList<String> doappLst = getDoAppList();
        ArrayList<String> newAppLst = new ArrayList<String>();
        for (String app :
                doappLst) {
            if (!app.equals(delapp)) {
                newAppLst.add(app);
            }
        }
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(applistfile)));
            for (String app :
                    newAppLst) {
                pw.println(app);
                pw.flush();
            }
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void  AddAndWriteToDoAppListFile(String addapp) {
        ArrayList<String> doappLst = getDoAppList();
        ArrayList<String> newAppLst = new ArrayList<String>();
        for (String app :
                doappLst) {
            if (app.equals(addapp)) {
                //no need add
                return;
            }
            newAppLst.add(app);
        }
        newAppLst.add(addapp);
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(applistfile)));
            for (String app :
                    newAppLst) {
                pw.println(app);
                pw.flush();
            }
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
