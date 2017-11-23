package com.example.webview_qrcode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.decoding.CaptureActivityHandler;
import com.mining.app.zxing.decoding.InactivityTimer;
import com.mining.app.zxing.decoding.RGBLuminanceSource;
import com.mining.app.zxing.view.ViewfinderView;
public class MipcaActivityCapture extends Activity implements Callback , View.OnClickListener{

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	
	
	private static final int REQUEST_CODE = 100;
	private static final int PARSE_BARCODE_SUC = 300;
	private static final int PARSE_BARCODE_FAIL = 303;
	private ProgressDialog mProgress;
	private String photo_path;
	private Bitmap scanBitmap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
		
		{//=重至所有參數
			MyApplication.getInstance().a = "";
			MyApplication.getInstance().b = "";
			MyApplication.getInstance().au = false;
			MyApplication.getInstance().bu = false;
		}
		
		//ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		
		Button mButtonBack = (Button) findViewById(R.id.button_back);
		mButtonBack.setOnClickListener(this);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		
		ImageButton mImageButton = (ImageButton) findViewById(R.id.button_function);
		mImageButton.setOnClickListener(this);
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.button_back:
			this.finish();
			break;
		case R.id.button_function:
			//打开手机中的相册
			Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
	        innerIntent.setType("image/*");
	        Intent wrapperIntent = Intent.createChooser(innerIntent, "選擇二維碼圖片");
	        this.startActivityForResult(wrapperIntent, REQUEST_CODE);
			break;
		}
	}
	
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			mProgress.dismiss();
			switch (msg.what) {
			case PARSE_BARCODE_SUC:
				onResultHandler((String)msg.obj, scanBitmap);
				break;
			case PARSE_BARCODE_FAIL:
				Toast.makeText(MipcaActivityCapture.this, (String)msg.obj, Toast.LENGTH_LONG).show();
				break;

			}
		}
		
	};
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch(requestCode){
			case REQUEST_CODE:
				//获取选中图片的路径
				Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
				if (cursor.moveToFirst()) {
					photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				}
				cursor.close();
				
				mProgress = new ProgressDialog(MipcaActivityCapture.this);
				mProgress.setMessage("正在掃描...");
				mProgress.setCancelable(false);
				mProgress.show();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						Result result = scanningImage(photo_path);
						if (result != null) {
							Message m = mHandler.obtainMessage();
							m.what = PARSE_BARCODE_SUC;
							m.obj = result.getText();
							mHandler.sendMessage(m);
						} else {
							Message m = mHandler.obtainMessage();
							m.what = PARSE_BARCODE_FAIL;
							m.obj = "Scan failed!";
							mHandler.sendMessage(m);
						}
					}
				}).start();
				
				break;
			
			}
		}
	}
	
	/**
	 * 扫描二维码图片的方法
	 * @param path
	 * @return
	 */
	public Result scanningImage(String path) {
		if(TextUtils.isEmpty(path)){
			return null;
		}
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小
		int sampleSize = (int) (options.outHeight / (float) 200);
		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);
		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);

		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (ChecksumException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		
		 if (mediaPlayer != null) {
			 mediaPlayer.stop();
			 mediaPlayer.release();
         }
		super.onDestroy();
	}
	
	/**
	 * 处理扫描结果
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		Log.i("掃描結果", result.getText());
		onResultHandler(resultString, barcode);
	}
	
	public String Ra,Rb;
	
	/** 处理扫描结果 ///2017/11/22 左右*/
	public void handleDecode2() {
		String resultString = "";
		
		if (!MyApplication.getInstance().a.equals("") && !MyApplication.getInstance().b.equals("")) {
			Log.i("阿姆挖出運啦", MyApplication.getInstance().a+MyApplication.getInstance().b);
			Ra = MyApplication.getInstance().a;
			Rb = MyApplication.getInstance().b;
			playBeepSoundAndVibrate();
			
			String aandb;
			
			String a = Ra.substring(0, 2);
			String b = Rb.substring(0, 2);
			
			try {
				if (a.endsWith("**")) {
					//b才是左
					aandb = Rb+Ra.substring(2, Ra.length());
					
					resultString = new Gson().toJson(invoice(aandb));
					
				} else if (b.endsWith("**")) {
					aandb = Ra+Rb.substring(2, Rb.length());
					resultString = new Gson().toJson(invoice(aandb));
				} else {
					resultString = "無法解析，左右格式錯誤";
				}
			} catch (Exception e) {
				resultString = "無法解析，左右格式錯誤";
			}
			
		}
		Log.i("結果", resultString);
		
		
		Intent resultIntent = new Intent();
		Bundle bundle = new Bundle();
		
		bundle.putString("result", resultString);
		resultIntent.putExtras(bundle);
		this.setResult(RESULT_OK, resultIntent);
		MipcaActivityCapture.this.finish();
		
	}
	
	private Invoice invoice(String aandb) {
		
		String track = aandb.substring(0, 10);//發票字軌 10
		
		String date = aandb.substring(10, 17);//發票開立日期 7
		
		String Randomcode = aandb.substring(17, 21);//隨機碼 4
		
		String Sales = aandb.substring(21, 29);//銷售額 8
		
		String Total = aandb.substring(29, 37);//總計額 8
		
		String BuyerUnified = aandb.substring(37, 45);//買方統一編號 8
		
		String SellerUnified = aandb.substring(45, 53);//賣方統一編號 8
		
		String Encryption = aandb.substring(53, 77);//加密驗證資訊 24
		
		//-接下來都是經過":"分開的資料
		
		String[] starl = aandb.split(":");//拆解分析
		
		//starl[0]//不需要
		
		
		String area = starl[1];//營業人自行使用區 (10 位)：提供營業人自行放置所需資訊，若不使用則以 10 個“*”符號呈現。
		String Count = starl[2];//完整品目筆數
		String totalnumber = starl[3];//交易品目總筆數
		String coding  = starl[4];//中文編碼參數//(1) Big5 編碼，則此值為 0  (2) UTF-8 編碼，則此值為 1   (3) Base64 編碼，則此值為 2
		
		Invoice invoice = new Invoice();
		invoice.setTrack(track);
		invoice.setDate(date);
		invoice.setRandomcode(Randomcode);
		invoice.setSales(Sales);
		invoice.setTotal(Total);
		invoice.setBuyerUnified(BuyerUnified);
		invoice.setSellerUnified(SellerUnified);
		invoice.setEncryption(Encryption);
		
		invoice.setArea(area);
		invoice.setCount(Count);
		invoice.setTotalnumber(totalnumber);
		invoice.setCoding(coding);
		ArrayList<iteer> iteers = new ArrayList<>();
		
		//接下來三項1組  品名   數量  單價		
		for (int i = 5; i < starl.length; i+=3) {//三個一組
			Log.i("品名", starl[i]);
			Log.i("數量", starl[i+1]);
			Log.i("單價", starl[i+2]);
			iteer iteer  = new iteer();
			iteer.setName(starl[i]);
			iteer.setNumber(starl[i+1]);
			iteer.setPrice(starl[i+2]);
			iteers.add(iteer);
		}
		invoice.setIteers(iteers);
		return invoice;
	}
	
	
	/**
	 * 跳转到上一个页面
	 * @param resultString
	 * @param bitmap
	 */
	private void onResultHandler(String resultString, Bitmap bitmap){
		if(TextUtils.isEmpty(resultString)){
			Toast.makeText(MipcaActivityCapture.this, "Scan failed!", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent resultIntent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("result", resultString);
		//Log.i("有图", bitmap.getWidth()+","+bitmap.getHeight()); 
		MyApplication.getInstance().bitmap = bitmap;
		//bundle.putParcelable("bitmap", bitmap);//---圖片不用傳
		resultIntent.putExtras(bundle);
		this.setResult(RESULT_OK, resultIntent);
		MipcaActivityCapture.this.finish();
	}
	
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd( R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;
	//播放哔哔声和振动
	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};


}