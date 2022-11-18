package com.migrsoft.main;

import java.util.Vector;

public class RenameThem {
    public class Item {
        private String mOriginName;
        private String mName;
        private String mExtName;
        private String mNewName;
        private String mNewExtName;

        public Item(String name) {
            mOriginName = name;
            int extPos = name.lastIndexOf(".");
            mName = name.substring(0, extPos);
            mExtName = name.substring(extPos + 1);
            mNewExtName = mExtName;
        }

        public String getOriginName() {
            return mOriginName;
        }

        public String getOriginNameWithoutExt() {
            return mName;
        }

        public String getOriginExtName() {
            return mExtName;
        }

        public String getNewName() {
            return mNewName + "." + mNewExtName;
        }

        public String getNewNameWithoutExt() {
            return mNewName;
        }

        public String getNewExtName() {
            return mNewExtName;
        }
    }

    private Vector<Item> mResult = null;

    private boolean mRenamed = false;

    public void initial(Vector<String> names) {
        mResult = new Vector<Item>();
        for (int i=0; i < names.size(); i++) {
            mResult.add(new Item(names.get(i)));
        }
        mRenamed = false;
    }

    public Vector<Item> getResult() {
        return mResult;
    }

    public void rename(String text, int start, int width) {
        String fmt = String.format("%s%%0%dd", text, width);
        int counter = start;
        for (int i=0; i < mResult.size(); i++) {
            Item item = mResult.get(i);
            item.mNewName = String.format(fmt, counter++);
        }
        mRenamed = true;
    }

    public boolean isRenamed() {
        return mRenamed;
    }
}
