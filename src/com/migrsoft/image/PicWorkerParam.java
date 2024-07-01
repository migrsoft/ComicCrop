package com.migrsoft.image;

public class PicWorkerParam {

	public enum SubtitleSwitch {
		Off,
		Original,
		Chinese,
	}
	
	static public final int OUTPUT_FORMAT_PNG = 1;
	static public final int OUTPUT_FORMAT_JPG = 2;
	static public final int OUTPUT_FORMAT_WEBP = 3;
	
	private int mOutputType;
	private float mJpegQuality;
	private boolean mForceGray;

	private SubtitleSwitch subtitleSwitch = SubtitleSwitch.Original;
	
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
		else if (mOutputType == OUTPUT_FORMAT_WEBP)
			return ".webp";
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

	public SubtitleSwitch getSubtitleSwitch() {
		return subtitleSwitch;
	}

	public void setSubtitleSwitch(SubtitleSwitch value) {
		subtitleSwitch = value;
	}
}
