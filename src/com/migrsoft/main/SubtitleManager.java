package com.migrsoft.main;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubtitleManager {

    private final Map<String, List<SubtitleItem>> map = new HashMap<>();

    private boolean modified = false;

    public boolean isModified() {
        return modified;
    }

    public List<SubtitleItem> getListByName(String name) {
        List<SubtitleItem> list = map.get(name);
        if (list == null) {
            list = addListByName(name);
        }
        return list;
    }

    private List<SubtitleItem> addListByName(String name) {
        List<SubtitleItem> list = new ArrayList<>();
        map.put(name, list);
        return list;
    }

    public void addSubtitle(String name, SubtitleItem item) {
        modified = true;
        int index = 0;
        List<SubtitleItem> subtitles = getListByName(name);
        for (SubtitleItem si : subtitles) {
            if (si.rect == item.rect) {
                subtitles.set(index, item);
                return;
            }
            index++;
        }
        subtitles.add(item);
    }

    public void removeSubtitle(String name, SubtitleItem item) {
        List<SubtitleItem> subtitles = getListByName(name);
        int index = 0;
        for (SubtitleItem si : subtitles) {
            if (si.rect == item.rect) {
                modified = true;
                subtitles.remove(index);
                return;
            }
            index++;
        }
    }

    public SubtitleItem getSubtitleByPos(String name, int x, int y) {
        List<SubtitleItem> subtitles = getListByName(name);
        for (SubtitleItem si : subtitles) {
            if (si.rect.contains(x, y)) {
                return si;
            }
        }
        return null;
    }

    private static class Wrapper {

        private Map<String, List<JsonSubtitle>> map;

        public Map<String, List<JsonSubtitle>> getMap() {
            return map;
        }

        public void setMap(Map<String, List<JsonSubtitle>> map) {
            this.map = map;
        }
    }

    public void save(String path) {
        Map<String, List<JsonSubtitle>> jmap = new HashMap<>();
        for (Map.Entry<String, List<SubtitleItem>> entry : map.entrySet()) {
            List<JsonSubtitle> subtitles = new ArrayList<>();
            for (SubtitleItem si : entry.getValue()) {
                subtitles.add(si.toJson());
            }
            if (!subtitles.isEmpty()) {
                jmap.put(entry.getKey(), subtitles);
            }
        }
        Wrapper wrapper = new Wrapper();
        wrapper.setMap(jmap);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(path), wrapper);
            modified = false;
            System.out.println("Save comic subtitles to file " + path);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void load(String path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(path);
            if (file.exists()) {
                Wrapper wrapper = mapper.readValue(file, Wrapper.class);
                for (Map.Entry<String, List<JsonSubtitle>> entry : wrapper.getMap().entrySet()) {
                    List<SubtitleItem> subtitles = new ArrayList<>();
                    for (JsonSubtitle js : entry.getValue()) {
                        SubtitleItem si = new SubtitleItem();
                        si.fromJson(js);
                        subtitles.add(si);
                    }
                    map.put(entry.getKey(), subtitles);
                    modified = false;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
