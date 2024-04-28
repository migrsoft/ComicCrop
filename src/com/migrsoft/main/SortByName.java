package com.migrsoft.main;

import java.util.Comparator;

public class SortByName implements Comparator<String> {
    @Override
    public int compare(String s, String t1) {
        if (s.length() < t1.length()) {
            return -1;
        } else if (s.length() > t1.length()) {
            return 1;
        } else {
            return s.compareTo(t1);
        }
    }
}
