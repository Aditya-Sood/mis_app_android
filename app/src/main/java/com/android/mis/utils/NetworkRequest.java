package com.android.mis.utils;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajat on 2/3/17.
 */

public class NetworkRequest{

    private String url,protocol,req_key,result,snackbar_message="";
    private int method,tag;
    private Activity cntxt;
    private View progress_layout,error_layout;
    private HashMap<String,String> params;
    private boolean addToCache = false,success=true,show_error_page=false,force_execute=false;
    private Callback callback;

    public NetworkRequest(Activity context, View progress_layout,View error_layout, Callback callback,String method, String protocol, String url, HashMap<String,String> params, Boolean addToCache,Boolean show_error_page,int tag)
    {
        this.progress_layout = progress_layout;
        this.error_layout = error_layout;
        this.cntxt = context;
        this.protocol = protocol;
        if(method.toLowerCase() == "get")
        {
            this.method = Request.Method.GET;
        }
        else if(method.toLowerCase() == "post")
        {
            this.method = Request.Method.POST;
        }
        this.show_error_page = show_error_page;
        this.tag = tag;
        this.callback = callback;
        this.url = url;
        this.params = params;
        this.req_key = generateRequestKeyFromUrlAndParams();
        this.addToCache = addToCache;
    }

    public void initiateRequest()
    {
        try{
            getData();
        }catch (Exception e){
            Log.d("err",e.toString());
        }
    }


    private void getData() throws JSONException {
        preExecute();
        if(force_execute || (result = checkCache()) == "")
        {
            StringRequest stringReq = new StringRequest(method,
                    url,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String reslt) {
                            result = reslt;
                            System.out.print(reslt);
                            postExecute();
                        }
                    },new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("error", "Error: " + error.getMessage());
                    success = false;
                    // hide the progress dialog
                    postExecute();
                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String,String> parameters = new HashMap<>();
                    if(params != null)
                    {
                        for (HashMap.Entry<String, String> entry : params.entrySet())
                        {
                            parameters.put(entry.getKey(),entry.getValue());
                        }
                        Log.e("params",parameters.toString());
                    }
                    return parameters;
                }
            };
            stringReq.setRetryPolicy(new DefaultRetryPolicy(15000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if(!addToCache)
            {
                stringReq.setShouldCache(false);
            }
           /* else{
                SessionManagement session = new SessionManagement(cntxt.getApplicationContext());
                session.insertCachedKeys(url);
            }*/
            addRequestToQueue(stringReq);
        }
        else{
            postExecute();
        }
    }

    public void setForceExecute(Boolean val){
        force_execute = val;
    }

    public void setSnackbar_message(String message)
    {
        this.snackbar_message = message;
    }

    private void preExecute()
    {
        progress_layout.setVisibility(View.VISIBLE);
        if(show_error_page)
        error_layout.setVisibility(View.GONE);
        Activity activity = cntxt;
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void postExecute()
    {
        progress_layout.setVisibility(View.GONE);
        Activity activity = cntxt;
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if(success)
        {
            try{
                JSONObject json = new JSONObject(result);
                String message = json.getString("message");
                if(message.contentEquals(Urls.token_expired_message)){
                    Util.logoutUser(cntxt,"Session Expired! Login Again");
                }
                else{
                    callback.performAction(result,tag);
                }
            }catch (Exception e) {
                callback.performAction(result, tag);
            }
        }
        else{
            if(show_error_page)
            {
                error_layout.setVisibility(View.VISIBLE);
            }
            else {
                Util.viewSnackbar(activity.getWindow().findViewById(android.R.id.content), snackbar_message);
            }
        }
    }

    private String checkCache() throws JSONException {
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(url);
        if(entry != null){
            try {
                String data = new String(entry.data, "UTF-8");
                System.out.print("Cache Hit "+req_key);
                return data;
                // handle data, like converting it to xml, json, bitmap etc.,
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
            // Cached response doesn't exists. Make network call here
            System.out.print("Cache miss "+req_key);
            return "";
        }
        return "";
    }

    private void addRequestToQueue(StringRequest req){
        AppController.getInstance().addToRequestQueue(req,req_key);
    }

    private void cancelRequest(){
        AppController.getInstance().getRequestQueue().cancelAll(req_key);
    }

    private String generateRequestKeyFromUrlAndParams(){

        String req_key = "",param_string = "";

        try{
            if(params != null)
            {
                for (HashMap.Entry<String, String> entry : params.entrySet())
                {
                    param_string += entry.getKey()+"="+ URLEncoder.encode(entry.getValue(),"UTF-8")+"&";
                }
                url = url+"?"+param_string;
                url = url.substring(0,url.length()-1);
            }
        }catch (Exception e)
        {
            Log.d("Exception in NR",e.toString());
        }

        Log.d("url",url);
        Log.d("params",param_string);
        String req = url+param_string;
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(req.getBytes(Charset.forName("UTF8")));
            final byte[] resultByte = messageDigest.digest();
            req_key = new String(resultByte,"UTF-8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return req_key;
    }
}
