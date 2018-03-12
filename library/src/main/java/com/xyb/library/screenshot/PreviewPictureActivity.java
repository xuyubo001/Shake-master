package com.xyb.library.screenshot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.xyb.library.R;

/**
 *
 */
public class PreviewPictureActivity extends FragmentActivity implements GlobalScreenshot.onScreenShotListener {



  private ImageView mPreviewImageView;
  private static  Bitmap mScreenshot;


  public static final void SetScreenshot(Bitmap bitmap) {
    mScreenshot = bitmap ;
  }
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_preview_layout);

    mPreviewImageView = (ImageView) findViewById(R.id.preview_image);


    mPreviewImageView.setImageBitmap(mScreenshot);
    mPreviewImageView.setVisibility(View.VISIBLE);
  }

  @Override
  public void onStartShot() {

  }

  @Override
  public void onFinishShot(boolean success) {
    mPreviewImageView.setVisibility(View.VISIBLE);
  }
}
