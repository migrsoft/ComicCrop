package com.migrsoft.image;

public class PicWorkerParam {
	
	static public final int OUTPUT_FORMAT_PNG = 1;
	static public final int OUTPUT_FORMAT_JPG = 2;
	
	private int mOutputType;
	private float mJpegQuality;
	private boolean mForceGray;
	
	public PicWorkerParam() {
		mOutputType = OUTPUT_FORMAT_JPG;
		mJpegQuality = 0.9f;
		mForceGray = false;
	}

	public int getOutputFormat() {
		return mOutputType;
	}

	public void setOutputFormat(int format) {
		this.mOutputType = format;
	}
	
	public String getOutputExtName() {
		if (mOutputType == OUTPUT_FORMAT_PNG)
			return ".png";
		else if (mOutputType == OUTPUT_FORMAT_JPG)
			return ".jpg";
		else
			return null;
	}

	public float getJpegQuality() {
		return mJpegQuality;
	}

	public void setJpegQuality(float quality) {
		this.mJpegQuality = quality;
	}

	public boolean isForceGray() {
		return mForceGray;
	}
	
	public void setForceGray(boolean force) {
		mForceGray = force;
	}
}
