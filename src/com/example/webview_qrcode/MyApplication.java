package com.example.webview_qrcode;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;

public class MyApplication extends Application{
	private static Context context;
    private static MyApplication instance;
    
    public static Bitmap bitmap;
    public static Bitmap bitmap2;
    
    public static Camera.Size previewSize;
    
    
    
    public static Context getAppContext() {
        return MyApplication.context;
    }
    
    public static MyApplication getInstance() {
        return instance;
    }
    
    
	@Override
	public void onCreate() {
		super.onCreate();
		MyApplication.context = getApplicationContext();
		instance = this;
		
		
		
	}
    
    
}
