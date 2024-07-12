package com.migrsoft.image;

public class PicWorkerParam {

	public enum SubtitleSwitch {
		Off,
		Original,
		Chinese,
	}

	private SubtitleSwitch subtitleSwitch = SubtitleSwitch.Original;
	public SubtitleSwitch getSubtitleSwitch() {
		return subtitleSwitch;
	}
	public void setSubtitleSwitch(SubtitleSwitch subtitleSwitch) {
		this.subtitleSwitch = subtitleSwitch;
	}

	public enum ImageFormat {
		Png,
		Jpeg,
		Webp,
	}

	private ImageFormat imageFormat = ImageFormat.Jpeg;
	public ImageFormat getImageFormat() {
		return imageFormat;
	}
	public void setImageFormat(ImageFormat imageFormat) {
		this.imageFormat = imageFormat;
	}

	public String getOutputExtName() {
		switch(imageFormat) {
            case Png -> {
                return ".png";
            }
            case Jpeg -> {
                return ".jpg";
            }
            case Webp -> {
                return ".webp";
            }
        }
		return ".unk";
	}

	private float jpegQuality = 0.9f;
	public float getJpegQuality() {
		return jpegQuality;
	}
	public void setJpegQuality(float quality) {
		this.jpegQuality = quality;
	}

	private boolean forceGray = false;
	public boolean isForceGray() {
		return forceGray;
	}
	public void setForceGray(boolean force) {
		forceGray = force;
	}
}
