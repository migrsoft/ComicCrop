package com.migrsoft.main;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

public class RenamePlus {
	
	public void SetData(Vector<String> names, int[] indices) {
		mNames = names;
		mIndices = indices;
	}
	
	public Boolean RenameAll(String prefix, String path) {
		mPrefix = prefix;
		mPath = path;
		
		GenerateNewNames();
//		PrintNewNames();
		RenameActually();
		
		return true;
	}
	
	public Vector<String> GetResult() {
		Vector<String> result = new Vector<String>();
		for (int i=0; i < mNames.size(); i++) {
			String name = mNames.get(i);
			result.add(name);
		}
		return result;
	}
	
	private void GenerateNewNames() {
		mNewNames = new HashMap<String, String>();
		int order = 1;
		for (int i=0; i < mNames.size(); i++) {
			for (int j : mIndices) {
				if (i == j) { // 选择的项目
					String name = mNames.get(i);
					String name_ext = name.substring(name.lastIndexOf("."));
					String name_new = String.format("%s%03d%s", mPrefix, order, name_ext);
					mNewNames.put(name, name_new);
					order++;
					break;
				}
			}
		}
	}
	
	private void RenameActually() {
		// 产生临时名称
		final String prefix = "__crop_";
		for (int i=0; i < mNames.size(); i++) {
			String name = mNames.get(i);
			String name_tmp = prefix + name;
			File src = new File(mPath + name);
			File dst = new File(mPath + name_tmp);
			src.renameTo(dst);
		}
		// 正式更名
		for (int i=0; i < mNames.size(); i++) {
			String name = mNames.get(i);
			String name_tmp = prefix + name;
			String name_new;
			if (mNewNames.containsKey(name)) {
				name_new = mNewNames.get(name);
			} else {
				name_new = name;
			}
			File src = new File(mPath + name_tmp);
			File dst = new File(mPath + name_new);
			src.renameTo(dst);
		}
	}
	
	@SuppressWarnings("unused")
	private void PrintNewNames() {
		for (int i=0; i < mNames.size(); i++) {
			String name = mNames.get(i);
			if (mNewNames.containsKey(name)) {
				System.out.println(name + " -> " + mNewNames.get(name));
			} else {
				System.out.println(name);
			}
		}
	}
	
	private Vector<String> mNames;
	
	private int[] mIndices;
	
	private HashMap<String, String> mNewNames;
	
	private String mPrefix;
	
	private String mPath;
}
