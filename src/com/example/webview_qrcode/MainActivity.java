package com.example.webview_qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	public WebView webView;
	
	public final int REQUEST_CODE_ASK_CALL_PHONEk=1;
	private final int OK_CAMERA = 11;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
    	
    }  
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
