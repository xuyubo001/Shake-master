package com.xyb.library;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by qingfeng on 1/18/17.
 */

public class ShakeSensorManager {

    private static final int SHOW_DIALOG = 0;
    public static final int REQUEST_MEDIA_PROJECTION = 18;
    private Activity activity;

    private String filePath = null;

    private OnShakeListener onShakeListener;


    private Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void handleMessage(Message msg) {
            if (SHOW_DIALOG == msg.what) {
                if (null == activity) {
                    return;
                }
                // 截屏
                boolean success = screenshot();

            }
        }
    };

    private ShakeSensor shakeSensor = null;

    private static ShakeSensorManager sShakeSensorManager = new ShakeSensorManager();

    private ShakeSensorManager() {


    }


    public static ShakeSensorManager getInstance() {

        return sShakeSensorManager;
    }







    private boolean screenshot() {
        if (null == activity) {
            return false;
        }
        try {
            // 获取屏幕
            View dView = activity.getWindow().getDecorView();
            dView.setDrawingCacheEnabled(true);
            dView.buildDrawingCache();
            Bitmap bmp = dView.getDrawingCache();
            if (bmp == null) {
                return false;
            }
            // 获取内置SD卡路径
            String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + "Shake" + File.separator + "feedback";
            File parentDir = new File(parentPath);
            // 创建目录
            if (!parentDir.exists()) parentDir.mkdirs();
            // 图片文件路径
            filePath = parentDir + File.separator + System.currentTimeMillis() + ".png";
            // 保存截屏
            FileOutputStream os = new FileOutputStream(new File(filePath));
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getFilePath() {
        return filePath;
    }

}
