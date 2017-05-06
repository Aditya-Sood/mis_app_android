package com.android.mis.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.mis.R;
import com.android.mis.javac.Login.LoginActivity;
import com.androidadvance.topsnackbar.TSnackbar;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by RDC on 1/29/2016.
 */
public class Util {

    public static boolean checkInternet(Context context)
    {
        ConnectivityManager check = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = check.getAllNetworkInfo();
        for (int i = 0; i<info.length; i++){
            if (info[i].getState() == NetworkInfo.State.CONNECTED){
                return true;
            }
        }
        return false;
    }

    public static void viewSnackbar(View v,String message)
    {
        TSnackbar snackbar = TSnackbar.make(v, message, TSnackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.WHITE);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#f56954"));
        TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public static void moveToActivity(Activity source, Class destination, Bundle bundle,Boolean clearHistory)
    {
        Intent i = new Intent(source,destination);
        if(bundle!=null)
        i.putExtras(bundle);
        if(clearHistory)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        source.startActivity(i);
    }

    public static HashMap<String,Integer> getMenuMappedIds(){
        HashMap<String,Integer> hmap = new HashMap<>();
        hmap.put("home", R.id.home);
        hmap.put("user_details",R.id.user_details);
        hmap.put("view_details",R.id.view_details);
        hmap.put("edit_details",R.id.edit_details);
        hmap.put("attendance",R.id.attendance);
        hmap.put("generate_attendance_sheet",R.id.generate_attendance_sheet);
        hmap.put("view_attendance",R.id.view_attendance);
        hmap.put("view_defaulter_list",R.id.view_defaulter_list);
        hmap.put("course_structure",R.id.course_structure);
        hmap.put("view_course_structure",R.id.view_course_structure);
        hmap.put("others",R.id.others);
        hmap.put("logout",R.id.logout);
        return hmap;
    }

    public static void startDownload(String url, String name, Activity activity){
        String DownloadUrl = url;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadUrl));
        request.setDescription("Downloading...");   //appears the same in Notification bar while downloading
        request.setTitle(name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalFilesDir(activity.getApplicationContext(),null,name);
        DownloadManager manager = (DownloadManager)activity.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public static void logoutUser(Activity activity){
        AppController.getInstance().getRequestQueue().getCache().clear();
        SessionManagement session = new SessionManagement(activity.getApplicationContext());
        session.logoutUser();
        Intent i = new Intent(activity,LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);;
    }

    public static String getDateFromDateTime(String datetime)
    {
        return datetime.split("T")[0];
    }

    public static String getTimeFromDateTime(String datetime)
    {
        return datetime.split("T")[1].split(":")[0]+":"+datetime.split("T")[1].split(":")[1];
    }

}
