/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mining.app.zxing.camera;

import java.io.ByteArrayOutputStream;

import com.example.webview_qrcode.MyApplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

final class PreviewCallback implements Camera.PreviewCallback {

  private static final String TAG = PreviewCallback.class.getSimpleName();

  private final CameraConfigurationManager configManager;
  private final boolean useOneShotPreviewCallback;//使用一次预览回调
  private Handler previewHandler;
  private int previewMessage;

  PreviewCallback(CameraConfigurationManager configManager, boolean useOneShotPreviewCallback) {
    this.configManager = configManager;
    this.useOneShotPreviewCallback = useOneShotPreviewCallback;
  }

  void setHandler(Handler previewHandler, int previewMessage) {
    this.previewHandler = previewHandler;
    this.previewMessage = previewMessage;
  }
  
  //自動對焦後輸出圖片
  public void onPreviewFrame(byte[] data, Camera camera) {
	//获取到每个帧数据data  
    Point cameraResolution = configManager.getCameraResolution();//获得相机分辨率
    if (!useOneShotPreviewCallback) {//使用一次预览回调
    	//Log.i("使用一次预览回调", "使用一次预览回调");
      camera.setPreviewCallback(null);
    }
    
    
    if (previewHandler != null) {
    	
    	//Log.i("這裡獲得當前", "获得扫描圖像资料");
    	MyApplication.getInstance().previewSize = camera.getParameters().getPreviewSize();//获取尺寸,格式转换的时候要用到  
    	Message message = previewHandler.obtainMessage(previewMessage, cameraResolution.x,cameraResolution.y, data);
    	message.sendToTarget();
    	previewHandler = null;
    } else {
      Log.d(TAG, "Got preview callback, but no handler for it");
    }
  }

}
