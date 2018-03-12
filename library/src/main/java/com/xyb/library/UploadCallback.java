package com.xyb.library;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by qingfeng on 4/11/17.
 */

public interface UploadCallback {

    void onFailure(Call call, IOException e);

    void onResponse(Call call, Response response) throws IOException;

}
