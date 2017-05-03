package com.android.mis.utils;

/**
 * Created by rajat on 10/4/17.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.mis.R;
import com.android.mis.javac.Home.HomeActivity;
import com.android.mis.models.Download;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DownloadService extends IntentService {

    private String url,fileName;
    public DownloadService() {
        super("Download Service");
    }

    public DownloadService(String url){
        super("Download Service");
        this.url = url;
    }

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int totalFileSize;

    @Override
    protected void onHandleIntent(Intent intent) {
        url = intent.getStringExtra("url");
        fileName = intent.getStringExtra("file_name");
        Log.e("Url",url);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.download)
                .setContentTitle(fileName)
                .setContentText("Downloading File...")
                .setAutoCancel(true);
        notificationManager.notify(0, notificationBuilder.build());

        initDownload();

    }

    private void initDownload() {
        url = "https://download.learn2crack.com/files/Node-Android-Chat.zip";
        Toast.makeText(getApplicationContext(),url,Toast.LENGTH_LONG).show();
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        InputStream bis = new ByteArrayInputStream(response);
                        try {
                            int count;
                            byte data[] = new byte[1024 * 4];
                            long fileSize = response.length;
                            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileName);
                            OutputStream output = new FileOutputStream(outputFile);
                            long total = 0;
                            long startTime = System.currentTimeMillis();
                            int timeCount = 1;
                            while ((count = bis.read(data)) != -1) {

                                total += count;
                                totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
                                double current = Math.round(total / (Math.pow(1024, 2)));

                                int progress = (int) ((total * 100) / fileSize);

                                long currentTime = System.currentTimeMillis() - startTime;

                                Download download = new Download();
                                download.setTotalFileSize(totalFileSize);

                                if (currentTime > 1000 * timeCount) {

                                    download.setCurrentFileSize((int) current);
                                    download.setProgress(progress);
                                    sendNotification(download);
                                    timeCount++;
                                }
                                Log.e("Data",data.toString());
                                output.write(data, 0, count);
                            }
                            onDownloadComplete();
                            output.flush();
                            output.close();
                            bis.close();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                            e.printStackTrace();
                        }
                    }
                } ,new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error
                error.printStackTrace();
            }
        }, null);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
        mRequestQueue.add(request);
    }


    private void sendNotification(Download download) {
        sendIntent(download);
        notificationBuilder.setProgress(100, download.getProgress(), false);
        notificationBuilder.setContentText("Downloading file ..." + download.getCurrentFileSize() + "/" + totalFileSize + " MB");
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendIntent(Download download) {

        Intent intent = new Intent(HomeActivity.MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void onDownloadComplete() {

        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);

        Intent intent = new Intent();
        Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+fileName);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, "file/*");

        final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,intent, 0);

        notificationManager.cancel(0);
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText("File Downloaded");
        notificationBuilder.setContentIntent(contentIntent);
        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(0);
    }
}