package com.example.webview_qrcode;

import java.util.ArrayList;

public class Invoice {
	public String track;//發票字軌
	public String date;//發票開立日期
	public String Randomcode;//隨機碼
	public String Sales;//銷售額
	public String Total;//總計額 
	public String BuyerUnified;//買方統一編號
	public String SellerUnified;//賣方統一編號
	public String Encryption;//加密驗證資訊
	
	//-接下來都是經過":"分開的資料
	public String area;//營業人自行使用區 (10 位)：提供營業人自行放置所需資訊，若不使用則以 10 個“*”符號呈現。
	public String Count;//完整品目筆數
	public String totalnumber;//交易品目總筆數
	public String coding;//中文編碼參數//(1) Big5 編碼，則此值為 0  (2) UTF-8 編碼，則此值為 1   (3) Base64 編碼，則此值為 2
	
	public ArrayList<iteer> iteers;
	
	public String getTrack() {
		return track;
	}
	public void setTrack(String track) {
		this.track = track;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getRandomcode() {
		return Randomcode;
	}
	public void setRandomcode(String randomcode) {
		Randomcode = randomcode;
	}
	public String getSales() {
		return Sales;
	}
	public void setSales(String sales) {
		Sales = sales;
	}
	public String getTotal() {
		return Total;
	}
	public void setTotal(String total) {
		Total = total;
	}
	public String getBuyerUnified() {
		return BuyerUnified;
	}
	public void setBuyerUnified(String buyerUnified) {
		BuyerUnified = buyerUnified;
	}
	public String getSellerUnified() {
		return SellerUnified;
	}
	public void setSellerUnified(String sellerUnified) {
		SellerUnified = sellerUnified;
	}
	public String getEncryption() {
		return Encryption;
	}
	public void setEncryption(String encryption) {
		Encryption = encryption;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getCount() {
		return Count;
	}
	public void setCount(String count) {
		Count = count;
	}
	public String getTotalnumber() {
		return totalnumber;
	}
	public void setTotalnumber(String totalnumber) {
		this.totalnumber = totalnumber;
	}
	public String getCoding() {
		return coding;
	}
	public void setCoding(String coding) {
		this.coding = coding;
	}
	public ArrayList<iteer> getIteers() {
		return iteers;
	}
	public void setIteers(ArrayList<iteer> iteers) {
		this.iteers = iteers;
	}
	
}
