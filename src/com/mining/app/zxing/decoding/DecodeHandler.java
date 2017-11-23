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

package com.mining.app.zxing.decoding;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.webview_qrcode.MipcaActivityCapture;
import com.example.webview_qrcode.MyApplication;
import com.example.webview_qrcode.R;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.camera.PlanarYUVLuminanceSource;

final class DecodeHandler extends Handler {

	private static final String TAG = DecodeHandler.class.getSimpleName();

	private final MipcaActivityCapture activity;
	private final MultiFormatReader multiFormatReader;

	private Bitmap bm1;
	private Bitmap bm2;
	private Result ka1;
	private Result ka2;
	
	DecodeHandler(MipcaActivityCapture activity,
			Hashtable<DecodeHintType, Object> hints) {
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case R.id.decode:
			//Log.i("得到解码消息", "Got decode message");
			decode((byte[]) message.obj, message.arg1, message.arg2);
			break;
		case R.id.quit:
			Looper.myLooper().quit();
			break;
		}
	}

	int a = 0;
	
	/**
	 * Decode the data within the viewfinder rectangle, and time how long it
	 * took. For efficiency, reuse the same reader objects from one decode to
	 * the next.
	 * 
	 * @param data
	 *            The YUV preview frame.
	 * @param width *预览框的宽度。
	 *            The width of the preview frame.
	 * @param height 预览框架的高度。
	 *            The height of the preview frame.
	 */
	//-解码取景器矩形内的数据
	private void decode(byte[] data, int width, int height) {
		 // 首先，要取得该图片的像素数组内容
		//Log.i("解码开始", "预览框的宽度"+width);
		//Log.i("解码开始", "预览框的高度"+height);
		
		long start = System.currentTimeMillis();
		Result rawResult = null;

		// modify here 在這裡修改 // 将int数组转换为byte数组
		byte[] rotatedData = new byte[data.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				rotatedData[x * height + height - y - 1] = data[x + y * width];
		}
		int tmp = width; // Here we are swapping, that's the difference to #11
		width = height;
		height = tmp;
		
		//Log.i("解码开始2", "预览框的宽度"+width);
		//Log.i("解码开始2", "预览框的高度"+height);
		PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
		
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));//--傳入亮度来源解碼
		//Log.i("解码开始3", "预览框的宽度"+source.getDataWidth());
		//Log.i("解码开始3", "预览框的高度"+source.getDataHeight());
		
		if (source.renderCroppedGreyscaleBitmap()!=null) {
			//Log.i("解码开始2", "有圖");
			/**/
        	//参数说明：
        	//Bitmap source：要从中截图的原始位图
        	//int x:起始x坐标
        	//int y：起始y坐标
			//int width：要截的图的宽度
			//int height：要截的图的高度
        	//http://blog.csdn.net/fq813789816/article/details/54017074
      
          	Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
    		//hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //UTF-8设置二维码内容的编码
    		hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
    		
    		
    		bm1 = null;
    		bm2 = null;
    		ka1 = null;
    		ka2 = null;
    		
    		if (!MyApplication.getInstance().au) {
    			
    		  	bm1 = Bitmap.createBitmap(source.renderCroppedGreyscaleBitmap()
            			, 0
            			, 0
                        , source.renderCroppedGreyscaleBitmap().getWidth()/2
                        , source.renderCroppedGreyscaleBitmap().getHeight());//邊長取正數+四捨五入
    		  	RGBLuminanceSource source1 = new RGBLuminanceSource(bm1);
        		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source1));
        		
    			try {
        			ka1 = multiFormatReader.decode(bitmap1, hints);
    				Log.i("左邊 ", ka1.toString());
    			} catch (Exception e) {
    				//Log.i("解析出錯", "繼續");
    			}finally {
    				multiFormatReader.reset();
        		}
			}
    		
    		if (!MyApplication.getInstance().bu) {
    			
    			
    			bm2 = Bitmap.createBitmap(source.renderCroppedGreyscaleBitmap()
            			, source.renderCroppedGreyscaleBitmap().getWidth()/2
            			, 0
                        , source.renderCroppedGreyscaleBitmap().getWidth()/2
                        , source.renderCroppedGreyscaleBitmap().getHeight());//邊長取正數+四捨五入
    			RGBLuminanceSource source2 = new RGBLuminanceSource(bm2);
        		BinaryBitmap bitmap2 = new BinaryBitmap(new HybridBinarizer(source2));
        		
        		try {
        			ka2 = multiFormatReader.decode(bitmap2, hints);
    				Log.i("右邊 ", ka2.toString());
    			} catch (Exception e) {
    				// TODO: handle exception
    			}finally {
    				multiFormatReader.reset();
        		}
    		}

    		
    		if (ka1 != null) {
    			MyApplication.getInstance().au = true;
    			MyApplication.getInstance().a = ka1.toString();
    			 // 回收并且置为null
    			bm1.recycle(); 
    			bm1 = null; 
			}
    		if (ka2 != null) {
    			MyApplication.getInstance().bu = true;
    			MyApplication.getInstance().b = ka2.toString();
    			// 回收并且置为null
    			bm2.recycle(); 
    			bm2 = null; 
			}
    		if (MyApplication.getInstance().au&&MyApplication.getInstance().bu) {
    			
    			Message message = Message.obtain(activity.getHandler(),R.id.decode_LROK, rawResult);
    			message.sendToTarget();
    			return;
			}
		}
		
		Message message = Message.obtain(activity.getHandler(),R.id.decode_failed);
		message.sendToTarget();
		
		
		/*///===20147/11/22=== 註解
		
		try {
			rawResult = multiFormatReader.decodeWithState(bitmap);
		} catch (ReaderException re) {
			// continue
			
		} finally {
			multiFormatReader.reset();
		}

		if (rawResult != null) {
			//~~~~~~~~~~~~~~~~~ 
			//Bitmap bitmap1 = getbitmap(data);
			//Log.i("圖片高寬", bitmap1.getHeight()+","+bitmap1.getWidth());
			//-此处截图测试
			//MyApplication.getInstance().bitmap2 = source.renderCroppedGreyscaleBitmap();
			//~~~~~~~~~~~~~~~~~
			//MyApplication.getInstance().bitmap2 = source.renderCroppedGreyscaleBitmap();
			long end = System.currentTimeMillis();
			Log.i("找到条形码", "Found barcode (" + (end - start) + " ms):\n"+ rawResult.toString());
			
			Message message = Message.obtain(activity.getHandler(),R.id.decode_succeeded, rawResult);
			Bundle bundle = new Bundle();
			bundle.putParcelable(DecodeThread.BARCODE_BITMAP,source.renderCroppedGreyscaleBitmap());
			message.setData(bundle);
			// Log.d(TAG, "Sending decode succeeded message...");
			message.sendToTarget();
			//Message message = Message.obtain(activity.getHandler(),R.id.decode_failed);
			//message.sendToTarget();
		} else {
			Message message = Message.obtain(activity.getHandler(),R.id.decode_failed);
			message.sendToTarget();
		}
		*/
	}

	//===================2017/11/20===================================================================
	public Bitmap getbitmap(byte[] data) {
      //处理data  
		byte[] rawImage;  
	    Bitmap bitmap; 
      Camera.Size previewSize = MyApplication.getInstance().previewSize;//获取尺寸,格式转换的时候要用到  
      BitmapFactory.Options newOpts = new BitmapFactory.Options();  
      newOpts.inJustDecodeBounds = true;  
      YuvImage yuvimage = new YuvImage(  
              data,  
              ImageFormat.NV21,  
              previewSize.width,  
              previewSize.height,  
              null);  
      ByteArrayOutputStream baos = new ByteArrayOutputStream();  
      yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);// 80--JPG图片的质量[0-100],100最高  
      rawImage = baos.toByteArray();  
      //将rawImage转换成bitmap  
      BitmapFactory.Options options = new BitmapFactory.Options();  
      options.inPreferredConfig = Bitmap.Config.RGB_565;  
      bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);  
      return bitmap;
      
	}
}
