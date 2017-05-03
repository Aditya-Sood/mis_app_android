package com.android.mis.utils;

/**
 * Created by rajat on 10/4/17.
 */


import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class InputStreamVolleyRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;
    private Map<String, String> mParams;
    //create a static map for directly accessing headers
    public Map<String, String> responseHeaders;

    public InputStreamVolleyRequest(int post, String mUrl, Response.Listener<byte[]> listener,
                                    Response.ErrorListener errorListener, HashMap<String, String> params) {
        // TODO Auto-generated constructor stub

        super(post, mUrl, errorListener);
        // this request would never use cache.
        setShouldCache(false);
        mListener = listener;
        mParams = params;
    }

    @Override
    protected Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        return mParams;
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {

        if (response instanceof InputStreamNetworkResponse) {
            // take the InputStream here.
            InputStream ins = ((InputStreamNetworkResponse) response).ins;

           // return Response.success(ins, null);
        }
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }


    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }
}