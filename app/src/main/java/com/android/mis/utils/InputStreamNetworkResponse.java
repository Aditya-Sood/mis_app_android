package com.android.mis.utils;

import java.io.InputStream;
import java.util.Map;
import com.android.volley.NetworkResponse;


/**
 * Created by rajat on 10/4/17.
 */

public class InputStreamNetworkResponse extends NetworkResponse{
    public InputStreamNetworkResponse(int statusCode, byte[] data, InputStream ins,
                                      Map<String, String> headers, boolean notModified) {
        super(statusCode, data, headers, notModified);
        this.ins = ins;
    }

    public InputStreamNetworkResponse(byte[] data, InputStream ins) {
        super(data);
        this.ins = ins;
    }

    public InputStreamNetworkResponse(byte[] data, InputStream ins, Map<String, String> headers) {
        super(data, headers);
        this.ins = ins;
    }

    public final InputStream ins;
}
