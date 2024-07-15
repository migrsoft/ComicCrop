package com.migrsoft.main;

import com.migrsoft.image.PicWorkerParam;

public class MainParam extends PicWorkerParam {

	static private MainParam instance = null;
	
	static public MainParam getInstance() {
		if (instance == null)
			instance = new MainParam();
		return instance;
	}

	public enum SubtitleSwitch {
		Off,
		Original,
		Chinese,
	}

	private SubtitleSwitch subtitleSwitch = SubtitleSwitch.Chinese;
	public SubtitleSwitch getSubtitleSwitch() {
		return subtitleSwitch;
	}
	public void setSubtitleSwitch(SubtitleSwitch subtitleSwitch) {
		this.subtitleSwitch = subtitleSwitch;
	}

	private boolean autoGrayLevel;
	
	private final int maxThreads;
	
	private boolean cropWhiteEdge;

	private boolean isCropAll;
	
	private int maxWidth;
	private int maxHeight;
	
	public MainParam() {
		maxThreads = Runtime.getRuntime().availableProcessors();
		autoGrayLevel = true;
		cropWhiteEdge = true;
		isCropAll = true;
		maxWidth = 600;
		maxHeight = 1000;
	}
	
	public boolean isAutoGrayLevel() {
		return autoGrayLevel;
	}

	public void setAutoGrayLevel(boolean auto) {
		this.autoGrayLevel = auto;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public boolean isCropWhite() { return cropWhiteEdge; }

	public void setCropWhite(boolean b) { cropWhiteEdge = b; }

	public boolean isCropAll() { return isCropAll; }

	public void setCropAll(boolean b) { isCropAll = b; }

	public int getMaxWidth() {
		return maxWidth;
	}
	
	public void setMaxWidth(int maxw) {
		maxWidth = maxw;
	}
	
	public int getMaxHeight() {
		return maxHeight;
	}
	
	public void setMaxHeight(int maxh) {
		maxHeight = maxh;
	}

	private boolean pageSpacingSwitch = true;

	public boolean getPageSpacingSwitch() {
		return pageSpacingSwitch;
	}

	public void setPageSpacingSwitch(boolean value) {
		pageSpacingSwitch = value;
	}

	private int pageSpacing = 20;

	public int getPageSpacing() {
		return (pageSpacingSwitch) ? pageSpacing : 0;
	}

	public void setPageSpacing(int spacing) {
		pageSpacing = spacing;
	}

	private boolean pageNumberSwitch = false;

	public boolean getPageNumberSwitch() {
		return pageNumberSwitch;
	}

	public void setPageNumberSwitch(boolean value) {
		pageNumberSwitch = value;
	}
}
