package com.xyb.library.screenshot;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.os.AsyncTaskCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;

import android.view.WindowManager;
import android.widget.ImageView;

import com.xyb.library.OnShakeListener;
import com.xyb.library.ShakeSensor;
import com.xyb.library.fileUtils.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by branch on 2016-5-25.
 *
 * 启动悬浮窗界面
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FloatWindowsService extends Service {


  public static Intent newIntent(Context context, Intent mResultData) {

    Intent intent = new Intent(context, FloatWindowsService.class);

    if (mResultData != null) {
      intent.putExtras(mResultData);
    }
    return intent;
  }

  private MediaProjection mMediaProjection;
  private VirtualDisplay mVirtualDisplay;

  private static Intent mResultData = null;


  private ImageReader mImageReader;
  private WindowManager mWindowManager;
  private WindowManager.LayoutParams mLayoutParams;
  private GestureDetector mGestureDetector;

  private ImageView mFloatView;

  private int mScreenWidth;
  private int mScreenHeight;
  private int mScreenDensity;
  private ShakeSensor shakeSensor = null;
  private OnShakeListener onShakeListener;


  private MediaActionSound mCameraSound;
  @Override
  public void onCreate() {
    super.onCreate();

    registShakeSensor();
    createFloatView();
    soundEffect();
    createImageReader();
  }


  private void soundEffect(){
    mCameraSound = new MediaActionSound();
    mCameraSound.load(MediaActionSound.SHUTTER_CLICK);

  }
  private void registShakeSensor(){
    if (null == shakeSensor) {
        shakeSensor = new ShakeSensor(getApplicationContext(), ShakeSensor.mSpeedThreshold);
        onShakeListener = new OnShakeListener() {
        @Override
        public void onShakeComplete() {
          startScreenShot();
        }
      };
    }

    shakeSensor.setShakeListener(onShakeListener);
    shakeSensor.register();
  }


  public static Intent getResultData() {
    return mResultData;
  }

  public static void setResultData(Intent mResultData) {
    FloatWindowsService.mResultData = mResultData;
  }

  @Override
  public IBinder onBind(Intent intent) {

    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    Notification notification = new Notification(0, "title",System.currentTimeMillis());
    Intent notificationIntent = new Intent(this, FloatWindowsService.class);


    PendingIntent pintent = PendingIntent.getService(this, 0, notificationIntent, 0);

    startForeground(0, notification);//设置最高级进程。 id 为 0状态栏的 notification 将不会显示
    return super.onStartCommand(intent, flags, startId);
  }

  private void createFloatView() {
    mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics metrics = new DisplayMetrics();
    mWindowManager.getDefaultDisplay().getMetrics(metrics);
    mScreenDensity = metrics.densityDpi;
    mScreenWidth = metrics.widthPixels;
    mScreenHeight = metrics.heightPixels;

  }


  private void startScreenShot() {

  //  mFloatView.setVisibility(View.GONE);

    Handler handler1 = new Handler();
    handler1.postDelayed(new Runnable() {
      public void run() {
        //start virtual
        startVirtual();
      }
    }, 5);

    handler1.postDelayed(new Runnable() {
      public void run() {
        //capture the screen
        startCapture();

      }
    }, 30);
  }


  private void createImageReader() {

    mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);

  }

  public void startVirtual() {
    if (mMediaProjection != null) {
      virtualDisplay();
    } else {
      setUpMediaProjection();
      virtualDisplay();
    }
  }

  public void setUpMediaProjection() {
    if (mResultData == null) {
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.addCategory(Intent.CATEGORY_LAUNCHER);
      startActivity(intent);
    } else {
      mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
    }
  }

  private MediaProjectionManager getMediaProjectionManager() {

    return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
  }

  private void virtualDisplay() {
    mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
        mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        mImageReader.getSurface(), null, null);
  }

  private void startCapture() {

    Image image = mImageReader.acquireLatestImage();

    if (image == null) {
      startScreenShot();
    } else {
      SaveTask mSaveTask = new SaveTask();
      AsyncTaskCompat.executeParallel(mSaveTask, image);
    }
  }


  public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(Image... params) {

      if (params == null || params.length < 1 || params[0] == null) {

        return null;
      }

      Log.d("SaveTask","save begin");
      Image image = params[0];

      int width = image.getWidth();
      int height = image.getHeight();
      final Image.Plane[] planes = image.getPlanes();
      final ByteBuffer buffer = planes[0].getBuffer();
      //每个像素的间距
      int pixelStride = planes[0].getPixelStride();
      //总的间距
      int rowStride = planes[0].getRowStride();
      int rowPadding = rowStride - pixelStride * width;
      Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
      bitmap.copyPixelsFromBuffer(buffer);
      bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
      image.close();
      File fileImage = null;
      if (bitmap != null) {
        try {
          fileImage = new File(FileUtil.getScreenShotsName(getApplicationContext()));
          if (!fileImage.exists()) {
            fileImage.createNewFile();
          }
          FileOutputStream out = new FileOutputStream(fileImage);
          if (out != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
            out.flush();
            out.close();
            Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(fileImage);
            media.setData(contentUri);
            sendBroadcast(media);
          }
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          fileImage = null;
        } catch (IOException e) {
          e.printStackTrace();
          fileImage = null;
        }
      }

      if (fileImage != null) {
        return bitmap;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      //预览图片
      if (bitmap != null) {
        mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
        Log.d("SaveTask","save end");
        PreviewPictureActivity.SetScreenshot(bitmap);
        Intent bitmapintent = new Intent(getApplicationContext(),PreviewPictureActivity.class);
        bitmapintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(bitmapintent);
      }


    }
  }


  private void tearDownMediaProjection() {
    if (mMediaProjection != null) {
      mMediaProjection.stop();
      mMediaProjection = null;
    }
  }

  private void stopVirtual() {
    if (mVirtualDisplay == null) {
      return;
    }
    mVirtualDisplay.release();
    mVirtualDisplay = null;
  }

  @Override
  public void onDestroy() {
    // to remove mFloatLayout from windowManager
    super.onDestroy();
    stopVirtual();
    tearDownMediaProjection();
    stopForeground(true);//取消最高级进程

  }


}
