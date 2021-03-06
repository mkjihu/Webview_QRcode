package com.example.webview_qrcode;

import com.androidquery.AQuery;
import com.google.gson.Gson;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	public WebView webView;
	public final int REQUEST_CODE_ASK_CALL_PHONEk=1;
	private final int OK_CAMERA = 11;
	private final static int SCANNIN_GREQUEST_CODE = 1;
	
	public AQuery aq;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		aq = new AQuery(this);
		fid();
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
				||ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			//申请WRITE_EXTERNAL_STORAGE权限
			//Log.i("問", "問");
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA
					,Manifest.permission.CAPTURE_AUDIO_OUTPUT
					,Manifest.permission.READ_EXTERNAL_STORAGE
					,Manifest.permission.WRITE_EXTERNAL_STORAGE
					,Manifest.permission.READ_PHONE_STATE}, OK_CAMERA);

		}
		aq.id(R.id.button1).clicked(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Bitmap bitmap = MyApplication.getInstance().bitmap2;
				Log.i("幹", "幹");
				/**/
	        	//参数说明：
	        	//Bitmap source：要从中截图的原始位图
	        	//int x:起始x坐标
	        	//int y：起始y坐标
				//int width：要截的图的宽度
				//int height：要截的图的高度
	        	//http://blog.csdn.net/fq813789816/article/details/54017074
				/*
	        	Bitmap bm = Bitmap.createBitmap(bitmap
	        			, 0
	        			, 0
	                    , bitmap.getWidth()/2
	                    , bitmap.getHeight());//邊長取正數+四捨五入
	        	*/
				if (bitmap !=null) {
					aq.id(R.id.imageView1).image(bitmap);
				}else{
					Log.i("GG", "沒圖");
				}
				
			}
		});
		aq.id(R.id.button2).clicked(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MyApplication.getInstance().Type = "1";

		    	Intent intent = new Intent(MainActivity.this, MipcaActivityCapture.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
			}
		});
		
		
	}



	private void fid() {
		webView = (WebView)findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		
		webView.setWebViewClient(new WebViewClient(){
			//-onPageStarted(網頁開始載入)
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
			//--onPageFinished(網頁載入完畢)
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
		});
		webView.setWebChromeClient(new WebChromeClient());
		webView.loadUrl("file:///android_asset/text.html");//webView.loadUrl("file:///android_asset/text.html");
		webView.addJavascriptInterface(MainActivity.this , "AndroidFunction");//-註冊一個AndroidFunction 方法
		
	}
	
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();//bundle.getString("result")
				String dfg = bundle.getString("result");
				Log.i("回傳", dfg);
				String Type = bundle.getString("Type");
				switch (Type) {
				case "1":
					webView.loadUrl("javascript:callFromActivity('" + dfg + "')");
					break;
				case "2":
					if (!dfg.equals("無法解析，左右格式錯誤")) {
						Invoice invoice = new Gson().fromJson(dfg, Invoice.class);
						
						String dfg2 = "發票字軌:"+invoice.getTrack()+"\n"
										+",發票開立日期:"+invoice.getDate()+"\n"
										+",隨機碼:"+invoice.getRandomcode()+"\n"
										+",銷售額:"+invoice.getSales()+"\n"
										+",總計額:"+invoice.getTotal()+"\n"
										+",買方統一編號:"+invoice.getBuyerUnified()+"\n"
										+",賣方統一編號:"+invoice.getSellerUnified()+"\n"				
										+",加密驗證資訊:"+invoice.getEncryption()+"\n"	
										+",營業人自行使用區:"+invoice.getArea()+"\n"	
										+",完整品目筆數:"+invoice.getCount()+"\n"	
										+",交易品目總筆數:"+invoice.getTotalnumber()+"\n"	
										+",中文編碼參數:"+invoice.getCoding()+"\n";
						String dfg3 = "";
						for (int i = 0; i < invoice.getIteers().size(); i++) {
							dfg3 =dfg3
									+"品名:"+invoice.getIteers().get(i).getName()
									+",數量:"+invoice.getIteers().get(i).getNumber()
									+",單價:"+invoice.getIteers().get(i).getPrice();
						}
						webView.loadUrl("javascript:callFromActivity('" + dfg2+dfg3 + "')");
					}
					else
					{
						webView.loadUrl("javascript:callFromActivity('" + dfg + "')");
					}
					break;
				}
				
				
				
				
				//Bitmap bitmap = MyApplication.getInstance().bitmap;
				//aq.id(R.id.imageView1).image(bitmap);
			}
			break;
		}
		
		
	}
	
	//相機權限同意返回
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

		//Log.i("!!!", requestCode+"");
		switch (requestCode) {
			case OK_CAMERA:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
					Log.i("!!!", "同意");
				}
				else {
					//Log.i("!!!", "不同意");
					//finish();
					Toast.makeText(MainActivity.this, "不同意無法使用掃描", Toast.LENGTH_SHORT).show();
				}
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	
	
	
	//=====================================================================================
	
	//在js中调用window.AndroidFunction.openqr()，便会触发此方法。  
    @JavascriptInterface  
    public void openqr() {  
    	Log.i("測試", "測試");
    	MyApplication.getInstance().Type = "2";
    	Intent intent = new Intent(MainActivity.this, MipcaActivityCapture.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
    }  
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
