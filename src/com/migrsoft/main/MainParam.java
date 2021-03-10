package com.migrsoft.main;

import com.migrsoft.image.PicWorkerParam;

public class MainParam extends PicWorkerParam {
	
	static private MainParam sMainParam = null;
	
	static public MainParam getInstance() {
		if (sMainParam == null)
			sMainParam = new MainParam();
		return sMainParam;
	}

	private boolean mAutoGrayLevel;
	
	private int mMaxThreads;
	
	private boolean mCropWhite;
	
	private int mMaxWidth;
	private int mMaxHeight;
	
	public MainParam() {
		mAutoGrayLevel = true;
		mMaxThreads = 2;
		mCropWhite = true;
		
		mMaxWidth = 600;
		mMaxHeight = 1000;
	}
	
	public boolean isAutoGrayLevel() {
		return mAutoGrayLevel;
	}

	public void setAutoGrayLevel(boolean auto) {
		this.mAutoGrayLevel = auto;
	}

	public int getMaxThreads() {
		return mMaxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.mMaxThreads = maxThreads;
	}
	
	public boolean isCropWhite() {
		return mCropWhite;
	}
	
	public void setCropWhite(boolean b) {
		mCropWhite = b;
	}
	
	public int getMaxWidth() {
		return mMaxWidth;
	}
	
	public void setMaxWidth(int maxw) {
		mMaxWidth = maxw;
	}
	
	public int getMaxHeight() {
		return mMaxHeight;
	}
	
	public void setMaxHeight(int maxh) {
		mMaxHeight = maxh;
	}
}
