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
import java.util.Vector;

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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.camera.PlanarYUVLuminanceSource;
import com.yanzhenjie.zbar.Config;
import com.yanzhenjie.zbar.Image;
import com.yanzhenjie.zbar.ImageScanner;
import com.yanzhenjie.zbar.Symbol;
import com.yanzhenjie.zbar.SymbolSet;

//-----


//-----

final class DecodeHandler extends Handler {

	private static final String TAG = DecodeHandler.class.getSimpleName();

	private final MipcaActivityCapture activity;
	private final MultiFormatReader multiFormatReader;
	private QRCodeMultiReader qrCodeMultiReader;
	private Bitmap bm1;
	private Bitmap bm2;
	private Result ka1;
	private Result ka2;
	private Hashtable<DecodeHintType, Object> hints;
	
	private Hashtable<DecodeHintType, Object> hints2;
	
	
	//
	private ImageScanner scanner;//声明扫描器 	
	
	
	DecodeHandler(MipcaActivityCapture activity,
			Hashtable<DecodeHintType, Object> hints) {
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		this.hints = hints;
		qrCodeMultiReader = new QRCodeMultiReader();
		
		
		this.activity = activity;
		
		//======11/27======
	    // 解码的参数
	    hints2 = new Hashtable<DecodeHintType, Object>(2);
	    // 可以解析的编码类型
	    Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
	    if (decodeFormats == null || decodeFormats.isEmpty()) {
	        decodeFormats = new Vector<BarcodeFormat>();
	        // 这里设置可扫描的类型，我这里选择了都支持
	        //decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
	        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
	        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
	    }
	    hints2.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
	    // 设置继续的字符编码格式为UTF8
	    //hints2.put(DecodeHintType.CHARACTER_SET, "UTF-8");
		
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
		
		
		PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
		//BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));//--傳入亮度来源解碼
	

		switch (MyApplication.getInstance().Type) {
		case "1"://原生解碼
			decoding1(source);
			//suru(rotatedData);//目前問題未修正
			break;
		case "2"://自製解碼
			decoding2(source, rawResult);
			break;
		}
		
	}

	
	//===================2017/11/20===================================================================
	
	public void decoding1(PlanarYUVLuminanceSource source) {
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		
		Result[] rawResult = null;
		
		// 设置继续的字符编码格式为UTF8
		//hints.put(DecodeHintType.CHARACTER_SET, "BIG5");
		//hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
		
		try {
			rawResult = qrCodeMultiReader.decodeMultiple(bitmap, hints);
			//判斷
			if (rawResult != null && rawResult.length>1){
				if (fii3(rawResult)){
					hints.put(DecodeHintType.CHARACTER_SET, "BIG5");
					rawResult = qrCodeMultiReader.decodeMultiple(bitmap,hints);//.decodeWithState(bitmap);
				}
			}
		} catch (ReaderException re) {
			// continue
		} finally {
			qrCodeMultiReader.reset();
		}
		
		
		if (rawResult != null && rawResult.length > 1) {
			Log.i("測試掃描", "測試掃描"+rawResult.length);
			Message message = Message.obtain(activity.getHandler(),R.id.decode_succeeded, rawResult);
			Bundle bundle = new Bundle();
			bundle.putParcelable(DecodeThread.BARCODE_BITMAP,source.renderCroppedGreyscaleBitmap());
			message.setData(bundle);
			message.sendToTarget();
		} else {
			Message message = Message.obtain(activity.getHandler(),R.id.decode_failed);
			message.sendToTarget();
		}
		
	}
	
	
	///---判斷是否重解
	public boolean fii3(Result[] rawResult)
	{
		String a = rawResult[0].getText().substring(0, 2);
		String[] starl;
		String coding;
		if (a.endsWith("**")) {
			//解rawResult[0]
			starl = rawResult[1].getText().split(":");//拆解分析
			coding  = starl[4];

		}else{
			//解rawResult[1]
			starl = rawResult[0].getText().split(":");//拆解分析
			coding  = starl[4];
		}
		return coding.equals("0") ?  true : false;  //判斷解碼方式 0 就用BIG5重來
	}
	
	//自定
	public void decoding2(PlanarYUVLuminanceSource source,Result rawResult) {
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
        			ka1 = multiFormatReader.decode(bitmap1, hints2);
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
	
	///===============Zbar===================2017/11/29========================
	
		private void suru(byte[] data) {
			
			scanner = new ImageScanner();//创建扫描器
		    scanner.setConfig(0, Config.X_DENSITY, 3);
		    scanner.setConfig(0, Config.Y_DENSITY, 3);
		    //scanner.setConfig(0, Config.X_DENSITY, 2);//行扫描间隔
		    //scanner.setConfig(0, Config.Y_DENSITY, 2);//列扫描间隔
		      scanner.setConfig(0, Config.ENABLE, 0); //停用所有符号
		    scanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1); //只有QRCODE被启用
		    scanner.setConfig(0, 512, 1);//是否开启同一幅图一次解多个条码,0表示只解一个，1为多个
		    
			/**
			 *创建解码图像,width, height 分别为摄像头预览分辨率的宽度和高度,一般来说,分辨率越高图
			 *像越清晰,但解码速度越慢。由于解码算法需要处理的是原始灰度数据,而预览图像的默认格式为 
			 *YCbCr_420_SP,需要转换格式才能处理, 参数"Y800"表示待转换的图像格式。
			 */
		    Camera.Size size = MyApplication.getInstance().previewSize;

		    Image barcode = new Image(size.width, size.height, "Y800");//);
			
			 /**
			    *设置扫描区域范围,为了较好的识读较长的一维码,扫描框的宽度不应过小,由于预览的图像为横屏, 
			    *注意扫描区域需要转换为竖屏对应的位置 
			    */
			 //Rect cropRect = finder_view.getScanImageRect(size.height, size.width);
			 //finder_view为DEMO中自定义的扫码区域控件。
			 //source.setCrop(cropRect.top,cropRect.left,cropRect.height(),cropRect.width());

			 /*填充图像数据,data 为摄像头原始数据*/
		     barcode.setData(data); 

			 /*解码,返回值为 0 代表失败,>0 表示成功*/
			 int result = scanner.scanImage(barcode); 
			 if (result != 0) {
				 SymbolSet syms = scanner.getResults();
			      Log.i("數量",syms.size()+"");
			      for (Symbol sym : syms) {
			        //scanText.setText("barcode result " + sym.getData());
			        Log.i("幹",sym.getData());
			      }
			 }
			 Message message = Message.obtain(activity.getHandler(),R.id.decode_failed);
				message.sendToTarget();
		}
		
}
