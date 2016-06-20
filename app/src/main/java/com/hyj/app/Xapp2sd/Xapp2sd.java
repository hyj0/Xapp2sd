package com.hyj.app.Xapp2sd;

//https://raw.githubusercontent.com/moneytoo/ObbOnSd/master/app/src/main/java/com/smartmadsoft/xposed/obbonsd/Relocator.java
import android.util.Log;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Xapp2sd implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    public static final String PACKAGE_NAME = Xapp2sd.class.getPackage().getName();
    private static XSharedPreferences prefs;

    public static final boolean DEBUG = true;
    public static final String TAG = "xapp2sd";

    String datadir = "/data/data/";

    ArrayList<String> filepaths = new ArrayList<String>();

    boolean app2ext = false;//if true, the sdpath need on an executable filesystem(ext3, ext4,...)
    String sdpath = "/sdcard/xapp2sd/";
    private String applistConfigFile = "/sdcard/xapp2sd.applist.txt";

    int scanValue = 10;
    private String scanValueFile = "/sdcard/xapp2sd.scan.txt";

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        prefs = new XSharedPreferences(PACKAGE_NAME);
        prefs.makeWorldReadable();
        log("initZygote:"+PACKAGE_NAME);
    }

    public ArrayList<String> getAppList(String listfile) {
        ArrayList<String> retStrs = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(listfile)));
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    retStrs.add(datadir + line);
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


    public static boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        return !file.getAbsolutePath().equals(file.getCanonicalPath());
    }

    private void cmdlinelog(Process proc, String cmdline) {
        BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String line = "";
        String oneline;
        try {
            while ((oneline = br.readLine()) != null) {
                line += oneline;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log(cmdline+" ret:" + line);
    }

    private boolean copyFilewithLink(String src, String dest) {
        try {
            String cmdline = "cp -f   " + src + "  " + dest + "";
            Process proc = Runtime.getRuntime().exec(cmdline);
            proc.waitFor();
            if (proc.exitValue() != 0) {
                cmdlinelog(proc, cmdline);
                return false;
            }

            cmdline = "rm -f "  + " " +  src + "";
            proc = Runtime.getRuntime().exec(cmdline);
            proc.waitFor();

            if (proc.exitValue() != 0) {
                cmdlinelog(proc, cmdline);
                return false;
            }

            cmdline = "ln -s " + dest + " " +  src + "";
            proc = Runtime.getRuntime().exec(cmdline);
            proc.waitFor();
            if (proc.exitValue() != 0) {
                cmdlinelog(proc, cmdline);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void handleFun(String openFile, ArrayList<String> filepaths, LoadPackageParam lpparam, File srcfile, String sdpath) throws IOException {
        for (String hookpath :
                filepaths) {
            if (openFile.startsWith(hookpath) && !openFile.startsWith(hookpath + "/cache/")) {
                if (!app2ext) {
                    if (openFile.endsWith(".so") || openFile.endsWith(".dex") || openFile.endsWith(".jar")) {
//                    log(lpparam.packageName+" call "+ "java.io.File(" + openFile+")" + " sep file !!");
                        continue;
                    }
                }
                if (srcfile == null) {
//                    log(lpparam.packageName+" call "+ "java.io.File(" + openFile+")" + " handle null");
                    continue;
                }

                boolean exists = srcfile.exists();
                if (!exists) {
//                    log(lpparam.packageName+" call "+ "java.io.File(" + openFile+")" + " not exits");
                    continue;
                }
                boolean isLink = isSymlink(srcfile);
                if (isLink) {
//                    log(lpparam.packageName+" call "+ "java.io.File(" + openFile+")" + " is link File !!!!");
                    continue;
                }
                if (srcfile.canExecute()) {
//                    log(lpparam.packageName+" call "+ "java.io.File(" + openFile+")" + " is execute File !!!!");
                    continue;
                }
                if (!srcfile.isDirectory()) {
                    String src = srcfile.getPath();
                    String dest = sdpath + "/" + lpparam.packageName + "/" + src.replace("/", "_");
                    if (!copyFilewithLink(src, dest)) {
                        log(lpparam.packageName+" call "+ "java.io.File(" + src+")" + " hook----->" + dest + "  copy false");
                    } else {
                        log(lpparam.packageName+" call "+ "java.io.File(" + src+")" + " hook----->" + dest + " Ok");
                    }
                }
            }
        }
    }

    public  void getFileList(String strPath, LoadPackageParam lpparam, String sdpath) throws IOException {
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) { // 判断是文件还是文件夹
                    getFileList(file.getAbsolutePath(), lpparam, sdpath); // 获取文件绝对路径
                } else { //
                    String openFile = file.getAbsolutePath();
//                    log("getFileList:" + openFile);
                    handleFun(openFile, filepaths, lpparam, new File(openFile), sdpath);
                }
            }
        }
    }

    int randomBetwen(int min, int max) {
        Random R = new Random();
        return R.nextInt((max - min) + 1) + min;
    }


    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        ArrayList<String> applist = getAppList(applistConfigFile);
        if (applist.size() > 0) {
            filepaths = applist;
        }

        /*check need hook or not*/
        boolean needHook = false;
        for (String filepath :
                filepaths) {
            if (filepath.contains(lpparam.packageName)) {
                log("load package:"+lpparam.packageName + " in " + filepath);
                needHook = true;
                break;
            }
        }
        if (!needHook) {
            return;
        }


        XposedHelpers.findAndHookConstructor("java.io.File", lpparam.classLoader, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String openFile = param.args[0].toString();
                handleFun(openFile, filepaths, lpparam, (File) param.thisObject, sdpath);
            }
        });

        XposedHelpers.findAndHookConstructor("java.io.File", lpparam.classLoader, String.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String openFile = param.args[0].toString() + "/" + param.args[1].toString();
                handleFun(openFile, filepaths, lpparam, (File) param.thisObject, sdpath);
            }
        });

        XposedBridge.hookAllMethods(XposedHelpers.findClass("java.io.File", lpparam.classLoader), "renameTo", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

            }
        });

        for (String filepath :
                filepaths) {
            if (filepath.contains(lpparam.packageName)) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(scanValueFile)));
                    String line;
                    while ((line = in.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            scanValue = Integer.parseInt(line);
                            log("handleLoadPackage scanValue="+scanValue);
                        }
                    }
                    if (randomBetwen(0, 100) < scanValue) {
                        log("do scan file app:"+lpparam.packageName);
                        getFileList(filepath, lpparam, sdpath);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void log(String text) {
        if (DEBUG) {
            XposedBridge.log("[" + TAG + "] " + text);
            Log.d(TAG, text);
        }
    }
}