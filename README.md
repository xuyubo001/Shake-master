# Shake

摇一摇上传屏幕截图

# 使用

## Step 1

注册权限：

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

## Step 2

注册服务

```java
@Override
protected void onResume() {
    super.onResume();
    ShakeSensorManager.getInstance().onActivityResumed(this);
}

@Override
protected void onPause() {
    super.onPause();
    ShakeSensorManager.getInstance().onActivityPaused();
}
```

# FAQ

Android 6.0 以上设备会提示"保存截屏失败"，可能是因为客户端没有动态申请"写入外部存储设备"的权限，之后会解决。