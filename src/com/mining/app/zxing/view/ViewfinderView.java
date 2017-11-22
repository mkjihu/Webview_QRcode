/*
 * Copyright (C) 2008 ZXing authors
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

package com.mining.app.zxing.view;

import java.util.Collection;
import java.util.HashSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.webview_qrcode.MyApplication;
import com.example.webview_qrcode.R;
import com.google.zxing.ResultPoint;
import com.mining.app.zxing.camera.CameraManager;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 */
public final class ViewfinderView extends View {
	/**
	 * 刷新界面的时间
	 */
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;

	/**
	 * 四个绿色边角对应的长度
	 */
	private int ScreenRate;
	
	/**
	 * 四个绿色边角对应的宽度
	 */
	private static final int CORNER_WIDTH = 5;
	/**
	 * 扫描框中的中间线的宽度
	 */
	private static final int MIDDLE_LINE_WIDTH = 6;
	
	/**
	 * 扫描框中的中间线的与扫描框左右的间隙
	 */
	private static final int MIDDLE_LINE_PADDING = 5;
	
	/**
	 * 中间那条线每次刷新移动的距离
	 */
	private static final int SPEEN_DISTANCE = 5;
	
	/**
	 * 手机的屏幕密度
	 */
	private static float density;
	/**
	 * 字体大小
	 */
	private static final int TEXT_SIZE = 16;
	/**
	 * 字体距离扫描框下面的距离
	 */
	private static final int TEXT_PADDING_TOP = 30;
	
	/**
	 * 画笔对象的引用
	 */
	private Paint paint;
	
	/**
	 * 中间滑动线的最顶端位置
	 */
	private int slideTop;
	
	/**
	 * 中间滑动线的最底端位置
	 */
	private int slideBottom;
	
	/**
	 * 将扫描的二维码拍下来，这里没有这个功能，暂时不考虑
	 */
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	
	private final int resultPointColor;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;

	boolean isFirst;
	
	
	//--2016 08 10 -- 底部+顯示文字
	public String tesr="";
	
	
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		density = context.getResources().getDisplayMetrics().density;
		//将像素转换成dp
		ScreenRate = (int)(15 * density);

		paint = new Paint();
		
		//--修修改支援API22 
		//Resources resources = getResources();
		//maskColor = resources.getColor(R.color.viewfinder_mask);
		//resultColor = resources.getColor(R.color.result_view);
		//resultPointColor = resources.getColor(R.color.possible_result_points);
		
		maskColor = ContextCompat.getColor(getContext(), R.color.viewfinder_mask);//--修修改支援API22 
		resultColor = ContextCompat.getColor(getContext(), R.color.result_view);//--修修改支援API22 
		resultPointColor = ContextCompat.getColor(getContext(), R.color.possible_result_points);//--修修改支援API22 
		
		possibleResultPoints = new HashSet<ResultPoint>(5);
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		//中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
		Rect frame = CameraManager.get().getFramingRect();
		if (frame == null) {
			return;
		}
		//Log.i("邊長距離", frame.left+"_"+frame.right+"_~~~~~_"+frame.top+"_"+frame.bottom);
		
		//初始化中间线滑动的最上边和最下边
		if(!isFirst){
			isFirst = true;
			slideTop = frame.top;
			slideBottom = frame.bottom;
		}
		
		//获取屏幕的宽和高
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		
		//画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
		//扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);
		
		

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {

			//画扫描框边上的角，总共8个部分
			paint.setColor(Color.GREEN);//--設定顏色
			//int myColor = context.getResources().getColor(R.color.aabswet);
			//paint.setColor(myColor);
			canvas.drawRect(frame.left , frame.top , frame.left + ScreenRate,frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top+ ScreenRate, paint);
			
			canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top+ ScreenRate, paint);
			
			canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left+ ScreenRate, frame.bottom, paint);
			canvas.drawRect(frame.left, frame.bottom - ScreenRate,frame.left + CORNER_WIDTH, frame.bottom, paint);
			
			canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,frame.right, frame.bottom, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,frame.right, frame.bottom, paint);

			//===2017/11/21===繪製切齊左右的線
			Paint linePaint = new Paint();
	        linePaint.setAntiAlias(true);//设置抗锯齿开关
	        linePaint.setColor(Color.parseColor("#E63F00"));//--設定顏色
	        //linePaint.setStyle(Style.STROKE);//设置绘制模式
	        //linePaint.setPathEffect(new DashPathEffect(new float[]{20f,10f,5f}, 0));//线的显示效果：破折号格式
	        linePaint.setStrokeWidth(5);// 设置线宽
			/*	startX：起始端点的X坐标。
			startY：起始端点的Y坐标。
			stopX：终止端点的X坐标。
			stopY：终止端点的Y坐标。
			paint：绘制直线所使用的画笔。*/
			int ux = frame.left+((frame.right - frame.left)/2);
			canvas.drawLine(ux, frame.top, ux, frame.bottom, linePaint); //绘制直线 
			
			
			//===2017/11/22===繪製內縮框+圓
			
			//paint.setColor(Color.BLACK);//--設定顏色
			//int myColor = context.getResources().getColor(R.color.aabswet);
			int x = frame.left+((frame.right - frame.left)/4);
			int y = frame.top +((frame.bottom - frame.top)/2); //y起始座標 
			if (MyApplication.getInstance().au) {
				/*A圓 cx：圆心的x坐标。cy：圆心的y坐标。radius：圆的半径。paint：绘制时所使用的画笔。*/
				canvas.drawCircle(x, y, 60, paint);  
			}
			int x2 = frame.left+((frame.right - frame.left)*3/4);
			if (MyApplication.getInstance().bu) {
				/*B圓 cx：圆心的x坐标。cy：圆心的y坐标。radius：圆的半径。paint：绘制时所使用的画笔。*/
				canvas.drawCircle(x2, y, 60, paint);   
			} 
			
			
			
			
			
			//绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
			slideTop += SPEEN_DISTANCE;
			if(slideTop >= frame.bottom){
				slideTop = frame.top;
			}
			Rect lineRect = new Rect();  
            lineRect.left = frame.left;  
            lineRect.right = frame.right;  
            lineRect.top = slideTop;  
            lineRect.bottom = slideTop + 18;  
            
            //--修修改支援API22 
            //canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.qrcode_scan_line))).getBitmap(), null, lineRect, paint); 
            //===2017/11/21===註解之後就沒有線在那邊移動
            //canvas.drawBitmap(((BitmapDrawable)(ContextCompat.getDrawable(getContext(), R.drawable.qrcode_scan_line))).getBitmap(), null, lineRect, paint); //--修修改支援API22 
   
            
            //Log.i("讀取點", "讀取點");
            
        	//画扫描框下面的字
            paint.setColor(Color.WHITE);    
            paint.setTextSize(TEXT_SIZE * density);    
            paint.setAlpha(0x40);    
            paint.setTypeface(Typeface.create("System", Typeface.BOLD));   
            
            //---
            String text = getResources().getString(R.string.scan_text);  
            if (!tesr.equals("")) {
				text = tesr;
			}
            
            float textWidth = paint.measureText(text);  
              
            canvas.drawText(text, (width - textWidth)/2, (float) (frame.bottom + (float)TEXT_PADDING_TOP *density), paint); 
			

			Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 3.0f, paint);
				}
			}

			
			//只刷新扫描框的内容，其他地方不刷新
			//===2017/11/21===註解後不刷新
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,frame.right, frame.bottom);
			
		}
	}

	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}

	public String getTesr() {
		return tesr;
	}

	public void setTesr(String tesr) {
		this.tesr = tesr;
	}
	
}

